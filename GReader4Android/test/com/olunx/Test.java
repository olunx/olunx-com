package com.olunx;

import java.util.List;

import be.lechtitseb.google.reader.api.core.GoogleReader;
import be.lechtitseb.google.reader.api.core.GoogleReaderDataProvider;
import be.lechtitseb.google.reader.api.model.exception.AuthenticationException;
import be.lechtitseb.google.reader.api.model.exception.GoogleReaderException;
import be.lechtitseb.google.reader.api.model.feed.FeedDescriptor;
import be.lechtitseb.google.reader.api.util.GoogleReaderUtil;

public class Test {

	/**
	 * @param args
	 * @throws GoogleReaderException 
	 * @throws AuthenticationException 
	 */
	public static void main(String[] args) throws GoogleReaderException, AuthenticationException {
		// TODO Auto-generated method stub

		GoogleReader googleReader = new GoogleReader("olunxs@gmail.com", "646895472");
		googleReader.login();
		GoogleReaderDataProvider dataProvider = googleReader.getApi();
		
//		System.out.println(dataProvider.getSubscriptions());
		
		List<FeedDescriptor> list = GoogleReaderUtil.getFeedDescriptorsFromXml(dataProvider.getSubscriptions());
		
		for(int i=0;i<list.size();i++) {
			FeedDescriptor feed = list.get(i);
			System.out.println(feed.getId());
		}
		
//		System.out.println(dataProvider.exportSubscriptionsToOPML());
	}

}
