package com.olunx.iknow;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class Others extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 系统状态栏
		
        setContentView(R.layout.others);
        
        ImageButton index = (ImageButton)this.findViewById(R.id.btn_index);
        index.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				Intent i = new Intent();
//				i.setClass(Others.this, Main.class);
//				Others.this.startActivity(i);
				Others.this.finish();
			}
        });
    }
}
