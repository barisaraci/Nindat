package nindat;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Launcher {
	
	public static void main(String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Nindat";
		config.addIcon("assets/graphics/icon.png", FileType.Internal);
		config.width = 1366;
		config.height = 768;
		config.resizable = false;
		new LwjglApplication(new Main(), config);
	}
	
}
