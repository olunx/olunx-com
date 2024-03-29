package com.olunx.irss.db;

import java.io.File;
import java.util.ArrayList;

import com.olunx.irss.R;
import com.olunx.irss.util.Config;
import com.olunx.irss.util.Utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class CategoryHelper implements IHelper {

	private final String c_id = "_id";
	public final static String c_title = "title";
	public final static String c_text = "text";
	public final static String c_icon = "icon";
	public final static String c_sortId = "sort_id";
	public final static String c_feedCount = "feed_count";
	public final static String c_feeds = "feeds";

	private static String TABLE = "t_category";
	private final String TAG = "com.olunx.db.CategoryHelper";

	private static SQLiteDatabase sqlite = null;

	public CategoryHelper() {
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
				"create table if not exists " + TABLE + "(" + c_id + " int primary key," + c_title + " text," + c_text + " text," + c_icon
						+ " text," + c_sortId + " int," + c_feedCount + " int," + c_feeds + " text);");
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
	 * @param catTitle
	 * @param feedCount
	 */
	public void addRecord(String catTitle, String feedCount) {
		ContentValues row = new ContentValues();
		row.put(c_title, catTitle);
		row.put(c_feedCount, feedCount);
		row.put(c_icon, R.drawable.cat_icon);
		getDB().insert(TABLE, null, row);
	}


	/**
	 * 获取所有数据
	 * 
	 * @return
	 */
	public ArrayList<String> getAllCats() {
		ArrayList<String> titles = new ArrayList<String>();
		Cursor result = getDB().query(TABLE, new String[] {c_title}, null, null, null, null, null);
		if (result != null) {
			result.moveToFirst();
			int index = result.getColumnIndex(c_title);
			while (!result.isAfterLast()) {
				titles.add(result.getString(index));
				result.moveToNext();
			}
		}
		result.close();
		return titles;
	}
	
	/**
	 * 删除一个分类
	 * 
	 * @param catTitle
	 */
	public void deleteRecord(String catTitle) {
		String sql = "delete from " + TABLE + " where " + c_title + " == '" + catTitle + "'";
		this.getDB().execSQL(sql);
	}

}
