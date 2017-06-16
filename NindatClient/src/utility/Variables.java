package utility;

public class Variables {
	
	public static final float PPM = 100f; // 1 meter = 100 pixels
	public static final float GRAVITY = -10f;
	
	public static final boolean IS_PHYSICS_VISUAL = false;
	public static final boolean IS_THERE_SLIDING = true;
	public static final boolean IS_THERE_ROTATION_ACCELERATION = false;
	
	public static final float PLAYER_SPEED = 1f;
	public static final float PLAYER_JUMP_SPEED = 5f;
	public static final float PLAYER_AIR_JUMP_SPEED = 10f;
	public static final float PLAYER_ROTATION_SPEED = 2.5f;
	public static final float PLAYER_ROT_ACC_SPEED = 0.001f;
	public static final float PLAYER_MAX_VELOCITY = 8f;
	public static final float PLAYER_MAX_ANGULAR_SPEED = 4f;
	
	public static final float KUNAI_SPEED = 0.4f;
	public static final float BOMB_SPEED = 0.6f;
	
	public static final int KUNAI_DAMAGE = 20;
	public static final int BOMB_DAMAGE = 85;
	
	public static final int KUNAI_MANA = 30;
	public static final int BOMB_MANA = 80;
	public static final int JUMP_MANA = 25;
	
	public static final byte JUMP_SINGLE = 0;
	public static final byte JUMP_DOUBLE = 1;
	public static final byte JUMP_MULTIPLE = 2;
	
	public static final byte JUMP_TYPE = JUMP_MULTIPLE;

	public static final int PLAYER_WIDTH = 90;
	public static final int PLAYER_HEIGHT = 140;

	public static final int KUNAI_WIDTH = 7;
	public static final int KUNAI_HEIGHT = 35;
	
	public static final int BOMB_WIDTH = 30;
	public static final int BOMB_HEIGHT = 30;
	
	public static final byte TYPE_MAIN_PLAYER = 0;
	public static final byte TYPE_PLAYER = 1;
	public static final byte TYPE_KUNAI = 2;
	public static final byte TYPE_BOMB = 3;
	
	public static final byte BIT_PLAYER = 2;
	public static final byte BIT_PROJECTILE = 4;
	public static final byte BIT_STATIC = 8;

	public static final byte STATE_IDLE = 0;
	public static final byte STATE_RUN = 1;
	public static final byte STATE_FLY = 2;

	public static final byte ACTION_JUMP = 10;
	public static final byte ACTION_THROW = 11;
	public static final byte ACTION_JUMP_THROW = 12;

	public static final byte TILE_IDS_WALL_UP[] = { 9, 12, 16 }; // up means ceiling
	public static final byte TILE_IDS_WALL_BOTTOM[] = { 1, 2, 3 };
	public static final byte TILE_IDS_WALL_LEFT[] = { 6 }; // walls look right side
	public static final byte TILE_IDS_WALL_RIGHT[] = { 4 };

	public static final byte EFFECT_JUMP = 0;
	public static final byte EFFECT_DUST = 1;
	public static final byte EFFECT_BLOOD = 2;
	public static final byte EFFECT_EXPLOSION = 3;
}
