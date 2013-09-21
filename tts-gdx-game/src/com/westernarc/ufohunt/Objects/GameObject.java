package com.westernarc.ufohunt.Objects;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.westernarc.ufohunt.Behaviours.Behaviour;

public class GameObject {
	public boolean polar = false;
	public float radius = 0;
	public ModelInstance mdi;
	public Vector3 pos;
	public Vector3 polpos;
	public Vector3 vel;
	public Vector3 acc;
	
	float angle = 0;

	Behaviour behaviour;
	
	public GameObject() {
		pos = new Vector3();
		polpos = new Vector3();
		vel = new Vector3();
		acc = new Vector3();
		
		radius = 0;
		angle = 0;
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
			angle = polpos.x / (2 * 3.1415f * radius);
			pos.x = (float)Math.cos(angle) * radius;
			pos.y = (float)Math.sin(angle) * radius;
		}
	}
}
