package com.olunx.irss.activity;

import com.olunx.irss.R;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.KeyEvent;

public class UpdateSettings extends PreferenceActivity {

	private final String TAG = "com.olunx.irss.activity.UpdateMainSettings";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.update_settings);

//		 Preference fontColor = findPreference("update_articles_count");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		Log.i(TAG, "preference click");
		switch (preference.getOrder()) {
		case 0: {
			break;
		}
		case 1: {
			break;
		}
		case 2: {
			break;
		}
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

}
