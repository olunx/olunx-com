package com.olunx.option.mandict;

import com.olunx.R;
import com.olunx.util.Config;

import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.InputType;
import android.util.Log;

public class TabDictSet extends PreferenceActivity {

	private PreferenceScreen root = null;
	private PreferenceScreen dictPathPref = null;// 词典路径
	private PreferenceScreen defaultDictPref = null;// 默认使用词典
	private EditTextPreference eachLesWCPref = null;// 每课单词数
	private ListPreference chasetPref = null;// 词库文件编码
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		root = getPreferenceManager().createPreferenceScreen(this);
		this.createPreScreen();
	}

	private void createPreScreen() {

		// 词典目录
		dictPathPref = getPreferenceManager().createPreferenceScreen(this);
		dictPathPref.setTitle(R.string.list_title_dict_dir);
		dictPathPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent();
				i.putExtra(Config.SELECTTYPE, Config.SELECT_DICTDIR);
				i.setClass(TabDictSet.this, TabDirSelect.class);
				startActivityForResult(i, 0);// 要获取返回值，必需用此方法
				return false;
			}
		});
		root.addPreference(dictPathPref);

		// 当前记忆词库
		defaultDictPref = getPreferenceManager().createPreferenceScreen(this);
		defaultDictPref.setTitle(R.string.list_title_dict_current_use);
		defaultDictPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent();
				i.putExtra(Config.DICTTYPE, Config.DICTTYPE_CSV);
				i.setClass(TabDictSet.this, TabDictList.class);
				startActivityForResult(i, 0);// 要获取返回值，必需用此方法
				return false;
			}
		});
		root.addPreference(defaultDictPref);

		// 每课单词数
		eachLesWCPref = new EditTextPreference(this);
		eachLesWCPref.setKey("config_each_lesson_word_count");// 暂存配置，无其它关联。
		eachLesWCPref.setDialogTitle(R.string.dialog_title_each_lesson_word_count);
		eachLesWCPref.setDialogIcon(android.R.drawable.ic_dialog_info);
		eachLesWCPref.setTitle(R.string.list_title_each_lesson_word_count);
		eachLesWCPref.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		eachLesWCPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				eachLesWCPref.getEditText().setText(Config.init().getEachLessonWordCount());
				return false;
			}
		});
		eachLesWCPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Log.i("editor", eachLesWCPref.getEditText().getText().toString());
				Config.init().setEachLessonWordCount(eachLesWCPref.getEditText().getText().toString());
				eachLesWCPref.setSummary(Config.init().getEachLessonWordCountDes());
				return false;
			}
		});
		root.addPreference(eachLesWCPref);

		chasetPref = new ListPreference(this);
		chasetPref.setKey("config_dict_charset");
		chasetPref.setTitle(R.string.dict_charset);
		chasetPref.setValue(Config.init().getDictCharset());//设置默认的词典编码
		chasetPref.setEntries(new String[]{"GBK","GB2312","Big5","UTF-8"});
		chasetPref.setEntryValues(new String[]{"GBK","GB2312","Big5","UTF-8"});
		chasetPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Log.i("newValue", String.valueOf(newValue));
				Config.init().setDictCharset( String.valueOf(newValue));
				chasetPref.setSummary(Config.init().getDictCharset());
				chasetPref.setValue(String.valueOf(newValue));
				return false;
			}});
		root.addPreference(chasetPref);
		
		// 例句词典设置
		PreferenceCategory transSetPrefCat = new PreferenceCategory(this);
		transSetPrefCat.setTitle("");
		root.addPreference(transSetPrefCat);


		
		refreshPref();
		setPreferenceScreen(root);

//		// 貌似是sdk的bug，要用代码定义依赖，就要这样做，在setPreferenceScreen方法后。
//		getPreferenceManager().findPreference("realpeople_function").setDependency("sents_function");
	}

	//显示配置信息
	private void refreshPref() {
		Log.i("refresh", Config.init().getDictDir());
		dictPathPref.setSummary(Config.init().getDictDir());
		chasetPref.setSummary(Config.init().getDictCharset());
		defaultDictPref.setSummary(Config.init().getCurrentUseDictName());
		eachLesWCPref.setSummary(Config.init().getEachLessonWordCountDes());
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

}
