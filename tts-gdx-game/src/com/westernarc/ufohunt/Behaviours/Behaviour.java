package com.westernarc.ufohunt.Behaviours;

public abstract class Behaviour {
	//Contains an update method for modifying the object
	//that has this behaviour
	protected float timer[];
	public Behaviour() {
		timer = new float[16];
	}
	public abstract void update(float tpf);
}
