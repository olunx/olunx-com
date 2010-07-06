package com.olunx.activity;

import com.olunx.db.ArticlesHelper;
import com.olunx.db.FeedsHelper;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ArticleList extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		createView();
	}

	private void createView() {

		ArticlesHelper helper = new ArticlesHelper();
		String args = this.getIntent().getStringExtra(FeedsHelper.c_title);

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, helper.getArticleByTitle(args),
				new String[] { ArticlesHelper.c_title, ArticlesHelper.c_link }, new int[] { android.R.id.text1, android.R.id.text2 });

		ListView listview = new ListView(this);
		listview.setAdapter(adapter);

		this.setContentView(listview);
	}

}
