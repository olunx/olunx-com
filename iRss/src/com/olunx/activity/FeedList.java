package com.olunx.activity;

import com.olunx.R;
import com.olunx.db.CategoryHelper;
import com.olunx.db.FeedsHelper;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteCursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class FeedList extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.feed_list);
		createView();
	}

	private void createView() {
		GridView gridview = (GridView) findViewById(R.id.GridView01);

		FeedsHelper helper = new FeedsHelper();
		String args = this.getIntent().getStringExtra(CategoryHelper.c_title);
		
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.main_item, helper.getFeedsByCategory(args), new String[] {
			FeedsHelper.c_icon, FeedsHelper.c_title, FeedsHelper.c_articleCount }, new int[] { R.id.ItemImage, R.id.ItemText,
				R.id.ItemNum });

		gridview.setAdapter(adapter);
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long rowId) {
				SQLiteCursor cursor = (SQLiteCursor)arg0.getItemAtPosition(position);
				String title = cursor.getString(cursor.getColumnIndex(FeedsHelper.c_title));
				System.out.println(title);//打印
				Intent i = new Intent();
				i.putExtra(FeedsHelper.c_title, title);
				i.setClass(FeedList.this, ArticleList.class);
				cursor.close();
				FeedList.this.startActivity(i);
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}

}
