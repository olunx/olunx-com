/*
 *author:olunx
 *date:2009-10-10
 */

package com.olunx.option.search;

import com.olunx.R;
import com.olunx.util.FetchWord;
import com.olunx.util.Word;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class TabSearch extends Activity {

	private EditText queryEt;
	private Button queryBtn;
	private TextView nameTv;
	private TextView phoneticsTv;
	private TextView translationTv;
	private TextView sentsTv;
	private ImageButton speakBtn;

	Word netWord = new Word();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTitle(getString(R.string.option_search_title));
		this.setContentView(R.layout.search_word);

		queryEt = (EditText) this.findViewById(R.id.EditText01);
		queryBtn = (Button) this.findViewById(R.id.Button01);
		queryBtn.setText("查找");
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
		phoneticsTv = (TextView) this.findViewById(R.id.TextView02);
		Typeface font = Typeface.createFromAsset(getAssets(), "KingSoft-Phonetic-Android.ttf");
		phoneticsTv.setTypeface(font);
		translationTv = (TextView) this.findViewById(R.id.TextView03);
		sentsTv = (TextView) this.findViewById(R.id.TextView04);
		speakBtn = (ImageButton) this.findViewById(R.id.ImageButton01);
		speakBtn.setVisibility(Button.INVISIBLE);
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
				if (netWord == null) {
					Toast.makeText(TabSearch.this, "没有数据！", Toast.LENGTH_SHORT).show();
				}else {
					nameTv.setText(netWord.getWord());
					phoneticsTv.setText(netWord.getPhonetic());
					Log.i("phonetics",netWord.getPhonetic());
					translationTv.setText(netWord.getTranslation());
					sentsTv.setText(netWord.getSentences());
				}
			}
		});
		pd.show();
		new Thread() {
			public void run() {
				FetchWord fetch = new FetchWord();
				netWord = fetch.getWord(thisWord);
				pd.dismiss();
			}
		}.start();
	}
}
