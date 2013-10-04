package com.westernarc.ufohunt.Objects;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.westernarc.ufohunt.Behaviours.Behaviour;

public class GameObject {
	public boolean polar = false;
	public ModelInstance mdi;
	public float radius;
	public Vector3 pos;
	public Vector3 polpos;
	public Vector3 vel;
	public Vector3 acc;
	public int hp;//Hp of object before dying
	public int dmg;//Damage object deals
	public void takeDamage(int dmg) {
		hp -= dmg;
		if(hp < 0) hp = 0;
	}
	
	//List of behaviours that affect this game object
	public Array<Behaviour> behaviours;
	
	public static final float PI = 3.1415f;
	public static final float TWOPI = 6.283f;
	
	public void acc(float x, float y, float z) {
		acc.add(x,y,z);
	}
	public void dec(float x, float y, float z) {
		//Move the acceleration until velocity is 0.
		//x, y, z MUST BE POSITIVE
		if(vel.x > 0) {
			
		}
	}
	public Vector3 rot;
	
	float angle = 0;
	float tiltAngle = 0;//Angle of rotational tilt
	float tiltRate = 0.02f;
	public void reset(){
		polpos.set(0,0,0);
		vel.set(0,0,0);
		acc.set(0,0,0);
		tiltAngle = 0;
		angle = 0;
	}
	public GameObject() {
		pos = new Vector3();
		polpos = new Vector3();
		vel = new Vector3();
		acc = new Vector3();
		radius = 0;
		angle = 0;
		tiltAngle = 0;
		behaviours = new Array<Behaviour>();
		//Have at least 1 hp for every object
		hp = 1;
	}
	//Create a game object thats polar
	public GameObject(boolean polar, float radius) {
		this();
		this.polar = polar;
		this.radius = radius;
	}
	public void update(float tpf) {
		vel.add(acc.scl(tpf));
		if(behaviours.size > 0) {
			for(Behaviour b : behaviours) {
				acc.add(b.modAcc.cpy().scl(tpf));
				vel.add(b.modVel.cpy().scl(tpf));
				pos.add(b.modPos.cpy().scl(tpf));
				polpos.add(b.modPos.cpy().scl(tpf));
				b.update(tpf);
			}
		}
		//If this is a polar object, convert its position on each update
		if(!polar) {
			pos.add(vel);
		} else {
			polpos.add(vel);
			//Translate the planar coordinates to tube coordinates
			pos.z = polpos.z;
			angle = polpos.x / (radius);
			
			//Loop x coordinates
			while(polpos.x < 0) polpos.x += TWOPI * radius;
			while(polpos.x > TWOPI * radius) polpos.x -= TWOPI * radius;
			
			pos.x = (float)Math.sin(angle) * radius;
			pos.y = -(float)Math.cos(angle) * radius;
		}	
		if(tiltAngle - tiltRate > vel.x * 2) {
			tiltAngle -= tiltRate;
		} else if(tiltAngle + tiltRate < vel.x * 2) {
			tiltAngle += tiltRate;
		}
		mdi.transform.setToRotation(Vector3.Z, ((angle - tiltAngle)* 360f / TWOPI));
		mdi.transform.setTranslation(pos);
	}
	
	public static boolean CCW = false;
	public static boolean CW = true;
	//Get the distance across the tube's surface
	//true is cw, false is ccw
	public boolean dist(float x1, float x2) {
		if(Math.abs(x1 - x2) < (radius * Math.PI * 2 - Math.max(x1, x2)) + Math.min(x1, x2) ) {
			return CW;
		} else {
			return CCW;
		}
	}
	
	//Simple box collision method
	public boolean boxCollides(GameObject obj, float dist) {
		if(Math.abs(pos.x - obj.pos.x) < dist &&
			Math.abs(pos.y - obj.pos.y) < dist &&
			Math.abs(pos.z - obj.pos.z) < dist) {
			return true;
		} else {
			return false;
		}
	}
	public void translate(float x, float y, float z) {
		pos.add(x,y,z);
	}
	public void translate(Vector3 v) {
		pos.add(v);
	}
}
