package com.olunx.db;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.olunx.util.Config;
import com.olunx.util.Utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class RememberHelper implements IHelper {

	private static String TABLE = "t_remember";

	private static SQLiteDatabase sqlite = null;

	@Override
	public SQLiteDatabase getDB() {
		try {
			if (sqlite == null || !sqlite.isOpen()) {
				File file = Utils.init().createFileIfNotExist(Config.FILE_SDCARD_DATABASE);
				sqlite = SQLiteDatabase.openOrCreateDatabase(file, null);
			}

			Log.i("sqlite.getPath() ", sqlite.getPath());
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
				"create table if not exists " + TABLE
						+ "(id int,lesson_no int,study_time text,next_study_time text,times int,ignore_words text);");
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
	public ArrayList<HashMap<String, String>> getRecords(boolean isNeedStudy) {
		createTable();

		//获取需要复习的课程，isNeedStudy为true时就要。
		String select = null;
		if(isNeedStudy) {
			select = "next_study_time <= '" + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()) + "'";
		}
		
		Cursor result = getDB().query(TABLE, new String[] { "lesson_no", "study_time", "next_study_time", "times" }, select, null, null,
				null, "lesson_no");
		ArrayList<HashMap<String, String>> records = new ArrayList<HashMap<String, String>>();

		int lessonNoColumn = result.getColumnIndex("lesson_no");
		int studyTimeColumn = result.getColumnIndex("study_time");
		int nextStudyTimeColumn = result.getColumnIndex("next_study_time");
		int timesColumn = result.getColumnIndex("times");

		if (result != null) {
			Log.i("Cursor Count", String.valueOf(result.getCount()));
			
			HashMap<String, String> map = null;

			result.moveToFirst();

			String title = "标题";
			String desc = "描述";
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
	
	// 返回一个需要复习的课程号
	public int getOneNeedStudyLesson() {
		createTable();

		int resultLessonNo = -1;
		
		String select = "next_study_time <= '" + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()) + "'";
		
		Cursor result = getDB().query(TABLE, new String[] { "lesson_no" }, select, null, null,
				null, "lesson_no");
		
		int lessonNoColumn = result.getColumnIndex("lesson_no");

		if (result != null) {
			result.moveToFirst();
			if(result.getCount() > 0) {
				Log.i("result.getString(lessonNoColumn)", result.getString(lessonNoColumn));
				resultLessonNo = Integer.parseInt(result.getString(lessonNoColumn));
			}
		}
		result.close();
		
		return resultLessonNo;
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
		result.close();
		
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
