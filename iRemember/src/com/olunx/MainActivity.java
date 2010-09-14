/*
 *author:olunx
 *date:2009-10-10
 */

package com.olunx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.olunx.option.mandict.OptionManDict;
import com.olunx.option.manual.OptionManual;
import com.olunx.option.preview.TabPreviewInit;
import com.olunx.option.review.OptionReview;
import com.olunx.option.search.TabSearch;
import com.olunx.option.settings.OptionSettings;
import com.olunx.util.Config;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Process;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity {

	public static Context context;
	
	public MainActivity(){
		context = this;
	}
	
	private ArrayList<Map<String, Object>> list = null;
	private ListView listview = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		System.out.println("test...");
		
		if (Config.init().isFirstRun()) {
			final ProgressDialog pd = new ProgressDialog(context);
			pd.setTitle(getString(R.string.init_dicts));
			pd.setMessage(getString(R.string.dialog_msg_wait));
			pd.setIcon(android.R.drawable.ic_dialog_info);
			pd.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface arg0) {
					Toast.makeText(context, R.string.init_finish, Toast.LENGTH_LONG).show();
				}
			});
			pd.show();
			new Thread() {
				@Override
				public void run() {
					Config.init().initInstall();//初始化软件
					pd.dismiss();
				}
			}.start();
		}
	
		// 显示内容
		showContent();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Process.killProcess(android.os.Process.myPid());
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.dialog_title_alert)
			.setMessage(R.string.sure_to_close_app)
			.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.setPositiveButton(R.string.btn_sure, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					finish();
				}
			}).show();
			
			return true;
			
		}else {
			return super.onKeyDown(keyCode, event);
		}
		
	}

	public void showContent() {
		list = new ArrayList<Map<String, Object>>();

		Map<String, Object> map;
		map = new HashMap<String, Object>();
		map.put(getString(R.string.options), getString(R.string.option_preview_title));
		map.put(getString(R.string.description), getString(R.string.option_preview_desc));
		map.put(getString(R.string.image), R.drawable.option_preview);
		list.add(map);

		map = new HashMap<String, Object>();
		map.put(getString(R.string.options), getString(R.string.option_review_title));
		map.put(getString(R.string.description), getString(R.string.option_review_desc));
		map.put(getString(R.string.image), R.drawable.option_review);
		list.add(map);

		map = new HashMap<String, Object>();
		map.put(getString(R.string.options), getString(R.string.option_search_title));
		map.put(getString(R.string.description), getString(R.string.option_search_desc));
		map.put(getString(R.string.image), R.drawable.option_search);
		list.add(map);

		map = new HashMap<String, Object>();
		map.put(getString(R.string.options), getString(R.string.option_mandict_title));
		map.put(getString(R.string.description), getString(R.string.option_mandict_desc));
		map.put(getString(R.string.image), R.drawable.option_mandict);
		list.add(map);

		map = new HashMap<String, Object>();
		map.put(getString(R.string.options), getString(R.string.option_sync_title));
		map.put(getString(R.string.description), getString(R.string.option_sync_desc));
		map.put(getString(R.string.image), R.drawable.option_sync);
		list.add(map);

		map = new HashMap<String, Object>();
		map.put(getString(R.string.options), getString(R.string.option_manual_title));
		map.put(getString(R.string.description), getString(R.string.option_manual_desc));
		map.put(getString(R.string.image), R.drawable.option_manual);
		list.add(map);

		SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.main_view, new String[] { getString(R.string.options),
				getString(R.string.description), getString(R.string.image) }, new int[] { R.id.main_view_tv_01, R.id.main_view_tv_02,
				R.id.main_view_iv });

		listview = new ListView(this);
		// listview.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.bg));
		// listview.setDivider(this.getResources().getDrawable(android.R.drawable.divider_horizontal_textfield));
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new MainOptionClickListener());

		setContentView(listview);
	}

	// 点击事件
	class MainOptionClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {

			// 获取上下文信息
			Context context = parent.getContext();
			switch (position) {
			case 0: {
				Intent i = new Intent();
				i.setClass(context, TabPreviewInit.class);
				context.startActivity(i);
				break;
			}
			case 1: {
				Intent i = new Intent();
				i.setClass(context, OptionReview.class);
				context.startActivity(i);
				break;
			}
			case 2: {
				Intent i = new Intent();
				i.setClass(context, TabSearch.class);
				context.startActivity(i);
				break;
			}
			case 3: {
				Intent i = new Intent();
				i.setClass(context, OptionManDict.class);
				context.startActivity(i);
				break;
			}
			case 4: {
				Intent i = new Intent();
				i.setClass(context, OptionSettings.class);
				context.startActivity(i);
				break;
			}
			case 5: {
				Intent i = new Intent();
				i.setClass(context, OptionManual.class);
				context.startActivity(i);
				break;
			}
			default: {
				Toast.makeText(context, R.string.no_this_function, Toast.LENGTH_SHORT).show();
			}
			}

		}

	}
}