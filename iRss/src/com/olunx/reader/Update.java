package com.olunx.reader;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;

import com.olunx.db.ArticlesHelper;
import com.olunx.db.FeedsHelper;

public class Update {

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

		JSONArray array = rss.getCategory();

		// 如果没有数据，则返回。
		if (array == null)
			return;

		FeedsHelper helper = new FeedsHelper();

		int len = array.length();
		System.out.println("feed count" + len);
		JSONObject object;
		for (int i = 0; i < len; i++) {
			try {
				object = (JSONObject) array.get(i);
				System.out.println("object:" + i + "  " + object.toString());
				if (helper.isExistsFeed(object.get(FeedsHelper.c_xmlUrl).toString())) {
					helper.updateRecord(object);
				} else {
					helper.addRecord(object);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		helper.updateCategory();// 添加完Feed后，为Category表重新统计Feed条数。
		helper.close();

		// new ArticlesHelper().updateFeeds();
		updateAllArticles();
	}

	/**
	 * 更新文章
	 */
	public void updateAllArticles() {

		System.out.println("start update all articles");

		FeedsHelper fHelper = new FeedsHelper();
		ArticlesHelper aHelper = new ArticlesHelper();
		// helper.getRecords(catTitle);
		String articleTitle = null;
		ContentValues[] articles = null;
		ContentValues article = null;
		JSONArray feeds = fHelper.getAllFeedsXmlUrl();

		int length = feeds.length();
		System.out.println("update feed number " + length);

		HashMap<String, Object> data = null;
		String charset;
		String feedXmlUrl = null;
		for (int i = 0; i < length; i++) {

			System.out.println("updating feed NO." + i);

			//获取数据
			try {
				feedXmlUrl = feeds.get(i).toString();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			data = rss.getFeedContent(feedXmlUrl, null);
			
			//更新Feed的编码
			charset = (String)data.get(FeedsHelper.c_charset);
			fHelper.updateFeedCharset(feedXmlUrl, charset);
			
			//更新文章
			articles = (ContentValues[]) data.get(FeedsHelper.c_articles);
			System.out.println("articles: " + articles);
			System.out.println("articles.length(): " + articles.length);
			for (int j = 0; j < articles.length; j++) {
				article = articles[j];

				System.out.println("article title: " + articleTitle);
				articleTitle = (String) article.get(ArticlesHelper.c_title);

				// 如果文章不存在则添加
				if (!aHelper.isExistsArticle(articleTitle)) {
					aHelper.addRecord(article);
					System.out.println("add article...");
				}

			}

		}

		aHelper.updateFeeds();// 添加完文章后，为Feed表重新统计文章数目。
		aHelper.close();
		fHelper.close();

		System.out.println("update finished!");

	}
}
