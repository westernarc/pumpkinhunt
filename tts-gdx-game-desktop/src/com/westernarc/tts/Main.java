package com.westernarc.tts;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.westernarc.ufohunt.Tts;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Pumpkin Hunt!";
		cfg.useGL20 = true;
		cfg.width = 640;
		cfg.height = 960;
		
		new LwjglApplication(new Tts(), cfg);
	}
}
