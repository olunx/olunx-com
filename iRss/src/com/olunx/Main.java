package com.olunx;

import java.util.ArrayList;

import com.olunx.activity.ArticleList;
import com.olunx.db.ArticlesHelper;
import com.olunx.db.CategoryHelper;
import com.olunx.db.FeedsHelper;
import com.olunx.reader.Update;
import com.olunx.util.Config;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.os.Bundle;
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
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class Main extends Activity {

	private final String TAG = "com.olunx.Main";
	private Cursor cursor;
	private GridView gridview;
	private ItemClickListener itemClickListener;
	private FeedsHelper helper;
	private String currentCatTitle;
	private ArrayList<String> allTitles;
	private ToggleButton lastBtn;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gridview);
		gridview = (GridView) findViewById(R.id.GridView01);

		// 初始化一次的对象
		itemClickListener = new ItemClickListener();

		// 获取所有栏目
		CategoryHelper helper = new CategoryHelper();
		allTitles = helper.getAllCats();
		helper.close();

		// 设置当前目录
		if (allTitles != null && allTitles.size() > 0) {
			currentCatTitle = allTitles.get(0);
		} else {
			return;
		}

		initConfig();
		createBtn();// 初始化底部栏目
	}

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
		gridview.setOnItemClickListener(itemClickListener);
	}

	/**
	 * 创建底部栏目
	 */
	private void createBtn() {

		RadioGroup group = (RadioGroup) findViewById(R.id.RadioGroup01);

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
			if (i == 0) {
				btn.setChecked(true);
				lastBtn = btn;
			}
			;// 如果在假如group前选择，则和后面的分成两组。
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
				if(lastBtn == btn) {
					btn.setChecked(true);
					return;
				};
				lastBtn.setChecked(false);
				lastBtn.setClickable(true);
			}
			//关闭当前cursor
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
			lastBtn = btn;
			btn.setClickable(false);
			currentCatTitle = btn.getText().toString();
			createFeedItem(currentCatTitle);
		}
	}

	/**
	 *按钮长按事件
	 */
	class BtnLongClickListener implements OnLongClickListener {

		@Override
		public boolean onLongClick(View v) {

			ToggleButton btn = (ToggleButton) v;
			final String selectTitle = btn.getText().toString();
			
			final AlertDialog.Builder ad = new AlertDialog.Builder(Main.this);
			ad.setInverseBackgroundForced(true);// 翻转底色
			ad.setIcon(android.R.drawable.ic_dialog_info);
			ad.setTitle("请选择操作");
			ad.setCancelable(false);
			ad.setItems(new String[] { "更新该分类", "修改分类名称", "删除该分类" }, new android.content.DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0:
						break;
					case 1:
						break;
					case 2:
						break;
					}
				}
			});
			ad.setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int arg1) {
					dialog.dismiss();
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
			// TODO Auto-generated method stub
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
		Config config = Config.init(this);

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
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_info).setTitle("请输入Google账户信息：").setView(view)
					.setNegativeButton("取消", null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
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
							Config.init(Main.this).setAccount(user, pwd);
						}
					}).show();

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
			break;
		}
		case 5: {
			Toast.makeText(this, "开始连接...", Toast.LENGTH_SHORT).show();
			new Thread() {
				public void run() {
					new Update().updateFeeds();
					System.out.println("finished!");
				}
			}.start();
			Toast.makeText(this, "链接完成。", Toast.LENGTH_SHORT).show();
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
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("警告").setMessage("是否真的要关闭程序？")
					.setNegativeButton("取消", null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							finish();
						}
					}).show();

			return true;

		} else {
			return super.onKeyDown(keyCode, event);
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// if (helper != null) {
		// helper.close();
		// }
		Process.killProcess(android.os.Process.myPid());
	}

}