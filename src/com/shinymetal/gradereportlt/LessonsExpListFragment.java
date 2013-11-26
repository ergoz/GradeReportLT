package com.shinymetal.gradereportlt;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.TextView;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.shinymetal.gradereport.objects.TS;
import com.shinymetal.gradereport.utils.GshisLoader;

public class LessonsExpListFragment extends Fragment implements
		UpdateableFragment {
 
	protected static final String ARG_SECTION_NUMBER = "section_number";
	protected LessonsExpListAdapter mAdapter;
	protected ExpandableListView mExpListView;
	
	static final SimpleDateFormat mDateFmt = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);

	public LessonsExpListFragment() {

	}

	public UpdateableAdapter getAdapter() {

		return mAdapter;
	}

	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final View rootView = inflater.inflate(R.layout.fragment_exp_list, container, false);
		mExpListView = (ExpandableListView) rootView.findViewById(R.id.section_label);
		
		int width = (int) (getResources().getDisplayMetrics().widthPixels - TypedValue
				.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources()
						.getDisplayMetrics()));
		
		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay()
				.getMetrics(metrics);
		float scale = getResources().getDisplayMetrics().density;
		
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
			mExpListView.setIndicatorBounds(
					width - (int) (50 * scale),
					width - (int) (10 * scale));
		} else {
			mExpListView.setIndicatorBoundsRelative(
					width - (int) (50 * scale),
					width - (int) (10 * scale));
		}

		View header = getLayoutInflater(savedInstanceState).inflate(
				R.layout.lessons_header, null);
		mExpListView.addHeaderView(header);
		
		final AdView mAdView = (AdView) rootView.findViewById(R.id.ad);
		
		AdRequest adRequest = new AdRequest();
	    adRequest.addKeyword("education");
	    adRequest.addKeyword("games");
	    adRequest.addTestDevice(AdRequest.TEST_EMULATOR); 
	    mAdView.loadAd(adRequest);

		mAdView.addOnLayoutChangeListener(new OnLayoutChangeListener() {

			@Override
			public void onLayoutChange(final View pV, final int pLeft,
					final int pTop, final int pRight, final int pBottom,
					final int pOldLeft, final int pOldTop, final int pOldRight,
					final int pOldBottom) {
				
//				if (BuildConfig.DEBUG)
//					Log.d(this.toString(), TS.get() + "ZZZ1"); 
				
				final float lAdHeight = mAdView.getHeight();
				if (lAdHeight != 0) {
					
//					if (BuildConfig.DEBUG)
//						Log.d(this.toString(), TS.get() + "ZZZ2");
					rootView.requestLayout();
				}
			}
		});

		Date day = GshisLoader.getInstance().getCurrWeekStart();
		int wantDoW = getArguments().getInt(ARG_SECTION_NUMBER);
		
		if (BuildConfig.DEBUG)
			Log.d(this.toString(), TS.get() + "refresh (), ARG_SECTION_NUMBER="
					+ wantDoW);

		switch (wantDoW) {
		case 1:
			wantDoW = Calendar.MONDAY;
			break;
		case 2:
			wantDoW = Calendar.TUESDAY;
			break;
		case 3:
			wantDoW = Calendar.WEDNESDAY;
			break;
		case 4:
			wantDoW = Calendar.THURSDAY;
			break;
		case 5:
			wantDoW = Calendar.FRIDAY;
			break;
		case 6:
			wantDoW = Calendar.SATURDAY;
			break;
		default:
			wantDoW = Calendar.SUNDAY;
			break;
		}

		Calendar cal = Calendar.getInstance();
		cal.setTime(day);

		if (wantDoW < cal.get(Calendar.DAY_OF_WEEK)) {
			while (cal.get(Calendar.DAY_OF_WEEK) != wantDoW) {
				cal.add(Calendar.DATE, -1);
			}
		} else if (wantDoW > cal.get(Calendar.DAY_OF_WEEK))
			while (cal.get(Calendar.DAY_OF_WEEK) != wantDoW) {
				cal.add(Calendar.DATE, 1);
			}

		day = cal.getTime();
		
		mExpListView.setOnGroupExpandListener(new OnGroupExpandListener() {

	        @Override
	        public void onGroupExpand(int groupPosition) {

	            if (groupPosition != mAdapter.getSelectedPosition()) {
	            	mExpListView.collapseGroup(mAdapter.getSelectedPosition());

	            }
	            mAdapter.setSelectedPosition(groupPosition);
	        }
	    });

		mAdapter = new LessonsExpListAdapter((DiaryActivity) getActivity(), day);
		mExpListView.setAdapter(mAdapter);

		((TextView) header.findViewById(R.id.itemHeader))
				.setText(mDateFmt.format(day));

		return rootView;
	}
}
