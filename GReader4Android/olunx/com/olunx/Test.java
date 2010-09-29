package com.olunx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import be.lechtitseb.google.reader.api.core.GoogleReader;
import be.lechtitseb.google.reader.api.core.GoogleReaderDataProvider;
import be.lechtitseb.google.reader.api.model.exception.AuthenticationException;
import be.lechtitseb.google.reader.api.model.exception.GoogleReaderException;
import be.lechtitseb.google.reader.api.model.feed.FeedDescriptor;
import be.lechtitseb.google.reader.api.model.item.Item;
import be.lechtitseb.google.reader.api.model.opml.Outline;

public class Test {

	/**
	 * @param args
	 * @throws GoogleReaderException 
	 * @throws AuthenticationException 
	 */
	public static void main(String[] args) throws GoogleReaderException, AuthenticationException {
		// TODO Auto-generated method stub

//		GoogleReader googleReader = new GoogleReader("olunxs@gmail.com", "646895472");
//		googleReader.login();
//		GoogleReaderDataProvider dataProvider = googleReader.getApi();
		
//		System.out.println(dataProvider.getSubscriptions());
		
//		List<FeedDescriptor> list = GoogleReaderUtil.getFeedDescriptorsFromXml(dataProvider.getSubscriptions());
//		
//		for(int i=0;i<list.size();i++) {
//			FeedDescriptor feed = list.get(i);
//			System.out.println(feed.getId());
//		}
		
//		System.out.println(dataProvider.exportSubscriptionsToOPML());
		
//		File file = new File("res/opml.xml");
//		
//		FileInputStream fis = null;
//		try {
//			fis = new FileInputStream(file);
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		ArrayList<Outline> outlines = GoogleReaderUtil.parseOPMLSubscriptions(fis);
//		for(Outline o : outlines) {
//			System.out.println(o.getTitle());
//			System.out.println(o.getXmlUrl());
//			System.out.println(o.getCategory());
//		}
		
		
		File file = new File("res/feed.xml");
		
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ArrayList<Item> items = GoogleReaderUtil.parseFeedContent(fis);
		for(Item i : items) {
			System.out.println(i.getTitle());
//			System.out.println(i.getUpdatedOn());
//			System.out.println(i.getUrl());
			System.out.println(i.getContent());
		}
	}

}
