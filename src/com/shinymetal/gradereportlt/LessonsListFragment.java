package com.shinymetal.gradereportlt;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.shinymetal.gradereport.objects.Lesson;
import com.shinymetal.gradereport.objects.TS;
import com.shinymetal.gradereport.utils.GshisLoader;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class LessonsListFragment extends Fragment implements UpdateableFragment {

	protected static final String ARG_SECTION_NUMBER = "section_number";
	protected LessonsListAdapter mAdapter;

	private volatile LessonsListFragment instance;

	public LessonsListFragment() {

		instance = this;
	}

	public UpdateableAdapter getAdapter() {

		return mAdapter;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_lessons, container,
				false);
		ListView listView = (ListView) rootView
				.findViewById(R.id.section_label);

		View header = getLayoutInflater(savedInstanceState).inflate(
				R.layout.lessons_header, null);
		listView.addHeaderView(header);

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
		mAdapter = new LessonsListAdapter((DiaryActivity) getActivity(), day);
		listView.setAdapter(mAdapter);

		((TextView) header.findViewById(R.id.itemHeader))
				.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)
						.format(day));

		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Fragment f = instance.getParentFragment();
				Lesson l = (Lesson) mAdapter.getItem(position - 1);
				
				mAdapter.setSelectedPosition(position - 1);

				if (f != null && f instanceof LessonsNestedFragment) {

					View convertView = ((LessonsNestedFragment) f)
							.getDetailsFragment().getView();

					TextView textTheme = (TextView) convertView
							.findViewById(R.id.itemTheme);
					String theme = l.getTheme();
					if (theme == null)
						theme = "";
					textTheme.setText(Html.fromHtml(getActivity().getString(
							R.string.label_theme)
							+ ": " + theme));

					TextView textHomework = (TextView) convertView
							.findViewById(R.id.itemHomework);
					String homework = l.getHomework();
					if (homework == null)
						homework = "";
					textHomework.setText(Html.fromHtml(getActivity().getString(
							R.string.label_homework)
							+ ": " + homework));

					TextView textMarks = (TextView) convertView
							.findViewById(R.id.itemMarks);
					String marks = l.getMarks();
					if (marks == null)
						marks = "";
					textMarks.setText(Html.fromHtml(getActivity().getString(
							R.string.label_marks)
							+ ": " + marks));

					TextView textComment = (TextView) convertView
							.findViewById(R.id.itemComment);
					String comment = l.getComment();
					if (comment == null)
						comment = "";
					textComment.setText(Html.fromHtml(getActivity().getString(
							R.string.label_comment)
							+ ": " + comment));
				}
			}
		});

		return rootView;
	}
}
