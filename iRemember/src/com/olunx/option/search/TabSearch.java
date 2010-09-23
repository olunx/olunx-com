/*
 *author:olunx
 *date:2009-10-10
 */

package com.olunx.option.search;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.olunx.R;
import com.olunx.stardict.SeekWord;
import com.olunx.util.Config;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class TabSearch extends Activity {

	private EditText queryEt;
	private ImageButton queryBtn;
	private TextView nameTv;
	private TextView phoneticsTv;
	private TextView sentsTv;
	private ImageButton speakBtn;
	
	private Map<String, String> word;

	SeekWord seek;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTitle(getString(R.string.option_search_title));
		this.setContentView(R.layout.search_word);
		
		if(Config.init().getDictPath(Config.init().getCurrentUseTransDictName()).equalsIgnoreCase("null")) {
			Toast.makeText(this, "你没有设置例句词典！(+﹏+) ", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		seek = new SeekWord(Config.init().getDictPath(Config.init().getCurrentUseTransDictName()));

		queryEt = (EditText) this.findViewById(R.id.EditText01);
		queryEt.setText("");
		queryBtn = (ImageButton) this.findViewById(R.id.ImageButton02);
		queryBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String queryWord = queryEt.getText().toString().trim();
				if (queryWord != "" && !queryWord.equals("")) {
					Log.i("queryWord", queryWord);
					getWord(queryWord);
				}
			}
		});
		nameTv = (TextView) this.findViewById(R.id.TextView01);
		nameTv.setText("");
		phoneticsTv = (TextView) this.findViewById(R.id.TextView02);
		Typeface font = Typeface.createFromAsset(getAssets(), Config.FONT_KINGSOFT_PATH);
		phoneticsTv.setTypeface(font);
		phoneticsTv.setText("");
		sentsTv = (TextView) this.findViewById(R.id.TextView04);
		sentsTv.setText("");
		speakBtn = (ImageButton) this.findViewById(R.id.ImageButton01);
		speakBtn.setVisibility(View.INVISIBLE);
		speakBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
	}

	//获取网络单词数据
	private void getWord(final String thisWord) {
		final ProgressDialog pd = new ProgressDialog(this);
		pd.setIcon(android.R.drawable.ic_dialog_info);
		pd.setTitle(R.string.dialog_title_loding_data);
		pd.setMessage(getString(R.string.dialog_msg_wait));
		pd.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface arg0) {
				if (word == null) {
					Toast.makeText(TabSearch.this, "没有数据！", Toast.LENGTH_SHORT).show();
				}else {
					nameTv.setText(thisWord);
					phoneticsTv.setText(word.get("音标"));
					sentsTv.setText(word.get("解释"));
				}
			}
		});
		pd.show();
		new Thread() {
			@Override
			public void run() {
				word = new HashMap<String, String>();
				word.put("单词", thisWord);
				String result = seek.getTrans(thisWord);
				
				Pattern p = Pattern.compile("/(.*?)/");
				Matcher m = p.matcher(result);
				
				if(m.find()) {
					String phonetic = m.group();
					word.put("音标", phonetic.replace("/", ""));
					
					word.put("解释", result.replace(phonetic, "").replaceAll("\\*", "\n\\*"));
				}else {
					word.put("解释", result);
				}
				
				
				pd.dismiss();
			}
		}.start();
	}
}
