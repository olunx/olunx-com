package com.olunx.irss.reader;

import java.util.ArrayList;
import java.util.List;

import com.olunx.GoogleReaderUtil;
import com.olunx.irss.R;
import com.olunx.irss.db.ArticlesHelper;
import com.olunx.irss.db.FeedsHelper;
import com.olunx.irss.util.Utils;

import android.content.ContentValues;
import android.util.Log;
import be.lechtitseb.google.reader.api.core.GoogleReader;
import be.lechtitseb.google.reader.api.core.GoogleReaderDataProvider;
import be.lechtitseb.google.reader.api.model.exception.AuthenticationException;
import be.lechtitseb.google.reader.api.model.exception.GoogleReaderException;
import be.lechtitseb.google.reader.api.model.item.Item;
import be.lechtitseb.google.reader.api.model.opml.Outline;

public class Google {

	private GoogleReader googleReader;
	private GoogleReaderDataProvider dataProvider;
	private Boolean logined;

	private final String TAG = "com.olunx.reader.Google";

	/**
	 * 登录
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public boolean login(String username, String password) {
		googleReader = new GoogleReader(username, password);
		logined = false;
		try {
			logined = googleReader.login();
			Log.i(TAG, "login success");
		} catch (AuthenticationException e) {
			Log.i(TAG, "login fail");
			return false;
		}
		dataProvider = googleReader.getApi();
		return logined;
	}

	/**
	 * 是否登录成功
	 * 
	 * @return
	 */
	public boolean isLongin() {
		return googleReader.isAuthenticated();
	}

	/**
	 * 注销
	 */
	public void logout() {
		if (logined) {
			googleReader.logout();
		}
	}

	/**
	 * 下载Feed列表
	 * 
	 * @return
	 */
	public ArrayList<ContentValues> downLoadAllFeeds() {
		if (dataProvider != null) {
			try {
				return this.parseOPMLSubscriptions(dataProvider.exportSubscriptionsToOPML());
			} catch (GoogleReaderException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 添加一条feed
	 * 
	 * @param feedUrl
	 * @param title
	 */
	public void addFeed(String feedXmlUrl, String title, String category) {
		if (dataProvider != null) {
			try {
				dataProvider.editSubscriptionLabel(feedXmlUrl, title, category, true);
			} catch (GoogleReaderException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 删除一条feed
	 * 
	 * @param feedUrl
	 */
	public void deleteFeed(String feedXmlUrl) {
		if (dataProvider != null) {
			try {
				dataProvider.removeSubscription(feedXmlUrl);
			} catch (GoogleReaderException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * 根据时间获取文章数据，如果时间为空则返回指定条数的数据。
	 * 
	 * @param feedXmlUrl
	 * @param timeStamp
	 * @param articleCount
	 */
	public ArrayList<ContentValues> downLoadFeedContent(String feedXmlUrl, String timeStamp, int articleCount) {

		String feedUrl = "feed/" + feedXmlUrl;
		String content = "";

		if (dataProvider != null) {
			try {
				if (timeStamp == null) {
					content = dataProvider.getFeedItems(feedUrl, (articleCount <= 0 ? 5 : articleCount));
				} else {
					content = dataProvider.getFeedItemsFromDate(feedUrl, timeStamp);
				}
			} catch (GoogleReaderException e) {
				e.printStackTrace();
			}

			// Utils.init().copyFile(content, Config.SDCARD_PATH +
			// System.currentTimeMillis() + "_feed.xml");
			// Log.i(TAG, "feed content: " + content);
			Log.i(TAG, "parse feed: " + feedUrl);
			return parseArticleXml(content, feedXmlUrl);// 解析rss内容
		}

		return null;
	}

	/**
	 * 处理OMPL文件内容，返回ArrayList<ContentValues>。内容为feed列表。
	 * 
	 * @param xmlContent
	 */
	public ArrayList<ContentValues> parseOPMLSubscriptions(String xmlContent) {

		ArrayList<ContentValues> array = new ArrayList<ContentValues>();

		List<Outline> outlines = GoogleReaderUtil.parseOPMLSubscriptions(xmlContent);

		for (Outline o : outlines) {

			ContentValues feed = new ContentValues();
			feed.put(FeedsHelper.c_title, o.getTitle());
			feed.put(FeedsHelper.c_text, o.getText());
			feed.put(FeedsHelper.c_htmlUrl, o.getHtmlUrl());
			feed.put(FeedsHelper.c_xmlUrl, o.getXmlUrl());
			feed.put(FeedsHelper.c_catTitle, o.getCategory());
			feed.put(FeedsHelper.c_charset, "utf-8");
			feed.put(FeedsHelper.c_icon, R.drawable.rss_recent_update);

			array.add(feed);
		}

		return array;
	}

	/**
	 * 解析文章数据
	 * 
	 * @param source
	 * @param feedUrl
	 * @return
	 */
	private ArrayList<ContentValues> parseArticleXml(String source, String feedUrl) {
		ArrayList<ContentValues> articles = new ArrayList<ContentValues>();

		ArrayList<Item> items = GoogleReaderUtil.parseFeedContent(source);

		for (Item item : items) {
			ContentValues article = new ContentValues();
			article.put(ArticlesHelper.c_id, System.currentTimeMillis());
			article.put(ArticlesHelper.c_title, item.getTitle());
			article.put(ArticlesHelper.c_link, item.getUrl());
			article.put(ArticlesHelper.c_content, Utils.init().parseTextToHtmlForWebview("utf-8", item.getTitle(), item.getContent(),
					item.getContentTextDirection()));
			article.put(ArticlesHelper.c_desc, item.getContentTextDirection());
			article.put(ArticlesHelper.c_publishTime, String.valueOf(item.getPublishedOn()));
			article.put(ArticlesHelper.c_unread, "true");
			article.put(ArticlesHelper.c_stared, "false");
			article.put(ArticlesHelper.c_feedXmlUrl, feedUrl);
			articles.add(article);
		}

		return articles;
	}
}
