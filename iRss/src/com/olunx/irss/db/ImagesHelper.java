package com.olunx.irss.db;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.olunx.irss.util.Config;
import com.olunx.irss.util.Utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ImagesHelper implements IHelper {

	public final static String c_id = "_id";
	public final static String c_articleId = "article_id";
	public final static String c_htmlUrl = "html_url";
	public final static String c_localUrl = "local_url";
	public final static String c_imageType = "image_type";
	public final static String c_isDownloaded = "is_downloaded";

	private static String TABLE = "t_images";
	private final String TAG = "com.olunx.db.ImagesHelper";

	private static SQLiteDatabase sqlite = null;

	public ImagesHelper() {
		super();
		getDB();
		createTable();
	}

	@Override
	public SQLiteDatabase getDB() {
		try {
			if (sqlite == null || !sqlite.isOpen()) {
				File file = Utils.init().createFileIfNotExist(Config.FILE_SDCARD_DATABASE);
				sqlite = SQLiteDatabase.openOrCreateDatabase(file, null);
			}
			return sqlite;
		} catch (SQLException e) {
			return null;
		}
	}

	@Override
	public void close() {
		if (sqlite != null || sqlite.isOpen()) {
			Log.i(TAG, "sqlite close");
			sqlite.close();
		}
	}

	@Override
	public void createTable() {
		this.getDB().execSQL(
				"create table if not exists " + TABLE + "(" + c_id + " int primary key," + c_articleId + " text," + c_htmlUrl + " text,"
						+ c_localUrl + " text,"+ c_imageType + " text," + c_isDownloaded + " text);");
	}

	@Override
	public void dropTable() {
		this.getDB().execSQL("drop table if exists " + TABLE + ";");
	}

	/**
	 * 删除所有数据
	 */
	public void deleteAll() {
		this.getDB().execSQL("delete from " + TABLE + ";");
	}

	/**
	 * 添加记录
	 * 
	 */
	public void addRecord(ContentValues row) {
		getDB().insert(TABLE, null, row);
	}

	/**
	 * 设置图片下载完成
	 * 
	 * @param localUrl
	 */
	public void updateDownloaded(String localUrl) {
		ContentValues row = new ContentValues();
		row.put(c_isDownloaded, "true");
		getDB().update(TABLE, row, c_localUrl + "== ? ", new String[] { localUrl });
	}

	/**
	 * 获取指定文章的图片数据
	 * 
	 * @return
	 */
	public Map<String, String> getImagesMapByArticleId(String articleId) {
		Map<String, String> map = new HashMap<String, String>();
		Cursor result = getDB().query(TABLE, new String[] { c_htmlUrl, c_localUrl },
				c_articleId + "== ? and " + c_isDownloaded + "== 'true'", new String[] { articleId }, null, null, null);
		if (result != null) {
			result.moveToFirst();
			int htmlIndex = result.getColumnIndex(c_htmlUrl);
			int localIndex = result.getColumnIndex(c_localUrl);
			while (!result.isAfterLast()) {
				map.put(result.getString(htmlIndex), result.getString(localIndex));
				result.moveToNext();
			}
		}
		result.close();
		return map;
	}

	public ArrayList<Map<String, String>> getImagesByArticleId(String articleId) {
		ArrayList<Map<String, String>> array = new ArrayList<Map<String, String>>();

		Cursor result = getDB().query(TABLE, new String[] { c_htmlUrl, c_localUrl, c_imageType }, c_articleId + "== ? and " + c_isDownloaded + "== 'false'", new String[] { articleId }, null, null, null);
		if (result != null) {
			result.moveToFirst();
			int htmlIndex = result.getColumnIndex(c_htmlUrl);
			int localIndex = result.getColumnIndex(c_localUrl);
			int typeIndex = result.getColumnIndex(c_imageType);
			while (!result.isAfterLast()) {
				Map<String, String> map = new HashMap<String, String>();
				map.put(c_htmlUrl, result.getString(htmlIndex));
				map.put(c_localUrl, result.getString(localIndex));
				map.put(c_imageType, result.getString(typeIndex));
				array.add(map);
				result.moveToNext();
			}
		}
		result.close();
		return array;
	}
	
	/**
	 * 获取所有数据
	 * 
	 * @return
	 */
	public ArrayList<Map<String, String>> getAllImages() {
		ArrayList<Map<String, String>> array = new ArrayList<Map<String, String>>();

		Cursor result = getDB().query(TABLE, new String[] { c_htmlUrl, c_localUrl, c_imageType }, c_isDownloaded + "== 'false'", null, null, null, null);
		if (result != null) {
			result.moveToFirst();
			int htmlIndex = result.getColumnIndex(c_htmlUrl);
			int localIndex = result.getColumnIndex(c_localUrl);
			int typeIndex = result.getColumnIndex(c_imageType);
			while (!result.isAfterLast()) {
				Map<String, String> map = new HashMap<String, String>();
				map.put(c_htmlUrl, result.getString(htmlIndex));
				map.put(c_localUrl, result.getString(localIndex));
				map.put(c_imageType, result.getString(typeIndex));
				array.add(map);
				result.moveToNext();
			}
		}
		result.close();
		return array;
	}

	/**
	 * 删除一篇文章内的所有本地图片
	 * 
	 * @param articleId
	 */
	public void deleteRecord(String articleId) {
		String sql = "delete from " + TABLE + " where " + c_articleId + " == '" + articleId + "'";
		this.getDB().execSQL(sql);
	}

}
