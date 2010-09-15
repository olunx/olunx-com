package com.olunx.activity;

import com.olunx.R;
import com.olunx.db.CategoryHelper;
import com.olunx.db.FeedsHelper;
import com.olunx.reader.Update;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class FeedList extends Activity {

	private Cursor cursor;
	private final String TAG = "com.olunx.activity.FeedList";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.feed_list);

		// createView();
	}

	private void createView() {
		GridView gridview = (GridView) findViewById(R.id.GridView01);

		FeedsHelper helper = new FeedsHelper();
		String title = this.getIntent().getStringExtra(CategoryHelper.c_title);
		cursor = helper.getFeedsByCategory(title);

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.main_item, cursor, new String[] { FeedsHelper.c_icon,
				FeedsHelper.c_title, FeedsHelper.c_articleCount }, new int[] { R.id.ItemImage, R.id.ItemText, R.id.ItemNum });

		gridview.setAdapter(adapter);
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long rowId) {
				SQLiteCursor cursor = (SQLiteCursor) arg0.getItemAtPosition(position);
				String xmlUrl = cursor.getString(cursor.getColumnIndex(FeedsHelper.c_xmlUrl));
				String charset = cursor.getString(cursor.getColumnIndex(FeedsHelper.c_charset));
				Log.i(TAG, xmlUrl);
				Intent i = new Intent();
				i.putExtra(FeedsHelper.c_xmlUrl, xmlUrl);// Feed地址
				i.putExtra(FeedsHelper.c_charset, charset);// 内容编码
				i.setClass(FeedList.this, ArticleList.class);
				cursor.close();
				FeedList.this.startActivity(i);
			}
		});
		
		//长按事件
		gridview.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				SQLiteCursor cursor = (SQLiteCursor) parent.getItemAtPosition(position);
				final String xmlUrl = cursor.getString(cursor.getColumnIndex(FeedsHelper.c_xmlUrl));
				Log.i(TAG, xmlUrl);
				Toast.makeText(FeedList.this, "开始更新...", Toast.LENGTH_SHORT).show();
				new Thread() {
					public void run() {
						new Update().updateArticlesByfeed(xmlUrl);
						Log.i(TAG, "update finish");
					}
				}.start();
				cursor.close();
				return false;
			}});

		helper.close();
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "onResume()");
		createView();
		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.i(TAG, "onPause()");
		if (cursor != null) {
			cursor.close();
		}
		super.onPause();
	}

}
