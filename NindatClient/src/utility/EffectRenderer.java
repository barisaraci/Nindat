package utility;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class EffectRenderer {
	
	private static ArrayList<Effect> effects = new ArrayList<>();
	private static SpriteBatch batch;
	private static TextureAtlas txtAtlas;
	
	public static EffectRenderer renderer = new EffectRenderer();
	
	private EffectRenderer() {
		
	}
	
	public void init(SpriteBatch batcher, TextureAtlas textureAtlas) {
		batch = batcher;
		txtAtlas = textureAtlas;
	}
	
	public void create(int posX, int posY, float angle, byte type) {
		Effect effect = new Effect(posX, posY, angle, type);
		effects.add(effect);
	}
	
	public void render() {
		for (Iterator<Effect> i = effects.iterator(); i.hasNext();) {
			Effect effect = i.next();
			if (effect.isDead)
				i.remove();
			else {
				effect.anim.playEffectAnim(effect);
				effect.anim.render(batch, txtAtlas, effect);
			}
		}
	}
	

}
