package com.olunx.option.preview;

import java.util.ArrayList;

import com.olunx.R;
import com.olunx.util.Config;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class TabPreviewInit extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTitle(getString(R.string.option_preview_title));

		// 获取组数
		String strLessonCount = Config.init().getLessonCount();
		int intLessonCount = 0;
		if (strLessonCount != null && !strLessonCount.equals("") && !strLessonCount.equals("0")) {
			intLessonCount = Integer.parseInt(strLessonCount);

			// 设置组数选择菜单，从1开始。
			final Spinner spinner = new Spinner(this);
			ArrayList<String> items = new ArrayList<String>();
			String lessonStr = this.getString(R.string.text_lesson);
			for (int i = 0; i < intLessonCount; i++) {
				items.add(lessonStr + " " + (i + 1));
			}
			ArrayAdapter<String> list = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
			list.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(list);

			// 选择准备记忆的课程
			int selection = Config.init().getNextStudyLesson();
			if (selection < intLessonCount && selection != 0) {
				spinner.setSelection(selection);
			}

			// 选择学习第x组
			AlertDialog ad = new AlertDialog.Builder(this).setTitle(R.string.dialog_title_please_select_lesson).setIcon(android.R.drawable.ic_dialog_info).create();
			ad.setButton(this.getString(R.string.btn_sure), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					int currentLessonNo = spinner.getSelectedItemPosition();
					if (currentLessonNo >= 0) {
						// 选定的组数，传值到下个界面。
						Intent i = new Intent();
						i.setClass(TabPreviewInit.this, TabPreviewShow.class);
						i.putExtra("currentLessonNo", currentLessonNo);
						TabPreviewInit.this.finish();
						TabPreviewInit.this.startActivity(i);
					}
				}
			});
			ad.setButton2(this.getString(R.string.btn_cancel), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					TabPreviewInit.this.finish();
				}
			});
			ad.setView(spinner);
			ad.setCancelable(false);
			ad.show();
		} else {
			AlertDialog ad = new AlertDialog.Builder(this).setTitle(R.string.dialog_title_tip).setMessage(R.string.dialog_msg_no_study_lesson)
					.setIcon(android.R.drawable.ic_dialog_info).create();
			ad.setButton(getString(R.string.btn_exit), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					TabPreviewInit.this.finish();
				}
			});
			ad.show();
		}

	}

}
