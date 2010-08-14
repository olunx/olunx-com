package com.olunx.option.sync;

import com.olunx.R;
import com.olunx.util.Config;
import com.olunx.util.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
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
		backup.setDialogIcon(android.R.drawable.ic_dialog_info);
		backup.setTitle(R.string.local_backup);
		backup.setSummary("手动备份数据到本地文件夹");
		backup.setDialogTitle("备份");
		backup.setDialogMessage("备份到/sdcard/iremember/");
		root.addPreference(backup);

		DialogPre undo = new DialogPre(this, null);
		undo.setDialogIcon(android.R.drawable.ic_dialog_info);
		undo.setTitle(R.string.local_undo);
		undo.setSummary("手动还原历史数据到软件");
		undo.setDialogTitle("备份");
		undo.setDialogMessage("从/sdcard/iremember/还原");
		root.addPreference(undo);

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
		root.addPreference(upload);

		DialogPre download = new DialogPre(this, null);
		download.setDialogIcon(android.R.drawable.ic_dialog_info);
		download.setTitle(R.string.download_data);
		download.setSummary("从Google Doc服务器获取备份数据");
		download.setDialogTitle("提示");
		download.setDialogMessage("是否下载数据到本地？");
		download.setEnabled(false);
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
							Utils.init().copyFile(Config.FILE_SYSTEM_CONFIG, Config.FILE_SDCARD_CONFIG);
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
							Utils.init().copyFile(Config.FILE_SDCARD_CONFIG, Config.FILE_SYSTEM_CONFIG);
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
