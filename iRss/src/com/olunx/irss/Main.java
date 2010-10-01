package com.olunx.irss;

import java.util.ArrayList;
import java.util.Map;

import com.olunx.irss.activity.ArticleList;
import com.olunx.irss.activity.help.Helps;
import com.olunx.irss.activity.settings.Settings;
import com.olunx.irss.db.CategoryHelper;
import com.olunx.irss.db.FeedsHelper;
import com.olunx.irss.reader.Update;
import com.olunx.irss.util.SysTools;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class Main extends Activity {

	private final String TAG = "com.olunx.irss.Main";
	private ListView listview;
	private ItemClickListener itemClickListener;// Feed Item点击事件
	private ItemLongClickListener itemLongClickListener;// Feed Item长按事件
	private FeedsHelper helper;
	private String currentCatTitle;// 当前选中的分类标题
	private ArrayList<String> allTitles;// 所有分类标题
	private ToggleButton lastBtn;// 最后一次选中的按钮

	private final int ALERT_DISCONNECT = 1;// 网络连接不可用
	private final int MSG_REFRESH_LISTVIEW = 2;// 刷新listview
	private final int MSG_NOTIFY_UPDATED = 3; // 消息栏通知
	private final int MSG_DELTE_FEED = 4; //删除一条订阅源

	private Notification notification;
	private NotificationManager notificationManager;
	private Intent intent;
	private PendingIntent pendIntent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mian_view);

		// 初始化一次的对象
		listview = (ListView) findViewById(R.id.ListView01);
		itemClickListener = new ItemClickListener();
		itemLongClickListener = new ItemLongClickListener();
		listview.setOnItemClickListener(itemClickListener);
		listview.setOnItemLongClickListener(itemLongClickListener);

		notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);// 获取系统服务（消息管理）
		// 点击通知时转移内容
		intent = new Intent(this, Main.class);
		// 设置点击通知时显示内容的类
		pendIntent = PendingIntent.getActivity(this, 0, intent, 0);
		notification = new Notification();

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
			if (currentCatTitle == null)// 如果没有选中分类名，则表示更新所有分类。
				currentCatTitle = allTitles.get(0);
		} else {
			return;
		}

		createBtn();// 初始化底部栏目
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
	 * 创建Feed Item
	 * 
	 * @param catTitle
	 */
	private void createListItem(final String catTitle) {
		this.setTitle("当前分类：" + catTitle);

		new Thread() {
			@Override
			public void run() {
				ArrayList<Map<String, Object>> list = helper.getFeedsByCategory(catTitle);
				SimpleAdapter adapter = new SimpleAdapter(Main.this, list, R.layout.feed_list_item, new String[] { FeedsHelper.c_icon,
						FeedsHelper.c_title, FeedsHelper.c_text }, new int[] { R.id.ImageView01, R.id.TextView01, R.id.TextView02 });
				Message msg = new Message();
				msg.obj = adapter;
				msg.what = MSG_REFRESH_LISTVIEW;
				mHandler.sendMessage(msg);
			}
		}.start();
	}

	@Override
	protected void onResume() {
		if (helper == null || !helper.isOpen()) {
			helper = new FeedsHelper();
		}
		if (currentCatTitle != null) {
			createListItem(currentCatTitle);
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
		Log.i(TAG, "onPause()");
		super.onPause();
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ALERT_DISCONNECT: {
				setNotification("网络连接失败，请检查网络是否可用。", "网络连接不可用，更新失败。");
				break;
			}
			case MSG_REFRESH_LISTVIEW: {
				listview.setAdapter((SimpleAdapter) msg.obj);
				break;
			}
			case MSG_NOTIFY_UPDATED: {
				init();
				onResume();
				setNotification("订阅更新完成了...o(∩_∩)o", "订阅更新完成");
				break;
			}case MSG_DELTE_FEED:{
				init();
				onResume();
				Toast.makeText(Main.this, "成功删除一条订阅！", Toast.LENGTH_LONG).show();
				break;
			}
			}
		}
	};

	/**
	 * 消息栏通知
	 */
	private void setNotification(String tickerText, String infoText) {
		notification.icon = R.drawable.logo_32;// 设置在状态栏显示的图标
		notification.tickerText = tickerText;// 设置在状态栏显示的内容
		// 设置通知显示的参数
		notification.setLatestEventInfo(Main.this, "iRss", infoText, pendIntent);
		notificationManager.notify(0, notification);// 执行通知.
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
			lastBtn = btn;
			currentCatTitle = btn.getText().toString();
			createListItem(currentCatTitle);// 根据分类名创建Feed Item
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
			ad.setItems(new String[] { "更新" + titlePlus + "分类所有订阅" }, new android.content.DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0: {
						Toast.makeText(Main.this, "开始更新文章数据...", Toast.LENGTH_SHORT).show();
						new Thread() {
							public void run() {
								if (!SysTools.isConnect(Main.this)) {
									mHandler.sendEmptyMessage(ALERT_DISCONNECT);
									return;
								}
								new Update().updateArticlesByCat(selectTitle);
								mHandler.sendEmptyMessage(MSG_NOTIFY_UPDATED);
							}
						}.start();
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
	 * ListView短按事件
	 */
	class ItemClickListener implements OnItemClickListener {

		@SuppressWarnings("unchecked")
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Map<String, Object> map = (Map<String, Object>) parent.getItemAtPosition(position);
			String title = (String) map.get(FeedsHelper.c_title);
			String xmlUrl = (String) map.get(FeedsHelper.c_xmlUrl);
			String charset = (String) map.get(FeedsHelper.c_charset);
			Log.i(TAG, xmlUrl);
			Intent i = new Intent();
			i.putExtra(FeedsHelper.c_title, title);// Feed标题
			i.putExtra(FeedsHelper.c_xmlUrl, xmlUrl);// Feed地址
			i.putExtra(FeedsHelper.c_charset, charset);// 内容编码
			i.setClass(Main.this, ArticleList.class);
			Main.this.startActivity(i);
		}

	}

	/**
	 * ListView长按事件
	 * 
	 */
	class ItemLongClickListener implements OnItemLongClickListener {

		@SuppressWarnings("unchecked")
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

			Map<String, Object> map = (Map<String, Object>) parent.getItemAtPosition(position);
			final String selectXmlUrl = (String) map.get(FeedsHelper.c_xmlUrl);

			final AlertDialog.Builder ad = new AlertDialog.Builder(Main.this);
			ad.setInverseBackgroundForced(true);// 翻转底色
			ad.setIcon(android.R.drawable.ic_dialog_info);
			ad.setTitle("请选择操作");
			ad.setCancelable(false);
			ad.setNegativeButton("取消", null);
			ad.setItems(new String[] { "更新这条[订阅]", "删除这条[订阅]" }, new android.content.DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0: {
						Toast.makeText(Main.this, "开始更新文章数据...", Toast.LENGTH_SHORT).show();
						new Thread() {
							public void run() {
								if (!SysTools.isConnect(Main.this)) {
									mHandler.sendEmptyMessage(ALERT_DISCONNECT);
									return;
								}
								new Update().updateArticlesByfeed(selectXmlUrl);
								mHandler.sendEmptyMessage(MSG_NOTIFY_UPDATED);
							}
						}.start();
						break;
					}
					case 1:
					{
						new Thread() {
							public void run() {
								if (!SysTools.isConnect(Main.this)) {
									mHandler.sendEmptyMessage(ALERT_DISCONNECT);
									return;
								}
								new Update().deleteFeed(selectXmlUrl);
								mHandler.sendEmptyMessage(MSG_DELTE_FEED);
							}
						}.start();
						break;
					}
					}
				}
			});
			ad.show();

			return false;
		}
	}

	private final int MENU_ADD = 10;
	private final int MENU_SETTINGS = 13;
	private final int MENU_UPDATE = 14;
	private final int MENU_HELP = 15;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(1, MENU_ADD, 1, "添加").setIcon(R.drawable.ic_menu_add);
		menu.add(1, MENU_SETTINGS, 2, "设置").setIcon(R.drawable.ic_menu_preferences);
		menu.add(2, MENU_UPDATE, 1, "更新").setIcon(R.drawable.ic_menu_refresh);
		menu.add(2, MENU_HELP, 2, "帮助").setIcon(R.drawable.ic_menu_help);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ADD: {
			// Intent i = new Intent();
			// this.startActivity(i);
			break;
		}
		case MENU_SETTINGS: {
			Intent i = new Intent();
			i.setClass(this, Settings.class);
			this.startActivity(i);
			break;
		}
		case MENU_UPDATE: {
			Toast.makeText(this, "开始更新所有[订阅]...", Toast.LENGTH_SHORT).show();
			currentCatTitle = null;// 更新所有Feed的文章
			new Thread() {
				public void run() {
					if (!SysTools.isConnect(Main.this)) {
						mHandler.sendEmptyMessage(ALERT_DISCONNECT);
						return;
					}
					System.out.println("network connected!");
					Update update = new Update();
					update.getFeedsFromGoogle();
					update.updateAllArticles();
					mHandler.sendEmptyMessage(MSG_NOTIFY_UPDATED);
				}
			}.start();
			break;
		}
		case MENU_HELP: {
			Intent i = new Intent();
			i.setClass(this, Helps.class);
			this.startActivity(i);
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