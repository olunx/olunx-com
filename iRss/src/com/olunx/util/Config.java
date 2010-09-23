package com.olunx.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

	private static final String SDCARD_PATH = "/sdcard/irss/";
	private static final String SDCARD_BACKUP_PATH = "/sdcard/irss/backup/";

	public static final String DATABASE_FILE = "data.db";
	public static final String FILE_SDCARD_DATABASE = SDCARD_PATH + DATABASE_FILE;
	public static final String BACKUP_FILE_SDCARD_DATABASE = SDCARD_BACKUP_PATH + DATABASE_FILE;

	public static final String CONFIG_FILE = "config.propertites";
	public static final String FILE_SDCARD_CONFIG = SDCARD_PATH + CONFIG_FILE;
	public static final String BACKUP_FILE_SDCARD_CONFIG = SDCARD_BACKUP_PATH + CONFIG_FILE;

	private static Config config = null;
	
	public static Properties p;

	public static Config init() {
		if (config == null) {
			config = new Config();
		}
		if (p == null) {
			Utils.init().createFileIfNotExist(FILE_SDCARD_CONFIG);
			p = new Properties();
			try {
				p.load(new BufferedInputStream(new FileInputStream(new File(FILE_SDCARD_CONFIG))));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return config;
	}

	/**
	 * 设置配置
	 * 
	 * @param key
	 * @param value
	 */
	public void setCon(String key, String value) {
		p.setProperty(key, value);
		// 保存配置文件
		try {
			p.store(new BufferedOutputStream(new FileOutputStream(new File(Config.FILE_SDCARD_CONFIG))), "");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取配置
	 * 
	 * @param key
	 * @param defValue
	 * @return
	 */
	public String getCon(String key, String defValue) {
		String value = p.getProperty(key);
		if (value == null) {
			return defValue;
		}
		return value;
	}
	/**
	 * 保存用户数据
	 * 
	 * @param context
	 * @param username
	 * @param password
	 */
	public void setAccount(String username, String password) {
		this.setCon("uaername", username);
		this.setCon("password", password);
		this.setCon("account_inputted", "true");
	}

	public String getUsername() {
		return this.getCon("username", "");
	}

	public String getPassword() {
		return this.getCon("password", "");
	}

	/**
	 * 是否已经输入账号
	 * 
	 * @return
	 */
	public boolean isAccountInputted() {
		return Boolean.parseBoolean(getCon("account_inputted", "false"));
	}

}
