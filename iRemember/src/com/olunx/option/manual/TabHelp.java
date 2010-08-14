package com.olunx.option.manual;

import com.olunx.R;
import com.olunx.util.Config;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class TabHelp extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.textview_full);

		TextView tv = (TextView) this.findViewById(R.id.TextView01);
		tv.setText(Config.init().getDataFromAssets( getString(R.string.help_file_path)));
	}

}
