package com.olunx.irss.reader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.olunx.irss.R;
import com.olunx.irss.db.ArticlesHelper;
import com.olunx.irss.db.FeedsHelper;
import com.olunx.irss.util.Utils;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import android.content.ContentValues;
import android.util.Log;
import be.lechtitseb.google.reader.api.core.GoogleReader;
import be.lechtitseb.google.reader.api.core.GoogleReaderDataProvider;
import be.lechtitseb.google.reader.api.model.exception.AuthenticationException;
import be.lechtitseb.google.reader.api.model.exception.GoogleReaderException;
import be.lechtitseb.google.reader.api.model.opml.Outline;
import be.lechtitseb.google.reader.api.util.GoogleReaderUtil;

public class Rss {

	private GoogleReader googleReader;
	private GoogleReaderDataProvider dataProvider;
	private Boolean logined;

	private final String TAG = "com.olunx.reader.Rss";

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
		} catch (AuthenticationException e) {
			// e.printStackTrace();
			System.out.println("login faild");
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
	public void addFeed(String feedUrl, String title) {
		if (dataProvider != null) {
			try {
				dataProvider.addSubscription(feedUrl, title);
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
	public void removeFeed(String feedUrl) {
		if (dataProvider != null) {
			try {
				dataProvider.removeSubscription(feedUrl);
			} catch (GoogleReaderException e) {
				e.printStackTrace();
			}
		}
	}
	
	

	/**
	 * 处理OMPL文件内容，返回ArrayList<Map<String, String>>。内容为feed列表。
	 * 
	 * @param xmlContent
	 */
	public ArrayList<ContentValues> parseOPMLSubscriptions(String xmlContent) {

		ArrayList<ContentValues> array = new ArrayList<ContentValues>();

		ContentValues feed = null;

		List<Outline> outlines = null;
		try {
			outlines = GoogleReaderUtil.parseOPMLSubscriptions(xmlContent);
		} catch (GoogleReaderException e) {
			e.printStackTrace();
		}

		for (Outline o : outlines) {

			// 未分类的Feed
			if (o.getXmlUrl() != null) {
				feed = new ContentValues();
				feed.put(FeedsHelper.c_title, o.getTitle());
				feed.put(FeedsHelper.c_text, o.getText());
				feed.put(FeedsHelper.c_htmlUrl, o.getHtmlUrl());
				feed.put(FeedsHelper.c_xmlUrl, o.getXmlUrl());
				feed.put(FeedsHelper.c_catTitle, "未分类");
				feed.put(FeedsHelper.c_icon, R.drawable.icon);
			} else {
				for (Outline c : o.getChilds()) {
					feed = new ContentValues();
					feed.put(FeedsHelper.c_title, c.getTitle());
					feed.put(FeedsHelper.c_text, c.getText());
					feed.put(FeedsHelper.c_htmlUrl, c.getHtmlUrl());
					feed.put(FeedsHelper.c_xmlUrl, c.getXmlUrl());
					feed.put(FeedsHelper.c_catTitle, o.getTitle());
					feed.put(FeedsHelper.c_icon, R.drawable.icon);
					array.add(feed);
				}
			}
			array.add(feed);
		}

		return array;
	}

	/**
	 * 
	 * 根据时间获取文章数据，如果时间为空则返回指定条数的数据。
	 * 
	 * @param feedXmlUrl
	 * @param fromDate
	 */
	public HashMap<String, Object> downLoadFeedContent(String feedXmlUrl, String fromDate, int articleCount) {

		String feedUrl = "feed/" + feedXmlUrl;
		String content = "";

		if (dataProvider != null) {
			try {
				if (fromDate == null) {
					content = dataProvider.getFeedItems(feedUrl, (articleCount <= 0 ? 5 : articleCount));
				} else {
					content = dataProvider.getFeedItemsFromDate(feedUrl, String.valueOf(Utils.init().getTimestamp(fromDate)));
				}
			} catch (GoogleReaderException e) {
				e.printStackTrace();
			}

			// Log.i(TAG, "feed content: " + content);
			Log.i(TAG, "parse feed: " + feedUrl);
			return parseFeed(content, feedXmlUrl);// 解析rss内容
		}

		return null;
	}

	/**
	 * 解析rss内容
	 * 
	 * @param source
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private HashMap<String, Object> parseFeed(String source, String feedUrl) {

		HashMap<String, Object> singleFeed = new HashMap<String, Object>();
		String feedCharset = null;

		XmlReader reader = null;
		try {
			reader = new XmlReader(new ByteArrayInputStream(source.getBytes()));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		SyndFeed feed = null;
		try {
			feed = new SyndFeedInput().build(reader);
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch (FeedException e1) {
			e1.printStackTrace();
		}

		feedCharset = reader.getEncoding();
		singleFeed.put(FeedsHelper.c_charset, feedCharset);
		singleFeed.put(FeedsHelper.c_rssType, feed.getFeedType());

		ContentValues[] articles;// 文章数组
		ContentValues article;// 单篇文章
		// 得到Rss新闻中子项列表
		List entries = feed.getEntries();

		String title, desc = null, parsedContent, author, link;
		Date date;
		StringBuilder content;
		SyndEntry entry;
		SyndContent description;
		List<SyndContent> contents;
		int entriesSize = entries.size();
		articles = new ContentValues[entriesSize];
		for (int i = 0; i < entriesSize; i++) {
			entry = (SyndEntry) entries.get(i);
			article = new ContentValues();

			article.put(ArticlesHelper.c_feedXmlUrl, feedUrl);
			// 标题
			title = entry.getTitle();
			if (title != null) {
				article.put(ArticlesHelper.c_title, title.trim());
				Log.i(TAG, "title: " + title);
			} else {
				article.put(ArticlesHelper.c_title, "无标题");
			}
			// // URI
			// uri = entry.getUri();
			// if (uri != null) {
			// uri.trim();
			// }
			// link
			link = entry.getLink();
			if (link != null) {
				article.put(ArticlesHelper.c_link, link.trim());
			}
			// 发表日期
			date = entry.getPublishedDate();
			if (date != null) {
				article.put(ArticlesHelper.c_publishTime, Utils.init().getCstTime(date));
			}
			// // 目录
			// category = new StringBuilder();
			// for (Object e : entry.getCategories()) {
			// category.append(((SyndCategory) e).getName());
			// }
			// 描述
			description = entry.getDescription();
			if (description != null) {
				desc = description.getValue();
				// Log.i(TAG, "desc: " + desc);
			}
			// 内容
			content = new StringBuilder();
			contents = entry.getContents();
			for (SyndContent c : contents) {
				c.getType();
				content.append(c.getValue());
			}

			// 处理content格式问题
			parsedContent = Utils.init().parseTextToHtmlForWebview(feedCharset, title, content.toString(), desc);
			article.put(ArticlesHelper.c_content, parsedContent);
			Log.i(TAG, "content: " + parsedContent);

			// 作者
			author = entry.getAuthor();
			if (author != null) {
				article.put(ArticlesHelper.c_author, author.trim());
			}

			articles[i] = article;
			Log.i(TAG, "add article: " + i);
		}

		singleFeed.put(FeedsHelper.c_articles, articles);

		return singleFeed;
	}
}
