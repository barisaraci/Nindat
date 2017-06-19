package entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import nindat.GameWorld;
import nindat.Main;
import packets.ActionPacket;
import utility.Animation;
import utility.EffectRenderer;
import utility.Variables;

public class Player extends LivingEntity {
	
	public Vector2 gravityVector = new Vector2(0, Variables.GRAVITY);
	public float hp, mana;
	public int score;
	public byte jumpType;
	public boolean isDoubleJumped, isDead;
	
	public Player() {
		
	}
	
	public Player(int uid, byte type, int posX, int posY, byte dir, World world, GameWorld gameWorld) {
		super(uid, type, posX, posY, dir, Variables.PLAYER_WIDTH, Variables.PLAYER_HEIGHT, world, gameWorld);
		speed = Variables.PLAYER_SPEED;
		jumpType = Variables.JUMP_TYPE;
		hp = 250;
		mana = 250;
		initAnims();
		initPhysics();
	}
	
	public void performAction(byte action) {
		if (action == Variables.ACTION_JUMP && mana > Variables.JUMP_MANA) {
			if (state == Variables.STATE_FLY && jumpType == Variables.JUMP_DOUBLE && !isDoubleJumped) {
				startActionAnim(action, (byte) 0);
				EffectRenderer.renderer.create(posX, posY, leftVectorDegree, Variables.EFFECT_JUMP);
				Vector2 impulse = new Vector2((float) (Variables.PLAYER_AIR_JUMP_SPEED * Math.cos(Math.toRadians(normalizedDegree))), (float) (Variables.PLAYER_AIR_JUMP_SPEED * Math.sin(Math.toRadians(normalizedDegree))));
				body.applyLinearImpulse(impulse, body.getWorldCenter(), true);
				isDoubleJumped = true;
			} else if (state == Variables.STATE_FLY && jumpType == Variables.JUMP_MULTIPLE) {
				startActionAnim(action, (byte) 0);
				EffectRenderer.renderer.create(posX, posY, leftVectorDegree, Variables.EFFECT_JUMP);
				Vector2 impulse = new Vector2((float) (Variables.PLAYER_AIR_JUMP_SPEED * Math.cos(Math.toRadians(normalizedDegree))), (float) (Variables.PLAYER_AIR_JUMP_SPEED * Math.sin(Math.toRadians(normalizedDegree))));
				body.applyLinearImpulse(impulse, body.getWorldCenter(), true);
			} else if (state != Variables.STATE_FLY) {
				startActionAnim(action, (byte) 0);
				EffectRenderer.renderer.create(posX, posY, leftVectorDegree, Variables.EFFECT_JUMP);
			}
			mana -= Variables.JUMP_MANA;
		}
	}
	
	public void performAction(byte action, byte projectileType) {
		if (action == Variables.ACTION_THROW || action == Variables.ACTION_JUMP_THROW) {
			if (projectileType == 2 && mana > Variables.KUNAI_MANA)
				mana -= Variables.KUNAI_MANA;
			else if (projectileType == 3 && mana > Variables.BOMB_MANA)
				mana -= Variables.BOMB_MANA;
			else 
				return;
			
			startActionAnim(action, projectileType);
			Entity projectile = new Entity(uid, projectileType, posX + width / 2, posY + height / 3, dir, world, gameWorld);
			projectile.angle = normalizedDegree;
			projectile.normalizedDegree = normalizedDegree;
			projectile.leftVectorDegree = leftVectorDegree;
			projectile.rightVectorDegree = rightVectorDegree;
			projectile.isChanged = true;
			gameWorld.addEntity(projectile, uid);
		}
	}
	
	public void startActionAnim(byte action, byte projectileType) {
		curAnim = animSet.get(action);
		curAnim.resetAnim();
		this.action = action;
		
		if (Main.isMultiplayer && uid == gameWorld.player.uid) {
			ActionPacket packet = new ActionPacket();
			packet.uid = uid;
			packet.action = action;
			packet.projectileType = projectileType;
			gameWorld.client.write(packet);
		}
	}
	
	private void initAnims() {
		animSet.put(Variables.STATE_IDLE, new Animation("idle", 10, 4));
		animSet.put(Variables.STATE_RUN, new Animation("run", 10, 3));
		animSet.put(Variables.ACTION_JUMP, new Animation("jump", 10, 3));
		animSet.put(Variables.ACTION_THROW, new Animation("throw", 10, 3));
		animSet.put(Variables.ACTION_JUMP_THROW, new Animation("jumpthrow", 10, 3));
		curAnim = animSet.get(Variables.STATE_IDLE);
	}
	
