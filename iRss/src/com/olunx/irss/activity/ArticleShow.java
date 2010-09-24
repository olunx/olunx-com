package com.olunx.irss.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.olunx.irss.db.ArticlesHelper;
import com.olunx.irss.db.FeedsHelper;
import com.olunx.irss.util.Config;
import com.olunx.irss.util.HtmlParser;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;

public class ArticleShow extends Activity {

	private WebView mWebView;
	private final String TAG = "com.olunx.activity.ArticleShow";

	private final int LOAD_DATA = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// setContentView(R.layout.article_show);
		// mWebView = (WebView) findViewById(R.id.WebView01);
		mWebView = new WebView(this);
		setContentView(mWebView);
		// this.setTitle(this.getIntent().getStringExtra(ArticlesHelper.c_title));
		mWebView.setBackgroundColor(Integer.parseInt(Config.init().getArticleBgColor()));
		// setContentView(R.layout.article_show);
		// createView();
	}

	private void getData() {

		ArticlesHelper helper = new ArticlesHelper();
		String link = this.getIntent().getStringExtra(ArticlesHelper.c_link);// 文章链接
		String charset = this.getIntent().getStringExtra(FeedsHelper.c_charset);// 内容编码
		String content = helper.getArticleContentByLink(link);
		helper.close();

		// 处理页面样式
		HtmlParser parser = new HtmlParser(Config.init().getArticleFontColor(), Config.init().getArticleFontSize());
		content = parser.parseHtml(content);

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
			switch (msg.what) {
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
				getData();
			}
		}.start();
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 1, "全屏").setIcon(android.R.drawable.ic_menu_mapmode);
		menu.add(0, 2, 2, "反转屏幕").setIcon(android.R.drawable.ic_menu_set_as);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case 1: {
			Log.i(TAG, "full window");
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			System.out.println(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			break;
		}
		case 2: {
			switch (Math.abs(getRequestedOrientation())) {
			case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT: {
				this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				break;
			}
			case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE: {
				this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				break;
			}
			}

			break;
		}
		}
		return super.onMenuItemSelected(featureId, item);
	}

}
