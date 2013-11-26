package com.shinymetal.gradereportlt;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LessonsNestedFragment extends Fragment implements
		UpdateableFragment {
	
	private LessonsListFragment mLessonsFragment;
	private LessonDetailsFragment mLessonsDetailsFragment;

	@Override
	public UpdateableAdapter getAdapter() {

		return mLessonsFragment.getAdapter();
	}
	
	public LessonDetailsFragment getDetailsFragment () {
		
		return mLessonsDetailsFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_nested, container,
				false);
		
		AdView mAdView = (AdView) rootView.findViewById(R.id.ad);
		
		AdRequest adRequest = new AdRequest();
	    adRequest.addKeyword("education");
	    adRequest.addKeyword("games");
	    adRequest.addTestDevice(AdRequest.TEST_EMULATOR); 
	    mAdView.loadAd(adRequest);
		
		mLessonsFragment = new LessonsListFragment();
		mLessonsFragment.setArguments(getArguments());
		mLessonsDetailsFragment = new LessonDetailsFragment();
		
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		
		transaction.replace(R.id.fragment_list, mLessonsFragment, "left");
		transaction.replace(R.id.fragment_detail, mLessonsDetailsFragment, "right");

		transaction.commit();
				
		return rootView;
	}
}
