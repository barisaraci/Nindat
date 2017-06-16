package utility;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public class Assets {
	public static AssetManager manager = new AssetManager();

	public static void load() {
		manager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
		
		manager.load("assets/graphics/NindatPack.pack", TextureAtlas.class);
		manager.load("assets/graphics/bg.png", Texture.class);
		manager.load("assets/graphics/map.tmx", TiledMap.class);

		manager.finishLoading();
	}

	public static boolean isLoaded() {
		return manager.update();
	}

	public static void resetManager() {
		manager = null;
		manager = new AssetManager();
	}
}
