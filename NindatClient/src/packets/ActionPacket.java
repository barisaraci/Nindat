package packets;

@SuppressWarnings("serial")
public class ActionPacket extends Packet {
	
	public int uid;
	public byte action;
	public byte projectileType;
	
	public ActionPacket() {
		type = 4;
	}
}
