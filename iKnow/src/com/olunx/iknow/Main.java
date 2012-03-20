package com.olunx.iknow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class Main extends Activity {

	private String TAG = "com.olunx.iknow.Main";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 系统状态栏

		setContentView(R.layout.main);

		ImageButton study = (ImageButton) this.findViewById(R.id.btn_study);
		study.setOnClickListener(listener);
		
		ImageButton others = (ImageButton) this.findViewById(R.id.btn_others);
		others.setOnClickListener(listener);
	}

	OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.i(TAG, "click " + v.getId());
			Intent i = new Intent();
			switch (v.getId()) {
			case R.id.btn_study: {
				i.setClass(Main.this, Study.class);
				break;
			}
			case R.id.btn_others: {
				i.setClass(Main.this, Others.class);
				break;
			}
			default: {
				i = null;
			}
			}
			
			if (i != null) {
				Main.this.startActivity(i);
			}
		}
	};
}