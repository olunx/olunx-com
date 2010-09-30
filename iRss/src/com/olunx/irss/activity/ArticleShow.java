package com.olunx.irss.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.olunx.irss.R;
import com.olunx.irss.activity.settings.Settings;
import com.olunx.irss.db.ArticlesHelper;
import com.olunx.irss.db.FeedsHelper;
import com.olunx.irss.util.Config;
import com.olunx.irss.util.HtmlParser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Toast;

public class ArticleShow extends Activity {

	private ScrollView mScrollView;
	private WebView mWebView;
	private Button mButton;
	private ImageButton upBtn;
	private final String TAG = "com.olunx.activity.ArticleShow";

	private String currentFeedXmlUrl;
	private String currentArticleId;
	private String currentCharset;
	private String currentContent;
	private String nextArticleId;

	private final int LOAD_DATA = 0;
	private final int MENU_FULLSCREEN = 10;
	private final int MENU_SWITCHSCREEN = 11;
	private final int MENU_SETTINGS = 12;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.article_show);

		mScrollView = (ScrollView) findViewById(R.id.ScrollView01);
		mWebView = (WebView) findViewById(R.id.WebView01);
		mButton = (Button) findViewById(R.id.Button01);
		upBtn = (ImageButton) findViewById(R.id.ImageButton01);
		upBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mScrollView.scrollTo(0, 0);
			}
		});
		mButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(TAG, "click");
				mButton.setEnabled(false);
				if (nextArticleId != null) {
					currentArticleId = nextArticleId;
					setWebViewContent();
				} else {
					Toast.makeText(ArticleShow.this, "你已经阅读完该订阅的所有文章了。", Toast.LENGTH_LONG).show();
					finish();
				}
			}
		});

		currentFeedXmlUrl = getIntent().getStringExtra(FeedsHelper.c_xmlUrl);
		currentArticleId = getIntent().getStringExtra(ArticlesHelper.c_id);
		currentCharset = getIntent().getStringExtra(FeedsHelper.c_charset);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LOAD_DATA: {
				mScrollView.scrollTo(0, 0);
				mWebView.clearView();
				mWebView.setBackgroundColor(Color.parseColor(Config.init().getArticleBgColor()));
				mWebView.loadData(currentContent, "text/html", currentCharset);
				mButton.setEnabled(true);
			}
			}
		}
	};

	private void setWebViewContent() {
		final ProgressDialog pd = new ProgressDialog(this);
		pd.setMessage("正在加载文章数据...");
		pd.show();
		new Thread() {
			public void run() {
				ArticlesHelper helper = new ArticlesHelper();

				currentContent = helper.getArticleContentById(currentArticleId);
				
				// 处理页面样式
				HtmlParser parser = new HtmlParser(Config.init().getArticleFontColor(), Config.init().getArticleFontSize());
				currentContent = parser.parseHtml(currentContent);
				try {
					currentContent = URLEncoder.encode(currentContent, "utf-8").replaceAll("\\+", " ").trim();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				mHandler.sendEmptyMessage(LOAD_DATA);
				pd.dismiss();

				// 预读下一篇未读的文章
				nextArticleId = helper.getUnreadArticleIdByFeedXmlUrl(currentFeedXmlUrl);// 文章链接
				helper.close();
			}
		}.start();
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "onResume()");
		setWebViewContent();
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(1, MENU_SETTINGS, 1, "设置").setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(2, MENU_FULLSCREEN, 1, "全屏").setIcon(android.R.drawable.ic_menu_mapmode);
		menu.add(3, MENU_SWITCHSCREEN, 1, "旋转屏幕").setIcon(android.R.drawable.ic_menu_set_as);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case MENU_FULLSCREEN: {
			Log.i(TAG, "full window");
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			System.out.println(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			break;
		}
		case MENU_SWITCHSCREEN: {
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
		case MENU_SETTINGS: {
			Intent i = new Intent();
			i.setClass(this, Settings.class);
			this.startActivity(i);
			break;
		}
		}
		return super.onMenuItemSelected(featureId, item);
	}

}
