package com.olunx.db;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.olunx.util.Config;
import com.olunx.util.Utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class CategoryHelper implements IHelper {

	private final String c_id = "id";
	private final String c_title = "title";
	private final String c_text = "text";
	private final String c_icon = "icon";
	private final String c_sortId = "sort_id";
	private final String c_feedCount = "feed_count";
	private final String c_feeds = "feeds";

	private static String TABLE = "t_category";

	private static SQLiteDatabase sqlite = null;

	@Override
	public SQLiteDatabase getDB() {
		try {
			if (sqlite == null || !sqlite.isOpen()) {
				File file = Utils.init().createFileIfNotExist(Config.FILE_SDCARD_DATABASE);
				sqlite = SQLiteDatabase.openOrCreateDatabase(file, null);
				Log.i("sqlite.getPath() ", sqlite.getPath());
				Log.i("Its open? ", String.valueOf(sqlite.isOpen()));
			}
			return sqlite;
		} catch (SQLException e) {
			return null;
		}
	}

	@Override
	public void close() {
		if (sqlite != null || sqlite.isOpen()) {
			sqlite.close();
		}
	}

	@Override
	public void createTable() {
		this.getDB().execSQL(
				"create table if not exists " + TABLE + "(" + c_id + " int primary key," + c_title + " text," + c_text + " text," + c_icon
						+ " text," + c_sortId + " int," + c_feedCount + " int," + c_feeds + " int);");
	}

	@Override
	public void dropTable() {
		this.getDB().execSQL("drop table if exists " + TABLE + ";");
	}

	private ContentValues row = null;

	/**
	 * 添加记录
	 * 
	 * @param title
	 * @param icon
	 * @param feedCount
	 * @param feeds
	 */
	public void addRecord(String title, String icon, int feedCount, int feeds) {
		row = new ContentValues();
		row.put(c_title, title);
		row.put(c_icon, icon);
		row.put(c_feedCount, feedCount);
		row.put(c_feeds, feeds);
		createTable();
		Log.i("addRecord", title);
		getDB().insert(TABLE, null, row);
	}

	/**
	 * 返回记录
	 * 
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getRecords() {
		createTable();

		Cursor result = getDB().query(TABLE, new String[] { c_title, c_icon, c_feedCount, c_feeds }, null, null, null, null, null);
		Log.i("Cursor Count", String.valueOf(result.getCount()));
		ArrayList<HashMap<String, String>> records = new ArrayList<HashMap<String, String>>();

		int titleColumn = result.getColumnIndex(c_title);
		int iconColumn = result.getColumnIndex(c_icon);
		int feedCountColumn = result.getColumnIndex(c_feedCount);
		int feedsColumn = result.getColumnIndex(c_feeds);

		if (result != null) {
			HashMap<String, String> map = null;
			result.moveToFirst();
			while (!result.isAfterLast()) {
				map = new HashMap<String, String>();
				map.put("title", result.getString(titleColumn));
				map.put("icon", result.getString(iconColumn));
				map.put("count", result.getString(feedCountColumn));
				map.put("feeds", result.getString(feedsColumn));
				records.add(map);
				result.moveToNext();
			}
		}
		result.close();
		return records;
	}

	// 返回不需要再次记忆的单词编号
	public String getIgnoreWords(int lessonNo) {
		createTable();

		String ignoreWords = "";
		Cursor result = getDB().query(TABLE, new String[] { "ignore_words" }, "lesson_no == '" + lessonNo + "'", null, null, null, null);
		if (result != null) {
			result.moveToFirst();
			int ignoreWordsColumn = result.getColumnIndex("ignore_words");
			while (!result.isAfterLast()) {
				ignoreWords = result.getString(ignoreWordsColumn);
				Log.i("ignoreWords", ignoreWords);
				result.moveToNext();
			}
		}

		return ignoreWords;
	}

	// 获取学习次数
	public int getTimesByLessonNo(int lessonNo) {
		createTable();

		String times = "";
		Cursor result = getDB().query(TABLE, new String[] { "times" }, "lesson_no == '" + lessonNo + "'", null, null, null, null);
		if (result != null) {
			result.moveToFirst();
			int timesColumn = result.getColumnIndex("times");
			while (!result.isAfterLast()) {
				times = result.getString(timesColumn);
				Log.i("lessonNoTimes", times);
				result.moveToNext();
			}
		}
		result.close();

		return Integer.parseInt(times);
	}

	// 获取初学时间
	public String getStudyTimeByLessonNo(int lessonNo) {
		createTable();

		String studyTime = "";
		Cursor result = getDB().query(TABLE, new String[] { "study_time" }, "lesson_no == '" + lessonNo + "'", null, null, null, null);
		if (result != null) {
			result.moveToFirst();
			int studyTimeColumn = result.getColumnIndex("study_time");
			while (!result.isAfterLast()) {
				studyTime = result.getString(studyTimeColumn);
				Log.i("study_time", studyTime);
				result.moveToNext();
			}
		}
		result.close();

		return studyTime;
	}

	// 删除一条记录
	public void deleteRecord(int lessonNo) {
		String sql = "delete from " + TABLE + " where lesson_no == '" + lessonNo + "'";
		Log.i("deleteRecord", sql);
		this.getDB().execSQL(sql);
	}

	// 判断此课程号是否存在
	public boolean isExistsLessonNo(int lessonNo) {
		createTable();

		String lesson = "";
		Cursor result = getDB().query(TABLE, new String[] { "lesson_no" }, "lesson_no == '" + lessonNo + "'", null, null, null, null);
		if (result != null) {
			result.moveToFirst();
			int timesColumn = result.getColumnIndex("lesson_no");
			while (!result.isAfterLast()) {
				lesson = result.getString(timesColumn);
				Log.i("lesson_no", lesson);
				result.moveToNext();
			}
		}
		result.close();
		if (lesson == null || lesson == "" || lesson.equals("")) {
			Log.i("lesson_no", "don't exists");
			return false;
		}
		return true;
	}

}
