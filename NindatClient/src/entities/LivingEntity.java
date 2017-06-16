package entities;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.World;

import nindat.GameWorld;
import nindat.Main;
import packets.MovementPacket;
import utility.Animation;
import utility.Variables;

public class LivingEntity extends Entity {
	
	public Map<Byte, Animation> animSet = new HashMap<Byte, Animation>();
	public byte state, action;
	public Animation curAnim;
	
	public LivingEntity() {
		
	}
	
	public LivingEntity(int uid, byte type, int posX, int posY, byte dir, int width, int height, World world, GameWorld gameWorld) {
		super(uid, type, posX, posY, dir, width, height, world, gameWorld);
		state = Variables.STATE_FLY;
	}
	
	public void performAction (byte action) {
		
	}
	
	public void changeState (byte state) {
		if (action == 0 && state != Variables.STATE_FLY) 
			curAnim = animSet.get(state);
		
		this.state = state;
	}
	
	public void update() {
		super.update();
		
		if (action == 0) {
			curAnim.playAnim();
		} else {
			curAnim.playActionAnim(this);
		}
		
		if (Main.isMultiplayer && uid == gameWorld.player.uid) {
			MovementPacket packet = new MovementPacket();
			packet.uid = uid;
			packet.posX = posX;
			packet.posY = posY;
			packet.dir = dir;
			packet.rot = angle;
			packet.state = state;
			gameWorld.client.write(packet);
		}
	}
	
	public void render(SpriteBatch batch, TextureAtlas txtAtlas) {
		curAnim.render(batch, txtAtlas, this);
	}

}
