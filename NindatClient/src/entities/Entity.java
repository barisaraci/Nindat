package entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import nindat.GameWorld;
import utility.Variables;

public class Entity {
	
	public int uid, posX, posY, width, height;
	public float normalizedDegree, leftVectorDegree, rightVectorDegree;
	public byte type, dir;
	public float speed, angle;
	public boolean isChanged;
	public Texture texture;
	
	public Body body;
	
	public World world;
	public GameWorld gameWorld;
	
	public Entity() {
		
	}
	
	public Entity(int uid, byte type, int posX, int posY, byte dir, int width, int height, World world, GameWorld gameWorld) {
		this.uid = uid;
		this.type = type;
		this.posX = posX;
		this.posY = posY;
		this.dir = dir;
		this.width = width;
		this.height = height;
		this.world = world;
		this.gameWorld = gameWorld;
	}
	
	public Entity(int uid, byte type, int posX, int posY, byte dir, World world, GameWorld gameWorld) {
		this.uid = uid;
		this.type = type;
		this.posX = posX;
		this.posY = posY;
		this.dir = dir;
		this.world = world;
		this.gameWorld = gameWorld;
		
		if (type == Variables.TYPE_KUNAI) {
			width = Variables.KUNAI_WIDTH;
			height = Variables.KUNAI_HEIGHT;
		} else if (type == Variables.TYPE_BOMB) {
			width = Variables.BOMB_WIDTH;
			height = Variables.BOMB_HEIGHT;
		}
		
		initPhysics();
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
        fixtureDef.friction = 0.6f;
        fixtureDef.filter.categoryBits = Variables.BIT_PROJECTILE;
        fixtureDef.isSensor = true;
		body.createFixture(fixtureDef).setUserData(this);
		
		shape.dispose();
	}

	public void update() {
		checkChanged();
		
		normalizedDegree = (float) Math.toDegrees(body.getAngle());
		
		while (normalizedDegree < 0) {
			normalizedDegree += 360;
		}
		while (normalizedDegree >= 360) {
			normalizedDegree -= 360;
		}
		normalizedDegree += 90;
		if (normalizedDegree >= 360 && normalizedDegree <= 450) normalizedDegree -= 360;
		
		rightVectorDegree = (float) Math.toDegrees(body.getAngle());
		while (rightVectorDegree < 0) {
			rightVectorDegree += 360;
		}
		while (rightVectorDegree >= 360) {
			rightVectorDegree -= 360;
		}
		
		leftVectorDegree = rightVectorDegree;
		leftVectorDegree += 180;
		if (leftVectorDegree >= 360 && leftVectorDegree <= 540) leftVectorDegree -= 360;
	}
	
	private void checkChanged() {
		if (isChanged) {
			if (type == Variables.TYPE_MAIN_PLAYER || type == Variables.TYPE_PLAYER) {
				body.setLinearVelocity(Vector2.Zero);
		        body.setAngularVelocity(0);
				body.setTransform((posX + width / 2) / Variables.PPM , (posY + height / 2) / Variables.PPM, angle);
			} else {
				body.setGravityScale(0.2f);
				body.setTransform(body.getWorldCenter(), (float) Math.toRadians(angle));
				Vector2 impulse = null;
				if (type == Variables.TYPE_KUNAI)
					impulse = new Vector2((float) (Variables.KUNAI_SPEED * Math.cos(Math.toRadians((dir == 0) ? leftVectorDegree : rightVectorDegree))), (float) (Variables.KUNAI_SPEED * Math.sin(Math.toRadians((dir == 0) ? leftVectorDegree : rightVectorDegree))));
				else if (type == Variables.TYPE_BOMB)
					impulse = new Vector2((float) (Variables.BOMB_SPEED * Math.cos(Math.toRadians((dir == 0) ? leftVectorDegree : rightVectorDegree))), (float) (Variables.BOMB_SPEED * Math.sin(Math.toRadians((dir == 0) ? leftVectorDegree : rightVectorDegree))));
				body.applyLinearImpulse(impulse, body.getWorldCenter(), true);
			}
			isChanged = false;
		}
		
		if (type != Variables.TYPE_PLAYER) {
			posX = (int) lerp(posX, body.getPosition().x * Variables.PPM - width / 2, 0.3f);
			posY = (int) lerp(posY, body.getPosition().y * Variables.PPM - height / 2, 0.3f);
			angle = body.getAngle();
		}
	}
	
	private float lerp(float v0, float v1, float t) {
		return v0 * (1 - t) + v1 * t;
	}

	public void render(SpriteBatch batch, TextureAtlas txtAtlas) {
		TextureRegion region = txtAtlas.findRegion((type == 2) ? "kunai" : "bomb");
		batch.draw(region, posX, posY, width / 2, height / 2, width, height, 2f, 2f, (dir == 0) ? rightVectorDegree : leftVectorDegree);
	}

}
