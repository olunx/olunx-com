package com.olunx.db;

import android.database.sqlite.SQLiteDatabase;

public interface IHelper {
	
	SQLiteDatabase getDB();
	
	void close();
	
	void createTable();
	
	void dropTable();
}
