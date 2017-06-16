package server;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import entities.Entity;
import entities.LivingEntity;
import entities.Player;
import packets.ActionPacket;
import packets.CreateEntityPacket;
import packets.DestroyPacket;
import packets.MovementPacket;

public class Game implements Runnable {
	
	private Server server;
	private Game game;
	private Terminal terminal;
	
	private LinkedList<Client> clients = new LinkedList<Client>();
	private ArrayList<Entity> entities = new ArrayList<>();
	
	private static int uid = 0;
	private static final int SERVER_TICK = 60;

	public Game(Terminal terminal) {
		this.terminal = terminal;
		game = this;
	}

	public void run() {
		initServer();
	}

	private void initServer() {
		server = new Server(3333, terminal) {
			@Override
			protected void connected(SelectionKey key) {
				terminal.print("Client connected", Terminal.Status.INFO);
				synchronized (entities) {
					for (Iterator<Entity> i = entities.iterator(); i.hasNext();) {
						Entity entity = i.next();

						CreateEntityPacket packet = new CreateEntityPacket();
						packet.uid = entity.uid;
						packet.entityType = entity.type;
						packet.posX = entity.posX;
						packet.posY = entity.posY;
						packet.dir = entity.dir;
						packet.rot = entity.rot;
						server.write(key, packet.toByteArray());
					}
					
					addClient(new Client(key, game));
				}
			}

			@Override
			protected void disconnected(SelectionKey key) {
				key.cancel();
				terminal.print("Client disconnected", Terminal.Status.INFO);
			}

			@Override
			public void messageReceived(ByteBuffer message, SelectionKey key) {
				Client client = getClient(key);
				if (client != null)
					client.packets.add(message);
			}

			@Override
			public void started(boolean alreadyStarted) {
				terminal.print("Server has started", Terminal.Status.WARNING);
			}

			@Override
			public void stopped() {
				terminal.print("Server has stopped working", Terminal.Status.WARNING);
			}
		};

		new Thread(server).start();

		startServerLoop();
	}

	private void startServerLoop() {
		while (true) {
			synchronized (clients) {
				for (Client client : clients) {
					if (client.isDead == true) {
						removeClient(client);
					} else if (!client.socket.isValid()) {
						client.destroy();
					} else {
						client.update();
					}
				}
			}

			synchronized (entities) {
				for (Iterator<Entity> i = entities.iterator(); i.hasNext();) {
					Entity entity = i.next();
					if (entity.isDead) {
						removeEntity(entity);
						i.remove();
					} else {
						if (entity.isUpdated) {
							MovementPacket pMove = new MovementPacket();
							pMove.posX = entity.posX;
							pMove.posY = entity.posY;
							pMove.dir = entity.dir;
							pMove.rot = entity.rot;
							pMove.uid = entity.uid;
							
							if (entity instanceof LivingEntity) {
								pMove.state = ((LivingEntity) entity).state;
								server.broadcastExceptKey(pMove, ((Player) entity).socket);
							} else {
								server.broadcast(pMove);
							}
							entity.isUpdated = false;
						}
						
						if (entity instanceof Player) {
							Player player = (Player) entity;
							if (player.isActionUpdated) {
								ActionPacket pAction = new ActionPacket();
								pAction.uid = player.uid;
								pAction.action = player.action;
								pAction.projectileType = player.projectileType;
								
								if (entity instanceof Player) {
									server.broadcastExceptKey(pAction, ((Player) entity).socket);
								} else {
									server.broadcast(pAction);
								}
								player.isActionUpdated = false;
							}
						}
					}
				}
			}
			
			try {
				Thread.sleep(1000 / SERVER_TICK);
			} catch (InterruptedException e) {
				e.printStackTrace();
				terminal.print(e.getMessage(), Terminal.Status.ERROR);
			}
		}
	}
	
	private Client getClient(SelectionKey key) {
		synchronized (clients) {
			for (Client client : clients) {
				if (client.socket == key)
					return client;
			}
		}

		return null;
	}
	
	private void addClient(Client client) {
		synchronized (clients) {
			clients.add(client);
		}
	}
	
	public void removeClient(Client client) {
		synchronized (clients) {
			clients.remove(client);
		}
	}
	
	public void addEntity(Entity entity, byte entityType) {
		uid++;
		entity.uid = uid;
		entities.add(entity);

		CreateEntityPacket packet = new CreateEntityPacket();
		packet.uid = uid;
		packet.entityType = entityType;
		packet.posX = entity.posX;
		packet.posY = entity.posY;
		packet.dir = entity.dir;
		packet.rot = entity.rot;
		server.broadcast(packet);
	}
	
	public void addPlayer(Player player, SelectionKey key) {
		uid++;
		player.uid = uid;
		entities.add(player);
		
		CreateEntityPacket packet = new CreateEntityPacket();
		packet.uid = uid;
		packet.entityType = 0;
		packet.posX = player.posX;
		packet.posY = player.posY;
		packet.dir = player.dir;
		packet.rot = player.rot;
		server.write(key, packet.toByteArray());
		
		packet.entityType = 1;
		server.broadcastExceptKey(packet, key);
	}
	
	public void removeEntity(Entity entity) {
		synchronized (entities) {
			DestroyPacket packet = new DestroyPacket();
			packet.uid = entity.uid;
			server.broadcast(packet);
		}
	}

}
