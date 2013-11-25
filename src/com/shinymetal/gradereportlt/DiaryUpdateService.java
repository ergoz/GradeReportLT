package com.shinymetal.gradereportlt;

import java.util.ArrayList;

import com.shinymetal.gradereport.db.Database;
import com.shinymetal.gradereport.objects.TS;
import com.shinymetal.gradereport.utils.GshisLoader;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

public class DiaryUpdateService extends IntentService {
	
	private final IBinder mBinder = new DiaryUpdateBinder();
	private ArrayList<Messenger> mClients = new ArrayList<Messenger>(); 
	
	public static final int MSG_TASK_STARTED = 100;
	public static final int MSG_TASK_COMPLETED = 101;
	public static final int MSG_TASK_FAILED = 102;
	public static final int MSG_TASK_IDLE = 103;
	
	public static final int MSG_SET_INT_VALUE = 1;
	
	protected static boolean mServiceBusy = false;
		
	public class DiaryUpdateBinder extends Binder {
		DiaryUpdateService getService() {
	        return DiaryUpdateService.this;
	    }
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		
	    Bundle extras = arg0.getExtras();
	    if (BuildConfig.DEBUG)
	    	Log.d(this.toString(), TS.get() + " onBind () : started");

	    if (extras != null) {
	    	
	    	Messenger m = (Messenger) extras.get("MESSENGER"); 
	    	
	    	try {

				m.send(Message.obtain(null, MSG_SET_INT_VALUE,
						mServiceBusy ? MSG_TASK_STARTED : MSG_TASK_IDLE, 0));

				mClients.add(m);

			} catch (RemoteException e) {
				
				e.printStackTrace();
			}    	
	    }

	    return mBinder;
	}

	public DiaryUpdateService() {
		
		super("DiaryUpdate");
	}
	
	protected void updateActivityWithStatus(int status) {
		
		for (Messenger m : mClients) {
			try {

				m.send(Message.obtain(null, MSG_SET_INT_VALUE,
						status, 0));

			} catch (RemoteException e) {

				mClients.remove(m);
			}
		}
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		GshisLoader.getInstance().setLogin(prefs.getString(getString(R.string.pref_login_key), ""));
		GshisLoader.getInstance().setPassword(prefs.getString(getString(R.string.pref_password_key), ""));
		
		// this will only overwrite context if it's null
		Database.setContext(getApplicationContext());
		
		mServiceBusy = true;

		Thread update = new Thread(new Runnable() {

			public void run() {

				if (BuildConfig.DEBUG)
					Log.d(this.toString(),
							TS.get() + this.toString()
									+ " About to update current week for current pupil.");
				
				updateActivityWithStatus(MSG_TASK_STARTED);				
				GshisLoader.getInstance().getAllPupilsLessons(GshisLoader.getInstance().getCurrWeekStart());
				
				if (BuildConfig.DEBUG)
					Log.d (this.toString(), TS.get() + "getNonCachedLessonsByDate (): finished");
				
				mServiceBusy = false;
				if (!GshisLoader.getInstance().isLastNetworkCallFailed()) {
					
					updateActivityWithStatus(MSG_TASK_COMPLETED);
				} else {
					
					updateActivityWithStatus(MSG_TASK_FAILED);
				}
						
				stopSelf();
			}
		});
		
		update.setPriority(Thread.MIN_PRIORITY + 1);
		update.start();		
	}
}
