package com.olunx.option.settings;

import com.olunx.R;
import com.olunx.util.Config;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

public class TabSettings extends PreferenceActivity {

	private PreferenceScreen root = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		root = getPreferenceManager().createPreferenceScreen(this);
		this.createPreScreen();
	}

	private void createPreScreen() {

		// 
		PreferenceCategory otherSetPrefCat = new PreferenceCategory(this);
		otherSetPrefCat.setTitle("发音、例句");
		root.addPreference(otherSetPrefCat);

		// 发音功能
		final CheckBoxPreference ttsPref = new CheckBoxPreference(this);
		ttsPref.setKey("tts_function");
		ttsPref.setTitle("单词发音");
		ttsPref.setSummary("利用系统自带的文本发音功能。");
		ttsPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if(ttsPref.isChecked()) {
					//设置发音类型
					Config.init().setSpeechType(1);
					Config.init().setCanSpeech(true);
					Log.i("flag()", Config.init().getSpeechType());
				}else {
					Config.init().setSpeechType(0);
					Config.init().setCanSpeech(false);
				}
				return false;
			}
		});
		otherSetPrefCat.addPreference(ttsPref);
		
		// 例句功能
		final CheckBoxPreference sentsPref = new CheckBoxPreference(this);
		sentsPref.setKey("sents_function");
		sentsPref.setTitle("例句功能");
		sentsPref.setSummary("开启此功能需要设置例句词典。");
		sentsPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
//				boolean flag = sentsPref.isChecked();
				return false;
			}
		});
		otherSetPrefCat.addPreference(sentsPref);
		
		// 网络备份
		PreferenceCategory manualSyncPrefCat = new PreferenceCategory(this);
		manualSyncPrefCat.setTitle("网络备份");
		root.addPreference(manualSyncPrefCat);

		DialogPre upload = new DialogPre(this, null);
		upload.setDialogIcon(android.R.drawable.ic_dialog_info);
		upload.setTitle(R.string.upload_data);
		upload.setSummary("上传本地数据到Google Doc服务器");
		upload.setDialogTitle("提示");
		upload.setDialogMessage("是否上传数据到服务器？");
		upload.setEnabled(false);
		manualSyncPrefCat.addPreference(upload);

		DialogPre download = new DialogPre(this, null);
		download.setDialogIcon(android.R.drawable.ic_dialog_info);
		download.setTitle(R.string.download_data);
		download.setSummary("从Google Doc服务器获取备份数据");
		download.setDialogTitle("提示");
		download.setDialogMessage("是否下载数据到本地？");
		download.setEnabled(false);
		manualSyncPrefCat.addPreference(download);

		// 其它设置
		PreferenceCategory clearPrefCat = new PreferenceCategory(this);
		clearPrefCat.setTitle("其它设置");
		root.addPreference(clearPrefCat);

		DialogPre clear = new DialogPre(this, null);
		clear.setDialogIcon(android.R.drawable.ic_dialog_info);
		clear.setTitle(R.string.clear_data);
		clear.setSummary("清除所有数据，还原到初始状态。");
		clear.setDialogTitle("警告");
		clear.setDialogMessage("是否真的要清除所有数据？");
		root.addPreference(clear);

		setPreferenceScreen(root);
	}

	class DialogPre extends DialogPreference {

		private Context context;

		public DialogPre(Context context, AttributeSet attrs) {
			super(context, attrs);
			this.context = context;
		}

		@Override
		public void onDismiss(DialogInterface dialog) {
			super.onDismiss(dialog);
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			System.out.println(which);
			if (which == -1) {
				String title = (String) this.getTitle();
				
				final ProgressDialog pd = new ProgressDialog(context);
				pd.setIcon(android.R.drawable.ic_dialog_alert);
				pd.setTitle("正在处理数据");
				pd.setMessage(getString(R.string.dialog_msg_wait));
				pd.show();
				
				if (title.equalsIgnoreCase(getString(R.string.local_backup))) {
					pd.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface arg0) {
							Toast.makeText(context, "备份成功", Toast.LENGTH_LONG).show();
						}
					});
					
					new Thread() {
						@Override
						public void run() {
//							Utils.init().copyFile(Config.FILE_SYSTEM_CONFIG, Config.FILE_SDCARD_CONFIG);
							pd.dismiss();
						}
					}.start();

				} else if (title.equalsIgnoreCase(getString(R.string.local_undo))) {
					pd.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface arg0) {
							Toast.makeText(context, "还原成功", Toast.LENGTH_LONG).show();
						}
					});
					
					new Thread() {
						@Override
						public void run() {
//							Utils.init().copyFile(Config.FILE_SDCARD_CONFIG, Config.FILE_SYSTEM_CONFIG);
							pd.dismiss();
						}
					}.start();
					
				} else if (title.equalsIgnoreCase(getString(R.string.upload_data))) {
					
					pd.dismiss();

				} else if (title.equalsIgnoreCase(getString(R.string.download_data))) {
					
					pd.dismiss();

				} else if (title.equalsIgnoreCase(getString(R.string.clear_data))) {
					pd.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface arg0) {
							Toast.makeText(context, "清除数据成功。", Toast.LENGTH_LONG).show();
						}
					});
					
					Config.init().setDefaultConfig();
					pd.dismiss();

				}
			}

			super.onClick(dialog, which);
		}

	}
}
