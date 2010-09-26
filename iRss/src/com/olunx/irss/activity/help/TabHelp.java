package com.olunx.irss.activity.help;

import com.olunx.irss.R;
import com.olunx.irss.util.SysTools;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class TabHelp extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.help_manual);

		TextView tv = (TextView) this.findViewById(R.id.TextView01);
		tv.setText(SysTools.getDataFromAssets(this, getString(R.string.help_file_path)));
	}

}
