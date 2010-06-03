/*
 *author:olunx
 *date:2009-10-14
 */

package com.olunx.option.preview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class TabPreviewShow extends Activity implements OnClickListener {

	private Context context = this;
	
	private TextView nameTv;
	private TextView phoneticsTv;
	private TextView translationTv;
	private TextView sentsTv;
	private Button yesBtn;
	private Button noBtn;
	private ImageButton speakBtn;
	private ArrayList<HashMap<String, Object>> thisWordList;
	private int currentLessonNo = 0;
	private int currentWordNo = 0;
	private int totalWordCount = 0;
	private HashMap<String, Object> currentWord = null;

	// 记录当前单词数据
	private String thisWord = null;
	private String thisPhonetics = null;
	private String thisTranslation = null;

	// 按钮文字
	private String NEVERAGAIN;
	private String REMEMBER;
	private String RIGHT;
	private String WRONG;
	private String UNREMEMBER;
	private String NEXTWORD;
	private String WORD;
	private String PHONETICS;
	private String TRANSLATION;

	// 标题栏文字
	private String LESSON;
	private String REMEMBERTIMES;

	// 更新记忆曲线
	private boolean isCanUpdate = false;

	// 网络单词数据
	private Word netWord = new Word();
	private boolean isCanGetNetWord = false;

	// 发音设置
	private TextToSpeech speech;
	private String speechType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 按钮字符串
		NEVERAGAIN = this.getString(R.string.btn_neverangain);
		REMEMBER = this.getString(R.string.btn_remember);
		RIGHT = this.getString(R.string.btn_right);
		WRONG = this.getString(R.string.btn_wrong);
		UNREMEMBER = this.getString(R.string.btn_unremember);
		NEXTWORD = this.getString(R.string.btn_nextword);
		WORD = this.getString(R.string.text_word);
		PHONETICS = this.getString(R.string.text_phonetics);
		TRANSLATION = this.getString(R.string.text_translation);

		// 标题栏文字
		LESSON = this.getString(R.string.text_lesson);
		REMEMBERTIMES = this.getString(R.string.text_remember_times);

		this.setContentView(R.layout.preview);
		nameTv = (TextView) this.findViewById(R.id.TextView01);
		phoneticsTv = (TextView) this.findViewById(R.id.TextView02);
		Typeface font = Typeface.createFromAsset(getAssets(), "KingSoft-Phonetic-Android.ttf");
		phoneticsTv.setTypeface(font);
		translationTv = (TextView) this.findViewById(R.id.TextView03);
		sentsTv = (TextView) this.findViewById(R.id.TextView04);
		yesBtn = (Button) this.findViewById(R.id.Button01);
		yesBtn.setText(REMEMBER);
		yesBtn.setOnClickListener(this);
		noBtn = (Button) this.findViewById(R.id.Button02);
		noBtn.setText(UNREMEMBER);
		noBtn.setOnClickListener(this);
		speakBtn = (ImageButton) this.findViewById(R.id.ImageButton01);
		speakBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				speakWord();
			}
		});
		if(!Config.getConfig().isCanSpeech(this)) {
			speakBtn.setEnabled(false);
		}
		
		// 是否可以读取网络单词数据
		this.isCanGetNetWord = Config.getConfig().getCanConNetWord(this);
		Log.i("isCanGetNetWord", String.valueOf(isCanGetNetWord));

		// 获取课程数据
		Bundle i = TabPreviewShow.this.getIntent().getExtras();
		currentLessonNo = i.getInt("currentLessonNo");
		initWords();
	}

	// 显示单词
	private void showWord() {
		this.setTitle(LESSON + " " + (currentLessonNo + 1) + "\t" + REMEMBERTIMES + " " + (currentWordNo + 1) + "/" + totalWordCount);

		// 显示第x个单词
		currentWord = thisWordList.get(currentWordNo);
		thisWord = (String) currentWord.get(this.WORD);
		thisPhonetics = (String) currentWord.get(this.PHONETICS);
		thisTranslation = (String) currentWord.get(this.TRANSLATION);
		nameTv.setText(this.thisWord);
		phoneticsTv.setText(this.thisPhonetics);
		translationTv.setText("");
		sentsTv.setText("");

		// 获取网络单词数据
		this.getNetTrans();
	}

	private Set<String> ignoreWords = new HashSet<String>();
	private boolean isNeedAddRepeat = true;

	@Override
	public void onClick(View v) {
		Button btn = (Button) v;
		String text = (String) btn.getText();
		if (text.equals(REMEMBER)) {// 记得
			isNeedAddRepeat = true;
			yesBtn.setText(RIGHT);
			noBtn.setText(WRONG);
			this.showAnswer();
		}
		if (text.equals(UNREMEMBER) || text.equals(WRONG)) {// 忘了或者不正确
			isNeedAddRepeat = true;
			noBtn.setText(NEXTWORD);
			yesBtn.setVisibility(Button.INVISIBLE);
			this.showAnswer();
		}
		if (text.equals(RIGHT)) {// 正确
			isNeedAddRepeat = false;
			yesBtn.setText(NEXTWORD);
			noBtn.setText(NEVERAGAIN);
		}
		if (text.equals(NEVERAGAIN)) {// 不再记忆
			yesBtn.setText(REMEMBER);
			noBtn.setText(UNREMEMBER);
			ignoreWords.add(this.thisWord);// 当前单词不再记忆
			this.showNext();
		}
		if (text.equals(NEXTWORD)) {// 下一个
			yesBtn.setText(REMEMBER);
			noBtn.setText(UNREMEMBER);
			yesBtn.setVisibility(Button.VISIBLE);
			if (isNeedAddRepeat) {
				this.addRepeatWord();// 循环记忆
			}
			this.showNext();
		}
	}

	// 显示答案
	private void showAnswer() {
		translationTv.setText(this.thisTranslation);
		sentsTv.setText(netWord.getSentences());
		netWord.setSentences("");
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
			public void run() {

				thisWordList = Config.getConfig().getWordsFromFileByLessonNo(context, currentLessonNo);
				totalWordCount = thisWordList.size();

				// 初始化语音数据
				if (speechType == null) {
					speechType = Config.getConfig().getSpeechType(context);
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

			if (this.isCanUpdate) {
				Log.i("ignoreWords.toString()",ignoreWords.toString());
				// 更新记忆曲线
				Config.getConfig().setRememberLine(this, this.currentLessonNo, ignoreWords.toString());
				this.isCanUpdate = false;

				// 保存当前学习完的课程号数
				Config.getConfig().setNextStudyLesson(this, this.currentLessonNo + 1);
			}

			Log.i("currentLessonNo", String.valueOf(this.currentLessonNo));

			// 询问是否开始下一课的学习
			AlertDialog ad = new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_info).setTitle(R.string.dialog_title_tip)
					.setMessage(R.string.dialog_msg_is_goto_next_lesson).create();
			ad.setButton(getString(R.string.btn_yes), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int arg1) {
					String lessonCount = Config.getConfig().getLessonCount(context);
					if ((currentLessonNo + 1) < Integer.parseInt(lessonCount)) {
						currentLessonNo++;
						initWords();
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
