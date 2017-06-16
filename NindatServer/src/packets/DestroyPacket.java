package packets;

@SuppressWarnings("serial")
public class DestroyPacket extends Packet {
	
	public int uid;
	
	public DestroyPacket() {
		type = 2;
	}
	
}
