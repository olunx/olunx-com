package com.olunx.irss.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang.math.RandomUtils;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

import com.olunx.irss.db.ImagesHelper;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class SysTools {

	private static final String TAG = "com.olunx.irss.util.SysTools";

	/**
	 * 检测网络是否可用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isConnect(Context context) {
		// 获取手机所有连接管理对象
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			// 获取网络信息
			NetworkInfo info = connectivity.getActiveNetworkInfo();
			if (info != null) {
				// 判断当前网络是否已经连接
				if (info.getState() == NetworkInfo.State.CONNECTED) {
					System.out.println("network connected");
					return true;
				}
			}
		}
		System.out.println("network disconnect");
		return false;
	}

	/**
	 * 读取assets的文件内容
	 * 
	 * @param filePath
	 * @return
	 */
	public static String getDataFromAssets(Context context, String filePath) {
		BufferedReader br = null;
		StringBuffer sb = new StringBuffer();
		try {
			br = new BufferedReader(new InputStreamReader(context.getAssets().open(filePath)));
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

	private static ArrayList<ContentValues> mValues;

	/**
	 * 保存图片路径
	 * 
	 * @param articleId
	 * @param html
	 */
	public static void storeImageUrlToDatabase(String articleId, String html) {
		System.out.println("process images");
		NodeList list = null;
		try {
			Parser parser = new Parser(html);
			list = parser.parse(null);
		} catch (ParserException e) {
			e.printStackTrace();
		}
		mValues = new ArrayList<ContentValues>();
		processImages(articleId, list);
		ImagesHelper helper = new ImagesHelper();
		for (ContentValues row : mValues) {
			helper.addRecord(row);
		}
		helper.close();
	}

	private static void processImages(String articleId, NodeList list) {
		if (list == null)
			return;

		Node node = null;
		SimpleNodeIterator iterator = list.elements();

		while (iterator.hasMoreNodes()) {
			node = iterator.nextNode();
			if (node == null)
				break;

			// 处理图片节点
			if (node instanceof ImageTag) {// img
				ImageTag img = (ImageTag) node;
				String htmlUrl = img.getImageURL();
				if (htmlUrl != null && !htmlUrl.equals("")) {
					ContentValues row = new ContentValues();
					row.put(ImagesHelper.c_id, System.currentTimeMillis() + RandomUtils.nextInt(2010));
					row.put(ImagesHelper.c_articleId, articleId);
					row.put(ImagesHelper.c_htmlUrl, htmlUrl);
					Log.i(TAG, htmlUrl);
					String ext = htmlUrl.substring(htmlUrl.lastIndexOf(".") + 1, htmlUrl.length());
					if (ext.contains("jpg") || ext.contains("jpeg")) {
						ext = "jpg";
					} else if (ext.contains("png")) {
						ext = "png";
					} else if (ext.contains("gif")) {
						ext = "gif";
					}
					String image = System.currentTimeMillis() + RandomUtils.nextInt(2010) + "." + ext;
					Log.i(TAG, image);
					row.put(ImagesHelper.c_imageType, ext);
//					row.put(ImagesHelper.c_localUrl, image);
					row.put(ImagesHelper.c_localUrl, Config.SDCARD_PATH + "images/" + image);
					row.put(ImagesHelper.c_isDownloaded, "false");
					mValues.add(row);
				}

			}

			processImages(articleId, node.getChildren());// 处理子节点
		}

	}

	/**
	 * 下载图片
	 * 
	 * @param articleId
	 */
	public static void downloadImagesToStorage(String articleId) {
		ImagesHelper helper = new ImagesHelper();
		ArrayList<Map<String, String>> array = null;

		if (articleId == null) {
			array = helper.getAllImages();
		} else {
			array = helper.getImagesByArticleId(articleId);
		}

		for (Map<String, String> map : array) {
			String htmlUrl = map.get(ImagesHelper.c_htmlUrl);
			String localUrl = map.get(ImagesHelper.c_localUrl);
			String imageType = map.get(ImagesHelper.c_imageType);
			boolean finished = download(htmlUrl, localUrl, imageType);
			if (finished) {
				helper.updateDownloaded(localUrl);
			}
		}

		helper.close();
	}

	private static boolean download(String htmlUrl, String localUrl, String imageType) {
		Log.i(TAG, "download offline image");
		
		URL url = null;
		try {
			url = new URL(htmlUrl);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		URLConnection conn = null;
		try {
			conn = url.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}

		InputStream is = null;
		try {
			is = url.openStream();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Bitmap bitmap = null;
		int length = (int) conn.getContentLength();
		if (length != -1) {
			byte[] imgData = new byte[length];
			byte[] temp = new byte[512];
			int readLen = 0;
			int destPos = 0;
			try {
				while ((readLen = is.read(temp)) > 0) {
					System.arraycopy(temp, 0, imgData, destPos, readLen);
					destPos += readLen;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			bitmap = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
		}

		Utils.init().createFileIfNotExist(localUrl);// 创建文件
		File image = new File(localUrl);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(image);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		if(bitmap == null) return false;
		if ("png".equals(imageType)) {
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
			try {
				fos.flush();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		} else if ("jpeg".equals(imageType) || "jpg".equals(imageType)) {
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			try {
				fos.flush();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		} else {
			return false;
		}

	}
}
