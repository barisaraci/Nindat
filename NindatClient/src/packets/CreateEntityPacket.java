package packets;

@SuppressWarnings("serial")
public class CreateEntityPacket extends Packet {

	public int uid, posX, posY;
	public float rot;
	public byte entityType, dir;

	public CreateEntityPacket() {
		type = 1;
	}
}
