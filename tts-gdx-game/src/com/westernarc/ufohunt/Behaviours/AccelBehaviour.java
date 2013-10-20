package com.westernarc.ufohunt.Behaviours;

import com.badlogic.gdx.math.Vector3;

public class AccelBehaviour extends Behaviour {
	
	//Sets the object to have a velocity of -3z
	public AccelBehaviour(float x, float y, float z) {
		modAcc = new Vector3(x,y,z);
	}
	@Override
	public void update(float tpf) {
	}

}
