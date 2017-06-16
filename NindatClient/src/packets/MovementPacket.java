package packets;

@SuppressWarnings("serial")
public class MovementPacket extends Packet {
	
	public int uid;
	public int posX, posY;
	public float rot;
	public byte dir, state;
	
	public MovementPacket() {
		type = 3;
	}

}
