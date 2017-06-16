package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import packets.Packet;

public abstract class Server implements Runnable {
	
	private enum State {
		STOPPED, STOPPING, RUNNING
	}

	private final AtomicReference<State> state = new AtomicReference<State>(State.STOPPED);
	private final MessageLength messageLength;
	private final Map<SelectionKey, ByteBuffer> readBuffers = new ConcurrentHashMap<SelectionKey, ByteBuffer>();
	private final int port, defaultBufferSize;
	private Selector selector;
	private ServerSocketChannel server;
	private LinkedList<SelectionKey> clientList = new LinkedList<>();
	private Terminal terminal;

	private static short DEFAULT_MESSAGE_SIZE = 512;

	public Server(int port, Terminal terminal) {
		this.port = port;
		this.messageLength = new TwoByteMessageLength();
		this.defaultBufferSize = DEFAULT_MESSAGE_SIZE;
		this.terminal = terminal;
	}

	public void run() {
		if (!state.compareAndSet(State.STOPPED, State.RUNNING)) {
			started(true);
			return;
		}

		try {
			selector = Selector.open();
			server = ServerSocketChannel.open();
			server.socket().bind(new InetSocketAddress(port));
			server.configureBlocking(false);
			server.register(selector, SelectionKey.OP_ACCEPT);
			started(false);
			while (state.get() == State.RUNNING) {
				selector.select(100);
				for (Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext();) {
					SelectionKey key = i.next();
					try {
						i.remove();
						if (key.isConnectable())
							((SocketChannel) key.channel()).finishConnect();
						if (key.isAcceptable())
							accept(key);
						if (key.isReadable()) {
							for (ByteBuffer message : readIncomingMessage(key)) {
								messageReceived(message, key);
							}
						}
					} catch (IOException e) {
						resetKey(key);
						disconnected(key);
						e.printStackTrace();
						terminal.print(e.getMessage(), Terminal.Status.ERROR);
					}
				}
				selector.selectedKeys().clear();
			}
		} catch (Exception e) {
			terminal.print(e.getMessage(), Terminal.Status.ERROR);
			throw new RuntimeException("Server failure: " + e.getMessage());
		} finally {
			closeServer();
		}
	}

	private void accept(SelectionKey key) {
		SelectionKey newkey = null;

		try {
			SocketChannel client = server.accept();
			client.configureBlocking(false);
			client.socket().setTcpNoDelay(true);
			newkey = client.register(selector, SelectionKey.OP_READ);
		} catch (IOException e) {
			e.printStackTrace();
			terminal.print(e.getMessage(), Terminal.Status.ERROR);
		}

		synchronized (clientList) {
			clientList.add(newkey);
		}

		connected(newkey);
	}

	private List<ByteBuffer> readIncomingMessage(SelectionKey key) {
		ByteBuffer readBuffer = readBuffers.get(key);
		if (readBuffer == null) {
			readBuffer = ByteBuffer.allocate(defaultBufferSize);
			readBuffers.put(key, readBuffer);
		}

		try {
			if (((ReadableByteChannel) key.channel()).read(readBuffer) == -1) {
				throw new IOException("Read on closed key");
			}
		} catch (IOException e) {
			e.printStackTrace();
			key.cancel();
			terminal.print(e.getMessage(), Terminal.Status.ERROR);
		}

		readBuffer.flip();

		List<ByteBuffer> result = new ArrayList<ByteBuffer>();

		ByteBuffer msg = readMessage(key, readBuffer);
		while (msg != null) {
			result.add(msg);
			msg = readMessage(key, readBuffer);
		}

		return result;
	}

	private ByteBuffer readMessage(SelectionKey key, ByteBuffer readBuffer) {
		int bytesToRead;
		if (readBuffer.remaining() > messageLength.byteLength()) {
			byte[] lengthBytes = new byte[messageLength.byteLength()];
			readBuffer.get(lengthBytes);
			bytesToRead = (int) messageLength.bytesToLength(lengthBytes);
			if ((readBuffer.limit() - readBuffer.position()) < bytesToRead) {
				if (readBuffer.limit() == readBuffer.capacity()) {
					int oldCapacity = readBuffer.capacity();
					ByteBuffer tmp = ByteBuffer.allocate(bytesToRead + messageLength.byteLength());
					readBuffer.position(0);
					tmp.put(readBuffer);
					readBuffer = tmp;
					readBuffer.position(oldCapacity);
					readBuffer.limit(readBuffer.capacity());
					readBuffers.put(key, readBuffer);
					return null;
				} else {
					readBuffer.position(readBuffer.limit());
					readBuffer.limit(readBuffer.capacity());
					return null;
				}
			}
		} else {
			readBuffer.position(readBuffer.limit());
			readBuffer.limit(readBuffer.capacity());
			return null;
		}
		byte[] resultMessage = new byte[bytesToRead];
		readBuffer.get(resultMessage, 0, bytesToRead);
		int remaining = readBuffer.remaining();
		readBuffer.limit(readBuffer.capacity());
		readBuffer.compact();
		readBuffer.position(0);
		readBuffer.limit(remaining);
		return ByteBuffer.wrap(resultMessage);
	}

	private void resetKey(SelectionKey key) {
		key.cancel();
		synchronized (clientList) {
			clientList.remove(key);
		}
		readBuffers.remove(key);
	}

	private void closeServer() {
		try {
			selector.close();
			server.socket().close();
			server.close();
			state.set(State.STOPPED);
			stopped();
		} catch (Exception e) {
			e.printStackTrace();
			terminal.print(e.getMessage(), Terminal.Status.ERROR);
		}
	}

	public void write(SelectionKey channelKey, byte[] buffer) {
		short len = (short) buffer.length;
		byte[] lengthBytes = messageLength.lengthToBytes(len);
		ByteBuffer writeBuffer = ByteBuffer.allocate(len + lengthBytes.length);
		writeBuffer.put(lengthBytes);
		writeBuffer.put(buffer);
		writeBuffer.flip();

		if (buffer != null && state.get() == State.RUNNING) {
			int bytesWritten = 0;
			try {
				SocketChannel channel = (SocketChannel) channelKey.channel();
				while (writeBuffer.remaining() > 0) {
					bytesWritten = channel.write(writeBuffer);
					if (bytesWritten == -1) {
						resetKey(channelKey);
						disconnected(channelKey);
					}

					if (bytesWritten == 0)
						Thread.sleep(5);
				}

			} catch (Exception e) {
				resetKey(channelKey);
				disconnected(channelKey);
				terminal.print(e.getMessage(), Terminal.Status.ERROR);
			}
		}
	}

	public void broadcast(Packet packet) {
		byte[] buffer = packet.toByteArray();
		synchronized (clientList) {
			for (SelectionKey key : clientList) {
				write(key, buffer);
			}
		}
	}
	
	public void broadcastExceptKey(Packet packet, SelectionKey channelKey) {
		byte[] buffer = packet.toByteArray();
		synchronized (clientList) {
			for (SelectionKey key : clientList) {
				if (key != channelKey)
					write(key, buffer);
			}
		}
	}

	protected abstract void connected(SelectionKey key);

	protected abstract void disconnected(SelectionKey key);

	public abstract void messageReceived(ByteBuffer message, SelectionKey key);

	public abstract void started(boolean alreadyStarted);

	public abstract void stopped();

}
