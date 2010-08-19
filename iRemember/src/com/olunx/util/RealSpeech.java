package com.olunx.util;

import java.io.File;
import java.io.IOException;

import android.media.MediaPlayer;

public class RealSpeech {

	private static RealSpeech real;
	private MediaPlayer player;

	public RealSpeech() {
		player = new MediaPlayer();
	}

	public static RealSpeech getReal() {
		if (real == null) {
			real = new RealSpeech();
		}
		return real;
	}

	private String subPath = null;
	private String fullPath = null;
	private String baseDir = Config.init().getSoundDir();

	private String getSoundFile(String word) {
		subPath = word.substring(0, 1).toLowerCase();
		fullPath = baseDir + subPath + File.separator + word.toLowerCase() + ".mp3";
		return fullPath;
	}

	private File soundFile;
	private String soundPath;

	public boolean prepare(String word) {

		soundPath = getSoundFile(word);
		soundFile = new File(soundPath);
		System.out.println("soundFile.getPath():" + soundFile.getPath());
		player.reset();
		if (soundFile.exists()) {
			try {
				player.setDataSource(soundPath);
				player.prepare();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
		
		return false;

	}

	public void speak() {
		player.start();
	}

	public void stop() {
		player.stop();
	}
	
	public void shutdown() {
		player.release();
	}
}
