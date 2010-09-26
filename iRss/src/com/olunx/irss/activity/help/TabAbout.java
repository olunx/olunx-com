package com.olunx.irss.activity.help;

import com.olunx.irss.R;
import com.olunx.irss.util.SysTools;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class TabAbout extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.help_about);

		TextView tv = (TextView) this.findViewById(R.id.TextView02);
		tv.setText(SysTools.getDataFromAssets(this, getString(R.string.about_file_path)));
	}

}
