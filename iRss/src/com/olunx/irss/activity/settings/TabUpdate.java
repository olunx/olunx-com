package com.olunx.irss.activity.settings;

import com.olunx.irss.R;
import com.olunx.irss.util.Config;
import com.olunx.irss.util.SysTools;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

public class TabUpdate extends PreferenceActivity {

	private final String TAG = "com.olunx.irss.activity.UpdateSettings";

	private CheckBoxPreference offlineMode;// 离线阅读
	private AlertDialog.Builder ad;
	private ListPreference articleStoreTime;// 文章保存时间

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.update_settings);

		offlineMode = (CheckBoxPreference) findPreference("offline_read");
		offlineMode.setOnPreferenceChangeListener(changeListener);
		offlineMode.setChecked(Config.init().isOffLineReadMode());

		ad = new AlertDialog.Builder(TabUpdate.this);
		ad.setIcon(android.R.drawable.ic_dialog_info);
		ad.setTitle("提示:");
		ad.setMessage("真的要下载已更新的文章的图片数据？");
		ad.setCancelable(false);
		ad.setNegativeButton("取消", null);
		ad.setPositiveButton("确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.i(TAG, String.valueOf(true));
				SysTools.downloadImagesToStorage(null);
			}
		});

		articleStoreTime = (ListPreference) findPreference("store_article_time");
		articleStoreTime.setOnPreferenceChangeListener(changeListener);
		articleStoreTime.setValue(Config.init().getArticleDataStoreTime());
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		String key = preference.getKey();
		if (key.equals("download_offline_data")) {
			ad.show();
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	OnPreferenceChangeListener changeListener = new OnPreferenceChangeListener() {

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			Log.i(TAG, String.valueOf(newValue));
			
			String key = preference.getKey();
			if (key.equals("offline_read")) {
				boolean value = Boolean.valueOf(String.valueOf(newValue));
				offlineMode.setChecked(value);
				Config.init().setOffLineReadMode(value);
			} else if (key.equals("store_article_time")) {
				String value = String.valueOf(newValue);
				articleStoreTime.setValue(value);
				Config.init().setArticleDataStoreTime(value);
			}
			return false;
		}
	};
}
