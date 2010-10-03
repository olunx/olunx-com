package com.olunx.irss.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

	public static final String SDCARD_PATH = "/sdcard/irss/";
	private static final String SDCARD_BACKUP_PATH = "/sdcard/irss/backup/";
	public static final String SDCARD_IMAGES_PATH = "/sdcard/irss/images/";

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
	synchronized public void setCon(String key, String value) {
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
	 * @param username
	 * @param password
	 */
	public void setAccount(String username, String password) {
		this.setCon("username", username);
		this.setCon("password", password);
		this.setAccountInputed("true");
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
	public void setAccountInputed(String value) {
		this.setCon("account_inputted", value);
	}

	public boolean isAccountInputted() {
		return Boolean.parseBoolean(getCon("account_inputted", "false"));
	}

	/**
	 * 文章字体大小
	 * 
	 * @param value
	 */
	public void setArticleFontSize(String value) {
		this.setCon("article_font_size", value);
	}

	public String getArticleFontSize() {
		return this.getCon("article_font_size", "18px");
	}

	/**
	 * 文章字体颜色
	 * 
	 * @param value
	 */
	public void setArticleFontColor(String value) {
		this.setCon("article_font_color", value);
	}

	public String getArticleFontColor() {
		return this.getCon("article_font_color", "#000000");
	}

	/**
	 * 文章背景颜色
	 * 
	 * @param value
	 */
	public void setArticleBgColor(String value) {
		this.setCon("article_bg_color", value);
	}

	public String getArticleBgColor() {
		return this.getCon("article_bg_color", "#ffffff");
	}

	/**
	 * 文章配色方案
	 * 
	 * @param value
	 */
	public void setSysFontStyle(String value) {
		this.setCon("sys_font_style", value);
	}

	public String getSysFontStyle() {
		return this.getCon("sys_font_style", "白天模式");
	}

	/**
	 * 设置是否为离线阅读
	 * 
	 * @param value
	 */
	public void setOffLineReadMode(boolean value) {
		this.setCon("off_line_read_mode", String.valueOf(value));
	}

	public boolean isOffLineReadMode() {
		return Boolean.valueOf(getCon("off_line_read_mode", "false"));
	}

	/**
	 * 设置文章保存时间
	 * 
	 * @param days
	 */
	public void setArticleDataStoreTime(String days) {
		this.setCon("article_data_store_time", days);
	}

	public String getArticleDataStoreTime() {
		return this.getCon("article_data_store_time", "7");
	}
}
