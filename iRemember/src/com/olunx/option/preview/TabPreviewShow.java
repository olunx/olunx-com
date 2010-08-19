/*
 *author:olunx
 *date:2009-10-14
 */

package com.olunx.option.preview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.olunx.R;
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
import android.os.Handler;
import android.os.Message;
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
	private Button neverBtn;
	private Button nextBtn;
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
//	private Word netWord = new Word();
	private boolean isCanGetTransDict = false;
	SeekWord seek = null;
	Handler mHandler = null;

	// 发音设置
	private TextToSpeech ttsSpeech;
	private RealSpeech realSpeech;
	private int speechType = 0;
	private boolean isCanSpeech = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 按钮字符串
		NEVERAGAIN = this.getString(R.string.btn_neverangain);
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
		neverBtn = (Button) this.findViewById(R.id.Button01);
		neverBtn.setText(NEVERAGAIN);
		neverBtn.setOnClickListener(this);
		nextBtn = (Button) this.findViewById(R.id.Button02);
		nextBtn.setText(NEXTWORD);
		nextBtn.setOnClickListener(this);
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
		mHandler = new Handler() {  
	        public void handleMessage(Message msg) {
	        	sentsTv.setText(msg.getData().getString("translations"));
	            super.handleMessage(msg);   
	        }   
	   };
		Log.i("isCanGetTransDict", String.valueOf(isCanGetTransDict));

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
		translationTv.setText(this.thisTranslation);
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

	private Set<String> ignoreWords = new HashSet<String>();

	@Override
	public void onClick(View v) {
		Button btn = (Button) v;
		String text = (String) btn.getText();
		if (text.equals(NEVERAGAIN)) {// 不再记忆
			ignoreWords.add(this.thisWord);// 当前单词不再记忆
			this.showNext();
		}
		if (text.equals(NEXTWORD)) {// 下一个
			neverBtn.setVisibility(View.VISIBLE);
			this.showNext();
		}
	}

//	// 显示答案
//	private void showAnswer() {
//		translationTv.setText(this.thisTranslation);
//		sentsTv.setText(netWord.getSentences());
//		netWord.setSentences("");
//	}
	
	
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
					currentWordNo = Config.init().getPreviewWordIndex(currentLessonNo);//获取上次记忆的单词位置
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
				
				//初始化详细解释词典
				if(isCanGetTransDict) {
					seek = new SeekWord(Config.init().getDictPath(Config.init().getCurrentUseTransDictName()));
				}
				
				pd.dismiss();
			}
		}.start();
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

	// 获取词条的详细解释
	private void getTrans() {
		if (this.isCanGetTransDict) {
			new Thread() {
				@Override
				public void run() {
					Message msg = new Message();
					Bundle b = new Bundle();
					b.putString("translations", seek.getWordTrans(thisWord).get("解释"));
					msg.setData(b);
					mHandler.sendMessage(msg);
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
			neverBtn.setEnabled(false);
			nextBtn.setEnabled(false);
			
			new Thread(){
				public void run() {
					if (isCanUpdate) {
						Log.i("ignoreWords.toString()",ignoreWords.toString());
						// 更新记忆曲线
						Config.init().setRememberLine( currentLessonNo, ignoreWords.toString(), true);
						isCanUpdate = false;

						Config.init().setPreviewWordIndex(currentLessonNo, 0);//清除本次记忆的单词位置
						Config.init().setNextStudyLesson( currentLessonNo + 1);// 保存当前学习完的课程号数
					}
				}
			}.run();


			Log.i("currentLessonNo", String.valueOf(this.currentLessonNo));

			// 询问是否开始下一课的学习
			new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_info)
			.setTitle(R.string.dialog_title_tip)
			.setMessage(getString(R.string.dialog_msg_is_goto_next_lesson))
			.setPositiveButton(getString(R.string.btn_yes),	new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int arg1) {
					String lessonCount = Config.init().getLessonCount();
					if ((currentLessonNo + 1) < Integer.parseInt(lessonCount)) {
						currentLessonNo++;
						initWords();
						neverBtn.setEnabled(true);
						nextBtn.setEnabled(true);
					} else {
						Toast.makeText(context, R.string.toast_msg_no_next_study_lesson, Toast.LENGTH_LONG).show();
						finish();
					}
					dialog.cancel();
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
		Config.init().setPreviewWordIndex(currentLessonNo, currentWordNo);//保存本次记忆的单词位置
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Config.init().setPreviewWordIndex(currentLessonNo, currentWordNo);//保存本次记忆的单词位置
		if (ttsSpeech != null) {
			ttsSpeech.stop();
			ttsSpeech.shutdown();
		}
		if(realSpeech != null) {
			realSpeech.stop();
			realSpeech.shutdown();
		}
		super.onDestroy();
	}
}
