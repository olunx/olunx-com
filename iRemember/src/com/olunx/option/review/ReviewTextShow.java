package com.olunx.option.review;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.olunx.R;
import com.olunx.util.Config;
import com.olunx.util.FetchWord;
import com.olunx.util.Speech;
import com.olunx.util.Word;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class ReviewTextShow extends Activity implements OnClickListener {

	private Context context = this;

	// 界面元素
	private EditText wordEt;
	private TextView wordTv;
	private TextView phoneticsTv;
	private TextView translationTv;
	private Button sureBtn;
	private ImageButton speakBtn;

	private ArrayList<HashMap<String, Object>> thisWordList;
//	private ArrayList<String> originWordTranslation = null;
	private int currentLessonNo = 0;
	private int currentWordNo = 0;
	private int totalWordCount = 0;
	private HashMap<String, Object> currentWord = null;

	// 记录当前单词数据
	private String thisWord = null;
	private String thisPhonetics = null;
	private String thisTranslation = null;

	// 按钮文字
	private String SURE;
	private String NEXTWORD;
	private String WORD;
	private String PHONETICS;
	private String TRANSLATION;

	// 更新记忆曲线
	private boolean isCanUpdate = false;

	// 网络单词数据
	private Word netWord = new Word();
	private boolean isCanGetNetWord = false;

	// 标题栏文字
	private String LESSON;
	private String REMEMBERTIMES;

	// 发音设置
	private TextToSpeech speech;
	private String speechType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 按钮字符串
		SURE = this.getString(R.string.btn_sure);
		NEXTWORD = this.getString(R.string.btn_nextword);
		WORD = this.getString(R.string.text_word);
		PHONETICS = this.getString(R.string.text_phonetics);
		TRANSLATION = this.getString(R.string.text_translation);

		// 标题栏文字
		LESSON = this.getString(R.string.text_lesson);
		REMEMBERTIMES = this.getString(R.string.text_remember_times);

		this.setContentView(R.layout.review_text);
		wordEt = (EditText) this.findViewById(R.id.EditText01);
		wordTv = (TextView) this.findViewById(R.id.TextView01);
		wordEt.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView arg0, int keyCode, KeyEvent keyEvent) {
				Log.d("keycode", String.valueOf(keyCode));
				switch (keyCode) {
				case 0: {
					sureAction();
					break;
				}
				}
				return false;
			}
		});
		phoneticsTv = (TextView) this.findViewById(R.id.TextView02);
		Typeface font = Typeface.createFromAsset(getAssets(), "KingSoft-Phonetic-Android.ttf");
		phoneticsTv.setTypeface(font);
		translationTv = (TextView) this.findViewById(R.id.TextView03);
		sureBtn = (Button) this.findViewById(R.id.Button01);
		sureBtn.setText(this.SURE);
		sureBtn.setOnClickListener(this);
		speakBtn = (ImageButton) this.findViewById(R.id.ImageButton01);
		speakBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				speakWord();
			}
		});
		if (!Config.init().isCanSpeech()) {
			speakBtn.setEnabled(false);
		}

		// 是否可以读取网络单词数据
		this.isCanGetNetWord = Config.init().getCanConNetWord();
		Log.i("isCanConWord", String.valueOf(isCanGetNetWord));

		// 获取课程数据
		Bundle i = this.getIntent().getExtras();
		currentLessonNo = i.getInt("currentLessonNo");
		initWords();
	}

	// 显示单词
	private void showWord() {

		this.setTitle(LESSON + " " + (currentLessonNo + 1) + "\t" + REMEMBERTIMES + " " + (currentWordNo + 1) + "/" + totalWordCount);

		// 显示第x个单词
		currentWord = thisWordList.get(currentWordNo);
		this.thisWord = (String) currentWord.get(this.WORD);
		this.thisPhonetics = (String) currentWord.get(this.PHONETICS);
		this.thisTranslation = (String) currentWord.get(this.TRANSLATION);
		this.wordEt.setText("");
		this.wordTv.setText(thisWord);
//		this.wordTv.setVisibility(TextView.INVISIBLE);
		this.wordTv.setHeight(0);
		this.phoneticsTv.setText(this.thisPhonetics);
		this.translationTv.setText(this.thisTranslation);

		// 获取网络单词数据
		this.getNetTrans();
	}

	@Override
	public void onClick(View v) {
		Button btn = (Button) v;
		String text = (String) btn.getText();
		if (text.equals(this.SURE)) {
			sureAction();
		} else if (text.equals(this.NEXTWORD)) {
			this.addRepeatWord();
			this.showNext();
			btn.setText(this.SURE);
		}
	}

	// 确定按钮动作事件
	private void sureAction() {
		String result = wordEt.getText().toString();
		if (result.contains("\n")) {
			result = result.substring(0, result.indexOf("\n"));
		}
		Log.d("text", result);
		if (result.equalsIgnoreCase("") || result == "") {
			Toast.makeText(this, "你没有填写任何答案！", Toast.LENGTH_SHORT).show();
		}else {
			if (result.equalsIgnoreCase(thisWord)) {
				showNext();
			} else {
				sureBtn.setText(NEXTWORD);
				this.wordTv.setHeight(40);
//				this.wordTv.setVisibility(TextView.VISIBLE);
				Toast.makeText(context, "答案错误！", Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * 以下为相同逻辑代码
	 * */
	// 初始化单词学习界面
	private void initWords() {

		// 设置可更新记忆曲线
		this.isCanUpdate = true;

		final ProgressDialog pd = new ProgressDialog(context);
		pd.setTitle(R.string.dialog_title_loding_data);
		pd.setMessage(getString(R.string.dialog_msg_wait));
		pd.setIcon(android.R.drawable.ic_dialog_info);

		pd.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (thisWordList != null && thisWordList.size() > 0) {
					totalWordCount = thisWordList.size();
					currentWordNo = 0;
					showWord();
				}
			}
		});

		pd.show();
		new Thread() {
			@Override
			public void run() {

				thisWordList = Config.init().getWordsFromFileByLessonNo( currentLessonNo);
				totalWordCount = thisWordList.size();

//				// 复制出候选的翻译答案
//				if (originWordTranslation == null) {
//					originWordTranslation = new ArrayList<String>();
//					int length = thisWordList.size();
//					for (int i = 0; i < length; i++) {
//						originWordTranslation.add((String) thisWordList.get(i).get(TRANSLATION));
//					}
//				}

				// 初始化语音数据
				if (speechType == null) {
					speechType = Config.init().getSpeechType();
					Log.i("speechType", speechType);
					if (speechType.equalsIgnoreCase("tts")) {
						if (speech == null) {
							speech = new Speech(context, Locale.US).getTts();
						}
					}
				}

				pd.dismiss();
			}
		}.start();
	}

	private int FIRSTINS;
	private int SECONDINS;
	private int THIRDINS;
	private int FORTHINS;
	private List<String> alreadyRepeat = new ArrayList<String>();

	// 加入重复记忆单词
	private void addRepeatWord() {
		String wordString = (String) currentWord.get(this.WORD);
		int length = alreadyRepeat.size();
		for (int i = 0; i < length; i++) {
			if (alreadyRepeat.get(i).equals(wordString)) {
				return;
			}
		}
		alreadyRepeat.add(wordString);

		FIRSTINS = this.currentWordNo + 2;
		SECONDINS = this.currentWordNo + 5;
		THIRDINS = this.currentWordNo + 14;
		FORTHINS = this.currentWordNo + 80;
		if (this.totalWordCount > FIRSTINS) {
			thisWordList.add(FIRSTINS, this.currentWord);
		}
		if (this.totalWordCount > SECONDINS) {
			thisWordList.add(SECONDINS, this.currentWord);
		}
		if (this.totalWordCount > THIRDINS) {
			thisWordList.add(THIRDINS, this.currentWord);
		}
		if (this.totalWordCount > FORTHINS) {
			thisWordList.add(FORTHINS, this.currentWord);
		}

		// 重新计算总词数，因为加入了重复记忆的单词
		totalWordCount = thisWordList.size();
	}

	// 发音
	private void speakWord() {
		if (speechType.equalsIgnoreCase("tts")) {
			if (speech != null) {
				speech.speak(this.thisWord, TextToSpeech.QUEUE_FLUSH, null);
			}
		}
	}

	// 获取单词内容
	private void getNetTrans() {
		if (this.isCanGetNetWord) {
			new Thread() {
				@Override
				public void run() {
					FetchWord fetch = new FetchWord();
					netWord = fetch.getWord(thisWord);
					if (netWord == null) {
						netWord = new Word();
						netWord.setSentences("没有数据！");
						isCanGetNetWord = false;
					}
				}
			}.start();
		}
	}

	// 显示上一个
	private void showPre() {
		int no = currentWordNo;
		if (--no >= 0) {
			currentWordNo--;
			showWord();
		} else {
			Toast.makeText(this, R.string.toast_msg_nothing_front, Toast.LENGTH_SHORT).show();
		}
	}

	// 显示下一个
	private void showNext() {
		int no = currentWordNo;
		if (++no < totalWordCount) {
			currentWordNo++;
			showWord();
		} else {
			sureBtn.setEnabled(false);
			
			if (this.isCanUpdate) {
				// 更新记忆曲线
				Config.init().setRememberLine( this.currentLessonNo, "");
				this.isCanUpdate = false;
			}

			Log.i("currentLessonNo", String.valueOf(this.currentLessonNo));

			// 询问是否开始下一课的学习
			AlertDialog ad = new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_info).setTitle(R.string.dialog_title_tip)
					.setMessage(R.string.dialog_msg_is_goto_next_lesson).create();
			ad.setButton(getString(R.string.btn_yes), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int arg1) {
					String lessonCount = Config.init().getLessonCount();
					if ((currentLessonNo + 1) < Integer.parseInt(lessonCount)) {
						currentLessonNo++;
						initWords();
						sureBtn.setEnabled(true);
					} else {
						Toast.makeText(context, R.string.toast_msg_no_next_study_lesson, Toast.LENGTH_LONG).show();
						finish();
					}
					dialog.cancel();
				}
			});
			ad.setButton2(getString(R.string.btn_no), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					finish();
				}
			});
			ad.show();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_UP:
			showPre();
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			showNext();
			break;
		case KeyEvent.KEYCODE_BACK: {
			if (speech != null) {
				speech.stop();
				speech.shutdown();
			}
			finish();
			break;
		}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		if (speech != null) {
			speech.stop();
			speech.shutdown();
		}
		super.onDestroy();
	}
}
