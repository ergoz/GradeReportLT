package com.shinymetal.gradereportlt;

import com.shinymetal.gradereport.objects.TS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		Intent dailyUpdater = new Intent(context, DiaryUpdateService.class);
        context.startService(dailyUpdater);
        
        if (BuildConfig.DEBUG)        
        	Log.d(this.toString(), TS.get() + this.toString()
        			+ " Called context.startService from AlarmReceiver.onReceive");		
	}
}
