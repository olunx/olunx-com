package com.olunx.irss.activity;

import com.olunx.irss.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.KeyEvent;

public class MainSettings extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.main_settings);

		Preference fontColor = findPreference("font_color");
		fontColor.setOnPreferenceClickListener(listener);
	}

	OnPreferenceClickListener listener = new OnPreferenceClickListener() {

		@Override
		public boolean onPreferenceClick(Preference preference) {
			switch (preference.getOrder()) {
			case 0: {
				break;
			}
			case 1: {
				AlertDialog dialog = new ColorPickerDialog(MainSettings.this, null, 0);
				dialog.setTitle("标题");
				dialog.show();
				break;
			}
			}
			return false;
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		// TODO Auto-generated method stub
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

}
