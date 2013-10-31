package com.westernarc.ufohunt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
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
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.westernarc.ufohunt.Behaviours.*;
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
		
		static Color clrSky = new Color(105f/255f, 95f/255f, 134f/255f, 1);
		static float gameSize = 13;
		
		static AssetManager assets;
		
		static enum STATES {LOAD, MENU, GAME, DEAD, SCORE};
		static STATES state;
		
		static float tpf;
		static boolean flgDeathFreeze; //Used to freeze game updates
		static float tmrDeathFreeze;
		static boolean flgGameReset; //Game recycle
		static float tmrGameReset;
		final static float gameResetTime = 1f;
		final static float deathFreezeTime = 1.5f; //Duration that time stands still after player death
		
		static boolean flgDead;
		
		//Game variables that affect gameplay.  (Stats)
		static int level; //Affects enemy HP and type, as well as
		static int phase; //Each change in pattern adds to phase
		static int speed; //Speed of player acceleration
		static float distance; //This is the distance the player has travelled forwards
		static int kills; //Number of pumpkins killed; count on shooting them down
		static int score;
		static int hiscore;
		
		final static float baseGroundSpeed = -200f; //Base speed of ground and pumpkin scrolling
		
		static Preferences prefs;
		static Music musBgm;
		
		static ColorAttribute cabRed;
		static ColorAttribute cabWhite;
	}
	
	static class fadefilter {
		static Sprite sprFilter;
		static float fltFilterAlpha = 1;
		static float fltFilterBottom = 0;
		static boolean blnFilterOn;
		//True means filter shows; false is filter is transparent
		
		static float fltFilterRate = 0.05f;
	}
	
	AnimationController animController;
	
	ModelBatch modelBatch;
	
	PerspectiveCamera cam;
	ModelInstance mdiPlayer;

	Lights litGround;
	
	ModelInstance mdiMoon;
	ModelInstance mdiSky;
	Model mdlGround;
	GameObject[] objGround;
	final int numGround = 4;
	final int numGroundLength = 1200;
	Model cube;
	Model mdlUfoR;
	Model mdlShot;
	Model mdlPumpkin;
	Model mdlBoss;
	Model mdlBullet;
	ArrayList<ModelInstance> instances;
	ArrayList<GameObject> arrPumpkins;
	HashMap<String, GameObject> hshMappedPumpkins;
	ArrayList<GameObject> shots;
	
	SimpleBehaviour groundSpeed; //Speed things fly by at
	
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
		var.assets.load("3d/moon.g3dj", Model.class);
		var.assets.load("3d/cube.g3dj", Model.class);
		var.assets.load("3d/ufoR.g3dj", Model.class);
		var.assets.load("3d/pumpkinO.g3dj", Model.class);
		var.assets.load("3d/boss.g3dj", Model.class);
		var.assets.load("3d/shotR.g3dj", Model.class);
		var.assets.load("3d/bulletG.g3dj", Model.class);
		var.assets.load("2d/tex.png", Texture.class);
		var.assets.load("2d/cw.png", Texture.class);
		var.assets.load("2d/ccw.png", Texture.class);
		var.assets.load("2d/title.png", Texture.class);
		var.assets.load("2d/filter.png", Texture.class);
		var.assets.load("2d/Bimbo32.fnt", BitmapFont.class);
		
		var.assets.load("audio/Circumspection.mp3", Music.class);
		fadefilter.fltFilterAlpha = 1;
		
		var.state = var.STATES.LOAD;
		
		var.flgDeathFreeze = false;
		var.flgDead = false;
		
		player = new GameObject(true, 10);
		
		stateMenu = new MENU();
		stateGame = new GAME();
		
		litGround = new Lights();
		litGround.ambientLight.set(Color.WHITE);
		litGround.fog = var.clrSky;
		
		groundSpeed = new SimpleBehaviour(0,0,var.baseGroundSpeed);
		
		var.cabRed = new ColorAttribute(ColorAttribute.Diffuse,1,0.5f,0.5f,1);
		var.cabWhite = new ColorAttribute(ColorAttribute.Diffuse, 1, 1, 1, 1);
		recreate();
	}

	public void recreate() {
	
	}
	
	private void doneLoading() {
		Model mdlPlayer = var.assets.get("3d/player.g3dj", Model.class);
		mdiPlayer = new ModelInstance(mdlPlayer);
		//mdiPlayer.materials.first().set(new ColorAttribute(ColorAttribute.Diffuse, 1,1,1,1));
		player.mdi = mdiPlayer;
		
		
		animController = new AnimationController(mdiPlayer);
		animController.animate("up", -1, 1f, null, 0.2f);
		animController.setAnimation("up", -1, 1f, null);

		Model mdlSky = var.assets.get("3d/sky.g3dj", Model.class);
		mdiSky = new ModelInstance(mdlSky);
		//Translate sky
		mdiSky.materials.first().set(new ColorAttribute(ColorAttribute.Diffuse, 1,1,1,1));
		
		Model mdlMoon = var.assets.get("3d/moon.g3dj", Model.class);
		mdiMoon = new ModelInstance(mdlMoon);
		//Translate sky
		mdiMoon.transform.translate(-400,140,-340);
		mdiMoon.materials.first().set(new ColorAttribute(ColorAttribute.Diffuse, 1,1,1,1));
		
		Model mdlGround = var.assets.get("3d/ground.g3dj", Model.class);
		objGround = new GameObject[numGround];
		for(int i = 0; i < numGround; i++) {
			objGround[i] = new GameObject();
			objGround[i].mdi = new ModelInstance(mdlGround);
			objGround[i].mdi.materials.first().set(new ColorAttribute(ColorAttribute.Diffuse, 1,1,1,1));
			objGround[i].translate(0,0,i * numGroundLength);
			objGround[i].behaviours.add(groundSpeed);
		}
		
		cube = var.assets.get("3d/cube.g3dj", Model.class);
		mdlUfoR = var.assets.get("3d/ufoR.g3dj", Model.class);
		mdlShot = var.assets.get("3d/shotR.g3dj", Model.class);
		mdlPumpkin = var.assets.get("3d/pumpkinO.g3dj", Model.class);
		mdlBoss = var.assets.get("3d/boss.g3dj", Model.class);
		mdlBullet = var.assets.get("3d/bulletG.g3dj", Model.class);
		stateMenu.init();
		stateGame.init();
		
		fadefilter.sprFilter = new Sprite(var.assets.get("2d/filter.png", Texture.class));
		fadefilter.sprFilter.scale(var.h);
		
		var.musBgm = var.assets.get("audio/Circumspection.mp3", Music.class);
		var.musBgm.play();
	}
	
	@Override
	public void dispose() {	}

	//Update loop
	@Override
	public void render() {
		if(!var.flgDeathFreeze) { 
			var.tpf = Gdx.graphics.getDeltaTime();
		} else {
			var.tpf = 0;
			var.tmrDeathFreeze += Gdx.graphics.getDeltaTime();
			if(var.tmrDeathFreeze > var.deathFreezeTime){
				var.flgDeathFreeze = false;
				var.tmrDeathFreeze = 0;
				player.behaviours.add(groundSpeed);
			}
		}
		if(cam != null)
			cam.update();
		
		if(var.state == var.STATES.LOAD) {
			if(var.assets.update()) {
				doneLoading();
				var.state = var.STATES.MENU;
			}
		} else {
			Gdx.gl.glViewport(0,0,var.w,var.h);
			Gdx.gl.glClearColor(var.clrSky.r, var.clrSky.g, var.clrSky.b, var.clrSky.a);
			Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			animController.update(var.tpf);
			modelBatch.begin(cam);
			modelBatch.render(mdiMoon);
			modelBatch.render(mdiSky);
			mdiSky.transform.rotate(Vector3.Y, var.tpf);
			for(GameObject grndInstance : objGround) {
				modelBatch.render(grndInstance.mdi, litGround);
				if(!var.flgDeathFreeze) grndInstance.update(var.tpf);
				//reset ground position if it goes too far
				if(grndInstance.pos.z < -numGroundLength * 1.5){
					grndInstance.translate(0,0,numGroundLength * numGround);
				}
			}
			if(!var.flgDeathFreeze) player.update(var.tpf);
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
		//Alpha is the minimum that the filter will fade.
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
		final int WEDGE = 5;
		final int TUNNEL = 6;
		
		Sprite sprControlLeft; //Sprite for turning clockwise
		Sprite sprControlRight; //Sprite for turning counter clockwise
		BitmapFont bmfGameUi; //Font for displaying in game text
		
		boolean flgPostGame;
		
		public void resetPvars() {
			ptime = new float[64];
			pflag = new boolean[64];
			for(int i = 0; i < ptime.length; i++) ptime[i] = 0;
			for(int i = 0; i < pflag.length; i++) pflag[i] = false;
		}
		
		int estate;
		float[] ptime;//Pattern times
		boolean[] pflag;
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
			cam.far = 2000f;
			cam.update();
			
			
			var.prefs = Gdx.app.getPreferences("gameprefs");
            if(var.prefs.getInteger("hiscore", 0) == 0) {
                var.prefs.putInteger("hiscore", 0);
	        }
	        var.prefs.flush();
	        var.hiscore = var.prefs.getInteger("hiscore",0);
	        
			bmfGameUi = var.assets.get("2d/Bimbo32.fnt");
			//bmfGameUi.setScale(2);
			sprControlLeft = new Sprite(var.assets.get("2d/cw.png", Texture.class));
			sprControlRight = new Sprite(var.assets.get("2d/ccw.png", Texture.class));
			sprControlRight.setPosition(var.w - sprControlRight.getWidth(), 0);
			
			reinit();
		}
		public void reinit() {
			instances = new ArrayList<ModelInstance>();
			arrPumpkins = new ArrayList<GameObject>();
			hshMappedPumpkins = new HashMap<String, GameObject>();
			shots = new ArrayList<GameObject>();
			
			//Emitter state.  Defaults to RANDOM.
			estate = BOSS;
			//Initialize array of pattern timer temp variables
			gtime = new float[64];
			resetPvars();
			gtime[GAME] = 0;
			
			fadefilter.blnFilterOn = false;
			
			tmrUiFade = 0;
			sprControlLeft.setColor(1, 1, 1, 0);
			sprControlRight.setColor(1, 1, 1, 0);
			bmfGameUi.setColor(1, 1, 1, 0);
			
			player.reset();
			player.behaviours.removeValue(groundSpeed, true);
			player.active = true;
			player.mdi.materials.first().set(var.cabWhite);
			var.flgDead = false;
			/*
			var.flgDead = false;
			var.flgDeathFreeze = false;*/
			var.phase = 1;
			var.distance = 0;
			var.level = 1;
			var.kills = 0;
			var.score = 0;
            if(var.prefs.getInteger("hiscore", 0) < var.hiscore) {
                var.prefs.putInteger("hiscore", var.hiscore);
	        }
	        var.prefs.flush();
	        var.hiscore = var.prefs.getInteger("hiscore",0);
		}
		public void playerFire() {
			if(gtime[SHOT] > shotCooldown) {
				gtime[SHOT] = 0;
				GameObject obj = new GameObject(true, 10);
				obj.mdi = new ModelInstance(mdlShot);
				
				obj.polpos.set(player.polpos.x, player.polpos.y, player.pos.z);
				obj.behaviours.add(new SimpleBehaviour(0,0,240));
				obj.dmg = 1;
				shots.add(obj);
			}
		}
		public void handleInput(float tpf) {
			//Update shot timer 
			gtime[SHOT] += tpf;
			
			if(isKeyPressed(Keys.LEFT)) {
				//player.pos.x += 1f;
				//mdiPlayer.transform.rotate(0,1,0,-4);
				player.acc.set(-0.6f,0,0);
				if(animController.current.animation.id != "right"){
					//animController.animate("right", -1, 1f, null, 0.5f);
				}
			} else if(isKeyPressed(Keys.RIGHT)) {
				//mdiPlayer.transform.rotate(0,1,0,4);
				player.acc.set(0.6f,0,0);
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
		private void calculateLevel() {
			//Formula for level: Based on score (from kills), distance, and phases cleared
			var.level = Math.round(var.score / 150f);
			var.score = var.kills*10 + Math.round(var.distance * 4);
		}
		public void switchState(int nextState) {
			resetPvars();
			estate = nextState;
			if(nextState % 5 == 0) estate++;
			//Every time state changes, add to phase
			var.phase++;
			//Level up only
		}
		//Choose next state based on level
		public void nextState() {
			if(var.phase % 5 == 0) 
				switchState(BOSS);
			else
				switchState(chance(1,Math.min(var.level, 6)));
		}
		public void spawn(float tpf) {
			//spawn bullets
			switch(estate) {
			case RANDOM:
				ptime[0] += tpf;
				ptime[1] += tpf;
				if(ptime[0] > 0.1f) {
					addPumpkin(mdlPumpkin,(float)Math.random() * 62,4);
					ptime[0] = 0;
				}
				if(ptime[1] > 10) {
					if(Math.random() > 0.5) nextState();
					ptime[1] -= 2;
				}
				break;
			case SPIRAL2:
				//0: angle 1
				//1: angle 2
				//2: spawn interval 1
				//3: spawn interval 2
				ptime[0] += tpf * ptime[4];
				ptime[2] += tpf;
				
				//Orientation:
				//var 3 is orientation timer
				//flag 3 is orientation direction
				//chance to switch orientations every 5 seconds
				ptime[3] += tpf; 
				if(ptime[3] > 3) {
					pflag[3] = Math.random() >  0.5 ? true : false;
					ptime[3] = 0;
					ptime[1]++;
				}
				
				//var 1: number of orientation switches.  Past 3 switches, 50% each switch to go to next phase
				if(ptime[1] > 3) {
					if(Math.random() > 0.5) {
						nextState();
					}
					ptime[1]--;
				}
				
				//Spiral rotation speed, var 4
				if(pflag[3]) {
					if(ptime[4] < 16) {
						ptime[4] += tpf * 20;
					} else {
						ptime[4] = 16;
					}
				} else {
					if(ptime[4] > -16) {
						ptime[4] -= tpf * 20;
					} else {
						ptime[4] = -16;
					}
				}
				
				if(ptime[2] > 0.05f) {					
					addPumpkin(mdlPumpkin, ptime[0], 3);				
					addPumpkin(mdlPumpkin, ptime[0] + 5, 3);
					
					addPumpkin(mdlPumpkin, ptime[0] + 32, 3);				
					addPumpkin(mdlPumpkin, ptime[0] + 35, 3);
					ptime[2] = 0;
				}
				
				break;
			case WALL1GAP1:
				//0: angle 1
				//1: angle 2
				//2: spawn interval 1
				//3: spawn interval 2
				ptime[0] += tpf * 10f;
				ptime[1] += tpf;
				ptime[2] += tpf;
				float angles = 16f;
				int gap = chance(0,15);
				if(ptime[2] > 2f) {
					for(int i = 0; i < angles; i++) {
						if(i != gap) {
							addPumpkin(mdlPumpkin, (i / angles * 62.83f), 3);
						}
					}
					ptime[2] = 0;
				}
				if(ptime[1] > 10) {
					if(Math.random() > 0.5) nextState();
					ptime[1] -= 2;
				}
				break;
			case WEDGE:
				//0: angle 1
				//1: angle 2
				//2: spawn interval 1
				//3: spawn interval 2
				ptime[0] += tpf;
				ptime[3] += tpf;
				ptime[4] += tpf;
				ptime[1] += tpf * 9;
				ptime[2] -= tpf * 9;
				
				if(ptime[1] > GameObject.TWOPI * 10) ptime[1] = 0;
				if(ptime[2] < 0) ptime[2] = GameObject.TWOPI * 10;
				if(ptime[0] > 3) {
					ptime[0] = 0;
					float opening = chance(0,31) * GameObject.TWOPI * 10 / 32f;
					ptime[1] = opening;
					ptime[2] = opening;
				}
				if(ptime[4] > 0.05f) {
					if(pflag[0]) {
						addPumpkin(mdlPumpkin, ptime[1], 5);
						pflag[0] = !pflag[0];
					} else {
						addPumpkin(mdlPumpkin, ptime[2], 5);
						pflag[0] = !pflag[0];
					}
					ptime[4] = 0;
				}
				if(ptime[3] > 10) {
					//After 15 seconds, 50% chance to end phase
					if(Math.random() > 0.5) nextState();
					ptime[1] -= 2;
				}
				break;
			case TUNNEL:
				ptime[0] += tpf;
				ptime[1] += tpf;
				
				if(!pflag[0]) {
					ptime[2] = chance(0,15);
					ptime[3] = 0;
					ptime[4] = 0;
					pflag[0] = true;
				}
				
				if(ptime[0] > 2) {
					if(chance(0,1) == 1) {
						ptime[2]++;
						if(ptime[2] >= 16) ptime[2] = 0;
					} else {
						ptime[2]--;
						if(ptime[2] <= 0) ptime[2] = 15;
					}
					
					ptime[4] = chance(1,2);
					ptime[0] = 0;
				}
				
				if(ptime[1] > 0.15f) {
					if(ptime[3] > ptime[4]) ptime[3]--;
					else if(ptime[3] < ptime[4]) ptime[3]++;
					
					for(int i = 0; i < 16; i++) {
						switch((int)ptime[3]) {
						case 0:
							if(i != ptime[2])addPumpkin(mdlPumpkin, i * GameObject.TWOPI * 10 / 16f, 2);
							break;
						case 1:
							if(i != ptime[2] && 
							((i + 1) > 16 ? i + 1 - 16 : i + 1) != ptime[2] && 
							((i - 1) < 0 ? i - 1 + 16 : i - 1) != ptime[2])
								addPumpkin(mdlPumpkin, i * GameObject.TWOPI * 10 / 16f, 2);
							break;
						case 2:
							if(i != ptime[2] && 
							((i + 1) > 16 ? i + 1 - 16 : i + 1) != ptime[2] && 
							((i - 1) < 0 ? i - 1 + 16 : i - 1) != ptime[2] &&
							((i + 2) > 16 ? i + 2 - 16 : i + 2) != ptime[2] && 
							((i - 2) < 0 ? i - 2 + 16 : i - 2) != ptime[2])
								addPumpkin(mdlPumpkin, i * GameObject.TWOPI * 10 / 16f, 2);
							default:
							if(i != ptime[2])addPumpkin(mdlPumpkin, i * GameObject.TWOPI * 10 / 16f, 2);
						}
						
					}
					ptime[1] = 0;
				}
				
				break;
			case BOSS:
				if(!pflag[4] ) {
					GameObject boss = addPumpkin(mdlBoss, 0, 10);
					boss.behaviours.clear();
					//Make behaviour that slows down and stops at 200f Z
					boss.addBehaviour(new Behaviour() {
						{
							modPos.set(0,0,-300);
						}
						@Override
						public void update(float tpf) {
							if(!var.flgDeathFreeze) {
								if(parent.pos.z > 200) {
									modPos.set(0,0,-((parent.pos.z - 200f) / 200f)*300);
								} else {
									modPos.set(0,0,0);
								}
							}
						}
					});
					hshMappedPumpkins.put("pmkBoss", boss);
					pflag[4] = true;
				} else {
					GameObject boss = hshMappedPumpkins.get("pmkBoss");
					ptime[0] += tpf * 13f;
					ptime[1] += tpf * 10f;
					ptime[2] += tpf;
					ptime[4] += tpf;
					if(ptime[2] > 3f) {		
						Vector3 targ1 = new Vector3(player.polpos).add(10,0,0);
						Vector3 targ2 = new Vector3(player.polpos).add(-10,0,0);
						
						Vector3 bossVec = boss.polpos;
						if(targ2.x <= 0) targ2.x += GameObject.TWOPI * player.radius;
						
						shootLine(mdlBullet, 100, bossVec, targ1, 0.6f, 12);
						
						shootLine(mdlBullet, 100, bossVec, targ2, 0.6f, 12);
						
						ptime[2] = 0;
					}
					if(ptime[4] > 1) {
						Vector3 bossVec = boss.polpos;
						
						GameObject p1 = addPumpkin(mdlPumpkin, 0, 2);
						p1.polpos.set(bossVec);
						p1.behaviours.clear();
						Behaviour b = new Behaviour() {
							@Override
							public void update(float tpf) {
								modPos.set((float)Math.cos(parent.polpos.z * 3.1415f/200f) * 5, 0, -50);
							}
						};
						p1.addBehaviour(b);
						setTracking(p1);
						
						GameObject p2 = addPumpkin(mdlPumpkin, 0, 2);
						p2.polpos.set(bossVec);
						p2.behaviours.clear();
						Behaviour b2 = new Behaviour() {
							@Override
							public void update(float tpf) {
								modPos.set(-(float)Math.cos(parent.polpos.z * 3.1415f/200f) * 5, 0, -50);
							}
						};
						p2.addBehaviour(b2);
						setTracking(p2);
						ptime[4] = 0;
					}
					
					//Handle boss death
					if(!pflag[5]) {
						if(boss.hp < boss.maxHp / 2f) {
							boss.behaviours.clear();
							boss.behaviours.add(new AccelBehaviour(0,0,-100f));
							boss.invuln = true;
							pflag[5] = true;
						}
					}
					
					//If boss has spawned, boss has died, and boss has been deleted:
					if(pflag[5]) {
						if(!arrPumpkins.contains(hshMappedPumpkins.get("pmkBoss"))) {
							hshMappedPumpkins.remove("pmkBoss");
							nextState();
						}
					}
				}
				/*
				ptime[0] += tpf * 13f;
				ptime[1] += tpf * 10f;
				ptime[2] += tpf;
				
				if(ptime[2] > 0.5f) {					
					addPumpkin(mdlPumpkin, ptime[0], 3, groundSpeed);				
					addPumpkin(mdlPumpkin, ptime[0] + 5, 3, groundSpeed);
					
					addPumpkin(mdlPumpkin, -ptime[0] + 32, 3, groundSpeed);				
					addPumpkin(mdlPumpkin, -ptime[0] + 35, 3, groundSpeed);
					ptime[2] = 0;
				}*/
				break;
			case OFF:
				break;
			default:
				break;
			}
		}
		
		//Add a tracking behaviour to the pumpkin.
		public GameObject setTracking(GameObject p2) {
			SimpleBehaviour sb2;
			
			//Calculate distance 
			if(p2.checkDist(player.polpos) == GameObject.CW){
				sb2 = new SimpleBehaviour(p2.dist(player),0,-1);
			} else {
				sb2 = new SimpleBehaviour(-p2.dist(player),0,-1);
			}
			p2.addBehaviour(sb2);
			
			return p2;
		}
		public GameObject addPumpkin(Model mdlP) {
			GameObject pumpkin = new GameObject(true,10);
			pumpkin.mdi = new ModelInstance(mdlP);
			return pumpkin;
		}
		public GameObject addPumpkin(Model mdlP, float x, int hp) {
			GameObject pumpkin = new GameObject(true,10);
			pumpkin.behaviours.add(groundSpeed);
			pumpkin.polpos.set(x, 0, 1200);
			pumpkin.mdi = new ModelInstance(mdlP);
			pumpkin.setMaxHp(hp);
			arrPumpkins.add(pumpkin);
			return pumpkin;
		}
		public GameObject addPumpkin(Model mdlP, float x, int hp, Behaviour be) {
			GameObject pumpkin = addPumpkin(mdlP, x, hp);
			pumpkin.behaviours.add(be);
			return pumpkin;
		}
		public GameObject shootPumpkin(Model mdlP, int hp, Vector3 orig, Vector3 target, float speed) {
			GameObject pumpkin = addPumpkin(mdlP, orig.x, hp);
			pumpkin.polpos.z = orig.z;
			pumpkin.behaviours.clear();
			SimpleBehaviour sb;
			
			//Calculate distance 
			if(pumpkin.checkDist(target) == GameObject.CW){
				sb = new SimpleBehaviour(pumpkin.dist(target)*speed,0,-orig.z*speed);
			} else {
				sb = new SimpleBehaviour(-pumpkin.dist(target)*speed,0,- orig.z*speed);
			}
			pumpkin.addBehaviour(sb);
			return pumpkin;
		}
		public void shootLine(Model mdlP, int hp, Vector3 orig, Vector3 target, float speed, int num) {
			for(int i = 0; i < num; i++) {
				shootPumpkin(mdlP, hp, orig, target, speed - (i * speed / 20));
			}
		}
		public void render(float tpf) {
			//Update all game timers
			for(int i = 0; i < 64; i++) {
				gtime[i] += tpf;
			}
			if(!var.flgDeathFreeze && var.flgDead && player.pos.z < -230) {
				if(!fadefilter.blnFilterOn){
					fadefilter.blnFilterOn = true;
				}
			}
			//After every interval estateChangeTime long, change the estate
			if(gtime[ESTATE] > estateChangeTime) {
				estate = chance(3,3);
				gtime[ESTATE] = 0;
				resetPvars();
			}
			
			//Only spawn new pumpkins if the player isn't dead.
			if(!var.flgDead) {
				spawn(tpf);
				var.distance += tpf;  //Increase distance while player is alive
			}
			
			//Update and draw UFOs
			Iterator<GameObject> pumpkinIter = arrPumpkins.iterator();
			Iterator<GameObject> shotIter;
			GameObject curPumpkin;
			GameObject curShot;
			System.out.println(tpf);
			while(pumpkinIter.hasNext()) {
				curPumpkin = pumpkinIter.next();
				
				//Remove the pumpkin if it flies past the player or falls down
				if(curPumpkin.polpos.z < -50 || curPumpkin.polpos.y < -30) {
					pumpkinIter.remove();
					continue;
				}
				
				if(!var.flgDeathFreeze) curPumpkin.update(tpf);
				modelBatch.render(curPumpkin.mdi, litGround);

				//Test each ufo for collision with player
				
				if(!var.flgDead && curPumpkin.boxCollides(player, 2) && curPumpkin.active) {
					//pumpkinIter.remove();
					curPumpkin.mdi.materials.first().set(var.cabRed);
					player.mdi.materials.first().set(var.cabRed);
					curPumpkin.active = false;
					//Player hit
					if(!var.flgDeathFreeze) {
						var.flgDeathFreeze = true;
						var.flgDead = true;
						player.behaviours.clear();
						//player.vel.scl(0);
						//player.acc.scl(0);
					}
					continue;
				}
				
				shotIter = shots.iterator();
				while(shotIter.hasNext()) {
					curShot = shotIter.next();
					//boolean pumpRemoved = false;
					if(curPumpkin.boxCollides(curShot, 4)) {
						//Multiply curShot damage by player power
						curPumpkin.takeDamage(curShot.dmg);
						
						shotIter.remove();
						//Remove the shot, deal damage to the pumpkin
						
						if(curPumpkin.hp <= 0 && curPumpkin.polar) {
							curPumpkin.polar = false;
							//To dying pumpkins, add a behaviour that makes it pop up and fall down
							// Juuustttt like in easter run, because I'm unoriginal and lazy
							curPumpkin.vel.y = 0.7f;
							curPumpkin.addBehaviour(new AccelBehaviour(0,-300,0));
							curPumpkin.mdi.materials.first().set(new ColorAttribute(ColorAttribute.Diffuse,0.5f,0.8f,0.5f,1));
							//pumpkinIter.remove();
							//pumpRemoved = true;
							
							//Increment pumpkin killed counter
							var.kills++;
							continue;
						}
						continue;
					}
					//if(pumpRemoved) continue;
				}
			}
			
			//Update and draw shots
			shotIter = shots.iterator();
			while(shotIter.hasNext()) {
				curShot = shotIter.next();
				if(!var.flgDeathFreeze) curShot.update(tpf);
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
			
			calculateLevel();
			bmfGameUi.draw(spriteBatch, var.score + "p", 10, var.h - 10);
			
			if(var.score > var.hiscore) {
				var.hiscore = var.score;
			}
			bmfGameUi.setScale(0.5f);
			bmfGameUi.draw(spriteBatch, "hiscore " + var.hiscore + "p", 10, var.h - 40);
			bmfGameUi.setScale(1);
			String levelString = "Lv. " + var.level;
			bmfGameUi.draw(spriteBatch, levelString, var.w - bmfGameUi.getBounds(levelString).width - 10, var.h - 10);
			
			spriteBatch.end();
			
			//Check if faded out after attempting reinit
			if(fadefilter.fltFilterAlpha == 1) {
				reinit();
				var.flgDead = false;
				var.flgGameReset = false;
			}
		}
	}
	class MENU {
		float fltTitleFadeRate = 0.1f;
		float fltTitleAlpha = 0f;
		
		boolean blnShowTitle;
		boolean blnTitleCompleted;
		
		Vector3 vecCamMenuPos = new Vector3(6,-15,10);
		Vector3 vecCamMenuUp = new Vector3(-0.1f,1,0.3f);
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
			
			vecCamMenuFocus = new Vector3(0,-12,3f);
			vecCamGameFocus = new Vector3(0,0,0);
			
			sprTitle = new Sprite(var.assets.get("2d/title.png", Texture.class));
			//sprTitle.setPosition(var.w/2f - (sprTitle.getWidth()/2f), var.h / 3f - (sprTitle.getHeight()/2f));
			float widthScale = var.w / sprTitle.getWidth();
			sprTitle.setScale(widthScale);
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
				cam.up.lerp(vecCamMenuUp, fltCamLerpValue);
				cam.lookAt(vecCamMenuFocus);
			} else {
				if(animController.current.animation.id != "up"){
					animController.animate("up", -1, 1f, null, 1f);
				}
				
				if(fltTitleAlpha - fltTitleFadeRate > 0) {fltTitleAlpha -= fltTitleFadeRate; } else { fltTitleAlpha = 0; }
				
				if(fltCamLerpValue + fltCamLerpGameRate < 1) fltCamLerpValue += fltCamLerpGameRate; else fltCamLerpValue = 1;
				cam.position.lerp(vecCamGamePos, fltCamLerpValue);
				cam.up.lerp(Vector3.Y, fltCamLerpValue);
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
	//Returns true if inside the range specified by arguments
	public boolean isBetween(int arg, int low, int hi) {
		if(arg >= low && arg <= hi) return true;
		else return false;
	}
}
