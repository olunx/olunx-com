package com.olunx.util;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.olunx.util.Utils;
import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import be.lechtitseb.google.reader.api.core.GoogleReader;
import be.lechtitseb.google.reader.api.core.GoogleReaderDataProvider;
import be.lechtitseb.google.reader.api.model.authentication.GoogleCredentials;
import be.lechtitseb.google.reader.api.model.exception.AuthenticationException;
import be.lechtitseb.google.reader.api.model.exception.GoogleReaderException;
import be.lechtitseb.google.reader.api.model.feed.FeedDescriptor;
import be.lechtitseb.google.reader.api.model.format.OutputFormat;
import be.lechtitseb.google.reader.api.model.opml.Outline;
import be.lechtitseb.google.reader.api.model.preference.UserPreferences;
import be.lechtitseb.google.reader.api.model.user.UserInformation;
import be.lechtitseb.google.reader.api.util.GoogleReaderUtil;

/**
 * http://code.google.com/intl/ru-RU/apis/accounts/docs/AuthForInstalledApps.
 * html#AuthProcess
 * http://code.google.com/intl/ru-RU/apis/accounts/docs/OAuth.html
 * 
 * @author gizatullinm
 * 
 */
public class Test3 {

	public void testAuth() throws AuthenticationException, GoogleReaderException {

		GoogleReader googleReader = new GoogleReader("olunxs@gmail.com", "646895472");
		googleReader.login();
//		UserInformation info = googleReader.getUserInformation();
//		System.out.println(info.getUsername());
//		System.out.println(info.getUserId());
//		System.out.println(info.getUserProfileId());
//		System.out.println(info.getEmail());
//		UserPreferences pre = googleReader.getUserPreferences();
//		System.out.println(pre.getDisplayLanguage());
//		System.out.println(pre.getStartPage());
//		System.out.println(pre.getShuffleToken());
		
		GoogleReaderDataProvider grdp = googleReader.getApi();
//		System.out.println(grdp.getLabels());
//		GoogleCredentials gc = grdp.getCredentials();
//		System.out.println(grdp.getFeedItems("feed/http://www.ifanr.com/feed", 1));
//		File file = new File(grdp.getFeedItems("feed/http://www.ifanr.com/feed", 1));
//		System.out.println(grdp.getSubscriptions(OutputFormat.JSON));
//		System.out.println(grdp.exportSubscriptionsToOPML());
		String s = grdp.exportSubscriptionsToOPML();
//		System.out.println(s);
		List<Outline> outlines = GoogleReaderUtil.parseOPMLSubscriptions(s);
		
		for(Outline o : outlines) {
			System.out.println("目录： " + o.getTitle() + "  " + o.getText());
			for(Outline c : o.getChilds()) {
				System.out.println("条目： " + c.getTitle() + "  " + c.getHtmlUrl() + "  " + c.getXmlUrl());
			}
		}
//		String s = grdp.getFeedItems("feed/http://feed.kisshi.com/?n=10");
//		String s = grdp.getFeedItems("feed/http://api.live.net/Users(-527258525974762729)/Main?$format=rss20", 5);
//		String s = grdp.getFeedItemsFromDate("feed/http://feed.kisshi.com/", String.valueOf(Utils.init().getTimestamp("Sun Jul 04 09:46:27 CST 2010")));
//		ByteArrayInputStream stringInputStream = new ByteArrayInputStream(s.getBytes());
//		parseRss(stringInputStream);
//		List<FeedDescriptor> list = GoogleReaderUtil.getFeedDescriptorsFromXml(grdp.getSubscriptions());
//		for(FeedDescriptor f : list) {
//			System.out.println("id: " + f.getId());
//			System.out.println("title: " + f.getTitle());
//			System.out.println("目录: " + f.getCategories());
//		}
	}

	public void parseRss(InputStream is) {
		
		try {
			// 读取Rss源
			XmlReader reader = new XmlReader(is);

			System.out.println("Rss源的编码格式为：" + reader.getEncoding());
			
			SyndFeedInput input = new SyndFeedInput();
			// 得到SyndFeed对象，即得到Rss源里的所有信息
			SyndFeed feed = input.build(reader);
			// feed.getFeedType();

			System.out.println("Rss源类型：" + feed.getFeedType());
			// System.out.println(feed);
//			feed.getCategories();
			// 得到Rss新闻中子项列表
			List entries = feed.getEntries();
			System.out.println("信息条数：" + entries.size());
			// 循环得到每个子项信息
			for (int i = 0; i < entries.size(); i++) {
				SyndEntry entry = (SyndEntry) entries.get(i);

				// 标题、连接地址、标题简介、时间是一个Rss源项最基本的组成部分
				
				System.out.println("标题：" + entry.getTitle().trim());
				System.out.println("URI：" + entry.getUri());
				
				for(Object e : entry.getCategories()) {
					System.out.println("SyndCategory: " + ((SyndCategory)e).getName());
				}
				System.out.println("连接地址：" + entry.getLink());
				SyndContent description = entry.getDescription();
				if(description != null){
					System.out.println("标题简介：" + description.getValue());
				}
				System.out.println("发布时间：" + entry.getPublishedDate());
//				Pattern pattern = Pattern.compile("IMG[\\s\\S]*?src=\\\"?((http|https|ftp|rtsp|mms):(//|\\\\\\\\){1}((([A-Za-z0-9_-:])+[.])|([A-Za-z0-9_-.:])+[.]){1,}(net|com|cn|org|cc|tv|[0-9]{1,3})(\\S*/)((\\S)+[.]{1}(jpg|jpeg|gif|png){1}))(\\\"|>|\\\\s+)");
//				Matcher matcher;
				List<SyndContent> contents = entry.getContents();
				for(SyndContent c : contents) {
					System.out.println("类型：" + c.getType());
					System.out.println("内容：" + c.getValue());
//					matcher = pattern.matcher(c.getValue());
//					System.out.println("内容：" + matcher.group());
				}
				// 以下是Rss源可先的几个部分
				// System.out.println("标题的作者：" + entry.getAuthor());

				// 此标题所属的范畴
				// List categoryList = entry.getCategories();
				// if (categoryList != null) {
				// for (int m = 0; m < categoryList.size(); m++) {
				// SyndCategory category = (SyndCategory) categoryList.get(m);
				// System.out.println("此标题所属的范畴：" + category.getName());
				// }
				// }
				
				// 得到流媒体播放文件的信息列表
				List enclosureList = entry.getEnclosures();
				if (enclosureList != null) {
					for (int n = 0; n < enclosureList.size(); n++) {
						SyndEnclosure enclosure = (SyndEnclosure) enclosureList.get(n);
						System.out.println("流媒体播放文件：" + enclosure.getType());
					}
				}
				System.out.println();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws AuthenticationException, GoogleReaderException {
		Test3 test = new Test3();
		test.testAuth();
	}
}
