package com.olunx.option.review;

import java.util.ArrayList;
import java.util.HashMap;

import com.olunx.R;
import com.olunx.db.RememberHelper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class TabNoReview extends Activity {

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
		records = helper.getRecords(false);

		// 创建对象
		items = new ArrayList<HashMap<String, String>>();

		// 临时存储对象
		HashMap<String, String> tempMap;

		for (HashMap<String, String> record : records) {
			tempMap = new HashMap<String, String>();
			tempMap.put(title, "第 " + (Integer.parseInt(String.valueOf(record.get(title))) + 1) + " 组");
			tempMap.put(desc, record.get(desc) + "\n复习时间：" + record.get("复习时间"));
			items.add(tempMap);
		}
	}

	private void showList() {
		SimpleAdapter adapter = new SimpleAdapter(this, items, android.R.layout.simple_list_item_2, new String[] { this.title, this.desc },
				new int[] { android.R.id.text1, android.R.id.text2 });
		listview = new ListView(this);
		listview.setAdapter(adapter);
		this.setContentView(listview);
	}
}
