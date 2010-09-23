package com.olunx.option.mandict;

import com.olunx.R;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class OptionManDict extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTitle(R.string.option_mandict_title);
		
		final TabHost tabHost = getTabHost();

		//需要自定义layout时才需要执行此句
		//LayoutInflater.from(this).inflate(R.layout.threetab, tabHost.getTabContentView(), true);
		
		TabSpec ts1 = tabHost.newTabSpec(getString(R.string.tab_dictset_title));
		//使用selector配置动态切换tab的图标
		ts1.setIndicator(getString(R.string.tab_dictset_title), getResources().getDrawable(R.drawable.common_config));
		ts1.setContent(new Intent(this, TabDictSet.class));
		tabHost.addTab(ts1);
		
		TabSpec ts2 = tabHost.newTabSpec(getString(R.string.tab_dictlist_title));
		ts2.setIndicator(getString(R.string.tab_dictlist_title), getResources().getDrawable(R.drawable.dictman_dictlist));
		ts2.setContent(new Intent(this, TabDictList.class));
		tabHost.addTab(ts2);
		
		TabSpec ts3 = tabHost.newTabSpec(getString(R.string.tab_dictbrowser_title));
		ts3.setIndicator(getString(R.string.tab_dictbrowser_title), getResources().getDrawable(R.drawable.dictman_dictbrowser));
		ts3.setContent(new Intent(this, TabDirSelect.class));
		tabHost.addTab(ts3);
	}
}
