package com.olunx.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class AccountActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.addSubMenu(0, 0, 0, "添加账号").setIcon(android.R.drawable.ic_menu_add);
		menu.addSubMenu(0, 1, 1, "设置").setIcon(android.R.drawable.ic_menu_preferences);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()) {
		case 0: {
			Intent i = new Intent();
			i.setClass(this, AddPostActivity.class);
			this.startActivity(i);
			break;
		}
		}
		return super.onMenuItemSelected(featureId, item);
	}

}
