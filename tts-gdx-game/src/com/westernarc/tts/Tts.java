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
	
	static class fadefilter {
		static Sprite sprFilter;
		static float fltFilterAlpha = 1;
		static boolean blnFilterOn;
		//True means filter shows; false is filter is transparent
		
		static float fltFilterRate = 0.05f;
	}
	
	AnimationController animController;
	
	ModelBatch modelBatch;
	
	PerspectiveCamera cam;
	ModelInstance cube;
	Array<ModelInstance> instances;
	
	SpriteBatch spriteBatch;
	Sprite sprTitle;
	
	Player player; 
	
	MENU stateMenu;
	
	@Override
	public void create() {		
		var.w = Gdx.graphics.getWidth();
		var.h = Gdx.graphics.getHeight();
		
		modelBatch = new ModelBatch();
		spriteBatch = new SpriteBatch();
		
		cam = new PerspectiveCamera(45, var.w, var.h);
		cam.position.set(0, -19, 18);
		cam.lookAt(Vector3.Z);
		cam.near = 0.1f;
		cam.far = 300f;
		cam.update();
		
		var.assets = new AssetManager();
		instances = new Array<ModelInstance>();
		var.assets.load("3d/player.g3dj", Model.class);
		var.assets.load("2d/tex.png", Texture.class);
		var.assets.load("2d/filter.png", Texture.class);
		fadefilter.fltFilterAlpha = 1;
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
		
		sprTitle = new Sprite(var.assets.get("2d/tex.png", Texture.class));
		fadefilter.sprFilter = new Sprite(var.assets.get("2d/filter.png", Texture.class));
		fadefilter.sprFilter.scale(var.h);
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
			
			renderFilter();
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

	private void renderFilter() {
		if(fadefilter.blnFilterOn) {
			if(fadefilter.fltFilterAlpha + fadefilter.fltFilterRate < 1) {
				fadefilter.fltFilterAlpha += fadefilter.fltFilterRate;
			} else {
				fadefilter.fltFilterAlpha = 1;
			}
		} else {
			if(fadefilter.fltFilterAlpha - fadefilter.fltFilterRate > 0) {
				fadefilter.fltFilterAlpha -= fadefilter.fltFilterRate;
			} else {
				fadefilter.fltFilterAlpha = 0;
			}
		}
		fadefilter.sprFilter.setColor(1,1,1,fadefilter.fltFilterAlpha);
		
		spriteBatch.begin();
		fadefilter.sprFilter.draw(spriteBatch);
		spriteBatch.end();
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
		
		Vector3 vecCamMenuPos = new Vector3(2,-17,7);
		Vector3 vecCamGamePos = new Vector3(0, 20, -50);
		
		float fltCamLerpValue;
		float fltCamLerpMenuRate = 0.001f;
		float fltCamLerpGameRate = 0.0001f;
		float fltCamLerpMenuLimit = 0.1f;
		float fltCamLerpGameLimit = 0.1f;
		
		public void init() {
			fltTitleAlpha = 0;
			fltCamLerpValue = 0;
			
			blnTitleCompleted = false;
			blnShowTitle = true;
		}
		public void render(float tpf) {
			sprTitle.setColor(1,1,1,fltTitleAlpha);
			if(blnShowTitle) {
				fadefilter.blnFilterOn = false;
				if(fltCamLerpValue > fltCamLerpMenuLimit) {
					if(fltTitleAlpha + fltTitleFadeRate < 1) {fltTitleAlpha += fltTitleFadeRate;} else {fltTitleAlpha = 1;}
				}
				if(fltCamLerpValue + fltCamLerpMenuRate < 1) {
					fltCamLerpValue += fltCamLerpMenuRate; 
				} else {
					fltCamLerpValue = 1;
				}
				cam.position.lerp(vecCamMenuPos, fltCamLerpValue);
				cam.up.set(Vector3.Y);
				cam.lookAt(0,-10,4);
			} else {
				if(fltTitleAlpha - fltTitleFadeRate > 0) {fltTitleAlpha -= fltTitleFadeRate; } else { fltTitleAlpha = 0; }
				
				if(fltCamLerpValue + fltCamLerpGameRate < 1) fltCamLerpValue += fltCamLerpGameRate; else fltCamLerpValue = 1;
				cam.position.lerp(vecCamGamePos, fltCamLerpValue);
				cam.up.set(Vector3.Y);
				cam.lookAt(0,-10,4);
			}
			spriteBatch.begin();
			sprTitle.draw(spriteBatch);
			spriteBatch.end();
			
			if(Gdx.input.isTouched()) {
				blnShowTitle = false;
				fltCamLerpValue = 0;
			}
		}
	}
}
