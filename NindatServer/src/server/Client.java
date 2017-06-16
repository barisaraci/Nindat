package server;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import entities.Player;
import packets.ActionPacket;
import packets.MovementPacket;
import packets.Packet;

public class Client {
	
	public Queue<ByteBuffer> packets = new ConcurrentLinkedQueue<ByteBuffer>();
	public SelectionKey socket;
	private Player player;
	public boolean isDead;
	
	public Client(SelectionKey socket, Game game) {
		this.socket = socket;
		player = new Player(socket);
		player.dir = 1;
		player.posX = 1500;
		player.posY = 500;
		game.addPlayer(player, socket);
	}
	
	public void update() {
		processPackets();
	}
	
	public void processPackets() {
		while (!packets.isEmpty()) {
			ByteBuffer buffer = packets.poll();
			if (buffer != null) {
				byte[] bytes = buffer.array();
				Packet packet = Packet.fromByteArray(bytes);
				if (packet.getType() == 3) {
					MovementPacket p = (MovementPacket) packet;
					if (p.uid == player.uid) {
						player.posX = p.posX;
						player.posY = p.posY;
						player.dir = p.dir;
						player.rot = p.rot;
						player.state = p.state;
						player.isUpdated = true;
					}
				} else if (packet.getType() == 4) {
					ActionPacket p = (ActionPacket) packet;
					if (p.uid == player.uid) {
						player.action = p.action;
						player.projectileType = p.projectileType;
						player.isActionUpdated = true;
					}
				}
			}
		}
	}
	
	public void destroy() {
		player.isDead = true;
		isDead = true;
	}

}
