package com.olunx.activity;

import com.olunx.R;
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
		this.setContentView(R.layout.article_list);
		listview = (ListView)this.findViewById(R.id.ListView01);

		// createView();
	}

	private void createView() {

		ArticlesHelper helper = new ArticlesHelper();
		String url = this.getIntent().getStringExtra(FeedsHelper.c_xmlUrl);
		final String charset = this.getIntent().getStringExtra(FeedsHelper.c_charset);
		cursor = helper.getArticlesByFeedXmlUrl(url);

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.article_list_item, cursor, new String[] {
				ArticlesHelper.c_title, ArticlesHelper.c_publishTime }, new int[] { R.id.TextView01, R.id.TextView02 });
		listview.setAdapter(adapter);

		listview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long rowId) {
				SQLiteCursor cursor = (SQLiteCursor) arg0.getItemAtPosition(position);
				String link = cursor.getString(cursor.getColumnIndex(ArticlesHelper.c_link));
				Intent i = new Intent();
				i.putExtra(ArticlesHelper.c_link, link);// 文章链接
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
