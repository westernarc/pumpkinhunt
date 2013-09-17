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
	
	//transform m4:
	//Value [12]: X
	//Value [13]: Y
	//Value [14]: Z
	static class var {
		static int w;
		static int h;
		
		static float gameSize = 13;
		
		static AssetManager assets;
		
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
	ModelInstance mdiPlayer;

	ModelInstance mdiSky;
	Model mdlGround;
	ModelInstance[] mdiGround;
	final int numGround = 3;
	final int numGroundLength = 800;
	Model cube;
	Model mdlUfoR;
	Array<ModelInstance> instances;
	
	SpriteBatch spriteBatch;
	Sprite sprTitle;
	
	Player player; 
	
	MENU stateMenu;
	GAME stateGame;
	
	enum emitterstate {
		OFF,
		RANDOM,
		SPIRAL2
	}
	emitterstate estate;
	float[] ptime;
	
	@Override
	public void create() {		
		var.w = Gdx.graphics.getWidth();
		var.h = Gdx.graphics.getHeight();
		
		modelBatch = new ModelBatch();
		spriteBatch = new SpriteBatch();

		var.assets = new AssetManager();
		
		var.assets.load("3d/player.g3dj", Model.class);
		var.assets.load("3d/ground.g3dj", Model.class);
		var.assets.load("3d/sky.g3dj", Model.class);
		var.assets.load("3d/cube.g3dj", Model.class);
		var.assets.load("3d/ufoR.g3dj", Model.class);
		
		var.assets.load("2d/tex.png", Texture.class);
		var.assets.load("2d/title.png", Texture.class);
		var.assets.load("2d/filter.png", Texture.class);
		fadefilter.fltFilterAlpha = 1;
		
		var.state = var.STATES.LOAD;
		
		player = new Player();
		player.radPos.x = -1.55f;
		player.radPos.y = 10;
		player.polToRectCoords();
		stateMenu = new MENU();
		stateGame = new GAME();
	}

	public void recreate() {
	
	}
	
	private void doneLoading() {
		Model mdlPlayer = var.assets.get("3d/player.g3dj", Model.class);
		mdiPlayer = new ModelInstance(mdlPlayer);
		
		animController = new AnimationController(mdiPlayer);
		animController.animate("up", -1, 1f, null, 0.2f);
		animController.setAnimation("up", -1, 1f, null);

		Model mdlSky = var.assets.get("3d/sky.g3dj", Model.class);
		mdiSky = new ModelInstance(mdlSky);
		
		Model mdlGround = var.assets.get("3d/ground.g3dj", Model.class);
		mdiGround = new ModelInstance[numGround];
		for(int i = 0; i < numGround; i++) {
			mdiGround[i] = new ModelInstance(mdlGround);
			mdiGround[i].transform.translate(0,0,i * numGroundLength);
		}
		
		cube = var.assets.get("3d/cube.g3dj", Model.class);
		mdlUfoR = var.assets.get("3d/ufoR.g3dj", Model.class);
		
		
		stateMenu.init();
		stateGame.init();
		
		fadefilter.sprFilter = new Sprite(var.assets.get("2d/filter.png", Texture.class));
		fadefilter.sprFilter.scale(var.h);
	}
	
	@Override
	public void dispose() {	}

	//Update loop
	@Override
	public void render() {
		var.tpf = Gdx.graphics.getDeltaTime();
		if(cam != null)
			cam.update();
		
		if(var.state == var.STATES.LOAD) {
			if(var.assets.update()) {
				doneLoading();
				var.state = var.STATES.MENU;
			}
		} else {
			
			Gdx.gl.glViewport(0,0,var.w,var.h);
			Gdx.gl.glClearColor(0.3137f, 0.6824f, 0.8f, 1);
			Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
						
			animController.update(Gdx.graphics.getDeltaTime());
			 
			modelBatch.begin(cam);
			
			//modelBatch.render(mdiSky);
			for(ModelInstance grndInstance : mdiGround) {
				modelBatch.render(grndInstance);
				grndInstance.transform.translate(0, 0, -5);
				if(grndInstance.transform.getValues()[14] < -numGroundLength){
					grndInstance.transform.translate(0,0,numGroundLength * numGround);
				}
			}
		    mdiPlayer.transform.setToRotation(Vector3.Z, player.radPos.x * 360f / 6.283f + 90);
			mdiPlayer.transform.setTranslation(player.pos);
			modelBatch.render(mdiPlayer);
			modelBatch.end();
			
			if(var.state == var.STATES.MENU) {
				stateMenu.render(var.tpf);
			} else if(var.state == var.STATES.GAME) {
				stateGame.render(var.tpf);
			}
			
			renderFilter();
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
	public void resetPtime() {
		for(int i = 0; i < 64; i++) ptime[i] = 0;
	}
	class GAME {
		public void init() {
			cam = new PerspectiveCamera(55, var.w, var.h);
			cam.position.set(0, -19, 18);
			cam.lookAt(Vector3.Z);
			cam.near = 0.1f;
			cam.far = 1000f;
			cam.update();
			
			instances = new Array<ModelInstance>();
			
			//Emitter state.  Defaults to RANDOM.
			estate = emitterstate.SPIRAL2;
			//Initialize array of pattern timer temp variables
			ptime = new float[64];
			resetPtime();
		}
		
		public void render(float tpf) {
			//spawn bullets
			switch(estate) {
			case RANDOM:
				if(Math.random() < 0.08f & instances.size < 15) {
					ModelInstance cubeInstance = new ModelInstance(cube);
					float randAngle = (float)Math.random();
					cubeInstance.transform.setTranslation(new Vector3((float)Math.cos(randAngle * 6.28f) * 10, (float)Math.sin(randAngle * 6.28) * 10, 1200));
					instances.add(cubeInstance);
				}
				break;
			case SPIRAL2:
				//0: angle 1
				//1: angle 2
				//2: spawn interval 1
				//3: spawn interval 2
				ptime[0] += tpf * 10f;
				ptime[1] += tpf * 10f;
				ptime[2] += tpf;
				
				if(ptime[2] > 0.05f) {
					ModelInstance cubeInstance = new ModelInstance(mdlUfoR);
					cubeInstance.transform.setTranslation(new Vector3((float)Math.cos(ptime[0]) * 10, (float)Math.sin(ptime[0]) * 10, 1200));
					cubeInstance.transform.rotate(Vector3.Z,(ptime[0]) * 360 / 6.283f + 90);
					instances.add(cubeInstance);
					
					cubeInstance = new ModelInstance(mdlUfoR);
					cubeInstance.transform.setTranslation(new Vector3((float)Math.cos(ptime[0]+ 3.14f) * 10, (float)Math.sin(ptime[0] + 3.14f) * 10, 1200));
					cubeInstance.transform.rotate(Vector3.Z,ptime[0] * 360 / 3.14f);
					//instances.add(cubeInstance);
					ptime[2] = 0;
				}
				
				break;
			}
			for(ModelInstance instance : instances) {
				modelBatch.render(instance);
				
				//Move cubes down
				instance.transform.translate(0,0,-3f);
				
				//Remove cubes out of view
				if(instance.transform.getValues()[14] < -100) {
					instances.removeValue(instance, true);
					continue;
				}
				
				//Basic collision
				if(Math.abs(instance.transform.getValues()[12] - mdiPlayer.transform.getValues()[12]) < 2 && 
						Math.abs(instance.transform.getValues()[13] - mdiPlayer.transform.getValues()[13]) < 2 &&
						Math.abs(instance.transform.getValues()[14] - mdiPlayer.transform.getValues()[14]) < 2) {
					instances.removeValue(instance, true);
					continue;
				}
			}
			
			if(isKeyPressed(Keys.LEFT)) {
				//player.pos.x += 1f;
				//mdiPlayer.transform.rotate(0,1,0,-4);
				player.radPos.x += 0.05f;
				player.polToRectCoords();
				if(animController.current.animation.id != "right"){
					animController.animate("right", -1, 1f, null, 0.5f);
				}
			} else if(isKeyPressed(Keys.RIGHT)) {
				//mdiPlayer.transform.rotate(0,1,0,4);
				player.radPos.x -= 0.05f;
				player.polToRectCoords();
				if(animController.current.animation.id != "left"){
					animController.animate("left", -1, 1f, null, 0.5f);
				}
			} else {
				if(animController.current.animation.id != "up"){
					//animController.animate("up", -1, 1f, null, 1f);
				}
			}
			
			if(isKeyPressed(Keys.A)) {
				mdiPlayer.transform.rotate(0,0,1,4f);
			}
			if(Gdx.input.isKeyPressed(Input.Keys.S)) {
			}
		}
	}
	class MENU {
		float fltTitleFadeRate = 0.1f;
		float fltTitleAlpha = 0f;
		
		boolean blnShowTitle;
		boolean blnTitleCompleted;
		
		Vector3 vecCamMenuPos = new Vector3(2,-17,7);
		Vector3 vecCamGamePos = new Vector3(0, 0, -50);
		
		float fltCamLerpValue;
		float fltCamLerpMenuRate = 0.001f;
		float fltCamLerpGameRate = 0.0001f;
		float fltCamLerpMenuLimit = 0.1f;
		float fltCamLerpGameLimit = 0.1f;
		
		Vector3 vecCamMenuFocus;
		Vector3 vecCamGameFocus;
		
		public void init() {
			fltTitleAlpha = 0;
			fltCamLerpValue = 0;
			
			blnTitleCompleted = false;
			blnShowTitle = true;
			
			vecCamMenuFocus = new Vector3(0,-10,4);
			vecCamGameFocus = new Vector3(0,0,0);
			
			sprTitle = new Sprite(var.assets.get("2d/title.png", Texture.class));
			sprTitle.setPosition(var.w/2f - (sprTitle.getWidth()/2f), var.h / 3f - (sprTitle.getHeight()/2f));
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
				cam.lookAt(vecCamMenuFocus);
			} else {
				if(fltTitleAlpha - fltTitleFadeRate > 0) {fltTitleAlpha -= fltTitleFadeRate; } else { fltTitleAlpha = 0; }
				
				if(fltCamLerpValue + fltCamLerpGameRate < 1) fltCamLerpValue += fltCamLerpGameRate; else fltCamLerpValue = 1;
				cam.position.lerp(vecCamGamePos, fltCamLerpValue);
				cam.up.set(Vector3.Y);
				cam.lookAt(vecCamMenuFocus.lerp(vecCamGameFocus, fltCamLerpValue));
				if(fltCamLerpValue >= 0.03f) {
					//Camera is done transitioning; Menu state is over
					var.state = var.STATES.GAME;
				}
			}
			spriteBatch.begin();
			sprTitle.draw(spriteBatch);
			spriteBatch.end();
			
			if(Gdx.input.isTouched() && fltTitleAlpha == 1) {
				blnShowTitle = false;
				fltCamLerpValue = 0;
			}
		}
	}
}
