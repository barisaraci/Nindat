package nindat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

public class GameScreen implements Screen {
	
	private GameWorld gameWorld;
	private GameRenderer renderer;
	private InputHandler inputHandler;

	@Override
	public void show() {
		gameWorld = new GameWorld();
		renderer = new GameRenderer(gameWorld);
		inputHandler = new InputHandler(gameWorld);
        Gdx.input.setInputProcessor(inputHandler);
	}

	@Override
	public void render(float delta) {
		inputHandler.handle();
		gameWorld.update();
		renderer.render();
	}

	@Override
	public void resize(int width, int height) {
		
	}

	@Override
	public void pause() {
		
	}

	@Override
	public void resume() {
		
	}

	@Override
	public void hide() {
		
	}

	@Override
	public void dispose() {
		
	}

}
