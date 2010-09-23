package com.olunx.option.review;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.olunx.R;
import com.olunx.db.RememberHelper;
import com.olunx.stardict.SeekWord;
import com.olunx.util.Config;
import com.olunx.util.RealSpeech;
import com.olunx.util.TtsSpeech;

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

public class ReviewAnwserShow extends Activity implements OnClickListener {

	private Context context = this;

	// 界面元素
	private TextView nameTv;
	private TextView phoneticsTv;
	private TextView translationTv;
	private TextView sentsTv;
	private Button leftBtn;
	private Button rightBtn;
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

	// 详细解释词典
	private boolean isCanGetTransDict = false;
	private SeekWord seek = null;
	private String trans;

	// 发音设置
	private TextToSpeech ttsSpeech;
	private RealSpeech realSpeech;
	private int speechType = 0;
	private boolean isCanSpeech = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 按钮字符串
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
		Typeface font = Typeface.createFromAsset(getAssets(), Config.FONT_KINGSOFT_PATH);
		phoneticsTv.setTypeface(font);
		translationTv = (TextView) this.findViewById(R.id.TextView03);
		sentsTv = (TextView) this.findViewById(R.id.TextView04);
		leftBtn = (Button) this.findViewById(R.id.Button01);
		leftBtn.setText(REMEMBER);
		leftBtn.setOnClickListener(this);
		rightBtn = (Button) this.findViewById(R.id.Button02);
		rightBtn.setText(UNREMEMBER);
		rightBtn.setOnClickListener(this);
		speakBtn = (ImageButton) this.findViewById(R.id.ImageButton01);
		speakBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				speakWord();
			}
		});
		
		//是否可以发音
		isCanSpeech = Config.init().isCanSpeech();
		if(!isCanSpeech) {
			speakBtn.setEnabled(false);
		}
		
		// 是否可以读取详细解释词典
		isCanGetTransDict = Config.init().isCanGetTransDict();
		Log.i("isCanGetTransDict", String.valueOf(isCanGetTransDict));

		// 获取课程数据
		Bundle i = ReviewAnwserShow.this.getIntent().getExtras();
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

		//真人发音文件处理
		if(realSpeech != null) {
			if(realSpeech.prepare(thisWord)) {
				speakBtn.setEnabled(true);
			}else {
				speakBtn.setEnabled(false);
			}
		}
		
		// 获取词条的详细解释
		this.getTrans();
	}

	@Override
	public void onClick(View v) {
		Button btn = (Button) v;
		String text = (String) btn.getText();
		if (text.equals(REMEMBER)) {// 记得
			leftBtn.setText(RIGHT);
			rightBtn.setText(WRONG);
			this.showAnswer();
			return;
		}
		if (text.equals(UNREMEMBER)) {// 忘了
			this.addRepeatWord();// 循环记忆
			leftBtn.setVisibility(View.INVISIBLE);
			rightBtn.setText(NEXTWORD);
			this.showAnswer();
			return;
		}
		if (text.equals(RIGHT)) {// 正确
			leftBtn.setText(REMEMBER);
			rightBtn.setText(UNREMEMBER);
			this.showNext();
			return;
		}
		if (text.equals(WRONG)) {// 错误
			leftBtn.setText(REMEMBER);
			rightBtn.setText(UNREMEMBER);
			this.addRepeatWord();// 循环记忆
			this.showNext();
			return;
		}
		if (text.equals(NEXTWORD)) {// 下一个
			leftBtn.setText(REMEMBER);
			rightBtn.setText(UNREMEMBER);
			leftBtn.setVisibility(View.VISIBLE);
			this.showNext();
		}
	}

	// 显示答案
	private void showAnswer() {
		translationTv.setText(this.thisTranslation);
		sentsTv.setText(this.trans);
	}

	// 获取词条的详细解释
	private void getTrans() {
		if (this.isCanGetTransDict) {
			new Thread() {
				@Override
				public void run() {
					trans = seek.getWordTrans(thisWord).get("解释");
				}
			}.start();
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
//					currentWordNo = 0;
					currentWordNo = Config.init().getReviewWordIndex(currentLessonNo);//获取上次记忆的单词位置
					if(currentWordNo > totalWordCount) {currentWordNo = 0;}//处理越界情况
					
					showWord();
				}
			}
		});

		pd.show();
		new Thread() {
			@Override
			public void run() {

				thisWordList = Config.init().getWordsFromFileByLessonNo(currentLessonNo);
				totalWordCount = thisWordList.size();

				if(isCanSpeech) {
					// 初始化语音数据
					speechType = Config.init().getSpeechType();
					switch(speechType) {
					case Config.SPEECH_TTS : {
						if (ttsSpeech == null) {
							ttsSpeech = new TtsSpeech(context, Locale.US).getTts();
						}
						break;
					}
					case Config.SPEECH_REAL : {
						if(realSpeech == null) {
							realSpeech = new RealSpeech();
						}
						break;
					}
					}
				}
				
				// 初始化详细解释词典
				if (isCanGetTransDict) {
					seek = new SeekWord(Config.init().getDictPath(Config.init().getCurrentUseTransDictName()));
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
		switch(speechType) {
		case Config.SPEECH_TTS : {
			if (ttsSpeech != null) {
				ttsSpeech.speak(this.thisWord, TextToSpeech.QUEUE_FLUSH, null);
			}
			break;
		}
		case Config.SPEECH_REAL : {
			if(realSpeech != null) {
				realSpeech.speak();
			}
			break;
		}
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
			leftBtn.setEnabled(false);
			rightBtn.setEnabled(false);

			new Thread() {
				public void run() {
					if (isCanUpdate) {
						// 更新记忆曲线
						Config.init().setRememberLine(currentLessonNo, "");
						isCanUpdate = false;

						Config.init().setReviewWordIndex(currentLessonNo, 0);// 清除本次记忆的单词位置
					}
				}
			}.run();

			Log.i("currentLessonNo", String.valueOf(this.currentLessonNo));
			
			// 是否开始下一课的学习
			new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_info)
			.setTitle(R.string.dialog_title_tip)
			.setMessage(R.string.dialog_msg_is_goto_need_lesson)
			.setPositiveButton(getString(R.string.btn_yes),	new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int arg1) {
					RememberHelper helper = new RememberHelper();
					final int needStudyLesson = helper.getOneNeedStudyLesson();
					helper.close();
					if(needStudyLesson != -1) {
						currentLessonNo = needStudyLesson;
						initWords();
						leftBtn.setEnabled(true);
						rightBtn.setEnabled(true);
						dialog.cancel();
					}else {
						Toast.makeText(context, R.string.toast_msg_no_need_study_lesson, Toast.LENGTH_LONG).show();
						finish();
					}
				}
			})
			.setNegativeButton(getString(R.string.btn_no), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					finish();
				}
			})
			.show();
			
		}
	}

	private boolean isQuit = false;

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
			if (!isQuit) {
				Toast.makeText(this, "再按一次退出！", Toast.LENGTH_SHORT).show();
				isQuit = true;
			} else {
				finish();
			}
			break;
		}
		}
		return true;
	}

	@Override
	protected void onStop() {
		Config.init().setReviewWordIndex(currentLessonNo, currentWordNo);//保存本次记忆的单词位置
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		Config.init().setReviewWordIndex(currentLessonNo, currentWordNo);//保存本次记忆的单词位置
		if (ttsSpeech != null) {
			ttsSpeech.stop();
			ttsSpeech.shutdown();
		}
		if (realSpeech != null) {
			realSpeech.stop();
			realSpeech.shutdown();
		}
		super.onDestroy();
	}
}
