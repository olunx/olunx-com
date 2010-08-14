package com.olunx.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Utils {
	
	public static Utils utils;
	
	public static Utils init() {
		if(utils == null) {
			utils = new Utils();
		}
		return utils;
	}

	/**
	 * 复制文件
	 * 
	 * @param fileFromPath
	 * @param fileToPath
	 */
	public void copyFile(String fileFromPath, String fileToPath) {
		//如果文件不存在则创建它
		createFileIfNotExist(fileFromPath);
		createFileIfNotExist(fileToPath);

		//删除已有的目标文件
		File targetFile = new File(fileToPath);
		targetFile.delete();
		try {
			targetFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//开始复制文件
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
	
	/**
	 * 如果文件不存在则创建它
	 * @param path
	 * @return
	 */
	public File createFileIfNotExist(String path) {
		File file = new File(path);
		if (!file.exists()) {
//			Log.i("srcFile file exists", String.valueOf(file.exists()));
			try {
				new File(path.substring(0, path.lastIndexOf("/"))).mkdirs();
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
//		Log.i("file create finished", String.valueOf(file.exists()));
		return file;
	}
}