	private void initPhysics() {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set((posX + width) / Variables.PPM, (posY + height) / Variables.PPM);
		body = world.createBody(bodyDef);
		
		PolygonShape shape = new PolygonShape();
		shape.setAsBox((width / 2) / Variables.PPM, (height / 2) / Variables.PPM);
		
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.2f;
        fixtureDef.filter.categoryBits = Variables.BIT_PLAYER;
        fixtureDef.filter.maskBits = Variables.BIT_STATIC | Variables.BIT_PROJECTILE;
		body.createFixture(fixtureDef).setUserData(this);
		
		if (type == 0) {
			shape.setAsBox((width / 3) / Variables.PPM, (height / 20) / Variables.PPM, new Vector2(0, -80 / Variables.PPM), 0);
	        fixtureDef.shape = shape;
	        fixtureDef.filter.categoryBits = Variables.BIT_PLAYER;
	        fixtureDef.filter.maskBits = Variables.BIT_STATIC;
	        fixtureDef.isSensor = true;
	        body.createFixture(fixtureDef).setUserData("foot");
	        
	        shape.setAsBox((width / 4) / Variables.PPM, (height / 4) / Variables.PPM, new Vector2(0, -100 / Variables.PPM), 0);
	        fixtureDef.shape = shape;
	        fixtureDef.filter.categoryBits = Variables.BIT_PLAYER;
	        fixtureDef.filter.maskBits = Variables.BIT_STATIC;
	        fixtureDef.isSensor = true;
	        body.createFixture(fixtureDef).setUserData("wallsensor");
		}
		
		body.setGravityScale(0);
		shape.dispose();
	}
	
	public void getHit(int hp, int uid) {
		if (this.hp > hp) {
			this.hp -= hp;
		} else {
			this.hp = 0;
			((Player) gameWorld.getPlayer(uid)).score += 1;
			isDead = true;
		}
	}
	
	public void reset() {
		isDead = false;
        posX = 1500;
        posY = 500;
		angle = 0;
		hp = 250;
		dir = 0;
		isChanged = true;
	}
	
	public void update() {
		if (isDead) 
			reset();
		super.update();
		if (type == 0) body.applyForceToCenter(gravityVector, true);
		if (hp < 250) hp += 0.01;
		if (mana < 250) mana += 1;
	}
	
	public void render(SpriteBatch batch, TextureAtlas txtAtlas) {
		if (action == 0 && state == Variables.STATE_FLY) {
			TextureRegion region = txtAtlas.findRegion("jump9");
			int width = region.getRegionWidth();
			int height = region.getRegionHeight();
			if (dir == 1) {
				if (region.isFlipX()) { region.flip(true, false); }
				batch.draw(region, posX, posY, width / 2, height / 2, width, height, 1.1f, 1.1f, (float) Math.toDegrees(angle));
			} else {
				if (!region.isFlipX()) { region.flip(true, false); }
				batch.draw(region, posX, posY, width / 2, height / 2, width, height, 1.1f, 1.1f, (float) Math.toDegrees(angle));
			}
		} else {
			super.render(batch, txtAtlas);
		}
	}
	
	public void renderHp(ShapeRenderer shapeRenderer) {
		shapeRenderer.setColor(1, 1, 1, 1);
		//shapeRenderer.rect(posX - 30, posY + 175, width / 2 + 30, - height + 25, 150, 20, 1f, 1f, (float) Math.toDegrees(angle));
		shapeRenderer.rect(posX - 30, posY + 190, 150, 15);
		shapeRenderer.rect(posX - 30, posY + 170, 150, 15);
		shapeRenderer.setColor(1, 0, 0, 1);
		//shapeRenderer.rect(posX - 30, posY + 175, width / 2 + 30, - height + 25, hp * 150 / 250, 20, 1f, 1f, (float) Math.toDegrees(angle));
		shapeRenderer.rect(posX - 30, posY + 190, hp * 150 / 250, 15);
		shapeRenderer.setColor(0, 0, 1, 1);
		shapeRenderer.rect(posX - 30, posY + 170, mana * 150 / 250, 15);
	}

}
