package nindat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;

import entities.Entity;
import entities.LivingEntity;
import entities.Player;
import networking.Network;
import packets.ActionPacket;
import packets.CreateEntityPacket;
import packets.DestroyPacket;
import packets.MovementPacket;
import packets.Packet;
import utility.Assets;
import utility.EffectRenderer;
import utility.Variables;
import utility.WorldCreator;

public class GameWorld {
	
	public Network client;
	public World world;
	public Player player;
	public TiledMap tiledMap = Assets.manager.get("assets/graphics/map.tmx", TiledMap.class);
	public List<Packet> packets = Collections.synchronizedList(new ArrayList<Packet>());
	public List<Entity> entities = new ArrayList<Entity>();
	public float camZoom = 2f;

	public GameWorld() {
		if (Main.isMultiplayer) initClient();
		initWorld();
		if (!Main.isMultiplayer) initPlayer();
	}

	private void initClient() {
		client = new Network(Main.IP, Main.PORT) {
			@Override
			protected void connected(boolean alreadyConnected) {
				System.out.println("connected to server");
			}

			@Override
			protected void disconnected() {
				System.out.println("disconnected from server");
			}

			@Override
			protected void messageReceived(Packet packet) {
				packets.add(packet);
			}
		};
		new Thread(client).start();
	}

	public void processPackets() {
		synchronized (packets) {
			if (packets.size() > 0) {
				for (Iterator<Packet> i = packets.iterator(); i.hasNext();) {
					processPacket(i.next());
					i.remove();
				}
			}
		}
	}

	public void processPacket(Packet packet) {
		if (packet == null)
			return;

		switch (packet.getType()) {
		case 1: {
			CreateEntityPacket p = (CreateEntityPacket) packet;
			Entity pl = null;
			if (p.entityType == Variables.TYPE_MAIN_PLAYER || p.entityType == Variables.TYPE_PLAYER) {
				pl = new Player(p.uid, p.entityType, p.posX, p.posY, p.dir, world, this);
				if (p.entityType == Variables.TYPE_MAIN_PLAYER) 
					player = (Player) pl;
			}

			if (pl != null)
				addEntity(pl, p.uid);

			break;
		}
		
		case 2: {
			DestroyPacket p = (DestroyPacket) packet;
			getEntity(p.uid).isDead = true;
			
			break;
		}
		
		case 3: {
			MovementPacket p = (MovementPacket) packet;
			Entity entity = getEntity(p.uid);
			if (entity == null)
				break;
			
			entity.posX = p.posX;
			entity.posY = p.posY;
			entity.angle = p.rot;
			entity.dir = p.dir;
			entity.isChanged = true;
			if (entity instanceof LivingEntity)
				((LivingEntity) entity).changeState(p.state);
			
			break;
		}
		
		case 4: {
			ActionPacket p = (ActionPacket) packet;
			Player player = (Player) getEntity(p.uid);

			if (player != null) {
				if (p.action == Variables.ACTION_JUMP)
					player.performAction(p.action);
				else
					player.performAction(p.action, p.projectileType);
			}
			
			break;
		}
		
		default:
			break;
			
		}
	}
	
	public Entity getPlayer(int uid) {
		for (Entity entity : entities) {
			if (entity.uid == uid && entity instanceof Player)
				return entity;
		}
		return null;
	}
	
	public Entity getEntity(int uid) {
		for (Entity entity : entities) {
			if (entity.uid == uid)
				return entity;
		}
		return null;
	}

	public void addEntity(Entity entity, int uid) {
		entity.uid = uid;
		entities.add(entity);
	}

