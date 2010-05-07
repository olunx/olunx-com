/*
 *author:olunx
 *date:2009-10-11
 */

package com.olunx.db;

import com.olunx.util.Config;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	public DBHelper(Context context) {
		super(context, Config.FILENAME_DATABASE, null, 1);
	}

//	@Override
//	public synchronized SQLiteDatabase getWritableDatabase() {
//		File file = new File("/sdcard/iremember/data.db");
//		Log.i("sqlite file ",file.getPath());
//		Log.i("sqlite file exists", String.valueOf(file.exists()));
//		if(!file.exists()) {
//			Log.i("sqlite file mkdir", "sqlite file mkdir");
//			file.mkdirs();
//			try {
//				file.createNewFile();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		return SQLiteDatabase.openOrCreateDatabase(file, null);
//	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
	}

}
