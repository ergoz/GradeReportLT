package com.shinymetal.gradereportlt;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.shinymetal.gradereport.objects.TS;
import com.shinymetal.gradereport.objects.Week;
import com.shinymetal.gradereport.utils.GshisLoader;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;

public class GradesActivity extends AbstractActivity {

	private GradesPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	private DatePickerFragment mDateSetFragment;
	private Spinner mPupilSpinner;
	private String mPupilName;
	private int mCurPage = 0;

	private static volatile GradesActivity instance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_diary);
		instance = this;

		mSectionsPagerAdapter = new GradesPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		if (savedInstanceState != null) {

			mCurPage = savedInstanceState.getInt("mCurPage");
			mViewPager.setCurrentItem(mCurPage, false);
			mPupilName = savedInstanceState.getString("mPupilName");
		}
		else
		{
			// TODO: put pupil name in the intent
			ArrayList<String> names = mGshisLoader.getPupilNames();
			mPupilName = names.size() > 0 ? names.get(0) : null;
		}
	}
	
	public String getPupilName() {
		
		return mPupilName;
	}

	public void setPupilName(String mPupilName) {
		
		this.mPupilName = mPupilName;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {

		savedInstanceState.putInt("mCurPage",
				mCurPage = mViewPager.getCurrentItem());
		savedInstanceState.putString("mPupilName", mPupilName);

		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_settings:
			// Starting a new Intent
			Intent nextScreen = new Intent(getApplicationContext(),
					PreferencesActivity.class);
			startActivity(nextScreen);
			return true;

		case R.id.action_select_pupil:
			AlertDialog alertDialog;

			LayoutInflater inflater = (LayoutInflater) getApplicationContext()
					.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.pupil_dialog, null);

			ArrayList<String> names = mGshisLoader.getPupilNames();
			ArrayAdapter<String> adp = new ArrayAdapter<String>(
					GradesActivity.this, android.R.layout.simple_spinner_item,
					names);

			mPupilSpinner = (Spinner) layout.findViewById(R.id.pupilSpinner);
			mPupilSpinner.setAdapter(adp);

			AlertDialog.Builder builder = new AlertDialog.Builder(
					GradesActivity.this);
			builder.setView(layout);

			alertDialog = builder.create();
			alertDialog.setTitle(getString(R.string.action_select_pupil));
			alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
					getString(R.string.label_submit),
					new DialogInterface.OnClickListener() {

						public void onClick(final DialogInterface dialog,
								final int which) {

							mPupilName = mPupilSpinner.getSelectedItem().toString();
						}
					});
			alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
					getString(R.string.label_cancel),
					new DialogInterface.OnClickListener() {

						public void onClick(final DialogInterface dialog,
								final int which) {

						}
					});

			alertDialog.show();
			return true;

		case R.id.action_select_date:
			mDateSetFragment = new DatePickerFragment();
			mDateSetFragment.show(getSupportFragmentManager(), "dateDialog");
			return true;

		case R.id.action_reload:
			setRecurringAlarm(this, true);
			return true;
			
		case R.id.action_diary:
			// TODO: put pupil name in the intent
			Intent i = new Intent(GradesActivity.this, DiaryActivity.class);
            startActivity(i);

            // close this activity
            finish();
			return true;
		}
		return true;
	}

	public static class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			Calendar c = Calendar.getInstance();
			c.setTime(mGshisLoader.getCurrWeekStart());

			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH)
					+ ((GradesActivity) getActivity()).mViewPager
							.getCurrentItem();

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			// Do something with the date chosen by the user
			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, year);
			c.set(Calendar.MONTH, month);
			c.set(Calendar.DAY_OF_MONTH, day);
			
			Date weekStart = Week.getWeekStart(c.getTime());
			GshisLoader.getInstance().setCurrWeekStart(weekStart);
			
			// TODO: get correct semester
			
			// this picker should not load again
			instance.getHandler().postDelayed(new Runnable() {
				public void run() {

					instance.recreate();
				}
			}, 1);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the mMenu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.grades, menu);
		return true;
	}

	public class GradesPagerAdapter extends FragmentPagerAdapter {

		public GradesPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			
			Fragment fragment;
			
//	        if (getResources().getConfiguration().orientation
//	                == Configuration.ORIENTATION_LANDSCAPE) {
//	        	
//	        } else {

	        	fragment = new GradesExpListFragment();
//	        }
	        
			Bundle args = new Bundle();
			args.putInt(LessonsExpListFragment.ARG_SECTION_NUMBER, position + 1);
			
			fragment.setArguments(args);
			return fragment;
		}
		
		@Override
	    public int getItemPosition(Object object)
	    {
			if (BuildConfig.DEBUG)
				Log.d(this.toString(), TS.get() + this.toString()
						+ " getItemPosition () started");
			
	        return POSITION_UNCHANGED;
	    }

		@Override
		public int getCount() {
			// Show 6 mTotal pages.
			return 4;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section_gr1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section_gr2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section_gr3).toUpperCase(l);
			case 3:
				return getString(R.string.title_section_gr4).toUpperCase(l);
			}
			return null;
		}
	}
}
