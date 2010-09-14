package com.olunx.activity;

import com.olunx.db.ArticlesHelper;
import com.olunx.db.FeedsHelper;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class ArticleList extends Activity {

	private ListView listview;
	private Cursor cursor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		listview = new ListView(this);
		setContentView(listview);

		// createView();
	}

	private void createView() {

		ArticlesHelper helper = new ArticlesHelper();
		String url = this.getIntent().getStringExtra(FeedsHelper.c_xmlUrl);
		final String charset = this.getIntent().getStringExtra(FeedsHelper.c_charset);
		cursor = helper.getArticlesByXmlUrl(url);

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, cursor, new String[] {
				ArticlesHelper.c_title, ArticlesHelper.c_link }, new int[] { android.R.id.text1, android.R.id.text2 });
		listview.setAdapter(adapter);

		listview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long rowId) {
				SQLiteCursor cursor = (SQLiteCursor) arg0.getItemAtPosition(position);
				String title = cursor.getString(cursor.getColumnIndex(ArticlesHelper.c_title));
				System.out.println(title);// 打印
				Intent i = new Intent();
				i.putExtra(ArticlesHelper.c_title, title);// 文章标题
				i.putExtra(FeedsHelper.c_charset, charset);// 内容编码
				i.setClass(ArticleList.this, ArticleShow.class);
				cursor.close();
				ArticleList.this.startActivity(i);
			}
		});
		helper.close();
	}

	@Override
	protected void onResume() {
		createView();
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (cursor != null) {
			cursor.close();
		}
		super.onPause();
	}
}
