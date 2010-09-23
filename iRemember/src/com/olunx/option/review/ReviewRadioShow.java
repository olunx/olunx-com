package com.olunx.option.review;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.olunx.R;
import com.olunx.db.RememberHelper;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class ReviewRadioShow extends Activity implements OnClickListener {

	private Context context = this;

	// 界面元素
	private TextView nameTv;
	private TextView phoneticsTv;
	private TextView translationTv;
	private RadioButton radioBtn1;
	private RadioButton radioBtn2;
	private RadioButton radioBtn3;
	private RadioButton radioBtn4;
	private RadioGroup radioGroup;;
	private Button sureBtn;
	private ImageButton speakBtn;

	private ArrayList<HashMap<String, Object>> thisWordList;
	private ArrayList<String> originWordTranslation = null;
	private int currentLessonNo = 0;
	private int currentWordNo = 0;
	private int totalWordCount = 0;
	private HashMap<String, Object> currentWord = null;

	// 记录当前单词所在的RadioButton的id
	private int thisWordAnwserViewId;

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

	// 标题栏文字
	private String LESSON;
	private String REMEMBERTIMES;

	// 发音设置
	private TextToSpeech ttsSpeech;
	private RealSpeech realSpeech;
	private int speechType = 0;
	private boolean isCanSpeech = false;

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

		this.setContentView(R.layout.review_radio);
		nameTv = (TextView) this.findViewById(R.id.TextView01);
		phoneticsTv = (TextView) this.findViewById(R.id.TextView02);
		Typeface font = Typeface.createFromAsset(getAssets(), Config.FONT_KINGSOFT_PATH);
		phoneticsTv.setTypeface(font);
		translationTv = (TextView) this.findViewById(R.id.TextView03);
		radioGroup = (RadioGroup) this.findViewById(R.id.RadioGroup01);
		radioBtn1 = (RadioButton) this.findViewById(R.id.RadioButton01);
		radioBtn2 = (RadioButton) this.findViewById(R.id.RadioButton02);
		radioBtn3 = (RadioButton) this.findViewById(R.id.RadioButton03);
		radioBtn4 = (RadioButton) this.findViewById(R.id.RadioButton04);
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

		isCanSpeech = Config.init().isCanSpeech();
		if (!isCanSpeech) {
			speakBtn.setEnabled(false);
		}

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
		this.nameTv.setText(this.thisWord);
		this.phoneticsTv.setText(this.thisPhonetics);
		this.translationTv.setPadding(0, 0, 0, 0);
		this.translationTv.setText("");
		this.radioGroup.setVisibility(View.VISIBLE);

		// 随机出选择的词条
		Random random = new Random();
		String[] translation = getRand();
		switch (random.nextInt(3)) {
		case 0:
			radioBtn1.setText(this.thisTranslation);
			thisWordAnwserViewId = radioBtn1.getId();
			radioBtn2.setText(translation[0]);
			radioBtn3.setText(translation[1]);
			radioBtn4.setText(translation[2]);
			break;
		case 1:
			radioBtn1.setText(translation[0]);
			radioBtn2.setText(this.thisTranslation);
			thisWordAnwserViewId = radioBtn2.getId();
			radioBtn3.setText(translation[1]);
			radioBtn4.setText(translation[2]);
			break;
		case 2:
			radioBtn1.setText(translation[0]);
			radioBtn2.setText(translation[1]);
			radioBtn3.setText(this.thisTranslation);
			thisWordAnwserViewId = radioBtn3.getId();
			radioBtn4.setText(translation[2]);
			break;
		case 3:
			radioBtn1.setText(translation[0]);
			radioBtn2.setText(translation[1]);
			radioBtn3.setText(translation[2]);
			radioBtn4.setText(this.thisTranslation);
			thisWordAnwserViewId = radioBtn4.getId();
			break;
		}

		// 真人发音文件处理
		if (realSpeech != null) {
			if (realSpeech.prepare(thisWord)) {
				speakBtn.setEnabled(true);
			} else {
				speakBtn.setEnabled(false);
			}
		}
	}

	//产生随机候选答案
	private String[] getRand() {
		String[] result = new String[3];
		HashSet<Integer> randSet = new HashSet<Integer>();
		// 随机出选择的词条
		Random random = new Random();
		originWordTranslation.remove(this.thisTranslation);
		Log.i("this.thisTranslation", this.thisTranslation);
		int length = originWordTranslation.size();
		Log.i("originWordTranslation.size()", String.valueOf(length));
		int size = randSet.size();
		for (int i = 0; size < result.length; i++) {
			randSet.add(random.nextInt(length));
			size = randSet.size();
		}
		Object[] randObject = randSet.toArray();
		for(int i=0; i<result.length; i++) {
			result[i] = originWordTranslation.get((Integer)randObject[i]);
		}
		originWordTranslation.add(this.thisTranslation);
		return result;
	}

	@Override
	public void onClick(View v) {
		Button btn = (Button) v;
		String text = (String) btn.getText();
		if (text.equals(this.SURE)) {
			int checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
			Log.i("checkedBtnId", String.valueOf(checkedRadioButtonId));
			if (checkedRadioButtonId == -1) {
				Toast.makeText(this, "请选择答案！", Toast.LENGTH_SHORT).show();
			} else if (checkedRadioButtonId == this.thisWordAnwserViewId) {
				this.showNext();
			} else {
				this.translationTv.setPadding(10, 10, 10, 10);
				this.translationTv.setText(this.thisTranslation + "\n\n");
				btn.setText(this.NEXTWORD);
				this.radioGroup.setVisibility(View.INVISIBLE);
			}
		} else if (text.equals(this.NEXTWORD)) {
			this.addRepeatWord();
			this.showNext();
			btn.setText(this.SURE);
		}

		// 清除选中
		radioGroup.clearCheck();
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

				thisWordList = Config.init().getWordsFromFileByLessonNo( currentLessonNo);
				totalWordCount = thisWordList.size();

				// 复制出候选的翻译答案
				if (originWordTranslation == null) {
					originWordTranslation = new ArrayList<String>();
					int length = thisWordList.size();
					for (int i = 0; i < length; i++) {
						originWordTranslation.add((String) thisWordList.get(i).get(TRANSLATION));
					}
				}

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
						if (realSpeech == null) {
							realSpeech = new RealSpeech();
						}
						break;
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
		switch(speechType) {
		case Config.SPEECH_TTS : {
			if (ttsSpeech != null) {
				ttsSpeech.speak(this.thisWord, TextToSpeech.QUEUE_FLUSH, null);
			}
			break;
		}
		case Config.SPEECH_REAL : {
			if (realSpeech != null) {
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
			sureBtn.setEnabled(false);
			
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
						sureBtn.setEnabled(true);
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
			if(!isQuit) {
				Toast.makeText(this, "再按一次退出！", Toast.LENGTH_SHORT).show();
				isQuit = true;
			}else {
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
