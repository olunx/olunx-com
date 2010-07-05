package com.olunx.db;

import android.database.sqlite.SQLiteDatabase;

public interface IHelper {
	
	/**
	 * @return
	 */
	SQLiteDatabase getDB();
	
	/**
	 * 关闭数据库
	 */
	void close();
	
	/**
	 * 创建数据表
	 */
	void createTable();
	
	/**
	 * 删除数据表
	 */
	void dropTable();
}
