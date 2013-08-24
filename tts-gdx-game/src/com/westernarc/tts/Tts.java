package com.westernarc.tts;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.westernarc.objects.Player;

public class Tts implements ApplicationListener {
	static class var {
		static int w;
		static int h;
		
		static AssetManager assets;
		static boolean loading;
		
		static enum STATES {LOAD, MENU, GAME, DEAD, SCORE};
		static STATES state;
		
		static float tpf;
	}

	static class control {
		int h;
	}
	
	AnimationController animController;
	
	ModelBatch modelBatch;
	
	PerspectiveCamera cam;
	ModelInstance cube;
	Array<ModelInstance> instances;
	
	SpriteBatch spriteBatch;
	Sprite sprite;
	
	Player player; 
	
	MENU stateMenu;
	
	@Override
	public void create() {		
		var.w = Gdx.graphics.getWidth();
		var.h = Gdx.graphics.getHeight();
		
		modelBatch = new ModelBatch();
		spriteBatch = new SpriteBatch();
		
		cam = new PerspectiveCamera(45, var.w, var.h);
		cam.position.set(0, -19, 8);
		cam.lookAt(0,0,0);
		cam.near = 0.1f;
		cam.far = 300f;
		cam.update();
		
		var.assets = new AssetManager();
		instances = new Array<ModelInstance>();
		var.assets.load("3d/player.g3dj", Model.class);
		var.assets.load("2d/tex.png", Texture.class);
		var.loading = true;
		
		var.state = var.STATES.LOAD;
		
		stateMenu = new MENU();
		stateMenu.init();
	}

	public void recreate() {
	
	}
	
	private void doneLoading() {
		Model cubeModel = var.assets.get("3d/player.g3dj", Model.class);
		
		cube = new ModelInstance(cubeModel);
		cube.transform.translate(0,-10,0);
		
		instances.add(cube);
		
		animController = new AnimationController(cube);
		animController.animate("up", -1, 1f, null, 0.2f);
		animController.setAnimation("up", -1, 1f, null);
		
		sprite = new Sprite(var.assets.get("2d/tex.png", Texture.class));
		
		var.loading = false;
	}
	
	@Override
	public void dispose() {	}

	//Update loop
	@Override
	public void render() {
		var.tpf = Gdx.graphics.getDeltaTime();
		cam.update();
		
		if(var.state == var.STATES.LOAD) {
			if(var.assets.update()) {
				doneLoading();
				var.state = var.STATES.MENU;
			}
		} else {
			
			Gdx.gl.glViewport(0,0,var.w,var.h);
			Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			
			
			animController.update(Gdx.graphics.getDeltaTime());
			 
			modelBatch.begin(cam);
			
			for(ModelInstance instance : instances) {
				modelBatch.render(instance);
			}
			modelBatch.end();
			
			if(var.state == var.STATES.MENU) {
				stateMenu.render(var.tpf);
			}
		}
		
		if(isKeyPressed(Keys.LEFT)) {
			cube.transform.translate(1f,0,0);
		}
		if(isKeyPressed(Keys.RIGHT)) {
			cube.transform.translate(-1f,0,0);
		}
		if(isKeyPressed(Keys.A)) {
			cube.transform.rotate(0,0,1,4f);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.S)) {
		}

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
	private boolean isKeyPressed(int key){
		return Gdx.input.isKeyPressed(key);
	}
	class GAME {
		 
		public void render(float tpf) {
			
		}
	}
	class MENU {
		float fltTitleFadeRate = 0.1f;
		float fltTitleAlpha = 0f;
		
		boolean blnShowTitle;
		boolean blnTitleCompleted;
		
		Vector3 vecCamMenuPos = new Vector3(0,-17,7);
		Vector3 vecCamGamePos = new Vector3(0, 20, -50);
		
		float fltCamLerpValue;
		float fltCamLerpMenuRate = 0.001f;
		float fltCamLerpGameRate = 0.001f;
		
		public void init() {
			fltTitleAlpha = 0;
			fltCamLerpValue = 0;
			
			blnShowTitle = true;
		}
		public void render(float tpf) {
			
			if(blnShowTitle) {
				if(fltTitleAlpha + fltTitleFadeRate < 1) {
					fltTitleAlpha += fltTitleFadeRate;
				} else {
					fltTitleAlpha = 1;
				}
				sprite.setColor(1,1,1,fltTitleAlpha);
				
				if(fltCamLerpValue + fltCamLerpMenuRate < 1) fltCamLerpValue += fltCamLerpMenuRate; else fltCamLerpValue = 1;
				cam.position.lerp(vecCamMenuPos, fltCamLerpValue);
				cam.lookAt(0,-10,4);
				//cam.position.add(1,0,0);
			} else {
				if(fltTitleAlpha - fltTitleFadeRate > 0) {
					fltTitleAlpha -= fltTitleFadeRate;
				} else {
					fltTitleAlpha = 0;
				}
				if(blnShowTitle = false) {
					var.state = var.STATES.GAME;
				}
				sprite.setColor(1,1,1,fltTitleAlpha);
				
				if(fltCamLerpValue + fltCamLerpGameRate < 1) fltCamLerpValue += fltCamLerpGameRate; else fltCamLerpValue = 1;
				cam.position.lerp(vecCamGamePos, fltCamLerpValue);
				cam.up.set(Vector3.Y);
				cam.lookAt(0,0,0);
			}
			spriteBatch.begin();
			sprite.draw(spriteBatch);
			spriteBatch.end();
			
			if(Gdx.input.isTouched()) {
				blnShowTitle = false;
				fltCamLerpValue = 0;
			}
		}
	}
}
