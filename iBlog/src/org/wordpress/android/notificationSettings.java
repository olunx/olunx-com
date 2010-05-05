package org.wordpress.android;

import java.util.HashMap;
import java.util.Vector;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;


public class notificationSettings extends Activity {
    /** Called when the activity is first created. */
	public Vector accounts;
	public Vector accountNames = new Vector();
	private String selectedID = "";
	public int checkCtr = 0;
	protected static Intent svc = null;
	
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
      
        displayAccounts();

    }
    
public void displayAccounts(){
	//settings time!
    final settingsDB settingsDB = new settingsDB(this);
	accounts = settingsDB.getAccounts(this);
	HashMap notificationOptions = settingsDB.getNotificationOptions(this);
	boolean sound = false, vibrate = false, light = false;
	
	if (notificationOptions != null){
		if (notificationOptions.get("sound").toString().equals("1")){
			sound = true;
		}
		if (notificationOptions.get("vibrate").toString().equals("1")){
			vibrate = true;
		}
		if (notificationOptions.get("light").toString().equals("1")){
			light = true;
		}
	}
	
	if (accounts.size() > 0){
		ScrollView sv = new ScrollView(this);
		sv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));
		sv.setBackgroundColor(Color.parseColor("#e8e8e8"));
		LinearLayout layout = new LinearLayout(this);
		
		layout.setPadding(10, 10, 10, 0);
		layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));

		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setGravity(Gravity.LEFT);
		
		final LinearLayout cbLayout = new LinearLayout(this);
		
		cbLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));

		cbLayout.setOrientation(LinearLayout.VERTICAL);
		
        TextView textView = new TextView(this);
        textView.setTextColor(Color.parseColor("#444444"));
        textView.setTextSize(12);
        textView.setPadding(0, 20, 0, 0);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setText(getResources().getText(R.string.notifications_select));

        layout.addView(textView);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams 
        (LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        
        for (int i = 0; i < accounts.size(); i++) {
            
        	HashMap curHash = (HashMap) accounts.get(i);
        	String curBlogName = curHash.get("blogName").toString();
        	String curUsername = curHash.get("username").toString();
        	String accountID = curHash.get("id").toString();
        	int runService = Integer.valueOf(curHash.get("runService").toString());
        	accountNames.add(i, curBlogName);
        	cbLayout.setBackgroundColor(Color.parseColor("#e8e8e8"));
            
            final CheckBox checkBox = new CheckBox(this);
            checkBox.setTextColor(Color.parseColor("#444444"));
            checkBox.setTextSize(18);
            checkBox.setText(escapeUtils.unescapeHtml(curBlogName));
            checkBox.setId(Integer.valueOf(accountID));
            params.setMargins(0, 0, 0, 6);
            checkBox.setLayoutParams(params);
            
            if (runService == 1){
            	checkBox.setChecked(true);
            }

            cbLayout.addView(checkBox);  
        } 
        
        if (cbLayout.getChildCount() > 0){
        	layout.addView(cbLayout);
        }
        
        //add spinner and buttons
        TextView textView2 = new TextView(this);
        textView2.setTextColor(Color.parseColor("#444444"));
        textView2.setTextSize(12);
        textView2.setPadding(0, 20, 0, 0);
        textView2.setGravity(Gravity.CENTER_HORIZONTAL);
        textView2.setText(getResources().getText(R.string.notifications_interval));

        layout.addView(textView2);
        
        final Spinner sInterval = new Spinner(this);
        sInterval.setLayoutParams(params);
	    ArrayAdapter<Object> sIntervalArrayAdapter = new ArrayAdapter<Object>(this,
	    		R.layout.spinner_textview,
	            new String[] { "5 Minutes", "10 Minutes", "15 Minutes", "30 Minutes" , "1 Hour", "3 Hours", "6 Hours", "12 Hours", "Daily"});
	    sIntervalArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    sInterval.setAdapter(sIntervalArrayAdapter);
	    String interval = settingsDB.getInterval(this);
	    
	    if (interval != ""){
            sInterval.setSelection(sIntervalArrayAdapter.getPosition(interval));
	    }
	    
	    layout.addView(sInterval);
	    
	    LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams 
        (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT);
	    
	    final LinearLayout nOptionsLayout = new LinearLayout(this);
		
	    nOptionsLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));

	    nOptionsLayout.setOrientation(LinearLayout.VERTICAL);
	    
	    CheckBox soundCB = new CheckBox(this);
	    soundCB.setTag("soundCB");
	    soundCB.setTextColor(Color.parseColor("#444444"));
	    soundCB.setTextSize(18);
	    soundCB.setText(escapeUtils.unescapeHtml("Play notification sound"));
        params.setMargins(0, 0, 0, 6);
        soundCB.setLayoutParams(params);
        soundCB.setChecked(sound);
        
        nOptionsLayout.addView(soundCB);
        
        CheckBox vibrateCB = new CheckBox(this);
        vibrateCB.setTag("vibrateCB");
        vibrateCB.setTextColor(Color.parseColor("#444444"));
        vibrateCB.setTextSize(18);
        vibrateCB.setText(escapeUtils.unescapeHtml("Vibrate"));
        params.setMargins(0, 0, 0, 6);
        vibrateCB.setLayoutParams(params);
        vibrateCB.setChecked(vibrate);
        
        nOptionsLayout.addView(vibrateCB);
        
        CheckBox lightCB = new CheckBox(this);
        lightCB.setTag("lightCB");
        lightCB.setTextColor(Color.parseColor("#444444"));
        lightCB.setTextSize(18);
        lightCB.setText(escapeUtils.unescapeHtml("Blink notification light"));
        params.setMargins(0, 0, 0, 6);
        lightCB.setLayoutParams(params);
        lightCB.setChecked(light);
        
        nOptionsLayout.addView(lightCB);
        
        layout.addView(nOptionsLayout);
	    
	    final Button save = new Button(this);
	    save.setLayoutParams(params2);
	    save.setBackgroundDrawable(getResources().getDrawable(R.drawable.wp_button_small));
        save.setTextSize(18);
	    save.setText("Save");
	    
	    save.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	
            	boolean sound = false, vibrate = false, light = false;
            	checkCtr = 0;
            	
            	int listItemCount = cbLayout.getChildCount();
            	for( int i=0;i<listItemCount;i++ ) {
            	    CheckBox cbox = (CheckBox) ((View)cbLayout.getChildAt(i));
            	    	int id = cbox.getId();
                	    if( cbox.isChecked() ) {   
                	    	checkCtr++;
                	        settingsDB.updateNotificationFlag(notificationSettings.this, id, true);
                	        Log.i("CommentService", "Service enabled for " + cbox.getText());
                	    }
                	    else{
                	        settingsDB.updateNotificationFlag(notificationSettings.this, id, false);
                	    }
            	}
            	
            	int noOptionsItemCount = nOptionsLayout.getChildCount();
            	for( int i=0;i<noOptionsItemCount;i++ ) {
            	    CheckBox cbox = (CheckBox) ((View)nOptionsLayout.getChildAt(i));
	        	    if (cbox.getTag().equals("soundCB")){
		    			sound = cbox.isChecked();
		    		}
		    		else if (cbox.getTag().equals("vibrateCB")){
		    			vibrate = cbox.isChecked();
		    		}
		    		else if (cbox.getTag().equals("lightCB")){
		    			light = cbox.isChecked();
		    		}
            	}

            	settingsDB.updateNotificationSettings(notificationSettings.this, sInterval.getSelectedItem().toString(), sound, vibrate, light);
            	
            	if (checkCtr > 0){

        	        String updateInterval = sInterval.getSelectedItem().toString();
        	        int UPDATE_INTERVAL = 3600000;
        	        
        	      //configure time interval
        	         if (updateInterval.equals("5 Minutes")){
        	        	 UPDATE_INTERVAL = 300000;
        	         }
        	         else if (updateInterval.equals("10 Minutes")){
        	        	 UPDATE_INTERVAL = 600000;
        	         }
        	         else if (updateInterval.equals("15 Minutes")){
        	        	 UPDATE_INTERVAL = 900000;
        	         }
        	         else if (updateInterval.equals("30 Minutes")){
        	        	 UPDATE_INTERVAL = 1800000;
        	         }
        	         else if (updateInterval.equals("1 Hour")){
        	        	 UPDATE_INTERVAL = 3600000;
        	         }
        	         else if (updateInterval.equals("3 Hours")){
        	        	 UPDATE_INTERVAL = 10800000;
        	         }
        	         else if (updateInterval.equals("6 Hours")){
        	        	 UPDATE_INTERVAL = 21600000;
        	         }
        	         else if (updateInterval.equals("12 Hours")){
        	        	 UPDATE_INTERVAL = 43200000;
        	         }
        	         else if (updateInterval.equals("Daily")){
        	        	 UPDATE_INTERVAL = 86400000;
        	         }
        	        
        	        Intent intent = new Intent(notificationSettings.this, broadcastReceiver.class);
                	PendingIntent pIntent = PendingIntent.getBroadcast(notificationSettings.this, 0, intent, 0);
                	
                	AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                	
                	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (5 * 1000), UPDATE_INTERVAL, pIntent);
        			  
        		}
        		else{

        						Intent stopIntent = new Intent(notificationSettings.this, broadcastReceiver.class);
                            	PendingIntent stopPIntent = PendingIntent.getBroadcast(notificationSettings.this, 0, stopIntent, 0);
                            	AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                            	alarmManager.cancel(stopPIntent); 
                            	
                            	Intent service = new Intent(notificationSettings.this, commentService.class);
                            	stopService(service);

            	}
            	
            	finish();
            }
        });
	    
	    
	    layout.addView(save);
        
        sv.addView(layout);
        
        setContentView(sv);
	}
}


}