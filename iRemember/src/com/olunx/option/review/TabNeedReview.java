package com.olunx.option.review;

import java.util.ArrayList;
import java.util.HashMap;

import com.olunx.R;
import com.olunx.db.RememberHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class TabNeedReview extends Activity {

	private ListView listview = null;
	private ArrayList<HashMap<String, String>> items = null;
	private ArrayList<HashMap<String, String>> records = null;

	String title = null;
	String desc = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 获取字符名
		title = getString(R.string.title);
		desc = getString(R.string.description);

		init();
	}

	@Override
	protected void onRestart() {
		 init();
		super.onRestart();
	}

	private void init() {
		final ProgressDialog pd = new ProgressDialog(this);
		pd.setTitle("正在加载数据");
		pd.setMessage("请稍等...");
		pd.setIcon(android.R.drawable.ic_dialog_info);

		pd.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				showList();
			}
		});

		pd.show();

		new Thread() {
			@Override
			public void run() {
				getData();
				pd.dismiss();
			}
		}.start();
	}

	// 获取数据
	private void getData() {

		// 获取数据
		RememberHelper helper = new RememberHelper();
		records = helper.getRecords(true);
		helper.close();

		// 创建对象
		items = new ArrayList<HashMap<String, String>>();

		// 临时存储对象
		HashMap<String, String> tempMap;

		for (HashMap<String, String> record : records) {
			tempMap = new HashMap<String, String>();
			tempMap.put(title, "第 " + (Integer.parseInt(String.valueOf(record.get(title))) + 1) + " 组");
			tempMap.put(desc, record.get(desc));
			items.add(tempMap);
		}
	}

	private void showList() {
		SimpleAdapter adapter = new SimpleAdapter(this, items, android.R.layout.simple_list_item_2, new String[] { this.title, this.desc },
				new int[] { android.R.id.text1, android.R.id.text2 });
		listview = new ListView(this);
		listview.setAdapter(adapter);
		this.setContentView(listview);

		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				HashMap<String, String> map = new HashMap<String, String>();
				map = records.get(position);
				final int lessonNo = Integer.parseInt(map.get(title));
				// listview.getItemAtPosition(position);
				// arg1.setEnabled(false);
				final AlertDialog.Builder ad = new AlertDialog.Builder(TabNeedReview.this);
				ad.setInverseBackgroundForced(true);// 翻转底色
				ad.setIcon(android.R.drawable.ic_dialog_info);
				ad.setTitle("请选择记忆方式");
				ad.setCancelable(false);
				ad.setItems(new String[] { "浏览记忆[看单词想中文]", "词义记忆[看单词选词义]", "拼写记忆[看词义写单词]" }, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent i = new Intent();
						switch (which) {
						case 0:
							// 选定的组数，传值到下个界面。
							i.setClass(TabNeedReview.this, ReviewAnwserShow.class);
							i.putExtra("currentLessonNo", lessonNo);
							TabNeedReview.this.startActivity(i);
							break;
						case 1:
							i.setClass(TabNeedReview.this, ReviewRadioShow.class);
							i.putExtra("currentLessonNo", lessonNo);
							TabNeedReview.this.startActivity(i);
							break;
						case 2:
							i.setClass(TabNeedReview.this, ReviewTextShow.class);
							i.putExtra("currentLessonNo", lessonNo);
							TabNeedReview.this.startActivity(i);
							break;
						}
					}
				});
				ad.setNegativeButton("取消", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						dialog.dismiss();
					}
				});
				ad.show();

			}
		});
	}

}
