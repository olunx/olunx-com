package com.olunx;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Utils {

	public final static String publicKey = "publickey.x509.pem";
	public final static int publicKeySize = 1675;

	public final static String privateKey = "privatekey.pk8";
	public final static int privateKeySize = 1217;

	public static void createFile(String fileName, int fileSize) {

		String jarPath = null;

		// 获取当前Jar文件名，并对其解码，防止出现中文乱码
		try {
			jarPath = URLDecoder.decode(Utils.class.getProtectionDomain().getCodeSource().getLocation().getFile(), "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// 获取jar文件
		File jarFile = new File(jarPath);

		if (jarFile.isFile() && jarFile.exists()) {
			System.out.println(jarFile.getAbsolutePath());

			JarFile jar = null;
			try {
				jar = new JarFile(jarFile);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			JarEntry entry = jar.getJarEntry(fileName);

			// 下面开始复制jar里面的文件到外部

			BufferedInputStream bis = null;
			byte[] bytes = new byte[fileSize];
			try {
				bis = new BufferedInputStream(jar.getInputStream(entry));
				bis.read(bytes);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(new File(jarFile.getParent() + File.separator + fileName));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			try {
				fos.write(bytes);
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				fos.close();
				bis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * 获取当前路径
	 * 
	 * @return
	 */
	public static String getCurrentDir() {

		String jarPath = null;
		
		// 获取当前Jar文件名，并对其解码，防止出现中文乱码
		try {
			jarPath = URLDecoder.decode(Utils.class.getProtectionDomain().getCodeSource().getLocation().getFile(), "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		File jar = new File(jarPath);

		// 如果得到的是jar文件路径，则处理得到key的路径。
		if (jar.isFile()) {
			String path = jar.getAbsolutePath();
			jarPath = path.substring(0, path.lastIndexOf(File.separator));
		}
		System.out.println("当前路径:" + jarPath);

		return jarPath;

	}

	/**
	 * 关于
	 * @return
	 */
	public static String getAboutText() {
		StringBuffer sb = new StringBuffer();
		sb.append("作者：olunx\n");
		sb.append("网站：http://olunx.com\n");
		sb.append("本项目源码：http://olunx-com.googlecode.com/\n");
		sb.append("申明：这只是一个单纯的GUI，本项目引用了一下项目的源码。\n");
		sb.append("xml反编译：AXMLPrinter  http://android4me.googlecode.com/\n");
		sb.append("dex反编译：dex2jar  http://dex2jar.googlecode.com/\n");
		sb.append("apk签名：Google Android\n");
		
		return sb.toString();
	}
	
}
