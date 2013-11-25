package com.shinymetal.gradereportlt;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.shinymetal.gradereport.objects.Lesson;
import com.shinymetal.gradereport.utils.GshisLoader;

public class LessonsExpListAdapter extends BaseExpandableListAdapter implements UpdateableAdapter {

	private final DiaryActivity mActivity;
	private final Date mDay;
	
	private static final SimpleDateFormat mFormat = new SimpleDateFormat(
			"HH:mm ", Locale.ENGLISH);
	
	private Cursor mCursor;
	
	// used to keep selected position in ListView
	private int mSelectedPos = -1;	// init value for not-selected

	public LessonsExpListAdapter(DiaryActivity activity, Date day) {

		mActivity = activity;
		mDay = day;
		
		mCursor = GshisLoader.getInstance().getCursorLessonsByDate(mDay, mActivity.getPupilName());
	}
	
	public void setSelectedPosition(int pos) {
		
        mSelectedPos = pos;
	}

	public int getSelectedPosition() {
		
		return mSelectedPos;
	}
	
	public void onUpdateTaskComplete () {
		
		if (mCursor != null && !mCursor.isClosed())
			mCursor.close();

		mCursor = GshisLoader.getInstance().getCursorLessonsByDate(mDay, mActivity.getPupilName());
		notifyDataSetChanged();
	}

	@Override
	public int getGroupCount() {
		
		if (mCursor == null || mCursor.isClosed())
			return 0;

		return mCursor.getCount();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		
		if (mCursor == null || mCursor.isClosed())
			return 0;

		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		
		if (mCursor == null || mCursor.isClosed())
			return null;
		
		mCursor.moveToPosition(groupPosition);

		if (!mCursor.isAfterLast())
			return Lesson.getFromCursor(mCursor);

		return null;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		
		if (mCursor == null || mCursor.isClosed())
			return null;

		mCursor.moveToPosition(groupPosition);
		
		if (!mCursor.isAfterLast())		
			return Lesson.getFromCursor(mCursor);
		
		return null;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mActivity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.lessons_list, null);
		}

		TextView itemNameView = (TextView) convertView
				.findViewById(R.id.itemName);
		TextView itemDetailView = (TextView) convertView
				.findViewById(R.id.itemDetail);

		Lesson l = (Lesson) getGroup(groupPosition);
		
		if (l != null) {

			itemNameView.setText(Html.fromHtml("" + l.getNumber() + ". " + l.getFormText()));
			itemDetailView.setText(Html.fromHtml(mFormat.format(l.getStart()) + l.getTeacher()));
		}

		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		if (convertView == null) {
			
			LayoutInflater inflater = (LayoutInflater) mActivity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.lessons_detail, null);
		}
		
		Lesson l = (Lesson) getChild(groupPosition, 0);
		
		if (l != null) {

			TextView textTheme = (TextView) convertView
					.findViewById(R.id.itemTheme);
			String theme = l.getTheme();
			if (theme == null)
				theme = "";
			textTheme.setText(Html.fromHtml(mActivity.getString(R.string.label_theme) + ": " + theme));
	
			TextView textHomework = (TextView) convertView
					.findViewById(R.id.itemHomework);
			String homework = l.getHomework();
			if (homework == null)
				homework = "";
			textHomework.setText(Html.fromHtml(mActivity.getString(R.string.label_homework) + ": "
					+ homework));
	
			TextView textMarks = (TextView) convertView
					.findViewById(R.id.itemMarks);
			String marks = l.getMarks();
			if (marks == null)
				marks = "";
			textMarks.setText(Html.fromHtml(mActivity.getString(R.string.label_marks) + ": " + marks));
	
			TextView textComment = (TextView) convertView
					.findViewById(R.id.itemComment);
			String comment = l.getComment();
			if (comment == null)
				comment = "";
			textComment.setText(Html.fromHtml(mActivity.getString(R.string.label_comment) + ": " + comment));
		}

		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
