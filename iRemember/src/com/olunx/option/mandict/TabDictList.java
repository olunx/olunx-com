/*
 *author:olunx
 *date:2009-10-11
 */

package com.olunx.option.mandict;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.olunx.R;
import com.olunx.util.Config;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class TabDictList extends Activity {

	private ListView listview = null;
	private ArrayList<HashMap<String, Object>> items = null;

	// 保存当前路径，以方便返回用。

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		updateDictList();
	}

	// 更新词典列表
	private void updateDictList() {

		// 进度框
		final ProgressDialog pd = new ProgressDialog(this);
		pd.setIcon(android.R.drawable.ic_dialog_info);
		pd.setTitle(getString(R.string.dialog_msg_refresh_dict_list));
		pd.setMessage(getString(R.string.dialog_msg_wait));
		pd.show();
		// 对话框关闭后触发的事件
		pd.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface arg0) {
				setContent();// 设置界面内容
			}
		});

		new Thread() {
			@Override
			public void run() {
				new GetDictList(TabDictList.this).getList();
				items = Config.init().getDictList();
				pd.dismiss();
			}
		}.start();

	}

	// 填充列表到界面上
	private void setContent() {
		SimpleAdapter adapter = new SimpleAdapter(this, items, android.R.layout.simple_list_item_2, new String[] {
				getString(R.string.title), getString(R.string.description) }, new int[] { android.R.id.text1, android.R.id.text2 });
		listview = new ListView(this);
		listview.setAdapter(adapter);

		setContentView(listview);

		Toast.makeText(TabDictList.this, R.string.toast_msg_set_dict_using, Toast.LENGTH_LONG).show();

		// 单击事件。
		listview.setOnItemClickListener(new OnItemClickListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void onItemClick(AdapterView<?> av, View v, int position, long arg3) {

				Map map = (Map) av.getItemAtPosition(position);
				final String dictName = (String) map.get(getString(R.string.title));

				AlertDialog.Builder ad = new AlertDialog.Builder(av.getContext());
				ad.setInverseBackgroundForced(true);// 翻转底色
				ad.setIcon(android.R.drawable.ic_dialog_info);
				ad.setTitle(dictName);

				ad.setItems(new String[] { getString(R.string.dialog_item_set_current_dict) }, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						// 设置该词库为默认使用
						case 0: {
							setDictCurrentUse(dictName);
						}
						}
					}
				});

				// 取消按钮
				ad.setNegativeButton(getString(R.string.btn_cancel), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

				ad.show();
			}
		});

		// 长按事件，将选择的项目返回。
		listview.setOnItemLongClickListener(new OnItemLongClickListener() {

			@SuppressWarnings("unchecked")
			@Override
			public boolean onItemLongClick(AdapterView<?> av, View v, int position, long arg3) {

				Map map = (Map) av.getItemAtPosition(position);
				final String dictName = (String) map.get(getString(R.string.title));

				AlertDialog.Builder ad = new AlertDialog.Builder(av.getContext());
				ad.setIcon(android.R.drawable.ic_dialog_alert);
				ad.setTitle(dictName);
				ad.setMessage(getString(R.string.dialog_msg_is_set_current_dict));

				// 确定按钮
				ad.setPositiveButton(getString(R.string.btn_sure), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						setDictCurrentUse(dictName);
					}
				});

				// 取消按钮
				ad.setNegativeButton(getString(R.string.btn_cancel), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

				ad.show();
				return false;
			}
		});

	}

	// 计算单词数并完成相应处理
	private void setDictCurrentUse(final String dictName) {
		final String dictPath = Config.init().getDictPath( dictName);

		String dictType = Config.init().getDictType( dictName);

		if (dictType.equalsIgnoreCase(Config.DICTTYPE_CSV)) {
			
			// 保存配置，当前使用的词库。
			final ProgressDialog pd = new ProgressDialog(TabDictList.this);
			pd.setIcon(android.R.drawable.ic_dialog_info);
			pd.setTitle(getString(R.string.dialog_title_count_words));
			pd.setMessage(getString(R.string.dialog_msg_wait));
			pd.show();
			pd.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface arg0) {
					setResult(RESULT_OK, new Intent());// 返回数据给上层
					// 保存配置，当前使用的词库。
					Config.init().setCurrentUseDictName( dictName);
					
					// 清空记忆曲线表
					Config.init().cleanRememberLine();
					
					Toast.makeText(TabDictList.this, TabDictList.this.getString(R.string.toast_msg_set_success), Toast.LENGTH_SHORT).show();
					setResult(RESULT_OK, new Intent());// 返回数据给上层
					
					finish();
				}
			});

			// 从文件计算单词数
			new Thread() {
				@Override
				public void run() {
					Config.init().setCurrentUseDictWordCount(new GetCsvInfo(dictPath).getWordCount());
					pd.dismiss();
				}
			}.start();

		}

	}

}