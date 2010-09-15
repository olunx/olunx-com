package com.olunx.db;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import com.olunx.util.Config;
import com.olunx.util.Utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class FeedsHelper implements IHelper {

	private final String c_id = "_id";
	public final static String c_title = "title";
	public final static String c_text = "text";
	public final static String c_icon = "icon";
	public final static String c_sortId = "sort_id";
	public final static String c_catTitle = "cat_title";
	public final static String c_xmlUrl = "xml_url";
	public final static String c_htmlUrl = "html_url";
	public final static String c_googleFeedId = "google_feed_id";
	public final static String c_updateTime = "update_time";
	public final static String c_charset = "charset";
	public final static String c_rssType = "rss_type";
	public final static String c_articleCount = "article_count";
	public final static String c_articles = "articles";

	private static String TABLE = "t_feeds";
	private final String TAG = "com.olunx.db.FeedsHelper";

	private static SQLiteDatabase sqlite = null;

	public FeedsHelper() {
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
						+ " text," + c_sortId + " int," + c_catTitle + " text," + c_xmlUrl + " text," + c_htmlUrl + " text,"
						+ c_googleFeedId + " text," + c_updateTime + " text," + c_charset + " text," + c_rssType + " text,"
						+ c_articleCount + " int," + c_articles + " text);");
	}

	@Override
	public void dropTable() {
		this.getDB().execSQL("drop table if exists " + TABLE + ";");
	}

	/**
	 * 添加Feed
	 * 
	 * @param object
	 */
	public void addRecord(ContentValues mValues) {
		getDB().insert(TABLE, null, mValues);
	}

	/**
	 * 更新一条记录
	 * 
	 * @param object
	 */
	public void updateRecord(ContentValues object) {
		String where = (String) object.get(c_xmlUrl);
		getDB().update(TABLE, object, c_xmlUrl + "== ? ", new String[] { where });
	}

	/**
	 * 更新文章数目
	 * 
	 * @param feedXmlUrl
	 * @param articleCount
	 */
	public void updateArticleCount(String feedXmlUrl, String articleCount) {
		ContentValues row = new ContentValues();
		row.put(c_articleCount, articleCount);
		row.put(c_updateTime, Utils.init().getCstTime(new Date()));
		getDB().update(TABLE, row, c_xmlUrl + "== ? ", new String[] { feedXmlUrl });
	}

	/**
	 * 更新Feed的内容编码
	 * 
	 * @param feedXmlUrl
	 * @param charset
	 */
	public void updateFeedCharset(String feedXmlUrl, String charset) {
		ContentValues row = new ContentValues();
		row.put(c_charset, charset);
		getDB().update(TABLE, row, c_xmlUrl + "== ? ", new String[] { feedXmlUrl });
	}

	/**
	 * 返回分类并统计每个分类下的feed数目
	 * 
	 * @return
	 */
	public void updateCategoryStatus() {
		Cursor result = getDB().query(TABLE, new String[] { c_catTitle, "count(" + c_catTitle + ") as count" }, null, null, c_catTitle,
				null, null);

		int titleColumn = result.getColumnIndex(c_catTitle);
		int countColumn = result.getColumnIndex("count");

		CategoryHelper helper = new CategoryHelper();

		String title = null;
		String count = null;
		if (result != null) {
			result.moveToFirst();
			while (!result.isAfterLast()) {
				title = result.getString(titleColumn);
				count = result.getString(countColumn);
				if (helper.isExistsCat(title)) {
					helper.updateFeedCount(title, count);
				} else {
					helper.addRecord(title, count);
				}

				result.moveToNext();
			}
		}
		result.close();
		helper.close();
	}

	/**
	 * 获取指定分类的Feed
	 * 
	 * @return
	 */
	public Cursor getFeedsByCategory(String catTitle) {
		return getDB().query(TABLE, new String[] { c_id, c_icon, c_title, c_articleCount, c_xmlUrl, c_charset }, c_catTitle + "== ?",
				new String[] { catTitle }, null, null, null);
	}

	/**
	 * 返回指定栏目下的所有Feed地址
	 * 
	 * @param catTitle
	 * @return
	 */
	public ArrayList<String> getFeedsXmlUrlByCategory(String catTitle) {
		ArrayList<String> array = new ArrayList<String>();

		Cursor result = getDB().query(TABLE, new String[] { c_id, c_xmlUrl }, c_catTitle + "== ?", new String[] { catTitle }, null, null,
				null);
		if (result != null) {
			result.moveToFirst();
			int index = result.getColumnIndex(c_xmlUrl);
			while (!result.isAfterLast()) {
				array.add(result.getString(index));
				result.moveToNext();
			}
		}
		result.close();

		return array;
	}

	/**
	 * 返回所有Feed地址
	 * 
	 * @return
	 */
	public ArrayList<String> getAllFeedsXmlUrl() {
		ArrayList<String> array = new ArrayList<String>();

		Cursor result = getDB().query(TABLE, new String[] { c_id, c_xmlUrl }, null, null, null, null, null);
		if (result != null) {
			result.moveToFirst();
			int index = result.getColumnIndex(c_xmlUrl);
			while (!result.isAfterLast()) {
				array.add(result.getString(index));
				result.moveToNext();
			}
		}
		result.close();

		return array;
	}

	/**
	 * 获取更新时间
	 * 
	 * @param feedXmlUrl
	 * @return
	 */
	public String getFeedUpdateTime(String feedXmlUrl) {
		String str = null;
		Cursor result = getDB().query(TABLE, new String[] { c_updateTime }, c_xmlUrl + "== ?", new String[] { feedXmlUrl }, null, null,
				null);
		if (result != null) {
			result.moveToFirst();
			int index = result.getColumnIndex(c_updateTime);
			while (!result.isAfterLast()) {
				str = result.getString(index);
				result.moveToNext();
			}
		}
		result.close();

		return str;
	}

	/**
	 * 判断Feed是否存在
	 * 
	 * @param feedXmlUrl
	 * @return
	 */
	public boolean isExistsFeed(String feedXmlUrl) {
		String str = null;
		Cursor result = getDB().query(TABLE, new String[] { c_xmlUrl }, c_xmlUrl + "== ?", new String[] { feedXmlUrl }, null, null, null);
		if (result != null) {
			result.moveToFirst();
			int index = result.getColumnIndex(c_xmlUrl);
			while (!result.isAfterLast()) {
				str = result.getString(index);
				result.moveToNext();
			}
		}
		result.close();
		if (str == null || str == "" || str.equals("")) {
			return false;
		}
		return true;
	}

	/**
	 * 删除一条feed
	 * 
	 * @param feedTitle
	 */
	public void deleteRecord(String feedTitle) {
		String sql = "delete from " + TABLE + " where " + c_title + " == '" + feedTitle + "'";
		this.getDB().execSQL(sql);
	}

}
