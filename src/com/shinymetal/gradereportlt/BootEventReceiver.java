package com.shinymetal.gradereportlt;

import java.util.Date;

import com.shinymetal.gradereport.objects.TS;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

public class BootEventReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
        if (BuildConfig.DEBUG)        
        	Log.d(this.toString(), TS.get() + this.toString()
        			+ " onReceive() : started");

		String action = intent.getAction();
        if (action.equals("android.intent.action.BOOT_COMPLETED")) {
        	
    		Intent downloader = new Intent(context, AlarmReceiver.class);
    		downloader.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		
    		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
    				downloader, PendingIntent.FLAG_UPDATE_CURRENT);
    		
    		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

    		Date firstRun = new Date();
    		long mSyncInterval = Long.parseLong(PreferenceManager
    				.getDefaultSharedPreferences(context).getString(
    						context.getString(R.string.pref_sync_key), "15")) * 60000;
    				
    		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
    				firstRun.getTime() + 10,
    				mSyncInterval, pendingIntent);
    		
            if (BuildConfig.DEBUG)        
            	Log.d(this.toString(), TS.get() + this.toString()
            			+ " Set alarm for update service in onReceive()");	
        }		
	}
}
