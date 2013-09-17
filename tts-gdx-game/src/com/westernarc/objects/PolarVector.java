package com.westernarc.objects;

import com.badlogic.gdx.math.Vector3;

public class PolarVector {
	private Vector3 pos;
	public float angle;
	public float rad;
	public float dist;
	
	public PolarVector() {
		pos = new Vector3();
		angle = 0;
		rad = 0;
		dist = 0;
		pos.set(angle, rad, dist);
	}
	public void translateAngle(float a) {
		pos.x += a;
	}
	public void translateRad(float r) {
		pos.y += r;
	}
	public void translateDist(float d) {
		pos.z += d;
	}
	
	public void setAngle(float a) {
		pos.x = a;
	}
	public void setRad(float r) {
		pos.y = r;
	}
	public void setDist(float d) {
		pos.z = d;
	}
	
	public float x() {
		return (float)Math.cos(pos.x) * pos.y;
	}
	public float y() {
		return (float)Math.sin(pos.x) * pos.y;
	}
	public float z() {
		return pos.z;
	}
}
