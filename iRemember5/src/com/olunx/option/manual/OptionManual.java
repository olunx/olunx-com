/*
 *author:olunx
 *date:2009-10-10
 */

package com.olunx.option.manual;

import com.olunx.R;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class OptionManual extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setTitle("使用帮助");
		
		final TabHost tabHost = getTabHost();

		//需要自定义layout时才需要执行此句
		//LayoutInflater.from(this).inflate(R.layout.threetab, tabHost.getTabContentView(), true);
		
		TabSpec ts1 = tabHost.newTabSpec("使用帮助");
		//使用selector配置动态切换tab的图标
		ts1.setIndicator("使用帮助", getResources().getDrawable(R.drawable.manual_help));
		ts1.setContent(new Intent(this, TabHelp.class));
		tabHost.addTab(ts1);
		
		TabSpec ts2 = tabHost.newTabSpec("关于软件");
		ts2.setIndicator("关于软件", getResources().getDrawable(R.drawable.manual_about));
		ts2.setContent(new Intent(this, TabAbout.class));
		tabHost.addTab(ts2);
		
	}
}
