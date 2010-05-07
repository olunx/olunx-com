package com.olunx.option.sync;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

public class Backup {

	public void copyFile(String fileFromPath, String fileToPath) {
		File srcFile = new File(fileFromPath);
		File targetFile = new File(fileToPath);
		if (!srcFile.exists()) {
			Log.i("srcFile file exists", String.valueOf(srcFile.exists()));
			try {
				new File(fileFromPath.substring(0, fileFromPath.lastIndexOf("/"))).mkdirs();
				srcFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (!targetFile.exists()) {
			Log.i("targetFile file exists", String.valueOf(targetFile.exists()));
			try {
				new File(fileToPath.substring(0, fileToPath.lastIndexOf("/"))).mkdirs();
				targetFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			targetFile.delete();
			try {
				targetFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		InputStream inBuffer = null;
		OutputStream outBuffer = null;
		try {
			InputStream in = null;
			OutputStream out = null;

			try {
				in = new FileInputStream(fileFromPath);
				out = new FileOutputStream(fileToPath);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			inBuffer = new BufferedInputStream(in);
			outBuffer = new BufferedOutputStream(out);

			int byteData = 0;
			try {
				while (true) {
					byteData = inBuffer.read();
					if (byteData == -1)
						break;
					out.write(byteData);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		} finally {

			try {
				if (inBuffer != null)
					inBuffer.close();
				if (outBuffer != null)
					outBuffer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
