package com.olunx.irss;

import java.util.ArrayList;

import com.olunx.irss.activity.ArticleList;
import com.olunx.irss.activity.MainSettings;
import com.olunx.irss.db.ArticlesHelper;
import com.olunx.irss.db.CategoryHelper;
import com.olunx.irss.db.FeedsHelper;
import com.olunx.irss.reader.Update;
import com.olunx.irss.util.Config;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class Main extends Activity {

	private final String TAG = "com.olunx.Main";
	private Cursor cursor;// Feed Item每次检索结果
	private GridView gridview;// Feed Item布局区域
	private ItemClickListener itemClickListener;// Feed Item点击事件
	private ItemLongClickListener itemLongClickListener;// Feed Item长按事件
	private FeedsHelper helper;
	private String currentCatTitle;// 当前选中的分类标题
	private ArrayList<String> allTitles;// 所有分类标题
	private ToggleButton lastBtn;// 最后一次选中的按钮

	private final int MSG_REFRESH = 0;// 刷新UI

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gridview);

		initConfig();// 软件配置初始化

		// 初始化一次的对象
		gridview = (GridView) findViewById(R.id.GridView01);
		itemClickListener = new ItemClickListener();
		itemLongClickListener = new ItemLongClickListener();
		gridview.setOnItemClickListener(itemClickListener);
		gridview.setOnItemLongClickListener(itemLongClickListener);

		init();// 初始化数据
	}

	/**
	 * 初始化，写成方法方便在软件中重新调用。
	 */
	private void init() {

		// 获取所有栏目
		CategoryHelper helper = new CategoryHelper();
		allTitles = helper.getAllCats();
		helper.close();

		// 设置当前目录
		if (allTitles != null && allTitles.size() > 0) {
			if (currentCatTitle == null)//如果没有选中分类名，则表示更新所有分类。
				currentCatTitle = allTitles.get(0);
		} else {
			return;
		}

		createBtn();// 初始化底部栏目
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REFRESH: {
				init();
				// 关闭当前cursor
				if (cursor != null) {
					cursor.close();
				}
				onResume();
				Toast.makeText(Main.this, "更新数据完成！o(∩_∩)o 哈哈", Toast.LENGTH_SHORT).show();
				break;
			}
			}
		}
	};

	/**
	 * 创建Feed Item
	 * 
	 * @param catTitle
	 */
	private void createFeedItem(String catTitle) {
		this.setTitle("当前分类：" + catTitle);

		cursor = helper.getFeedsByCategory(catTitle);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.gridview_item, cursor, new String[] { FeedsHelper.c_icon,
				FeedsHelper.c_title, FeedsHelper.c_articleCount }, new int[] { R.id.ImageView01, R.id.TextView01, R.id.TextView02 });
		gridview.setAdapter(adapter);
	}

	/**
	 * 创建底部栏目
	 */
	private void createBtn() {

		LinearLayout group = (LinearLayout) findViewById(R.id.LinearLayout04);
		group.removeAllViews();

		int length = allTitles.size();
		String title = null;
		BtnClickListener listener = new BtnClickListener();
		BtnLongClickListener longListener = new BtnLongClickListener();
		for (int i = 0; i < length; i++) {
			ToggleButton btn = new ToggleButton(this);
			btn.setMinWidth(70);
			title = allTitles.get(i);
			btn.setText(title);
			btn.setTextOff(title);
			btn.setTextOn(title);
			btn.setOnClickListener(listener);
			btn.setOnLongClickListener(longListener);
			group.addView(btn);
			if (i == 0) {// 默认选中第一项
				btn.setChecked(true);
				lastBtn = btn;
			}
		}

	}

	/**
	 * 按钮短按事件
	 */
	class BtnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			ToggleButton btn = (ToggleButton) v;

			// 取消上次选择的按钮
			if (lastBtn != null) {
				// 如果重复选择同一按钮，则返回。
				if (lastBtn == btn) {
					btn.setChecked(true);
					return;
				}
				lastBtn.setChecked(false);
			}
			// 关闭当前cursor
			if (cursor != null) {
				cursor.close();
			}
			lastBtn = btn;
			currentCatTitle = btn.getText().toString();
			createFeedItem(currentCatTitle);// 根据分类名创建Feed Item
		}
	}

	/**
	 * 按钮长按事件
	 */
	class BtnLongClickListener implements OnLongClickListener {

		@Override
		public boolean onLongClick(View v) {

			ToggleButton btn = (ToggleButton) v;
			final String selectTitle = btn.getText().toString();
			String titlePlus = "[" + selectTitle + "]";

			final AlertDialog.Builder ad = new AlertDialog.Builder(Main.this);
			ad.setInverseBackgroundForced(true);// 翻转底色
			ad.setIcon(android.R.drawable.ic_dialog_info);
			ad.setTitle("请选择操作");
			ad.setCancelable(false);
			ad.setNegativeButton("取消", null);
			ad.setItems(new String[] { "更新该分类所有Feed", "修改分类名称" + titlePlus, "删除当前分类" + titlePlus },
					new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							case 0: {
								Toast.makeText(Main.this, "开始更新文章数据...", Toast.LENGTH_SHORT).show();
								new Thread() {
									public void run() {
										new Update().updateArticlesByCat(selectTitle);
										mHandler.sendEmptyMessage(MSG_REFRESH);
									}
								}.start();
								break;
							}
							case 1: {
								break;
							}
							case 2: {
								break;
							}
							}
						}
					});
			ad.show();

			return false;
		}

	}

	/**
	 * GridView短按事件
	 */
	class ItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			SQLiteCursor cursor = (SQLiteCursor) parent.getItemAtPosition(position);
			String xmlUrl = cursor.getString(cursor.getColumnIndex(FeedsHelper.c_xmlUrl));
			String charset = cursor.getString(cursor.getColumnIndex(FeedsHelper.c_charset));
			Log.i(TAG, xmlUrl);
			Intent i = new Intent();
			i.putExtra(FeedsHelper.c_xmlUrl, xmlUrl);// Feed地址
			i.putExtra(FeedsHelper.c_charset, charset);// 内容编码
			i.setClass(Main.this, ArticleList.class);
			cursor.close();
			Main.this.startActivity(i);
		}

	}

	/**
	 * GridView长按事件
	 * 
	 */
	class ItemLongClickListener implements OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

			SQLiteCursor cursor = (SQLiteCursor) parent.getItemAtPosition(position);
			// final String selectTitle =
			// cursor.getString(cursor.getColumnIndex(FeedsHelper.c_title));
			final String selectXmlUrl = cursor.getString(cursor.getColumnIndex(FeedsHelper.c_xmlUrl));

			final AlertDialog.Builder ad = new AlertDialog.Builder(Main.this);
			ad.setInverseBackgroundForced(true);// 翻转底色
			ad.setIcon(android.R.drawable.ic_dialog_info);
			ad.setTitle("请选择操作");
			ad.setCancelable(false);
			ad.setNegativeButton("取消", null);
			ad.setItems(new String[] { "更新当前Feed", "修改Feed名称", "删除这条Feed" }, new android.content.DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0: {
						Toast.makeText(Main.this, "开始更新文章数据...", Toast.LENGTH_SHORT).show();
						new Thread() {
							public void run() {
								new Update().updateArticlesByfeed(selectXmlUrl);
								mHandler.sendEmptyMessage(MSG_REFRESH);
							}
						}.start();
						break;
					}
					case 1:
						break;
					case 2:
						break;
					}
				}
			});
			ad.show();

			return false;
		}
	}

	@Override
	protected void onResume() {
		if (helper == null || !helper.isOpen()) {
			helper = new FeedsHelper();
		}
		if (currentCatTitle != null) {
			createFeedItem(currentCatTitle);
		}
		Log.i(TAG, "onResume()");
		super.onResume();
	}

	@Override
	protected void onPause() {
		// 关闭数据库
		if (helper != null) {
			helper.close();
		}
		// 关闭cursor
		if (cursor != null) {
			cursor.close();
		}
		Log.i(TAG, "onPause()");
		super.onPause();
	}

	/**
	 * 初始化软件
	 */
	private void initConfig() {
		final Config config = Config.init();

		if (!config.isAccountInputted()) {
			final View view = LayoutInflater.from(this).inflate(R.layout.account_input, null);

			// 点击清空编辑框事件
			final EditText username = (EditText) view.findViewById(R.id.EditText01);
			username.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					username.setText("");
					return false;
				}
			});
			final EditText password = (EditText) view.findViewById(R.id.EditText02);
			password.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					password.setText("");
					return false;
				}
			});

			// 输入对话框
			AlertDialog.Builder ad = new AlertDialog.Builder(this);
			ad.setIcon(android.R.drawable.ic_dialog_info);
			ad.setTitle("请输入Google账户信息：");
			ad.setView(view);
			ad.setNegativeButton("取消", null);
			ad.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// 保存账户信息
					String user = username.getText().toString();
					if (user != null) {
						user = user.trim();
					}
					String pwd = password.getText().toString();
					if (pwd != null) {
						pwd = pwd.trim();
					}
					config.setAccount(user, pwd);
				}
			});
			ad.show();

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(1, 1, 1, "添加").setIcon(android.R.drawable.ic_menu_add);
		menu.add(1, 2, 2, "退出").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		menu.add(2, 3, 1, "搜索").setIcon(android.R.drawable.ic_menu_search);
		menu.add(2, 4, 2, "设置").setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(3, 5, 1, "更新").setIcon(android.R.drawable.ic_menu_upload);
		menu.add(3, 6, 2, "帮助").setIcon(android.R.drawable.ic_menu_help);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case 1: {
			break;
		}
		case 2: {
			break;
		}
		case 3: {
			break;
		}
		case 4: {
			Intent i = new Intent();
			i.setClass(this, MainSettings.class);
			this.startActivity(i);
			break;
		}
		case 5: {
			Toast.makeText(this, "开始更新所有Feed...", Toast.LENGTH_SHORT).show();
			currentCatTitle = null;//更新所有Feed的文章
			new Thread() {
				public void run() {
					new Update().updateFeeds();
					System.out.println("finished!");
					mHandler.sendEmptyMessage(MSG_REFRESH);
				}
			}.start();
			break;
		}
		case 6: {
			CategoryHelper cHelper = new CategoryHelper();
			cHelper.dropTable();
			cHelper.close();
			FeedsHelper fHelper = new FeedsHelper();
			fHelper.dropTable();
			fHelper.close();
			ArticlesHelper aHelper = new ArticlesHelper();
			aHelper.dropTable();
			aHelper.close();
			Toast.makeText(this, "删除数据!", Toast.LENGTH_SHORT).show();
			break;
		}
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AlertDialog.Builder ad = new AlertDialog.Builder(this);
			ad.setIcon(android.R.drawable.ic_dialog_alert);
			ad.setTitle("警告");
			ad.setMessage("是否真的要关闭程序？");
			ad.setNegativeButton("取消", null);
			ad.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					finish();
				}
			});
			ad.show();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Process.killProcess(android.os.Process.myPid());
	}

}