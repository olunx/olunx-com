package com.olunx.irss;

import com.olunx.irss.db.ArticlesHelper;
import com.olunx.irss.db.CategoryHelper;
import com.olunx.irss.db.FeedsHelper;
import com.olunx.irss.db.ImagesHelper;
import com.olunx.irss.reader.Google;
import com.olunx.irss.reader.Update;
import com.olunx.irss.util.Config;
import com.olunx.irss.util.SysTools;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Init extends Activity {

	private final String TAG = "com.olunx.irss.Init";

	private final int ALERT_RIGHT = 0;
	private final int ALERT_WRONG = 1;
	private final int ALERT_DISCONNECT = 2;
	private ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		String status = Environment.getExternalStorageState();
		if (!status.equals(Environment.MEDIA_MOUNTED)) {
			Toast.makeText(this, "没有内存卡，程序将自动退出。", Toast.LENGTH_LONG).show();
			finish();
		}
		
		if (Config.init().isAccountInputted()) {
			 startMain();
		}

		this.setContentView(R.layout.init);
		super.onCreate(savedInstanceState);
		
		final EditText usernameTv = (EditText) findViewById(R.id.EditText01);
		final EditText pwdTv = (EditText) findViewById(R.id.EditText02);
		Button sureBtn = (Button) findViewById(R.id.Button02);
		sureBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String user = usernameTv.getText().toString().trim();
				final String pwd = pwdTv.getText().toString().trim();

				Log.i(TAG, user);
				Log.i(TAG, pwd);

				if (user.equals("") || pwd.equals("")) {
					Toast.makeText(Init.this, "帐号用户名不能为空", Toast.LENGTH_SHORT).show();
				} else {
					pd = new ProgressDialog(Init.this);
					pd.setMessage("正在验证账户...");
					pd.show();
					
					pd.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							Log.i(TAG, "pd dismiss");
						}});
					pd.setOnCancelListener(new OnCancelListener(){
						@Override
						public void onCancel(DialogInterface dialog) {
							Log.i(TAG, "pd cancel");
							startMain();
						}});
					
					new Thread(){
						@Override
						public void run() {
							auth(user, pwd);
						};
					}.start();
				}
			}
		});

	}

	/**
	 * 登录认证
	 * 
	 * @param user
	 * @param pwd
	 */
	private void auth(String user, String pwd) {
		if(!SysTools.isConnect(this)) {
			mHandler.sendEmptyMessage(ALERT_DISCONNECT);
			return;
		}
		if (new Google().login(user, pwd)) {
			Config.init().setAccount(user, pwd);
			mHandler.sendEmptyMessage(ALERT_RIGHT);
			Log.i(TAG, "download feed start");
			CategoryHelper cHelper = new CategoryHelper();
			cHelper.dropTable();
			cHelper.close();
			FeedsHelper fHelper = new FeedsHelper();
			fHelper.dropTable();
			fHelper.close();
			ArticlesHelper aHelper = new ArticlesHelper();
			aHelper.dropTable();
			aHelper.close();
			ImagesHelper iHelper = new ImagesHelper();
			iHelper.dropTable();
			iHelper.close();
			new Update().getFeedsFromGoogle();
			pd.cancel();
		} else {
			mHandler.sendEmptyMessage(ALERT_WRONG);
		}
	}

	/**
	 * 启动主界面
	 */
	private void startMain() {
		Intent i = new Intent();
		i.setClass(this, Main.class);
		this.startActivity(i);
		this.finish();
	}
	
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ALERT_RIGHT: {
				pd.setMessage("登录成功,正在获取[订阅]数据...");
				break;
			}
			case ALERT_WRONG: {
				pd.dismiss();
				AlertDialog.Builder adb = new AlertDialog.Builder(Init.this);
				adb.setTitle("提示");
				adb.setMessage("登录失败，请检查用户名密码。");
				adb.setPositiveButton("确定", null);
				adb.show();
				break;
			}
			case ALERT_DISCONNECT:{
				pd.dismiss();
				AlertDialog.Builder adb = new AlertDialog.Builder(Init.this);
				adb.setTitle("提示");
				adb.setMessage("网络连接不可用，请检查网络。");
				adb.setPositiveButton("确定", null);
				adb.show();
			}
			}
		};
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			Process.killProcess(android.os.Process.myPid());
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
	
}
