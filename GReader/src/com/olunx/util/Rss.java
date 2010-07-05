package com.olunx.util;

import be.lechtitseb.google.reader.api.core.GoogleReader;
import be.lechtitseb.google.reader.api.core.GoogleReaderDataProvider;
import be.lechtitseb.google.reader.api.model.exception.AuthenticationException;
import be.lechtitseb.google.reader.api.model.exception.GoogleReaderException;

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
	private String username;
	private String password;

	public Rss(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}

	public boolean login(String username, String password) {
		googleReader = new GoogleReader(username, password);
		logined = false;
		try {
			logined = googleReader.login();
		} catch (AuthenticationException e) {
			e.printStackTrace();
		}
		dataProvider = googleReader.getApi();
		return logined;
	}
	
	public void logout() {
		if(logined) {
			googleReader.logout();
		}
	}
	
	public StringBuffer getCategory() {
		StringBuffer sb = new StringBuffer();
		String labels;
		if(dataProvider != null) {
			try {
				labels = dataProvider.getLabels();
			} catch (GoogleReaderException e) {
				e.printStackTrace();
			}
		}
		return sb;
	}
}
