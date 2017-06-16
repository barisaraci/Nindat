package nindat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;

import entities.Entity;
import entities.Player;
import utility.Assets;
import utility.EffectRenderer;
import utility.Variables;

public class GameRenderer {
	
	private GameWorld gameWorld;
	private SpriteBatch batch = new SpriteBatch();
	private OrthographicCamera cam = new OrthographicCamera(Main.width, Main.height), fixedCam = new OrthographicCamera(Main.width, Main.height);
	private TextureAtlas txtAtlas = Assets.manager.get("assets/graphics/NindatPack.pack", TextureAtlas.class);
	private Box2DDebugRenderer box2dRenderer = new Box2DDebugRenderer();
	private OrthogonalTiledMapRenderer tmr;
	private ShapeRenderer shapeRenderer = new ShapeRenderer();
	private Matrix4 box2dMatrix;
	private BitmapFont fontB = new BitmapFont();
	private BitmapFont fontS = new BitmapFont();
	private Texture background = Assets.manager.get("assets/graphics/bg.png", Texture.class);
	
	public GameRenderer(GameWorld gWorld) {
		this.gameWorld = gWorld;
		init();
	}
	
	private void init() {
		tmr = new OrthogonalTiledMapRenderer(gameWorld.tiledMap);
		EffectRenderer.renderer.init(batch, txtAtlas);
		fontS.setColor(Color.BLACK);
		fontB.setColor(Color.BLACK);
		fontB.getData().setScale(2.5f);
	}
	
	public void render() {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		if (gameWorld.player == null)
			return;
		
		cam.update();
		cam.zoom = gameWorld.camZoom;
		cam.position.set(gameWorld.player.posX, gameWorld.player.posY + 250, 0);
		
		// render background
		batch.setProjectionMatrix(fixedCam.combined);
		batch.disableBlending();
		batch.begin();
			batch.draw(background, - Main.width / 2, - Main.height / 2);
		batch.end();
		
		// render map
		tmr.setView(cam);
		tmr.render();
		
		// render objects and effects
		batch.setProjectionMatrix(cam.combined);
		batch.enableBlending();
		batch.begin();
			for (Entity entity : gameWorld.entities) {
				entity.render(batch, txtAtlas);
			}
			EffectRenderer.renderer.render();
		batch.end();
		
		// render hp and mana
		shapeRenderer.setProjectionMatrix(cam.combined);
		shapeRenderer.begin(ShapeType.Filled);
		for (Entity entity : gameWorld.entities) {
			if (entity instanceof Player) {
				((Player) entity).renderHp(shapeRenderer);
				
			}
		}
		shapeRenderer.end();
		
		// render names and scores if multiplayer is on
		if (Main.isMultiplayer) {
			batch.begin();
				for (Entity entity : gameWorld.entities) {
					if (entity instanceof Player) {
						fontB.draw(batch, "player" + entity.uid, entity.posX, entity.posY);
					}
				}
			batch.end();
			
			batch.setProjectionMatrix(fixedCam.combined);
			batch.begin();
				for (int i = 0; i < gameWorld.entities.size(); i++) {
					Entity e = gameWorld.entities.get(i);
					if (e instanceof Player) {
						fontS.draw(batch, "player" + e.uid + " score: " + ((Player) e).score, 550, 350 - i * 20);
					}
				}
			batch.end();
		}
		
		// render physics visually
		if (Variables.IS_PHYSICS_VISUAL) {
			box2dMatrix = batch.getProjectionMatrix().cpy().scale(Variables.PPM, Variables.PPM, 0);
			box2dRenderer.render(gameWorld.world, box2dMatrix);
		}
	}

}
