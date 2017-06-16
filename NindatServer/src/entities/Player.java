package entities;

import java.nio.channels.SelectionKey;

public class Player extends LivingEntity {
	
	public SelectionKey socket;
	public byte projectileType;
	
	public Player(SelectionKey socket) {
		type = 1;
		this.socket = socket;
	}

}
