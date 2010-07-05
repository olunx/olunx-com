package com.olunx;

import java.util.ArrayList;
import java.util.HashMap;

import com.olunx.db.CategoryHelper;
import com.olunx.reader.Rss;
import com.olunx.util.Config;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Main extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

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
		createView();
	}

	private void createView() {
		GridView gridview = (GridView) findViewById(R.id.GridView01);

		// 生成动态数组，并且转入数据
		ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < 20; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("ItemImage", R.drawable.icon);// 添加图像资源的ID
			map.put("ItemText", "NO." + String.valueOf(i));// 按序号做ItemText
			lstImageItem.add(map);
		}
		// 生成适配器的ImageItem <====> 动态数组的元素，两者一一对应
		SimpleAdapter saImageItems = new SimpleAdapter(this, // 没什么解释
				lstImageItem,// 数据来源
				R.layout.main_item,// night_item的XML实现

				// 动态数组与ImageItem对应的子项
				new String[] { "ItemImage", "ItemText" },

				// ImageItem的XML文件里面的一个ImageView,两个TextView ID
				new int[] { R.id.ItemImage, R.id.ItemText });
		// 添加并且显示
		gridview.setAdapter(saImageItems);
		// 添加消息处理
		gridview.setOnItemClickListener(new ItemClickListener());
	}

	// 当AdapterView被单击(触摸屏或者键盘)，则返回的Item单击事件
	class ItemClickListener implements OnItemClickListener {
		@SuppressWarnings("unchecked")
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long rowId) {
			// 在本例中arg2=arg3
			HashMap<String, Object> item = (HashMap<String, Object>) arg0.getItemAtPosition(position);
			// 显示所选Item的ItemText
			setTitle((String) item.get("ItemText"));
			new CategoryHelper().getDB();
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
			break;
		}
		case 6: {
			Toast.makeText(this, "开始连接...", Toast.LENGTH_SHORT).show();
			new Thread() {
				public void run() {
					Rss rss = new Rss();
					rss.login("olunxs@gmail.com", "646895472");
					rss.getCategory();
				}
			}.start();
			Toast.makeText(this, "链接完成。", Toast.LENGTH_SHORT).show();
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
		Process.killProcess(android.os.Process.myPid());
	}

}