	private void initWorld() {
		world = new World(new Vector2(0, Variables.GRAVITY), true);
		new WorldCreator(world, tiledMap).create();
		world.setContactListener(new ContactListener() {

			@Override
			public void beginContact(Contact contact) {
				Fixture fA = contact.getFixtureA();
				Fixture fB = contact.getFixtureB();
				
				if (fA.getUserData() == null || fB.getUserData() == null) return;
				
				if (fA.getUserData().equals("wallsensor")) {
					for (int i = 0; i < Variables.TILE_IDS_WALL_UP.length; i++) {
						if (fB.getUserData().equals("static_" + Variables.TILE_IDS_WALL_UP[i])) {
							player.gravityVector.x = 0;
							player.gravityVector.y = 30f;
							break;
						}
					}
					for (int i = 0; i < Variables.TILE_IDS_WALL_BOTTOM.length; i++) {
						if (fB.getUserData().equals("static_" + Variables.TILE_IDS_WALL_BOTTOM[i])) {
							player.gravityVector.x = 0;
							player.gravityVector.y = -30f;
							break;
						}
					}
					for (int i = 0; i < Variables.TILE_IDS_WALL_LEFT.length; i++) {
						if (fB.getUserData().equals("static_" + Variables.TILE_IDS_WALL_LEFT[i])) {
							player.gravityVector.x = -30;
							player.gravityVector.y = 0f;
							break;
						}
					}
					for (int i = 0; i < Variables.TILE_IDS_WALL_RIGHT.length; i++) {
						if (fB.getUserData().equals("static_" + Variables.TILE_IDS_WALL_RIGHT[i])) {
							player.gravityVector.x = 30f;
							player.gravityVector.y = 0f;
							break;
						}
					}
				} else if (fB.getUserData().equals("wallsensor")) {
					for (int i = 0; i < Variables.TILE_IDS_WALL_UP.length; i++) {
						if (fA.getUserData().equals("static_" + Variables.TILE_IDS_WALL_UP[i])) {
							player.gravityVector.x = 0;
							player.gravityVector.y = 30f;
							break;
						}
					}
					for (int i = 0; i < Variables.TILE_IDS_WALL_BOTTOM.length; i++) {
						if (fA.getUserData().equals("static_" + Variables.TILE_IDS_WALL_BOTTOM[i])) {
							player.gravityVector.x = 0;
							player.gravityVector.y = -30f;
							break;
						}
					}
					for (int i = 0; i < Variables.TILE_IDS_WALL_LEFT.length; i++) {
						if (fA.getUserData().equals("static_" + Variables.TILE_IDS_WALL_LEFT[i])) {
							player.gravityVector.x = -30;
							player.gravityVector.y = 0f;
							break;
						}
					}
					for (int i = 0; i < Variables.TILE_IDS_WALL_RIGHT.length; i++) {
						if (fA.getUserData().equals("static_" + Variables.TILE_IDS_WALL_RIGHT[i])) {
							player.gravityVector.x = 30f;
							player.gravityVector.y = 0f;
							break;
						}
					}
				}
				
				if (fA.getUserData() instanceof Entity && (((Entity) fA.getUserData()).type == Variables.TYPE_KUNAI || ((Entity) fA.getUserData()).type == Variables.TYPE_BOMB) && fB.getUserData() instanceof Player && ((Player) fB.getUserData()).uid != ((Entity) fA.getUserData()).uid) { 
					Player pl = ((Player) fB.getUserData());
					Entity projectile = ((Entity) fA.getUserData());
					pl.getHit((projectile.type == Variables.TYPE_KUNAI) ? Variables.KUNAI_DAMAGE : Variables.BOMB_DAMAGE, projectile.uid);
					EffectRenderer.renderer.create(projectile.posX, projectile.posY, projectile.leftVectorDegree, (projectile.type == Variables.TYPE_KUNAI) ? Variables.EFFECT_BLOOD : Variables.EFFECT_EXPLOSION);
					projectile.isDead = true;
				} else if (fB.getUserData() instanceof Entity && (((Entity) fB.getUserData()).type == Variables.TYPE_KUNAI || ((Entity) fB.getUserData()).type == Variables.TYPE_BOMB) && fA.getUserData() instanceof Player && ((Player) fA.getUserData()).uid != ((Entity) fB.getUserData()).uid) { 
					Player pl = ((Player) fA.getUserData());
					Entity projectile = ((Entity) fB.getUserData());
					pl.getHit((projectile.type == Variables.TYPE_KUNAI) ? Variables.KUNAI_DAMAGE : Variables.BOMB_DAMAGE, projectile.uid);
					EffectRenderer.renderer.create(projectile.posX, projectile.posY, projectile.leftVectorDegree, (projectile.type == Variables.TYPE_KUNAI) ? Variables.EFFECT_BLOOD : Variables.EFFECT_EXPLOSION);
					projectile.isDead = true;
				} else if (fB.getUserData() instanceof Entity && (((Entity) fB.getUserData()).type == Variables.TYPE_KUNAI || ((Entity) fB.getUserData()).type == Variables.TYPE_BOMB) && fA.getUserData() instanceof String) { 
					Entity projectile = ((Entity) fB.getUserData());
					EffectRenderer.renderer.create(projectile.posX, projectile.posY, projectile.normalizedDegree, (projectile.type == 2) ? Variables.EFFECT_DUST : Variables.EFFECT_EXPLOSION);
					projectile.isDead = true;
				} else if (fA.getUserData() instanceof Entity && (((Entity) fA.getUserData()).type == Variables.TYPE_KUNAI || ((Entity) fA.getUserData()).type == Variables.TYPE_BOMB) && fB.getUserData() instanceof String) { 
					Entity projectile = ((Entity) fA.getUserData());
					EffectRenderer.renderer.create(projectile.posX, projectile.posY, projectile.normalizedDegree, (projectile.type == 2) ? Variables.EFFECT_DUST : Variables.EFFECT_EXPLOSION);
					projectile.isDead = true;
				}
				
				if (fA.getUserData().equals("foot") || fB.getUserData().equals("foot")) {
					player.state = Variables.STATE_IDLE;
					player.isDoubleJumped = false;
				}
			}

			@Override
			public void endContact(Contact contact) {
				Fixture fA = contact.getFixtureA();
				Fixture fB = contact.getFixtureB();
				
				if (fA.getUserData() == null || fB.getUserData() == null) return;
				
				if (fA.getUserData().equals("wallsensor") || fB.getUserData().equals("wallsensor")) {
					player.gravityVector.x = 0;
					player.gravityVector.y = -10f;
				}
				
				if (fA.getUserData().equals("foot") || fB.getUserData().equals("foot")) {
					player.state = Variables.STATE_FLY;
				}
			}

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {
				
			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {
				
			}
			
		});
	}
	
	private void initPlayer() {
		player = new Player(0, Variables.TYPE_MAIN_PLAYER, 1500, 500, (byte) 1, world, this);
		addEntity(player, 0);
	}

	public void update() {
		// process network packets
		processPackets();
				
		// update entities
		for (Iterator<Entity> i = entities.iterator(); i.hasNext();) {
			Entity entity = i.next();
			if (entity.isDead) {
				if (entity.body != null && !world.isLocked()) {
					world.destroyBody(entity.body);
					entity.body.setUserData(null);
					entity.body = null;
				}
				i.remove();
			} else {
				entity.update();
			}
		}

		// simulate physics
		world.step(1 / 60f, 6, 2);
	}

}
