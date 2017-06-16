package utility;

public class Effect {
	
	public Animation anim;
	public boolean isDead;
	public int posX, posY;
	public float angle;
	
	public Effect(int posX, int posY, float angle, byte effect) {
		this.posX = posX;
		this.posY = posY;
		this.angle = angle;
		initAnim(effect);
	}
	
	private void initAnim(byte effect) {
		if (effect == Variables.EFFECT_JUMP) anim = new Animation("effectjump", 7, 4);
		else if (effect == Variables.EFFECT_DUST) anim = new Animation("effectdust", 13	, 4);
		else if (effect == Variables.EFFECT_BLOOD) anim = new Animation("effectblood", 8, 4);
		else if (effect == Variables.EFFECT_EXPLOSION) anim = new Animation("effectexpo", 19, 3);
	}

}
