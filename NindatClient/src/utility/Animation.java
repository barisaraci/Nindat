package utility;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import entities.LivingEntity;

public class Animation {
	
	private int delay, frameCount, curFrame, curMil = 0;
	private String animName;
	
	public Animation(String animName, int frameCount, int delay) { // delay 1 - 60
		this.animName = animName;
		this.frameCount = frameCount;
		this.delay = delay;
	}
	
	public void playAnim() {
		if (curMil >= delay) {
			curMil = 0;
			if (curFrame >= frameCount - 1) {
				curFrame = 0;
			} else {
				curFrame++;
			}
		} else {
			curMil++;
		}
	}
	
	public void resetAnim() {
		curFrame = 0;
		curMil = 0;
	}
	
	public void playActionAnim(LivingEntity entity) {
		if (curMil >= delay) {
			curMil = 0;
			if (curFrame >= frameCount - 1) {
				curFrame = 0;
				entity.action = 0;
				entity.curAnim = entity.animSet.get(Variables.STATE_IDLE);
			} else {
				curFrame++;
			}
		} else {
			curMil++;
		}
	}
	
	public void playEffectAnim(Effect effect) {
		if (curMil >= delay) {
			curMil = 0;
			if (curFrame >= frameCount - 1) {
				curFrame = 0;
				effect.isDead = true;
			} else {
				curFrame++;
			}
		} else {
			curMil++;
		}
	}
	
	public void render(SpriteBatch batch, TextureAtlas txtAtlas, LivingEntity entity) {
		TextureRegion region = txtAtlas.findRegion(animName + curFrame);
		int width = region.getRegionWidth();
		int height = region.getRegionHeight();
		
		float scale = (entity.action == Variables.ACTION_JUMP) ? 1.1f : 1f;
		
		if (entity.dir == 1) {
			if (region.isFlipX()) { region.flip(true, false); }
			batch.draw(region, entity.posX, entity.posY, entity.width / 2, entity.height / 2, width, height, scale, scale, (float) Math.toDegrees(entity.angle));
		} else {
			if (!region.isFlipX()) { region.flip(true, false); }
			batch.draw(region, entity.posX, entity.posY, entity.width / 2, entity.height / 2, width, height, scale, scale, (float) Math.toDegrees(entity.angle));
		}
	}
	
	public void render(SpriteBatch batch, TextureAtlas txtAtlas, Effect effect) {
		TextureRegion region = txtAtlas.findRegion(animName + curFrame);
		int width = region.getRegionWidth();
		int height = region.getRegionHeight();
		
		batch.draw(region, effect.posX, effect.posY, width / 2, height / 2, width, height, 2f, 2f, effect.angle);
	}

}
