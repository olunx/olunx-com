package com.olunx.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import com.olunx.R;
import com.olunx.db.ArticlesHelper;
import com.olunx.db.FeedsHelper;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class ArticleShow extends Activity {

	private TextView mWebView;
	
	private final String TAG = "com.olunx.activity.ArticleShow";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.article_show);
		mWebView = (TextView) findViewById(R.id.TextView01);
		// setContentView(R.layout.article_show);

		// createView();
	}

	private void createView() {

		ArticlesHelper helper = new ArticlesHelper();
		String title = this.getIntent().getStringExtra(ArticlesHelper.c_title);// 文章标题
		String charset = this.getIntent().getStringExtra(FeedsHelper.c_charset);// 内容编码
		HashMap<String, String> article = helper.getArticleByTitle(title);
		helper.close();
		String content = article.get(ArticlesHelper.c_content);
		System.out.println("article.get(ArticlesHelper.c_content) " + content);
		mWebView.setText(Html.fromHtml(content));
		// mWebView.loadUrl("http://t.qq.com");
//		try {
//			mWebView.loadData(URLEncoder.encode(content, "utf-8"), "text/html", charset);
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
		// mWebView.setWebViewClient(new WebViewClient(){
		// public boolean shouldOverrideUrlLoading(WebView view, String url) {
		// view.loadUrl(url);
		// return true;
		// }
		// });
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "onResume()");
		createView();
		super.onResume();
	}
}
