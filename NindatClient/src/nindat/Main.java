package nindat;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import utility.Assets;

public class Main extends Game {
	
	public static String IP = "localhost";
	public static final int PORT = 3333;
	public static boolean isMultiplayer = false;
	
	public static int width, height;
	private int stage = 0;
	
	@Override
	public void create() {
		width = Gdx.graphics.getWidth();
		height = Gdx.graphics.getHeight();
		Assets.load();
		Timer.schedule(new Task() {
			@Override
			public void run() {
				if (Assets.isLoaded() && stage == 0) {
					getIp();
					stage = 1;
				} else if (stage == 2) {
					setScreen(new GameScreen());
					stage = 3;
					cancel();
				}
			}
		}, 0, (float) Float.MIN_NORMAL);
	}
	
	private void getIp() {
		Gdx.input.getTextInput(new TextInputListener() {

			@Override
			public void canceled() {
				Main.isMultiplayer = false;
				stage = 2;
			}

			@Override
			public void input(String ip) {
				Main.IP = ip;
				Main.isMultiplayer = true;
				stage = 2;
			}
			
		}, "Enter server IP", "", "Press cancel to play single player");
		
	}
	
}
