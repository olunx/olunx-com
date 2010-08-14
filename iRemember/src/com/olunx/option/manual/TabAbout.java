package com.olunx.option.manual;

import com.olunx.R;
import com.olunx.util.Config;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class TabAbout extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.manual_about);
		
		TextView tv = (TextView) this.findViewById(R.id.TextView02);
		tv.setText(Config.init().getDataFromAssets( getString(R.string.about_file_path)));
	}

}
