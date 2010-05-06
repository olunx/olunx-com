package com.olunx.db;

import java.util.ArrayList;
import java.util.HashMap;

import com.olunx.R;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class RememberHelper implements HelperInterface {

	private static String TABLE = "t_remember";

	private static SQLiteDatabase sqlite = null;
	private Context context;

	public RememberHelper(Context context) {
		this.context = context;
	}

	@Override
	public SQLiteDatabase getDB() {
		try {
			if (sqlite == null || !sqlite.isOpen()) {
				sqlite = new DBHelper(context).getWritableDatabase();
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
				"create table if not exists " + TABLE + "(id int,lesson_no int,study_time text,next_study_time text,times int,ignore_words text);");
	}

	@Override
	public void dropTable() {
		this.getDB().execSQL("drop table if exists " + TABLE + ";");
	}

	// 添加记录
	private ContentValues row = null;

	public void addRecord(long lessonNo, String studyTime, String nextStudyTime, int times, String ignoreWords) {
		row = new ContentValues();
		row.put("lesson_no", lessonNo);
		row.put("study_time", studyTime);
		row.put("next_study_time", nextStudyTime);
		row.put("times", times);
		row.put("ignore_words", ignoreWords);
		createTable();
		Log.i("addRecord", String.valueOf(lessonNo));
		getDB().insert(TABLE, null, row);
	}

	// 返回记录
	public ArrayList<HashMap<String, String>> getRecords() {
		createTable();

		Cursor result = getDB().query(TABLE, new String[] { "lesson_no", "study_time", "next_study_time", "times" }, null, null, null,
				null, "lesson_no");
		Log.i("Cursor Count", String.valueOf(result.getCount()));
		ArrayList<HashMap<String, String>> records = new ArrayList<HashMap<String, String>>();

		int lessonNoColumn = result.getColumnIndex("lesson_no");
		int studyTimeColumn = result.getColumnIndex("study_time");
		int nextStudyTimeColumn = result.getColumnIndex("next_study_time");
		int timesColumn = result.getColumnIndex("times");

		if (result != null) {
			HashMap<String, String> map = null;

			result.moveToFirst();

			String title = context.getString(R.string.title);
			String desc = context.getString(R.string.description);
			while (!result.isAfterLast()) {
				map = new HashMap<String, String>();
				map.put(title, result.getString(lessonNoColumn));
				map.put(desc, "初记时间：" + result.getString(studyTimeColumn) + "  记忆次数：" + result.getString(timesColumn));
				map.put("复习时间", result.getString(nextStudyTimeColumn));
				records.add(map);
				result.moveToNext();
			}
		}
		result.close();
		return records;
	}
	
	//返回不需要再次记忆的单词编号
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
