package com.westernarc.ufohunt.Behaviours;

import com.badlogic.gdx.math.Vector3;

public abstract class Behaviour {	
	//Contains an update method for modifying the object
	//that has this behaviour.
	
	//Behaviours can modify position, velocity, or acceleration
	public Vector3 modPos;
	public Vector3 modVel;
	public Vector3 modAcc;
	
	protected float timer[];
	protected boolean flag[];
	
	public Behaviour() {
		timer = new float[16];
		for(int i = 0; i < 16; i++) {
			timer[i] = 0;
		}
		flag = new boolean[16];
		for(int i = 0; i < 16; i++) {
			flag[i] = false;
		}
		
		modPos = new Vector3();
		modVel = new Vector3();
		modAcc = new Vector3();
	}
	public abstract void update(float tpf);
}
