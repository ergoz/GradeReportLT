package com.shinymetal.gradereportlt;

import java.util.Date;

import com.bugsense.trace.BugSenseHandler;
import com.shinymetal.gradereport.db.Database;
import com.shinymetal.gradereport.objects.TS;
import com.shinymetal.gradereport.utils.GshisLoader;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Window;

public class AbstractActivity extends FragmentActivity {
	
	protected static volatile AbstractActivity instance;	
	protected static final GshisLoader mGshisLoader = GshisLoader.getInstance();
	
	protected static class IncomingHandler extends Handler {
		
        @Override
		public void handleMessage(Message message) {

			if (BuildConfig.DEBUG)
				Log.d(this.toString(), TS.get() + "received Message what="
						+ message.what + " arg1 = " + message.arg1);

			switch (message.what) {
			case DiaryUpdateService.MSG_SET_INT_VALUE:

				switch (message.arg1) {
				
				case DiaryUpdateService.MSG_TASK_STARTED:
					instance.setProgressBarIndeterminateVisibility(true);
					break;
				
				case DiaryUpdateService.MSG_TASK_COMPLETED:
					instance.recreate();
					break;
					
				case DiaryUpdateService.MSG_TASK_IDLE:
					instance.setProgressBarIndeterminateVisibility(false);
					break;

				case DiaryUpdateService.MSG_TASK_FAILED:
					instance.setProgressBarIndeterminateVisibility(false);
					
					if (!mGshisLoader.isLastNetworkCallRetriable())
						instance.showAlertDialog(mGshisLoader.getLastNetworkFailureReason());
					break;
				}

				break;
			}
		}
	}
	
	private IncomingHandler mHandler = new IncomingHandler ();	
	private DiaryUpdateService mUpdateService;
	
	public Handler getHandler() { return mHandler; }
	
	public ServiceConnection mConnection = new ServiceConnection() {

	    public void onServiceConnected(ComponentName className, IBinder binder) {
	    	
	        mUpdateService = ((DiaryUpdateService.DiaryUpdateBinder) binder).getService();
	    }

	    public void onServiceDisconnected(ComponentName className) {

	        mUpdateService = null;
	        setProgressBarIndeterminateVisibility(false);
	    }
	};
		
	public void doBindService() {
	    Intent intent = null;
	    intent = new Intent(this, DiaryUpdateService.class);
	    // Create a new Messenger for the communication back
	    // From the Service to the Activity
	    Messenger messenger = new Messenger(mHandler);
	    intent.putExtra("MESSENGER", messenger);

	    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	public void showAlertDialog (String text) {
		
    	if (isFinishing()) {
    		// Don't update UI if Activity is finishing.
    		return;
    	}
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.title_error)); 
        builder.setMessage(text);
        builder.setPositiveButton(getString(R.string.label_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
            }
        });
        
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
	
	protected void setRecurringAlarm(Context context, boolean force) {
		
		boolean alarmUp = (PendingIntent.getBroadcast(context, 0, 
		        new Intent(context, AlarmReceiver.class), 
		        PendingIntent.FLAG_NO_CREATE) != null);
		
		if (alarmUp && !force)
			return;
		
		Intent downloader = new Intent(context, AlarmReceiver.class);
		downloader.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
				downloader, PendingIntent.FLAG_UPDATE_CURRENT);
		
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		Date firstRun = new Date();
		long mSyncInterval = Long.parseLong(PreferenceManager
				.getDefaultSharedPreferences(this).getString(
						getString(R.string.pref_sync_key), "15")) * 60000;
				
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
				firstRun.getTime() + 10,
				mSyncInterval, pendingIntent);
		
		if (BuildConfig.DEBUG)
			Log.d(this.toString(),
					TS.get() + this.toString()
							+ " Set alarmManager.setRepeating to: "
							+ firstRun.toString() + " interval: "
							+ mSyncInterval);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		BugSenseHandler.initAndStartSession(AbstractActivity.this, getString(R.string.bugsense_id));
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		instance = this;

		Database.setContext(this.getApplicationContext());
		
		// this is required to get proper list of pupils in fragments
		mGshisLoader.setLogin(PreferenceManager.getDefaultSharedPreferences(
				this).getString(getString(R.string.pref_login_key), ""));
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	
//		Do not uncomment this!
//		super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	public void onPause() {
		
		if (mUpdateService != null) {
			
			unbindService(mConnection);
			mUpdateService = null;
		}

		super.onPause();
	}
	
	@Override
	public void onResume() {
		
		if (mUpdateService == null) {
			
			doBindService();
		}
		
		setRecurringAlarm(this, false);
		super.onResume();
	}
}
