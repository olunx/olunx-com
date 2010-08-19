package com.olunx.option.settings;

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

public class TabDataSet extends PreferenceActivity {

	private PreferenceScreen root = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		root = getPreferenceManager().createPreferenceScreen(this);
		this.createPreScreen();
	}

	private void createPreScreen() {

		// 数据备份
		PreferenceCategory manualSyncPrefCat = new PreferenceCategory(this);
		manualSyncPrefCat.setTitle("数据备份");
		root.addPreference(manualSyncPrefCat);

		DialogPre backup = new DialogPre(this, null);
		backup.setDialogIcon(android.R.drawable.ic_dialog_info);
		backup.setTitle(R.string.local_backup);
		backup.setSummary("备份个人记忆数据和软件设置。");
		backup.setDialogTitle("提示");
		backup.setDialogMessage("此操作会覆盖原有的备份数据！");
		manualSyncPrefCat.addPreference(backup);

		DialogPre restore = new DialogPre(this, null);
		restore.setDialogIcon(android.R.drawable.ic_dialog_info);
		restore.setTitle(R.string.local_restore);
		restore.setSummary("从SDCard中还原最近备份的数据。");
		restore.setDialogTitle("提示");
		restore.setDialogMessage("此操作会覆盖现有的记忆数据和软件设置！");
		manualSyncPrefCat.addPreference(restore);

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
							Utils.init().copyBinFile(Config.FILE_SDCARD_CONFIG, Config.BACKUP_FILE_SDCARD_CONFIG);
							Utils.init().copyBinFile(Config.FILE_SDCARD_DATABASE, Config.BACKUP_FILE_SDCARD_DATABASE);
							pd.dismiss();
						}
					}.start();

				} else if (title.equalsIgnoreCase(getString(R.string.local_restore))) {
					pd.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface arg0) {
							Toast.makeText(context, "还原成功", Toast.LENGTH_LONG).show();
						}
					});

					new Thread() {
						@Override
						public void run() {
							Utils.init().copyBinFile(Config.BACKUP_FILE_SDCARD_CONFIG, Config.FILE_SDCARD_CONFIG);
							Utils.init().copyBinFile(Config.BACKUP_FILE_SDCARD_DATABASE, Config.FILE_SDCARD_DATABASE);
							pd.dismiss();
						}
					}.start();

				} else if (title.equalsIgnoreCase(getString(R.string.clear_data))) {
					pd.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface arg0) {
							Toast.makeText(context, "清除数据成功，请重新启动程序。", Toast.LENGTH_LONG).show();
						}
					});

					new Thread() {
						@Override
						public void run() {
							Config.init().setDefaultConfig();
							pd.dismiss();
						}
					}.start();

				}
			}

			super.onClick(dialog, which);
		}

	}
}
