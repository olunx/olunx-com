/*
 *author:olunx
 *date:2009-10-10
 */

package com.olunx.option.review;

import com.olunx.R;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class OptionReview extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setTitle("复习记忆");
		
		final TabHost tabHost = getTabHost();

		//需要自定义layout时才需要执行此句
		//LayoutInflater.from(this).inflate(R.layout.threetab, tabHost.getTabContentView(), true);
		
		TabSpec ts1 = tabHost.newTabSpec("需要复习");
		ts1.setIndicator("需要复习", getResources().getDrawable(R.drawable.review_need));
		ts1.setContent(new Intent(this, TabNeedReview.class));
		tabHost.addTab(ts1);
		
		TabSpec ts2 = tabHost.newTabSpec("已学课程");
		ts2.setIndicator("已学课程", getResources().getDrawable(R.drawable.review_finish));
		ts2.setContent(new Intent(this, TabNoReview.class));
		tabHost.addTab(ts2);
		
	}
}
