package com.olunx.option.settings;

import com.olunx.option.mandict.TabDictList;
import com.olunx.option.mandict.TabDirSelect;
import com.olunx.util.Config;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;

public class TabSoundSet extends PreferenceActivity {

	private PreferenceScreen root = null;
	private CheckBoxPreference ttsPref = null;
	private CheckBoxPreference realPref = null;
	private PreferenceScreen soundDirPref = null;;// 语音文件目录
	private CheckBoxPreference sentsPref = null;
	private PreferenceScreen transDictPref = null;// 例句库词典
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		root = getPreferenceManager().createPreferenceScreen(this);
		this.createPreScreen();
	}

	private void createPreScreen() {

		// 合成发音功能
		ttsPref = new CheckBoxPreference(this);
		ttsPref.setKey("tts_speech_function");
		ttsPref.setTitle("合成发音");
		ttsPref.setSummary("利用系统自带的合成发音功能。");
		
		// 真人发音功能
		realPref = new CheckBoxPreference(this);
		realPref.setKey("real_speech_function");
		realPref.setTitle("真人发音");
		realPref.setSummary("使用真人发音库。");
		
		ttsPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (ttsPref.isChecked()) {
					// 设置发音类型
					Config.init().setSpeechType(Config.SPEECH_TTS);
					Config.init().setCanSpeech(true);
					realPref.setEnabled(false);
				} else {
					Config.init().setCanSpeech(false);
					realPref.setEnabled(true);
				}
				return false;
			}
		});
		root.addPreference(ttsPref);
		
		realPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (realPref.isChecked()) {
					// 设置发音类型
					Config.init().setSpeechType(Config.SPEECH_REAL);
					Config.init().setCanSpeech(true);
					ttsPref.setEnabled(false);
				} else {
					Config.init().setCanSpeech(false);
					ttsPref.setEnabled(true);
				}
				return false;
			}
		});
		root.addPreference(realPref);
		
		soundDirPref = getPreferenceManager().createPreferenceScreen(this);
		soundDirPref.setKey("sound_dir");
		soundDirPref.setTitle("语音文件目录：");
		soundDirPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent();
				i.putExtra(Config.SELECTTYPE, Config.SELECT_SOUNDDIR);
				i.setClass(TabSoundSet.this, TabDirSelect.class);
				startActivityForResult(i, 0);// 要获取返回值，必需用此方法
				return false;
			}
		});
		root.addPreference(soundDirPref);

		// 例句
		PreferenceCategory speechSetPrefCat = new PreferenceCategory(this);
		speechSetPrefCat.setTitle("例句功能");
		root.addPreference(speechSetPrefCat);
		
		// 例句功能
		sentsPref = new CheckBoxPreference(this);
		sentsPref.setKey("sents_function");
		sentsPref.setTitle("例句功能");
		sentsPref.setSummary("开启此功能需要设置例句词典。");
		sentsPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				boolean flag = sentsPref.isChecked();
				if(flag) {
					Config.init().setCanGetTransDict(true);
				}else {
					Config.init().setCanGetTransDict(false);
				}
				return false;
			}
		});
		speechSetPrefCat.addPreference(sentsPref);
		
		// 例句库词典
		transDictPref = getPreferenceManager().createPreferenceScreen(this);
		transDictPref.setTitle("当前使用的例句库：");
		transDictPref.setKey("sents_dict");
		transDictPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent();
				i.putExtra(Config.DICTTYPE, Config.DICTTYPE_STARDICT);
				i.setClass(TabSoundSet.this, TabDictList.class);
				startActivityForResult(i, 0);// 要获取返回值，必需用此方法
				return false;
			}
		});
		speechSetPrefCat.addPreference(transDictPref);

		refreshPref();
		setPreferenceScreen(root);
		
		// 貌似是sdk的bug，要用代码定义依赖，就要这样做，在setPreferenceScreen方法后。
		getPreferenceManager().findPreference("sound_dir").setDependency("real_speech_function");
		getPreferenceManager().findPreference("sents_dict").setDependency("sents_function");
	}
	
	//显示配置信息
	private void refreshPref() {
		switch(Config.init().getSpeechType()) {
		case Config.SPEECH_TTS:
			realPref.setEnabled(false);
			break;
		case Config.SPEECH_REAL:
			ttsPref.setEnabled(false);
			break;
		default :
			ttsPref.setChecked(false);
			realPref.setChecked(false);
			break;
		}
		if(Config.init().isCanGetTransDict()) {
			sentsPref.setChecked(true);
		}else {
			sentsPref.setChecked(false);
		}
		soundDirPref.setSummary(Config.init().getSoundDir());
		transDictPref.setSummary(Config.init().getCurrentUseTransDictName());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case RESULT_OK: {
			this.refreshPref();
			break;
		}
		case RESULT_CANCELED:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		if(Config.init().getCurrentUseTransDictName().equalsIgnoreCase("null")) {
			Config.init().setCanGetTransDict(false);
		}
		super.onDestroy();
	}
	
	
}
