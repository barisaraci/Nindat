package networking;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import packets.Packet;

public abstract class Network implements Runnable {

	private enum State {
		STOPPED, STOPPING, RUNNING
	}

	private final AtomicReference<State> state = new AtomicReference<State>(State.STOPPED);
	private final MessageLength messageLength = new TwoByteMessageLength();
	private InetAddress ip;
	private final int port;
	public SocketChannel socketChannel;
	public ByteBuffer readBuffers;
	private final int byteLength = 2;
	
	public Network(String ip, int port) {
		try {
			this.ip = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.port = port;
	}

	public void run() {
		if (!state.compareAndSet(State.STOPPED, State.RUNNING)) {
			connected(true);
			return;
		}

		try {
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(true);
			socketChannel.connect(new InetSocketAddress(ip, port));
			socketChannel.socket().setTcpNoDelay(true);

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		readBuffers = ByteBuffer.allocate(512);
		try {
			connected(false);
			while (state.get() == State.RUNNING) {
				for (ByteBuffer message : readIncomingMessage(socketChannel)) {
					messageReceived(Packet.fromByteArray(message.array()));
				}
			}
		} finally {
			try {
				socketChannel.close();
				state.set(State.STOPPED);
				disconnected();
			} catch (Exception e) {
			}
		}
	}

	public synchronized boolean write(Packet packet) {
		byte[] buffer = packet.toByteArray();
		int len = buffer.length;
		byte[] lengthBytes = lengthToBytes(len);
		try {
			byte[] outBuffer = new byte[len + byteLength];
			System.arraycopy(lengthBytes, 0, outBuffer, 0, byteLength);
			System.arraycopy(buffer, 0, outBuffer, byteLength, len);
			socketChannel.write(ByteBuffer.wrap(outBuffer));
			return true;
		} catch (Exception e) {
			stop();
			return false;
		}
	}
	
	public byte[] lengthToBytes(long len) {
		if (len < 0 || len > 65535) {
			throw new IllegalStateException("");
		}
		return new byte[] { (byte) ((len >>> 8) & 0xff), (byte) (len & 0xff) };
	}

	public long bytesToLength(byte[] bytes) {
		if (bytes.length != 2) {
			throw new IllegalStateException("");
		}
		return ((0xFF & ((int) bytes[0])) << 8) + (int) (bytes[1] & 0xFF);
	}
	
	public boolean stop() {
		if (state.compareAndSet(State.RUNNING, State.STOPPING))
			return true;
		
		return false;
	}

	private List<ByteBuffer> readIncomingMessage(SocketChannel socket) {
		try {
			if (socket.read(readBuffers) == -1)
				throw new IOException("Read on closed key");
		} catch (IOException e) {
			e.printStackTrace();
		}

		readBuffers.flip();
		
		List<ByteBuffer> result = new ArrayList<ByteBuffer>();

		ByteBuffer msg = readMessage(readBuffers);
		while (msg != null) {
			result.add(msg);
			msg = readMessage(readBuffers);
		}

		return result;
	}

	private ByteBuffer readMessage(ByteBuffer readBuffer) {
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
					readBuffers = readBuffer;
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

	protected abstract void messageReceived(Packet packet);

	protected abstract void connected(boolean alreadyConnected);

	protected abstract void disconnected();

}
