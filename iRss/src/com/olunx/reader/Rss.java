package com.olunx.reader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.olunx.db.ArticlesHelper;
import com.olunx.db.FeedsHelper;
import com.olunx.util.Utils;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

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
	 * 返回Feed列表
	 * 
	 * @return
	 */
	public JSONArray getCategory() {

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
	 * 处理OMPL文件内容，返回JSONArray。内容为feed列表。
	 * 
	 * @param xmlContent
	 * @return
	 */
	public JSONArray parseOPMLSubscriptions(String xmlContent) {
		JSONArray array = new JSONArray();

		JSONObject feed = null;

		List<Outline> outlines = null;
		try {
			outlines = GoogleReaderUtil.parseOPMLSubscriptions(xmlContent);
		} catch (GoogleReaderException e) {
			e.printStackTrace();
		}

		for (Outline o : outlines) {

			// 未分类的Feed
			if (o.getXmlUrl() != null) {
				feed = new JSONObject();
				try {
					feed.put(FeedsHelper.c_title, o.getTitle());
					feed.put(FeedsHelper.c_text, o.getText());
					feed.put(FeedsHelper.c_htmlUrl, o.getHtmlUrl());
					feed.put(FeedsHelper.c_xmlUrl, o.getXmlUrl());
					feed.put(FeedsHelper.c_catTitle, "未分类");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				for (Outline c : o.getChilds()) {
					// System.out.println("child： " + c.getTitle() + "  " +
					// c.getHtmlUrl());
					feed = new JSONObject();
					try {
						feed.put(FeedsHelper.c_title, c.getTitle());
						feed.put(FeedsHelper.c_text, c.getText());
						feed.put(FeedsHelper.c_htmlUrl, c.getHtmlUrl());
						feed.put(FeedsHelper.c_xmlUrl, c.getXmlUrl());
						feed.put(FeedsHelper.c_catTitle, o.getTitle());
					} catch (JSONException e) {
						e.printStackTrace();
					}
					array.put(feed);
					System.out.println("feed:" + feed.toString());
				}
			}

			array.put(feed);
			System.out.println("feed:" + feed.toString());
		}

		return array;
	}

	/**
	 * 
	 * 根据时间获取文章数据，如果时间为空则返回指定条数的数据。
	 * 
	 * @param feedUrl
	 * @param fromDate
	 */
	public JSONObject getFeedContent(String feedUrl, String fromDate) {

		String feed = "feed/" + feedUrl;
		String content = "";

		if (dataProvider != null) {
			try {
				if (fromDate == null) {
					content = dataProvider.getFeedItems(feed, 5);
				} else {
					content = dataProvider.getFeedItemsFromDate(feed, String.valueOf(Utils.init().getTimestamp(fromDate)));
				}
			} catch (GoogleReaderException e) {
				e.printStackTrace();
			}

			System.out.println("parseRss： " + feed);
			return parseRss(content);// 解析rss内容
		}

		return null;
	}

	/**
	 * 解析rss内容
	 * 
	 * @param source
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSONObject parseRss(String source) {

		JSONObject singleFeed = new JSONObject();

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

		try {
			singleFeed.put(FeedsHelper.c_charset, reader.getEncoding());
			singleFeed.put(FeedsHelper.c_rssType, feed.getFeedType());
		} catch (JSONException e2) {
			e2.printStackTrace();
		}
		// System.out.println("Rss源的编码格式为：" + reader.getEncoding());
		// System.out.println("Rss源类型：" + feed.getFeedType());
		// feed.getCategories();

		JSONArray articles = new JSONArray();// 文章数组
		JSONObject article;// 单篇文章
		// 得到Rss新闻中子项列表
		List entries = feed.getEntries();

		String title, desc, author, link;
		Date date;
		StringBuilder content;
		SyndEntry entry;
		SyndContent description;
		List<SyndContent> contents;
		System.out.println("entries.size() " + entries.size());
		for (int i = 0; i < entries.size(); i++) {
			entry = (SyndEntry) entries.get(i);
			article = new JSONObject();

			try {
				// 标题
				title = entry.getTitle();
				if (title != null) {
					article.put(ArticlesHelper.c_title, title.trim());
				}else {
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
				// 描述
				description = entry.getDescription();
				if (description != null) {
					desc = description.getValue();
					if (desc != null) {
						article.put(ArticlesHelper.c_desc, desc.trim());
					}
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
				// 内容
				content = new StringBuilder();
				contents = entry.getContents();
				for (SyndContent c : contents) {
					c.getType();
					content.append(c.getValue());
				}
				article.put(ArticlesHelper.c_content, content.toString().trim());
				// 作者
				author = entry.getAuthor();
				if (author != null) {
					article.put(ArticlesHelper.c_author, author.trim());
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			articles.put(article);
			System.out.println("put article " + i);
		}

		try {
			singleFeed.put(FeedsHelper.c_articles, articles);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return singleFeed;
	}
}
