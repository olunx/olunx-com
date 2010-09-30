package com.olunx.irss.reader;

import java.util.ArrayList;

import android.content.ContentValues;
import android.util.Log;

import com.olunx.irss.R;
import com.olunx.irss.db.ArticlesHelper;
import com.olunx.irss.db.FeedsHelper;
import com.olunx.irss.util.Config;
import com.olunx.irss.util.Utils;

public class Update {

	private final String TAG = "com.olunx.reader.Update";

	private Google google;

	public Update() {
		super();
		String user = Config.init().getUsername();
		String pwd = Config.init().getPassword();
		if (google == null) {
			google = new Google();
			google.login(user, pwd);
		}
	}

	/**
	 * 更新Feed条目
	 */
	public void getFeedsFromGoogle() {
		Log.i(TAG, "update feeds start");

		ArrayList<ContentValues> array = google.downLoadAllFeeds();

		// 如果没有数据，则返回。
		if (array == null)
			return;

		FeedsHelper helper = new FeedsHelper();
		int len = array.size();
		Log.i(TAG, String.valueOf(len));
		for (int i = 0; i < len; i++) {
			ContentValues mValues = array.get(i);
			if (helper.isExistsFeed(mValues.get(FeedsHelper.c_xmlUrl).toString())) {
				helper.updateRecord(mValues);
			} else {
				helper.addRecord(mValues);
			}
		}
		helper.updateCategoryStatus();// 为Category表重新统计Feed条数。
		helper.close();

	}

	/**
	 * 添加一条Feed
	 * 
	 * @param feedXmlUrl
	 * @param title
	 * @param category
	 */
	public void addFeed(String feedXmlUrl, String title, String category) {
		ContentValues feed = new ContentValues();
		feed.put(FeedsHelper.c_title, title);
		feed.put(FeedsHelper.c_text, "");
		feed.put(FeedsHelper.c_htmlUrl, "");
		feed.put(FeedsHelper.c_xmlUrl, feedXmlUrl);
		feed.put(FeedsHelper.c_catTitle, category);
		feed.put(FeedsHelper.c_charset, "utf-8");
		feed.put(FeedsHelper.c_icon, R.drawable.rss_recent_update);
		
		FeedsHelper helper = new FeedsHelper();
		helper.addRecord(feed);
		helper.updateCategoryStatus();// 为Category表重新统计Feed条数。
		helper.close();
		
		google.addFeed(feedXmlUrl, title, category);
	}
	
	/**
	 * 删除一条feed
	 * @param feedXmlUrl
	 */
	public void deleteFeed(String feedXmlUrl){
		FeedsHelper helper = new FeedsHelper();
		helper.deleteRecord(feedXmlUrl);
		helper.updateCategoryStatus();// 为Category表重新统计Feed条数。
		helper.close();
		
		google.deleteFeed(feedXmlUrl);
	}
	/**
	 *更新所有Feed 
	 */
	public void updateAllArticles() {
		FeedsHelper helper = new FeedsHelper();
		updateArticles(helper.getAllFeedsXmlUrl());
		helper.close();
	}
	
	/**
	 * 更新指定目录下的所有feed
	 * 
	 * @param catTitle
	 */
	public void updateArticlesByCat(String catTitle) {
		if(catTitle == null) return;
		FeedsHelper helper = new FeedsHelper();
		updateArticles(helper.getFeedsXmlUrlByCategory(catTitle));
		helper.close();
	}
	
	public void updateArticlesByfeed(String feedXmlUrl) {
		if(feedXmlUrl == null) return;
		ArrayList<String> feeds = new ArrayList<String>();
		feeds.add(feedXmlUrl);
		updateArticles(feeds);
	}

	/**
	 * 更新文章
	 */
	private void updateArticles(ArrayList<String> feeds) {

		Log.i(TAG, "start update articles");

		if (feeds == null || feeds.size() == 0) {
			return;
		}

		FeedsHelper fHelper = new FeedsHelper();
		ArticlesHelper aHelper = new ArticlesHelper();
		// helper.getRecords(catTitle);
		
		int length = feeds.size();
		Log.i(TAG, "update feed number " + length);

		String feedXmlUrl = null;
		for (int i = 0; i < length; i++) {

			Log.i(TAG, "updating feed no. " + i);

			// 获取数据
			feedXmlUrl = feeds.get(i);

			//如果Feed的更新时间为空，则取指定条数据。
			String updateTime = fHelper.getFeedUpdateTime(feedXmlUrl);
			String timeStamp = null;
			if(updateTime != null) {
				timeStamp = String.valueOf(Utils.init().getTimestamp(updateTime));
			}
			
			ArrayList<ContentValues> articles = google.downLoadFeedContent(feedXmlUrl, timeStamp, 10);
			if(articles == null) continue;

			// 更新文章
			for (ContentValues article : articles) {
				String articleTitle = (String) article.get(ArticlesHelper.c_title);
				// 如果文章不存在则添加
				if (!aHelper.isExistsArticle(articleTitle)) {
					aHelper.addRecord(article);
				}
			}
			
		}

		aHelper.updateFeedsStatus();// 添加完文章后，为Feed表重新统计文章数目。
		aHelper.close();
		fHelper.close();

		Log.i(TAG, "update articles finished!");

	}
	
}
