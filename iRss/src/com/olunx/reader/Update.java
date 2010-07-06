package com.olunx.reader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
		JSONArray articles = null;
		JSONObject article = null;
		JSONArray feeds = fHelper.getAllFeedsXmlUrl();

		int length = feeds.length();
		System.out.println("update feed number " + length);

		for (int i = 0; i < length; i++) {

			System.out.println("updating feed NO." + i);

			try {
				articles = (JSONArray) rss.getFeedContent(feeds.get(i).toString(), null).get(FeedsHelper.c_articles);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			for (int j = 0; j < articles.length(); j++) {
				try {
					article = (JSONObject) articles.get(i);
					articleTitle = article.getString(ArticlesHelper.c_title);

					// 如果文章不存在则添加
					if (!aHelper.isExistsArticle(articleTitle)) {
						aHelper.addRecord(article);
						System.out.println("add article...");
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

		aHelper.updateFeeds();// 添加完文章后，为Feed表重新统计文章数目。
		aHelper.close();
		fHelper.close();

		System.out.println("update finished!");

	}
}
