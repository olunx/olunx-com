/*
 *author:olunx
 *date:2009-10-10
 */

package com.olunx.option.settings;

import com.olunx.R;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class OptionSettings extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setTitle("软件管理");
		
		final TabHost tabHost = getTabHost();

		TabSpec ts1 = tabHost.newTabSpec("发音功能");
		ts1.setIndicator("发音功能", getResources().getDrawable(R.drawable.common_sound));
		ts1.setContent(new Intent(this, TabSoundSet.class));
		tabHost.addTab(ts1);
		
		TabSpec ts2 = tabHost.newTabSpec("软件数据");
		ts2.setIndicator("软件数据", getResources().getDrawable(R.drawable.common_phone));
		ts2.setContent(new Intent(this, TabDataSet.class));
		tabHost.addTab(ts2);
		
	}
}
