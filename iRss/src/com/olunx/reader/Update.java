package com.olunx.reader;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.util.Log;

import com.olunx.db.ArticlesHelper;
import com.olunx.db.FeedsHelper;

public class Update {

	private final String TAG = "com.olunx.reader.Update";

	private Rss rss;

	public Update() {
		super();
		if (rss == null) {
			rss = new Rss();
			rss.login("olunxs@gmail.com", "646895472");
		} else if (!rss.isLongin()) {
			rss.login("olunxs@gmail.com", "646895472");
		}
	}

	/**
	 * 更新Feed条目
	 */
	public void updateFeeds() {

		ArrayList<ContentValues> array = rss.downLoadAllFeeds();

		// 如果没有数据，则返回。
		if (array == null)
			return;

		FeedsHelper helper = new FeedsHelper();

		int len = array.size();
		System.out.println("feed count" + len);
		ContentValues mValues;
		for (int i = 0; i < len; i++) {
			mValues = array.get(i);
			System.out.println("object:" + i + "  " + mValues.toString());
			if (helper.isExistsFeed(mValues.get(FeedsHelper.c_xmlUrl).toString())) {
				helper.updateRecord(mValues);
			} else {
				helper.addRecord(mValues);
			}
		}
		helper.updateCategoryStatus();// 添加完Feed后，为Category表重新统计Feed条数。
		helper.close();

		// new ArticlesHelper().updateFeeds();
		// updateAllArticles();
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
		String articleTitle = null;
		ContentValues[] articles = null;
		ContentValues article = null;

		int length = feeds.size();
		Log.i(TAG, "update feed number " + length);

		HashMap<String, Object> data = null;
		String charset;
		String feedXmlUrl = null;
		for (int i = 0; i < length; i++) {

			Log.i(TAG, "updating feed no. " + i);

			// 获取数据
			feedXmlUrl = feeds.get(i);

			data = rss.downLoadFeedContent(feedXmlUrl, null, 2);

			// 更新Feed的编码
			charset = (String) data.get(FeedsHelper.c_charset);
			fHelper.updateFeedCharset(feedXmlUrl, charset);

			// 更新文章
			articles = (ContentValues[]) data.get(FeedsHelper.c_articles);
			for (int j = 0; j < articles.length; j++) {
				article = articles[j];

				articleTitle = (String) article.get(ArticlesHelper.c_title);

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
