package com.olunx.reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import be.lechtitseb.google.reader.api.core.GoogleReader;
import be.lechtitseb.google.reader.api.core.GoogleReaderDataProvider;
import be.lechtitseb.google.reader.api.model.exception.AuthenticationException;
import be.lechtitseb.google.reader.api.model.exception.GoogleReaderException;
import be.lechtitseb.google.reader.api.model.opml.Outline;
import be.lechtitseb.google.reader.api.util.GoogleReaderUtil;

public class Rss {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

	private GoogleReader googleReader;
	private GoogleReaderDataProvider dataProvider;
	private Boolean logined;

	public boolean login(String username, String password) {
		googleReader = new GoogleReader(username, password);
		logined = false;
		try {
			logined = googleReader.login();
		} catch (AuthenticationException e) {
//			e.printStackTrace();
			System.out.println("login faild");
		}
		dataProvider = googleReader.getApi();
		return logined;
	}
	
	public void logout() {
		if(logined) {
			googleReader.logout();
		}
	}
	
	public ArrayList<HashMap<String,Object>> getCategory() {
		ArrayList<HashMap<String, Object>> result = null;
		
		if(dataProvider != null) {
			result = new ArrayList<HashMap<String, Object>>();
			HashMap<String, Object> map, child;
			
			List<Outline> outlines = null;
			try {
				outlines = GoogleReaderUtil.parseOPMLSubscriptions(dataProvider.exportSubscriptionsToOPML());
			} catch (GoogleReaderException e) {
				e.printStackTrace();
			}
			
			for(Outline o : outlines) {
				map = new HashMap<String, Object>();
				map.put("title", o.getTitle());
				map.put("text", o.getText());
				System.out.println("title： " + o.getTitle());
				for(Outline c : o.getChilds()) {
					System.out.println("child： " + c.getTitle() + "  " + c.getHtmlUrl());
					child = new HashMap<String, Object>();
					child.put("title", c.getTitle());
					child.put("title", c.getText());
					child.put("htmlurl", c.getHtmlUrl());
					child.put("xmlurl", c.getXmlUrl());
					map.put("feeds", child);
				}
				result.add(map);
			}
		}

		return result;
	}
}
