package com.olunx;

import com.olunx.activity.AccountActivity;
import com.olunx.activity.SyncActivity;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class Main extends TabActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       

		this.setTitle("iSNS");
		
		final TabHost tabHost = getTabHost();

		//需要自定义layout时才需要执行此句
		//LayoutInflater.from(this).inflate(R.layout.threetab, tabHost.getTabContentView(), true);
		
		TabSpec ts1 = tabHost.newTabSpec("主账户");
		ts1.setIndicator("主账户", getResources().getDrawable(android.R.drawable.star_big_on));
		ts1.setContent(new Intent(this, AccountActivity.class));
		tabHost.addTab(ts1);
		
		TabSpec ts2 = tabHost.newTabSpec("同步账号");
		ts2.setIndicator("同步账号", getResources().getDrawable(android.R.drawable.star_big_off));
		ts2.setContent(new Intent(this, SyncActivity.class));
		tabHost.addTab(ts2);
		
    }
}