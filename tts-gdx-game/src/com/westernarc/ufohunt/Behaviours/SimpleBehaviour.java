package com.westernarc.ufohunt.Behaviours;

import com.badlogic.gdx.math.Vector3;

public class SimpleBehaviour extends Behaviour {
	
	//Sets the object to have a velocity of -3z
	public SimpleBehaviour(float x, float y, float z) {
		modVel = new Vector3(x,y,z);
	}
	
	@Override
	public void update(float tpf) {
		if(!flag[0]) {
			modVel.set(0,0,0);
			flag[0] = true;
		}
	}

}
