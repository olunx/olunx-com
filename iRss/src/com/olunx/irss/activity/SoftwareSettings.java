package com.olunx.irss.activity;

import com.olunx.irss.R;
import com.olunx.irss.util.Config;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;

public class SoftwareSettings extends PreferenceActivity {

	private final String TAG = "com.olunx.irss.activity.SoftwareSettings";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.software_settings);

//		 Preference resetConfig = findPreference("reset_config");
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		Log.i(TAG, "preference click");
		String key = preference.getKey();
		if(key.equals("reset_config")) {
			Config.init().setAccountInputed("false");
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

}
