package nindat;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;

import utility.Variables;

public class InputHandler implements InputProcessor {
	
	private GameWorld gameWorld;
	private boolean keys[] = new boolean[155];
	
	public InputHandler(GameWorld gameWorld) {
		this.gameWorld = gameWorld;
	}
	
	public void handle() {
		if (gameWorld.player == null)
			return;
		
		Vector2 vel = new Vector2(gameWorld.player.body.getLinearVelocity());
		float speed = vel.len();
		if (speed > Variables.PLAYER_MAX_VELOCITY)
			gameWorld.player.body.setLinearVelocity(vel.x * (Variables.PLAYER_MAX_VELOCITY / speed), vel.y * (Variables.PLAYER_MAX_VELOCITY / speed));
		
		float angularVel = gameWorld.player.body.getAngularVelocity();
		
		if (angularVel <= - Variables.PLAYER_MAX_ANGULAR_SPEED) 
			gameWorld.player.body.setAngularVelocity(- Variables.PLAYER_MAX_ANGULAR_SPEED);
		else if (angularVel >= Variables.PLAYER_MAX_ANGULAR_SPEED)
			gameWorld.player.body.setAngularVelocity(Variables.PLAYER_MAX_ANGULAR_SPEED);
		
		if (keys[Input.Keys.LEFT] && gameWorld.player.body.getLinearVelocity().x > - Variables.PLAYER_MAX_VELOCITY) {
			gameWorld.player.dir = 0;
			if (gameWorld.player.state != Variables.STATE_FLY) {
				gameWorld.player.changeState(Variables.STATE_RUN);
				
				Vector2 impulse = new Vector2((float) (Variables.PLAYER_SPEED * Math.cos(Math.toRadians(gameWorld.player.leftVectorDegree))), (float) (Variables.PLAYER_SPEED * Math.sin(Math.toRadians(gameWorld.player.leftVectorDegree))));
				gameWorld.player.body.applyLinearImpulse(impulse, gameWorld.player.body.getWorldCenter(), true);
			} else {
				if (Variables.IS_THERE_ROTATION_ACCELERATION)
					gameWorld.player.body.applyLinearImpulse(- Variables.PLAYER_ROT_ACC_SPEED, 0, gameWorld.player.body.getWorldCenter().x, gameWorld.player.body.getWorldCenter().y + gameWorld.player.height - 10 / Variables.PPM, true);
				else
					gameWorld.player.body.setAngularVelocity(Variables.PLAYER_ROTATION_SPEED);
			}
        }
		
		if (keys[Input.Keys.RIGHT] && gameWorld.player.body.getLinearVelocity().x < Variables.PLAYER_MAX_VELOCITY) {
			gameWorld.player.dir = 1;
			if (gameWorld.player.state != Variables.STATE_FLY) {
				gameWorld.player.changeState(Variables.STATE_RUN);
				
				Vector2 impulse = new Vector2((float) (Variables.PLAYER_SPEED * Math.cos(Math.toRadians(gameWorld.player.rightVectorDegree))), (float) (Variables.PLAYER_SPEED * Math.sin(Math.toRadians(gameWorld.player.rightVectorDegree))));
				gameWorld.player.body.applyLinearImpulse(impulse, gameWorld.player.body.getWorldCenter(), true);
			} else {
				if (Variables.IS_THERE_ROTATION_ACCELERATION)
					gameWorld.player.body.applyLinearImpulse(Variables.PLAYER_ROT_ACC_SPEED, 0, gameWorld.player.body.getWorldCenter().x, gameWorld.player.body.getWorldCenter().y + gameWorld.player.height - 10 / Variables.PPM, true);
				else
					gameWorld.player.body.setAngularVelocity(- Variables.PLAYER_ROTATION_SPEED);
			}
		}
		
		if (keys[Input.Keys.SPACE]) {
			if (gameWorld.player.state != Variables.STATE_FLY) {
				Vector2 impulse = new Vector2((float) (Variables.PLAYER_JUMP_SPEED * Math.cos(Math.toRadians(gameWorld.player.normalizedDegree))), (float) (Variables.PLAYER_JUMP_SPEED * Math.sin(Math.toRadians(gameWorld.player.normalizedDegree))));
				gameWorld.player.body.applyLinearImpulse(impulse, gameWorld.player.body.getWorldCenter(), true);
			}
		}
		
		if (keys[Input.Keys.UP]) {
			if (gameWorld.camZoom >= 1f) {
				gameWorld.camZoom -= 0.025f;
			}
		} else if (keys[Input.Keys.DOWN]) {
			if (gameWorld.camZoom <= 3f) {
				gameWorld.camZoom += 0.025f;
			}
		}
	}

	@Override
	public boolean keyDown(int keycode) {
		keys[keycode] = true;
		
		if (keycode == Input.Keys.SPACE && gameWorld.player.mana > Variables.JUMP_MANA) {
			gameWorld.player.mana -= Variables.JUMP_MANA;
			gameWorld.player.performAction(Variables.ACTION_JUMP);
		}
		
		if (keycode == Input.Keys.E && gameWorld.player.mana > Variables.KUNAI_MANA) {
			gameWorld.player.mana -= Variables.KUNAI_MANA;
			if (gameWorld.player.state != Variables.STATE_FLY)
				gameWorld.player.performAction(Variables.ACTION_THROW, Variables.TYPE_KUNAI);
			else
				gameWorld.player.performAction(Variables.ACTION_JUMP_THROW, Variables.TYPE_KUNAI);
		}
		
		if (keycode == Input.Keys.Q && gameWorld.player.mana > Variables.BOMB_MANA) {
			gameWorld.player.mana -= Variables.BOMB_MANA;
			if (gameWorld.player.state != Variables.STATE_FLY)
				gameWorld.player.performAction(Variables.ACTION_THROW, Variables.TYPE_BOMB);
			else
				gameWorld.player.performAction(Variables.ACTION_JUMP_THROW, Variables.TYPE_BOMB);
		}
		
		if (keycode == Input.Keys.R) {
			gameWorld.player.reset();
		}
		
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		keys[keycode] = false;
		
		if (keycode == Input.Keys.LEFT) {
			if (gameWorld.player.state != Variables.STATE_FLY) {
				gameWorld.player.changeState(Variables.STATE_IDLE);
				if (Variables.IS_THERE_SLIDING)
					gameWorld.player.body.setLinearVelocity(gameWorld.player.body.getLinearVelocity().x / 2, gameWorld.player.body.getLinearVelocity().y / 2);
				else
					gameWorld.player.body.setLinearVelocity(0, 0);
			} else {
				gameWorld.player.body.setAngularVelocity(0);
			}
        }
		
		if (keycode == Input.Keys.RIGHT) {
			if (gameWorld.player.state != Variables.STATE_FLY) {
				gameWorld.player.changeState(Variables.STATE_IDLE);
				if (Variables.IS_THERE_SLIDING)
					gameWorld.player.body.setLinearVelocity(gameWorld.player.body.getLinearVelocity().x / 2, gameWorld.player.body.getLinearVelocity().y / 2);
				else
					gameWorld.player.body.setLinearVelocity(0, 0);
			} else {
				gameWorld.player.body.setAngularVelocity(0);
			}
		}
		
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

}
