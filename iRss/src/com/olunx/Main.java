package com.olunx;

import com.olunx.activity.FeedList;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class Main extends Activity {

	private Cursor cursor;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		initCOnfig();
		//
		// createView();
	}

	private void createView() {

		CategoryHelper helper = new CategoryHelper();
		cursor = helper.getAllRecords();

		GridView gridview = (GridView) findViewById(R.id.GridView01);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.main_item, cursor, new String[] { CategoryHelper.c_icon,
				CategoryHelper.c_title, CategoryHelper.c_feedCount }, new int[] { R.id.ItemImage, R.id.ItemText, R.id.ItemNum });
		gridview.setAdapter(adapter);
		//短按事件
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long rowId) {
				SQLiteCursor cursor = (SQLiteCursor) arg0.getItemAtPosition(position);
				String title = cursor.getString(cursor.getColumnIndex(CategoryHelper.c_title));
				System.out.println(title);// 打印
				Intent i = new Intent();
				i.putExtra(CategoryHelper.c_title, title);// 分类标题
				i.setClass(Main.this, FeedList.class);
				cursor.close();
				Main.this.startActivity(i);
			}
		});
		//长按事件
		gridview.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				SQLiteCursor cursor = (SQLiteCursor) parent.getItemAtPosition(position);
				final String title = cursor.getString(cursor.getColumnIndex(CategoryHelper.c_title));
				System.out.println(title);// 打印
				Toast.makeText(Main.this, "开始更新...", Toast.LENGTH_SHORT).show();
				new Thread() {
					public void run() {
						new Update().updateArticlesByCat(title);
						System.out.println("finished!");
					}
				}.start();
				cursor.close();
				return false;
			}});

		helper.close();
	}

	@Override
	protected void onResume() {
		createView();
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (cursor != null) {
			cursor.close();
		}
		super.onPause();
	}

	private void initCOnfig() {
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