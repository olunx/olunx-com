package com.olunx.irss.activity.settings;

import com.olunx.irss.R;
import com.olunx.irss.util.Config;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;

public class TabSoftware extends PreferenceActivity {

	private final String TAG = "com.olunx.irss.activity.SoftwareSettings";

	private AlertDialog.Builder ad;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.software_settings);

		ad = new AlertDialog.Builder(TabSoftware.this);
		ad.setIcon(android.R.drawable.ic_dialog_alert);
		ad.setTitle("警告:");
		ad.setMessage("真的要清空软件数据？");
		ad.setCancelable(false);
		ad.setNegativeButton("取消", null);
		ad.setPositiveButton("确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Config.init().setAccountInputed("false");
			}
		});
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		Log.i(TAG, "preference click");
		String key = preference.getKey();
		if(key.equals("reset_config")) {
			ad.show();
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

}
