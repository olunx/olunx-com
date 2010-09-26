package com.olunx.irss.activity.settings;

import com.olunx.irss.R;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class Settings extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setTitle("软件设置");
		
		final TabHost tabHost = getTabHost();

		//需要自定义layout时才需要执行此句
		//LayoutInflater.from(this).inflate(R.layout.threetab, tabHost.getTabContentView(), true);
		
		TabSpec ts1 = tabHost.newTabSpec("阅读设置");
		//使用selector配置动态切换tab的图标
		ts1.setIndicator("阅读设置", getResources().getDrawable(R.drawable.common_phone));
		ts1.setContent(new Intent(this, TabDisplay.class));
		tabHost.addTab(ts1);
		
		TabSpec ts2 = tabHost.newTabSpec("更新设置");
		ts2.setIndicator("更新设置", getResources().getDrawable(R.drawable.common_update));
		ts2.setContent(new Intent(this, TabUpdate.class));
		tabHost.addTab(ts2);
		
		TabSpec ts3 = tabHost.newTabSpec("软件设置");
		ts3.setIndicator("软件设置", getResources().getDrawable(R.drawable.common_config));
		ts3.setContent(new Intent(this, TabSoftware.class));
		tabHost.addTab(ts3);
		
	}
}
