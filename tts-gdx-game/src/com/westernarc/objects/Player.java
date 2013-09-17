package com.westernarc.objects;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector3;
public class Player {
	//3d coords
	public Vector3 pos;
	Vector3 vel;
	
	//radial coords
	//Modify these directly
	public Vector3 radPos;
	//float angle;  	//Polar angle
	//float rad;		//Radius from center
	//float dist;		//Distance on z axis
	
	ModelInstance modelInstance;

	public float rotAngle;
	
	public Player() {
		pos = new Vector3();
		vel = new Vector3();
		
		radPos = new Vector3();		
	}
	
	public void rectToPolCoords() {
		//TODO Implement
	}
	
	public void polToRectCoords() {
		if(pos != null) {
			pos.z = radPos.z;
			pos.y = (float)Math.sin(radPos.x) * radPos.y;
			pos.x = (float)Math.cos(radPos.x) * radPos.y;
		}
	}
}
