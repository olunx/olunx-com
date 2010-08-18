package com.olunx.util;

import java.util.Locale;

import com.olunx.R;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

public class TtsSpeech implements TextToSpeech.OnInitListener {

	private TextToSpeech tts;
	private Context context;
	private Locale language;

	public TtsSpeech(Context context, Locale language) {
		this.context = context;
		this.language = language;
	}

	public TextToSpeech getTts() {
		if(tts == null) {
			tts = new TextToSpeech(context, this);
		}
		return tts;
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			int result = tts.setLanguage(language);
			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Toast.makeText(context, R.string.toast_msg_init_texttospeech_fail, Toast.LENGTH_SHORT).show();
			} else {
				Log.i("speech_init", "success");
			}
		} else {
			Toast.makeText(context, R.string.toast_msg_load_texttospeech_fail, Toast.LENGTH_SHORT).show();
		}
	}

}
