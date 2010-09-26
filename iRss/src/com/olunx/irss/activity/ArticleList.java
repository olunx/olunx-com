package com.olunx.irss.activity;

import java.util.ArrayList;
import java.util.Map;

import com.olunx.irss.R;
import com.olunx.irss.db.ArticlesHelper;
import com.olunx.irss.db.FeedsHelper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class ArticleList extends Activity {

	private ListView listview;
	private String xmlUrl;
	private String charset;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.listview);
		listview = (ListView) this.findViewById(R.id.ListView01);
		this.setTitle(this.getIntent().getStringExtra(FeedsHelper.c_title));
		xmlUrl = this.getIntent().getStringExtra(FeedsHelper.c_xmlUrl);
		charset = this.getIntent().getStringExtra(FeedsHelper.c_charset);
		listview.setOnItemClickListener(new OnItemClickListener() {
			@SuppressWarnings("unchecked")
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long rowId) {
				Map<String, Object> map = (Map<String, Object>) arg0.getItemAtPosition(position);
				String title = (String) map.get(ArticlesHelper.c_title);
				String link = (String) map.get(ArticlesHelper.c_link);
				Intent i = new Intent();
				i.putExtra(ArticlesHelper.c_title, title);// 文章标题
				i.putExtra(ArticlesHelper.c_link, link);// 文章链接
				i.putExtra(FeedsHelper.c_charset, charset);// 内容编码
				i.putExtra(FeedsHelper.c_xmlUrl, xmlUrl);// Feed链接
				i.setClass(ArticleList.this, ArticleShow.class);
				ArticleList.this.startActivity(i);
			}
		});
	}

	private void createView() {
		final ProgressDialog pd = new ProgressDialog(this);
		pd.setMessage("正在加载文章列表...");
		pd.show();
		
		new Thread(){@Override
		public void run() {
			ArticlesHelper helper = new ArticlesHelper();
			ArrayList<Map<String, Object>> list = helper.getArticlesByFeedXmlUrl(xmlUrl);
			helper.close();

			SimpleAdapter adapter = new SimpleAdapter(ArticleList.this, list, R.layout.listview_item, new String[] { ArticlesHelper.c_unread,
					ArticlesHelper.c_title, ArticlesHelper.c_publishTime }, new int[] { R.id.ImageView01, R.id.TextView01, R.id.TextView02 });
			Message msg = new Message();
			msg.what = REFRESH;
			msg.obj = adapter;
			mHandler.sendMessage(msg);
			
			pd.dismiss();
		}}.start();
		
	}
	
	private final int REFRESH = 0;
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REFRESH: {
				listview.setAdapter((SimpleAdapter)msg.obj);
			}
			}
		}
	};

	@Override
	protected void onResume() {
		createView();
		super.onResume();
	}

}
