package com.westernarc.ufohunt.Objects;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.westernarc.ufohunt.Behaviours.Behaviour;

public class GameObject {
	public boolean polar = false;
	public float radius = 0;
	public ModelInstance mdi;
	public Vector3 pos;
	public Vector3 polpos;
	public Vector3 vel;
	public Vector3 acc;
	
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
	
	public GameObject() {
		pos = new Vector3();
		polpos = new Vector3();
		vel = new Vector3();
		acc = new Vector3();
		
		radius = 0;
		angle = 0;
		
		behaviours = new Array<Behaviour>();
	}
	
	//Create a game object thats polar
	public GameObject(boolean polar, float radius) {
		this();
		this.polar = polar;
		this.radius = radius;
	}
	public void update(float tpf) {
		vel.add(acc);
		
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
		
		if(behaviours.size > 0) {
			for(Behaviour b : behaviours) {
				acc.add(b.modAcc);
				vel.add(b.modVel);
				pos.add(b.modPos);
				b.update(tpf);
			}
		}
		
		mdi.transform.setToRotation(Vector3.Z, ((angle - vel.x*2)* 360f / TWOPI));
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
	
	public static void main(String[] args) {
		GameObject test = new GameObject(true, 10);
		System.out.println(test.dist(15, 5f));
	}
}
