package com.olunx.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

public class Utils {

	public static Utils utils;

	public static Utils init() {
		if (utils == null) {
			utils = new Utils();
		}
		return utils;
	}

	/**
	 * 复制二进制文件
	 * 
	 * @param fileFromPath
	 * @param fileToPath
	 */
	public void copyBinFile(String fileFromPath, String fileToPath) {
		// 如果文件不存在则创建它
		createFileIfNotExist(fileFromPath);
		createNewFile(fileToPath);

		InputStream in = null;
		OutputStream out = null;

		try {
			in = new FileInputStream(fileFromPath);
			out = new FileOutputStream(fileToPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		this.copyBinFile(in, out);
	}

	public void copyBinFile(InputStream in, OutputStream out) {
		// 开始复制文件
		InputStream inBuffer = null;
		OutputStream outBuffer = null;

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
	 * 复制文本文件
	 * 
	 * @param in
	 * @param targetFilePath
	 */
	public void copyFile(InputStream in, String targetFilePath) {
		createNewFile(targetFilePath);

		OutputStream out = null;

		try {
			out = new FileOutputStream(targetFilePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		this.copyFile(in, out);
	}

	public void copyFile(InputStream in, OutputStream out) {

		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			bw = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String data = null;
		try {
			while ((data = br.readLine()) != null) {
				bw.write(data + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 删除旧的文件，创建新的空白的文件。
	 * 
	 * @param targetFilePath
	 */
	public void createNewFile(String targetFilePath) {
		createFileIfNotExist(targetFilePath);

		// 删除已有的目标文件
		File targetFile = new File(targetFilePath);
		targetFile.delete();
		try {
			targetFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 如果文件不存在则创建它
	 * 
	 * @param path
	 * @return
	 */
	public File createFileIfNotExist(String path) {
		File file = new File(path);
		if (!file.exists()) {
			// Log.i("srcFile file exists", String.valueOf(file.exists()));
			try {
				new File(path.substring(0, path.lastIndexOf(File.separator))).mkdirs();
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// Log.i("file create finished", String.valueOf(file.exists()));
		return file;
	}
}
