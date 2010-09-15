package com.olunx.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.olunx.db.ArticlesHelper;
import com.olunx.db.FeedsHelper;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.WebView;

public class ArticleShow extends Activity {

	private WebView mWebView;
	private final String TAG = "com.olunx.activity.ArticleShow";
	
	private final int LOAD_DATA = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// setContentView(R.layout.article_show);
		// mWebView = (WebView) findViewById(R.id.WebView01);
		mWebView = new WebView(this);
		this.setContentView(mWebView);
		// setContentView(R.layout.article_show);

		// createView();
	}

	private void createView() {

		ArticlesHelper helper = new ArticlesHelper();
		String link = this.getIntent().getStringExtra(ArticlesHelper.c_link);// 文章链接
		String charset = this.getIntent().getStringExtra(FeedsHelper.c_charset);// 内容编码
		String content = helper.getArticleContentByLink(link);
		helper.close();

		try {
			content = URLEncoder.encode(content, "utf-8").replaceAll("\\+", " ").trim();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		Message msg = mHandler.obtainMessage();
		msg.what = LOAD_DATA;
		Bundle data = new Bundle();
		data.putString("content", content);
		data.putString("charset", charset);
		msg.setData(data);
		msg.sendToTarget();
		
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case LOAD_DATA: {
				Bundle data = msg.getData();
				mWebView.loadData(data.getString("content"), "text/html", data.getString("charset"));
			}
			}
		}
	};
	
	@Override
	protected void onResume() {
		Log.i(TAG, "onResume()");
		new Thread() {
			public void run() {
				createView();
			}
		}.start();
		super.onResume();
	}
}

