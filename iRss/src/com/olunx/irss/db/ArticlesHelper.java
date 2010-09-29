package com.olunx.irss.db;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.olunx.irss.R;
import com.olunx.irss.util.Config;
import com.olunx.irss.util.Utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ArticlesHelper implements IHelper {

	public final static String c_id = "_id";
	public final static String c_title = "title";
	public final static String c_desc = "desc";
	public final static String c_content = "content";
	public final static String c_author = "author";
	public final static String c_publishTime = "publish_time";
	public final static String c_link = "link";
	public final static String c_type = "type";
	public final static String c_unread = "unread";
	public final static String c_stared = "stared";
	public final static String c_tags = "tags";
	public final static String c_feedXmlUrl = "feed_xml_url";

	private static String TABLE = "t_articles";
	private final String TAG = "com.olunx.db.ArticlesHelper";

	private static SQLiteDatabase sqlite = null;

	public ArticlesHelper() {
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
		Log.i(TAG, "create table");
		this.getDB().execSQL(
				"create table if not exists " + TABLE + "(" + c_id + " int primary key," + c_title + " text," + c_desc + " text,"
						+ c_content + " text," + c_author + " text," + c_publishTime + " text," + c_link + " text," + c_type + " text,"
						+ c_unread + " text," + c_stared + " text," + c_tags + " text," + c_feedXmlUrl + " text);");
	}

	@Override
	public void dropTable() {
		Log.i(TAG, "drop table");
		this.getDB().execSQL("drop table if exists " + TABLE + ";");
	}

	public boolean isOpen() {
		return this.getDB().isOpen();
	}

	/**
	 * 添加Article
	 * 
	 * @param object
	 */
	public void addRecord(ContentValues values) {
		getDB().insert(TABLE, null, values);
	}

	// /**
	// * 更新文章内容
	// *
	// * @param link
	// * @param content
	// */
	// public void updateArticleContent(String link, String content) {
	// ContentValues row = new ContentValues();
	// row.put(c_content, content);
	// getDB().update(TABLE, row, c_link + "== ? ", new String[] { link });
	// }

	/**
	 * 更新Feed的状态信息
	 */
	public void updateFeedsStatus() {
		Cursor result = getDB().query(TABLE, new String[] { c_feedXmlUrl, "count(" + c_feedXmlUrl + ") as count" }, null, null,
				c_feedXmlUrl, null, null);

		int urlColumn = result.getColumnIndex(c_feedXmlUrl);
		int countColumn = result.getColumnIndex("count");

		FeedsHelper helper = new FeedsHelper();

		String url = null;
		String count = null;
		if (result != null) {
			result.moveToFirst();
			while (!result.isAfterLast()) {
				url = result.getString(urlColumn);
				count = result.getString(countColumn);
				helper.updateArticleCount(url, count);
				result.moveToNext();
			}
		}
		result.close();
		helper.close();
	}

	/**
	 * 获取指定Feed下的文章列表
	 * 
	 * @param feedXmlUrl
	 * @return
	 */
	public ArrayList<Map<String, Object>> getArticlesByFeedXmlUrl(String feedXmlUrl) {
		Cursor result = getDB().query(TABLE, new String[] { c_id, c_title, c_link, c_publishTime, c_unread, c_stared }, c_feedXmlUrl + "== ?",
				new String[] { feedXmlUrl }, null, null, c_publishTime + " desc");
		
		ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> map;
		if (result != null) {
			result.moveToFirst();
			int idIndex = result.getColumnIndex(c_id);
			int titleIndex = result.getColumnIndex(c_title);
			int linkIndex = result.getColumnIndex(c_link);
			int timeIndex = result.getColumnIndex(c_publishTime);
			int unreadIndex = result.getColumnIndex(c_unread);
			int staredIndex = result.getColumnIndex(c_stared);
			Utils utils = new Utils();
			while (!result.isAfterLast()) {
				map = new HashMap<String, Object>();
				map.put(c_id, result.getString(idIndex));
				map.put(c_title, result.getString(titleIndex));
				map.put(c_link, result.getString(linkIndex));
				map.put(c_publishTime, utils.formatCstTimeToLocal(result.getString(timeIndex), "MM月dd日 HH:mm"));
				if(result.getString(unreadIndex).equals("true")) {
					map.put(c_unread, R.drawable.article_unread);
				}else {
					map.put(c_unread, R.drawable.article_read);
				}
				if(result.getString(staredIndex).equals("true")) {
					map.put(c_stared, R.drawable.article_stared);
				}else {
					map.put(c_stared, R.drawable.article_unstar);
				}
				
				list.add(map);
				result.moveToNext();
			}
		}
		result.close();
		return list;
	}

	/**
	 * 获取指定文章内容
	 * 
	 * @param link
	 * @return
	 */
	public String getArticleContentById(String id) {

		if (id == null) {
			return null;
		}

		String content = null;

		Cursor result = getDB().query(TABLE, new String[] { c_id, c_content }, c_id + "== ?", new String[] { id }, null, null, null);

		if (result != null) {
			result.moveToFirst();
			int contentIndex = result.getColumnIndex(c_content);
			while (!result.isAfterLast()) {
				content = result.getString(contentIndex);
				result.moveToNext();
			}
		}
		result.close();

		setArticleUnread(id, false);

		return content;
	}

	/**
	 * 设置文章为已读或未读
	 * 
	 * @param link
	 * @param value
	 */
	public void setArticleUnread(String id, Boolean value) {
		ContentValues values = new ContentValues();
		values.put(c_unread, String.valueOf(value));
		getDB().update(TABLE, values, c_id + "== ?", new String[] { id });
	}

	/**
	 * 获取一篇未读文章的id
	 * 
	 * @param feedXmlUrl
	 * @return
	 */
	public String getUnreadArticleIdByFeedXmlUrl(String feedXmlUrl) {
		String content = null;

		Cursor result = getDB().query(true, TABLE, new String[] { c_id }, c_feedXmlUrl + "== ? and " + c_unread + "== 'true'",
				new String[] { feedXmlUrl }, null, null, null, "1");
		if (result != null) {
			result.moveToFirst();
			int idIndex = result.getColumnIndex(c_id);
			while (!result.isAfterLast()) {
				content = result.getString(idIndex);
				result.moveToNext();
			}
		}
		result.close();

		return content;
	}

	/**
	 * 判断文章是否存在
	 * 
	 * @param articleTitle
	 * @return
	 */
	public boolean isExistsArticle(String articleTitle) {
		String str = null;
		Cursor result = getDB().query(TABLE, new String[] { c_title }, c_title + "== ?", new String[] { articleTitle }, null, null, null);
		if (result != null) {
			result.moveToFirst();
			int index = result.getColumnIndex(c_title);
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
	 * 删除文章
	 * 
	 * @param feedXmlUrl
	 */
	public void deleteRecords(String feedXmlUrl) {
		String sql = "delete from " + TABLE + " where " + c_feedXmlUrl + " == '" + feedXmlUrl + "'";
		this.getDB().execSQL(sql);
	}

}
