package com.olunx.iknow;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;

public class Study extends Activity {

	private String TAG = "com.olunx.iknow.Study";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 系统状态栏

		setContentView(R.layout.study);

	}

	OnClickListener clickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			Log.i(TAG, "onClick");
		}
	};
	
}
