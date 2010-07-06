package com.olunx.util;

import android.content.Context;
import android.content.SharedPreferences;

public class Config {

	private static Config c = null;

	private static final String FILENAME_CONFIG = "irss";
	public static final String FILENAME_DATABASE = "irss.db";

	private static final String SYSTEM_PATH = "/data/data/com.olunx/";
	private static final String SDCARD_PATH = "/sdcard/irss/";

	public static final String DATABASE_FILE = FILENAME_DATABASE;
	public static final String FILE_SYSTEM_DATABASE = SYSTEM_PATH + "databases/" + DATABASE_FILE;
	public static final String FILE_SDCARD_DATABASE = SDCARD_PATH + DATABASE_FILE;

	public static final String CONFIG_FILE = Config.FILENAME_CONFIG + ".xml";
	public static final String FILE_SYSTEM_CONFIG = SYSTEM_PATH + "shared_prefs/" + CONFIG_FILE;
	public static final String FILE_SDCARD_CONFIG = SDCARD_PATH + CONFIG_FILE;

	public static Config init(Context context) {
		if (c == null) {
			c = new Config();
		}
		if (sp == null) {
			sp = context.getSharedPreferences(FILENAME_CONFIG, 0);
		}
		return c;
	}

	private static SharedPreferences sp;

	/**
	 * 设置配置
	 * 
	 * @param context
	 * @param key
	 * @param value
	 */
	public void setCon(String key, String value) {
		sp.edit().putString(key, String.valueOf(value)).commit();
	}

	public void setCon(String key, boolean value) {
		sp.edit().putBoolean(key, value).commit();
	}

	/**
	 * 获取配置
	 * 
	 * @param context
	 * @param key
	 * @param defValue
	 * @return
	 */
	public String getCon(String key, String defValue) {
		return sp.getString(key, defValue);
	}
	
	public boolean getCon(String key, boolean defValue){
		return sp.getBoolean(key, defValue);
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
		this.setCon("account_inputted", true);
	}

	public String getUsername() {
		return this.getCon("username", "");
	}

	public String getPassword() {
		return this.getCon("password", "");
	}
	
	/**
	 * 是否已经输入账号
	 * @return
	 */
	public boolean isAccountInputted() {
		return this.getCon("account_inputted", false);
	}

}
