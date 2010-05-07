package com.olunx.option.sync;

import com.olunx.R;
import com.olunx.util.Config;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.DialogPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.AttributeSet;
import android.widget.Toast;

public class TabSync extends PreferenceActivity {

	private PreferenceScreen root = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		root = getPreferenceManager().createPreferenceScreen(this);
		this.createPreScreen();
	}

	private void createPreScreen() {

		// 本地备份
		PreferenceCategory backupPrefCat = new PreferenceCategory(this);
		backupPrefCat.setTitle("本地备份");
		root.addPreference(backupPrefCat);

		final DialogPre backup = new DialogPre(this, null);
		backup.setTitle(R.string.local_backup);
		backup.setSummary("手动备份数据到本地文件夹");
		backup.setDialogTitle("备份");
		backup.setDialogMessage("是否确定备份数据到SDCard?");
		root.addPreference(backup);

		DialogPre undo = new DialogPre(this, null);
		undo.setTitle(R.string.local_undo);
		undo.setSummary("手动还原历史数据到软件");
		undo.setDialogTitle("备份");
		undo.setDialogMessage("选择备份数据保存的目录");
		root.addPreference(undo);

		// 网络备份
		PreferenceCategory manualSyncPrefCat = new PreferenceCategory(this);
		manualSyncPrefCat.setTitle("网络备份");
		root.addPreference(manualSyncPrefCat);

		DialogPre upload = new DialogPre(this, null);
		upload.setTitle(R.string.upload_data);
		upload.setSummary("上传本地数据到Google Doc服务器");
		upload.setDialogTitle("提示");
		upload.setDialogMessage("是否上传数据到服务器？");
		root.addPreference(upload);

		DialogPre download = new DialogPre(this, null);
		download.setTitle(R.string.download_data);
		download.setSummary("从Google Doc服务器获取备份数据");
		download.setDialogTitle("提示");
		download.setDialogMessage("是否下载数据到本地？");
		root.addPreference(download);

		// 自动同步
		// PreferenceCategory autoSyncPrefCat = new PreferenceCategory(this);
		// autoSyncPrefCat.setTitle("自动同步");
		// root.addPreference(autoSyncPrefCat);
		//
		// final CheckBoxPreference autoSyncPref = new CheckBoxPreference(this);
		// autoSyncPref.setTitle("自动同步");
		// autoSyncPref.setSummary("请确保你的Google账户可用");
		// root.addPreference(autoSyncPref);

		// 其它设置
		PreferenceCategory clearPrefCat = new PreferenceCategory(this);
		clearPrefCat.setTitle("其它设置");
		root.addPreference(clearPrefCat);

		DialogPre clear = new DialogPre(this, null);
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
				if (title.equalsIgnoreCase(getString(R.string.local_backup))) {
					new Thread() {
						public void run() {
							Backup backup = new Backup();
							backup.copyFile(Config.FILE_SYSTEM_CONFIG, Config.FILE_SDCARD_CONFIG);
							backup.copyFile(Config.FILE_SYSTEM_DATABASE, Config.FILE_SDCARD_DATABASE);
//							new Handler(){
//								public void handleMessage(Message msg){
//									Toast.makeText(context, "备份成功", Toast.LENGTH_LONG).show();
//								}
//							};
						}
					}.start();

				} else if (title.equalsIgnoreCase(getString(R.string.local_undo))) {
					new Thread() {
						public void run() {
							Backup backup = new Backup();
							backup.copyFile(Config.FILE_SDCARD_CONFIG, Config.FILE_SYSTEM_CONFIG);
							backup.copyFile(Config.FILE_SDCARD_DATABASE, Config.FILE_SYSTEM_DATABASE);
							Toast.makeText(context, "还原成功", Toast.LENGTH_LONG).show();
						}
					}.start();

				} else if (title.equalsIgnoreCase(getString(R.string.upload_data))) {

				} else if (title.equalsIgnoreCase(getString(R.string.download_data))) {

				} else if (title.equalsIgnoreCase(getString(R.string.clear_data))) {

				}
			}

			super.onClick(dialog, which);
		}

	}
}
