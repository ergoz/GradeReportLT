package com.shinymetal.gradereportlt;

import java.util.Date;

import com.shinymetal.gradereportlt.R;
import com.shinymetal.gradereport.objects.TS;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
// import android.widget.Button;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

public class PreferencesActivity extends PreferenceActivity implements OnPreferenceChangeListener {
	
	private static volatile PreferencesActivity instance;
	private boolean mRescheduleService = false;

	public static class MyPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			addPreferencesFromResource(R.xml.preferences);

			Preference pref = findPreference(getString(R.string.pref_sync_key));
			pref.setOnPreferenceChangeListener(instance);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preferences);
		
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new MyPreferenceFragment())
				.commit();
		
		instance = this;
	}
	
	@Override
	public void onPause() {
		
		if (BuildConfig.DEBUG)
			Log.d(this.toString(), TS.get()	+ "onPause () : start");
		
		if (mRescheduleService) {
			
			if (BuildConfig.DEBUG)
				Log.d(this.toString(), TS.get()	+ "onPause () : reschedule service");
			
			AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

			Date firstRun = new Date();
			long mSyncInterval = Long.parseLong(PreferenceManager
					.getDefaultSharedPreferences(this).getString(getString(R.string.pref_sync_key), "15")) * 60000;
			
			Intent downloader = new Intent(this, AlarmReceiver.class);
			downloader.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			
			PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
					downloader, PendingIntent.FLAG_UPDATE_CURRENT);
					
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
		
		super.onPause();
	}

	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue) {
		
		if (BuildConfig.DEBUG)
			Log.d(this.toString(), TS.get()	+ "onPreferenceChange () : start");

		// Check that the string is an integer
		if (newValue != null && newValue.toString().length() > 0) {
			
			if (BuildConfig.DEBUG)
				Log.d(this.toString(), TS.get()	+ "onPreferenceChange () : reschedule needed");
		
			mRescheduleService = true;
			return true;
		}
		
		// Must not get here
		return false;
	}
}