package com.westernarc.ufohunt;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.lights.BaseLight;
import com.badlogic.gdx.graphics.g3d.lights.Lights;
import com.badlogic.gdx.graphics.g3d.lights.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.westernarc.ufohunt.Behaviours.SimpleBehaviour;
import com.westernarc.ufohunt.Objects.GameObject;
import com.westernarc.ufohunt.Objects.Player;

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
		
		//Game variables that affect gameplay.  (Stats)
		static int power; //Strength of player shots
		static int level; //Affects enemy HP and type, as well as 
		static int speed; //Speed of player acceleration
		static int skill; //Affects what abilities are available
		static int distance; //This is the distance the player has travelled forwards
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

	Lights litGround;
	
	ModelInstance mdiSky;
	Model mdlGround;
	ModelInstance[] mdiGround;
	final int numGround = 3;
	final int numGroundLength = 800;
	Model cube;
	Model mdlUfoR;
	Model mdlShot;
	Model mdlPumpkin;
	ArrayList<ModelInstance> instances;
	ArrayList<GameObject> pumpkins;
	ArrayList<GameObject> shots;
	
	SpriteBatch spriteBatch;
	Sprite sprTitle;
	
	GameObject player; 
	
	MENU stateMenu;
	GAME stateGame;
	
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
		var.assets.load("3d/pumpkinO.g3dj", Model.class);
		var.assets.load("3d/shot.g3dj", Model.class);
		
		var.assets.load("2d/tex.png", Texture.class);
		var.assets.load("2d/cw.png", Texture.class);
		var.assets.load("2d/ccw.png", Texture.class);
		var.assets.load("2d/title.png", Texture.class);
		var.assets.load("2d/filter.png", Texture.class);
		fadefilter.fltFilterAlpha = 1;
		
		var.state = var.STATES.LOAD;
		
		player = new GameObject(true, 10);
		
		stateMenu = new MENU();
		stateGame = new GAME();
		
		litGround = new Lights();
		litGround.ambientLight.set(Color.WHITE);
		litGround.fog = new Color(54f/255f, 49f/255f, 68f/255f, 1);
	}

	public void recreate() {
	
	}
	
	private void doneLoading() {
		Model mdlPlayer = var.assets.get("3d/player.g3dj", Model.class);
		mdiPlayer = new ModelInstance(mdlPlayer);
		player.mdi = mdiPlayer;
		
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
		mdlShot = var.assets.get("3d/shot.g3dj", Model.class);
		mdlPumpkin = var.assets.get("3d/pumpkinO.g3dj", Model.class);
		
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
			Gdx.gl.glClearColor(54f/255f, 49f/255f, 68f/255f, 1);
			Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
						
			animController.update(Gdx.graphics.getDeltaTime());
			 
			modelBatch.begin(cam);
			
			modelBatch.render(mdiSky);
			for(ModelInstance grndInstance : mdiGround) {
				modelBatch.render(grndInstance, litGround);
				grndInstance.transform.translate(0, 0, -5);
				if(grndInstance.transform.getValues()[14] < -numGroundLength){
					grndInstance.transform.translate(0,0,numGroundLength * numGround);
				}
			}
			player.update(var.tpf);
			modelBatch.render(player.mdi);
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

	//Rolls a die with 'choices' number of sides, returns int
    public int chance(int min, int max) {
        return min + (int)(Math.random() * ((max - min) + 1));
	}
    
	class GAME {
		final int OFF = 0;
		final int RANDOM = 1;
		final int SPIRAL2 = 2;
		final int WALL1GAP1 = 3;
		final int BOSS = 4;
		
		Sprite sprControlLeft; //Sprite for turning clockwise
		Sprite sprControlRight; //Sprite for turning counter clockwise
		BitmapFont bmfGameUi; //Font for displaying in game text
		
		public void resetPtime() {
			for(int i = 0; i < 64; i++) ptime[i] = 0;
		}
		
		int estate;
		
		float[] ptime;//Pattern times
		
		float[] gtime;//Game times
		final int GAME = 0;
		final int ESTATE = 1;
		final int SHOT = 2;
		
		final int estateChangeTime = 200; 
		
		float shotCooldown = 0.3f;
		
		float tmrUiFade;
		
		public void init() {
			cam = new PerspectiveCamera(55, var.w, var.h);
			cam.position.set(0, -19, 18);
			cam.lookAt(Vector3.Z);
			cam.near = 0.1f;
			cam.far = 1600f;
			cam.update();
			
			bmfGameUi = new BitmapFont();
			
			sprControlLeft = new Sprite(var.assets.get("2d/cw.png", Texture.class));
			sprControlRight = new Sprite(var.assets.get("2d/ccw.png", Texture.class));
			sprControlRight.setPosition(var.w - sprControlRight.getWidth(), 0);
			reinit();
		}
		public void reinit() {
			instances = new ArrayList<ModelInstance>();
			pumpkins = new ArrayList<GameObject>();
			shots = new ArrayList<GameObject>();
			
			//Emitter state.  Defaults to RANDOM.
			estate = WALL1GAP1;
			//Initialize array of pattern timer temp variables
			ptime = new float[64];
			gtime = new float[64];
			resetPtime();
			gtime[GAME] = 0;
			
			fadefilter.blnFilterOn = false;
			
			tmrUiFade = 0;
			sprControlLeft.setColor(1, 1, 1, 0);
			sprControlRight.setColor(1, 1, 1, 0);
			bmfGameUi.setColor(1, 1, 1, 0);
		}
		public void playerFire() {
			if(gtime[SHOT] > shotCooldown) {
				gtime[SHOT] = 0;
				GameObject obj = new GameObject(true, 10);
				obj.mdi = new ModelInstance(mdlShot);
				
				obj.polpos.set(player.polpos.x, player.polpos.y, player.pos.z);
				obj.behaviours.add(new SimpleBehaviour(0,0,4));
				shots.add(obj);
			}
		}
		public void handleInput(float tpf) {
			//Update shot timer 
			gtime[SHOT] += tpf;
			
			if(isKeyPressed(Keys.LEFT)) {
				//player.pos.x += 1f;
				//mdiPlayer.transform.rotate(0,1,0,-4);
				player.acc.set(-0.01f,0,0);
				if(animController.current.animation.id != "right"){
					//animController.animate("right", -1, 1f, null, 0.5f);
				}
			} else if(isKeyPressed(Keys.RIGHT)) {
				//mdiPlayer.transform.rotate(0,1,0,4);
				player.acc.set(0.01f,0,0);
				if(animController.current.animation.id != "left"){
					//animController.animate("left", -1, 1f, null, 0.5f);
				}
			} else if(isKeyPressed(Keys.UP)){
				if(animController.current.animation.id != "up"){
					animController.animate("up", -1, 1f, null, 1f);
				}
			} else {
				player.acc.set(0,0,0);
				//player.dec(0.1f,0.1f,0.1f);
			}
			
			if(isKeyPressed(Keys.A)) {
				playerFire();
			}
			if(Gdx.input.isKeyPressed(Input.Keys.S)) {
				if(!fadefilter.blnFilterOn){
					fadefilter.blnFilterOn = true;
				}
			}
		}
		public void render(float tpf) {
			//Update all game timers
			for(int i = 0; i < 64; i++) {
				gtime[i] += tpf;
			}

			//After every interval estateChangeTime long, change the estate
			if(gtime[ESTATE] > estateChangeTime) {
				estate = chance(3,3);
				gtime[ESTATE] = 0;
				resetPtime();
			}
			
			//spawn bullets
			switch(estate) {
			case RANDOM:
				if(Math.random() < 0.08f) {
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
			case WALL1GAP1:
				//0: angle 1
				//1: angle 2
				//2: spawn interval 1
				//3: spawn interval 2
				ptime[0] += tpf * 10f;
				ptime[2] += tpf;
				float angles = 16f;
				int gap = chance(0,15);
				if(ptime[2] > 2f) {
					for(int i = 0; i < angles; i++) {
						if( i != gap) {
							ModelInstance cubeInstance = new ModelInstance(mdlPumpkin);
							GameObject ufo = new GameObject(true,10);
							//cubeInstance.transform.setTranslation(new Vector3((float)Math.cos(i/angles * 6.283f) * 10, (float)Math.sin(i/angles * 6.283f) * 10, 1200));
							//cubeInstance.transform.rotate(Vector3.Z,(i/angles*6.283f) * 360 / 6.283f + 90);
							//instances.add(cubeInstance);
							ufo.vel.set(0,0,-2);
							ufo.polpos.set((i / angles * 62.83f), 0, 1200);
							ufo.mdi = cubeInstance;
							pumpkins.add(ufo);
						}
					}
					ptime[2] = 0;
				}
				
				break;
			case BOSS:
				if(instances.size() == 0) {
					ModelInstance bossInstance = new ModelInstance(mdlUfoR);
					bossInstance.transform.scale(8, 8, 8);
					bossInstance.transform.setTranslation(new Vector3(0,-10,1200));
					instances.add(bossInstance);
				}
				break;
			case OFF:
				break;
			default:
				break;
			}
			//Update and draw UFOs
			Iterator<GameObject> pumpkinIter = pumpkins.iterator();
			Iterator<GameObject> shotIter;
			GameObject curPumpkin;
			GameObject curShot;
			while(pumpkinIter.hasNext()) {
				curPumpkin = pumpkinIter.next();
				curPumpkin.update(tpf);
				modelBatch.render(curPumpkin.mdi, litGround);
				
				//Test each ufo for collision with player
				if(curPumpkin.boxCollides(player, 2)) {
					pumpkinIter.remove();
					continue;
				}
				
				shotIter = shots.iterator();
				while(shotIter.hasNext()) {
					curShot = shotIter.next();
					
					if(curPumpkin.boxCollides(curShot, 4)) {
						shotIter.remove();
						continue;
					}
				}
			}
			
			//Update and draw shots
			shotIter = shots.iterator();
			while(shotIter.hasNext()) {
				curShot = shotIter.next();
				curShot.update(tpf);
				modelBatch.render(curShot.mdi);
				
				//If the bullet's position z is over 500 kill it
				if(curShot.pos.z > 500) {
					shotIter.remove();
					continue;
				}
			}
			
			handleInput(tpf);
			
			//Draw gui
			if(tmrUiFade != 1) tmrUiFade += tpf;
			if(tmrUiFade + tpf < 0.5f) {
				sprControlLeft.setColor(1,1,1,tmrUiFade*2);
				sprControlRight.setColor(1,1,1,tmrUiFade*2);
				bmfGameUi.setColor(1,1,1,tmrUiFade*2);
			} else if(tmrUiFade > 1) {
				tmrUiFade = 1;
				sprControlLeft.setColor(1,1,1,1);
				sprControlRight.setColor(1,1,1,1);
				bmfGameUi.setColor(1,1,1,1);
			}
			spriteBatch.begin();
			sprControlLeft.draw(spriteBatch);
			sprControlRight.draw(spriteBatch);
			
			bmfGameUi.draw(spriteBatch, "Power", 500, 50);
			
			spriteBatch.end();
			
			
			//Check if faded out after attempting reinit
			if(fadefilter.fltFilterAlpha == 1) {
				reinit();
			}
		}
	}
	class MENU {
		float fltTitleFadeRate = 0.1f;
		float fltTitleAlpha = 0f;
		
		boolean blnShowTitle;
		boolean blnTitleCompleted;
		
		Vector3 vecCamMenuPos = new Vector3(8,-14,5);
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
			
			vecCamMenuFocus = new Vector3(0,-11,1);
			vecCamGameFocus = new Vector3(0,0,0);
			
			sprTitle = new Sprite(var.assets.get("2d/title.png", Texture.class));
			sprTitle.setPosition(var.w/2f - (sprTitle.getWidth()/2f), var.h / 3f - (sprTitle.getHeight()/2f));
		}
		public void render(float tpf) {
			sprTitle.setColor(1,1,1,fltTitleAlpha);
			if(blnShowTitle) {
				fadefilter.blnFilterOn = false;
				if(!animController.current.animation.id.equals("pose")){
					animController.animate("pose", -1, 1f, null, 0.8f);
				}
				//if(animController.current.time > 1)	animController.current.time = 1;
				
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
				if(animController.current.animation.id != "up"){
					animController.animate("up", -1, 1f, null, 1f);
				}
				
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
