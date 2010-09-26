package com.olunx.irss.activity.settings;

import com.olunx.irss.R;
import com.olunx.irss.activity.ColorPickerDialog;
import com.olunx.irss.activity.ColorPickerDialog.OnColorChangedListener;
import com.olunx.irss.util.Config;
import com.olunx.irss.util.Utils;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.Html;
import android.util.Log;

public class TabDisplay extends PreferenceActivity {

	private final String TAG = "com.olunx.irss.activity.DisplaySettings";

	private ListPreference fontSize;// 字体大小
	private ListPreference sysFontStyle;// 系统内置样式
	private Preference fontColor;// 字体颜色
	private Preference bgColor;// 背景颜色

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.display_settings);

		fontSize = (ListPreference)findPreference("font_size");
		fontSize.setOnPreferenceChangeListener(changeListener);
		fontSize.setValue(Config.init().getArticleFontSize());

		sysFontStyle = (ListPreference)findPreference("sys_font_style");
		sysFontStyle.setOnPreferenceChangeListener(changeListener);
		sysFontStyle.setValue(Config.init().getSysFontStyle());
		
		fontColor = findPreference("font_color");
		bgColor = findPreference("bg_color");

		refresh();
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		Log.i(TAG, "preference click");
		String key = preference.getKey();
		if (key.equals("font_color")) {
			new ColorPickerDialog(TabDisplay.this, fontColorChanged, Config.init().getArticleFontColor()).show();
		}else if (key.equals("bg_color")) {
			new ColorPickerDialog(TabDisplay.this, bgColorChanged, Config.init().getArticleBgColor()).show();
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	/**
	 * 设置summary
	 */
	private void refresh() {
		fontSize.setSummary(Config.init().getArticleFontSize());
		sysFontStyle.setSummary(Config.init().getSysFontStyle());
		fontColor.setSummary(Html.fromHtml("<a><font color='" + Config.init().getArticleFontColor() + "'>字体颜色预览</font></a>"));
		bgColor.setSummary(Html.fromHtml("<a><font color='" + Config.init().getArticleBgColor() + "'>背景颜色预览</font></a>"));
	}

	/**
	 * 颜色改变，回调事件。
	 */
	OnColorChangedListener fontColorChanged = new OnColorChangedListener() {
		public void colorChanged(int color) {
			String hexColor = Utils.init().ArgbToHexRgb(color);
			fontColor.setSummary(Html.fromHtml("<a><font color='" + hexColor + "'>字体颜色预览</font></a> "));
			Config.init().setArticleFontColor(hexColor);
		}
	};
	
	OnColorChangedListener bgColorChanged = new OnColorChangedListener() {
		public void colorChanged(int color) {
			String hexColor = Utils.init().ArgbToHexRgb(color);
			bgColor.setSummary(Html.fromHtml("<a><font color='" + hexColor + "'>背景颜色预览</font></a> "));
			Config.init().setArticleBgColor(hexColor);
		}
	};

	OnPreferenceChangeListener changeListener = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			Log.i(TAG, "change " + newValue.toString());
			String key = preference.getKey();
			if (key.equals("font_size")) {//字体大小
				String size = String.valueOf(newValue);
				fontSize.setSummary(size);
				fontSize.setValue(size);
				Config.init().setArticleFontSize(size);
			} else if (key.equals("sys_font_style")) {//配色方案
				String style = String.valueOf(newValue);
				sysFontStyle.setSummary(style);
				sysFontStyle.setValue(style);
				
				String font = "#000000";
				String bg = "#ffffff";
				if (style.equals("白天模式")) {
					font = "#000000";
					bg = "#ffffff";
				} else if (style.equals("夜间模式")) {
					font = "#ffffff";
					bg = "#000000";
				} else if (style.equals("护眼模式")) {
					font = "#000000";
					bg = "#cce8cf";
				}
				
				fontColor.setSummary(Html.fromHtml("<a><font color='"+font+"'>字体颜色预览</font></a> "));
				bgColor.setSummary(Html.fromHtml("<a><font color='"+bg+"'>背景颜色预览</font></a> "));
				Config.init().setArticleFontColor(font);
				Config.init().setArticleBgColor(bg);
				Config.init().setSysFontStyle(style);
			}
			return false;
		}
	};
}
