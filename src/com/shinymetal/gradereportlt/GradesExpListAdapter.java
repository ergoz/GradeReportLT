package com.shinymetal.gradereportlt;

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import com.shinymetal.gradereport.objects.GradeRec;
import com.shinymetal.gradereport.objects.MarkRec;
import com.shinymetal.gradereport.utils.GshisLoader;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class GradesExpListAdapter extends BaseExpandableListAdapter implements UpdateableAdapter {
	
	private final GradesActivity mActivity;
	private Cursor mGradesCursor;
	private SortedMap<Integer, Cursor> mMarksCursors;
	private int mSemester;
	
	// used to keep selected position in ListView
	private int mSelectedPos = -1;	// init value for not-selected

	public GradesExpListAdapter(GradesActivity activity, int sem) {

		mActivity = activity;
		mSemester = sem;
		
		mGradesCursor = GshisLoader.getInstance().getCursorGradesBySemester(
				mActivity.getPupilName(), mSemester);
		mMarksCursors = new TreeMap<Integer, Cursor> ();
	}

	@Override
	public void onUpdateTaskComplete() {
		
		if (mGradesCursor != null && !mGradesCursor.isClosed())
			mGradesCursor.close();
		
		if (mMarksCursors != null && mMarksCursors.size() > 0)
			for(Entry<Integer, Cursor> e : mMarksCursors.entrySet()) {
				
				e.getValue().close();
				mMarksCursors.remove(e.getKey());
			}
		
		mGradesCursor = GshisLoader.getInstance().getCursorGradesBySemester(
				mActivity.getPupilName(), mSemester);
	}

	public int getSelectedPosition() {

		return mSelectedPos;
	}

	public void setSelectedPosition(int groupPosition) {

		mSelectedPos = groupPosition;		
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		
		if (mMarksCursors != null && mMarksCursors.size() > 0
				&& mMarksCursors.containsKey(groupPosition)) {
			
			Cursor c = mMarksCursors.get(groupPosition);
			if (c != null && !c.isClosed()) {
				
				c.moveToPosition(childPosition);
				if (!c.isAfterLast())
					return MarkRec.getFromCursor(c);
			}			
		}
		
		GradeRec gr = (GradeRec) getGroup (groupPosition);
		if (gr == null)
			return null;
		
		Cursor c = gr.getCursorMarks();
		if (c != null && !c.isClosed()) {
			
			c.moveToPosition(childPosition);
			
			if (!c.isAfterLast()) {
				
				MarkRec m = MarkRec.getFromCursor(c);
				mMarksCursors.put(groupPosition, c);
				return m;
			}
		}

		c.close();
		return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {

		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		if (convertView == null) {
			
			LayoutInflater inflater = (LayoutInflater) mActivity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.grades_detail, null);
		}
		
		MarkRec m = (MarkRec) getChild(groupPosition, childPosition);
		
		if (m != null) {
			
			TextView itemCommentView = (TextView) convertView.findViewById(R.id.itemComment);
			TextView itemMarksView = (TextView) convertView.findViewById(R.id.itemMarks);

			itemCommentView.setText(Html.fromHtml(m.getComment()));
			itemMarksView.setText(Html.fromHtml(m.getMarks()));
		}
		
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		
		if (mMarksCursors != null && mMarksCursors.size() > 0
				&& mMarksCursors.containsKey(groupPosition)) {
			
			Cursor c = mMarksCursors.get(groupPosition);
			if (c != null && !c.isClosed()) {
				
				return c.getCount();
			}			
		}
		
		GradeRec gr = (GradeRec) getGroup (groupPosition);
		if (gr == null)
			return 0;
		
		Cursor c = gr.getCursorMarks();
		if (c != null && !c.isClosed()) {
			
			mMarksCursors.put(groupPosition, c);
			return c.getCount();
		}

		return 0;
	}

	@Override
	public Object getGroup(int groupPosition) {

		if (mGradesCursor == null || mGradesCursor.isClosed())
			return null;

		mGradesCursor.moveToPosition(groupPosition);
		if (mGradesCursor.isAfterLast())
			return null;
		
		return GradeRec.getFromCursor(mGradesCursor);
	}

	@Override
	public int getGroupCount() {
		
		if (mGradesCursor == null || mGradesCursor.isClosed())
			return 0;
		
		return mGradesCursor.getCount();
	}

	@Override
	public long getGroupId(int groupPosition) {

		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mActivity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.grades_list, null);
		}

		GradeRec gr = (GradeRec) getGroup(groupPosition);

		if (gr != null) {
			
			TextView itemNameView = (TextView) convertView.findViewById(R.id.itemName);
			TextView itemAbsentView = (TextView) convertView.findViewById(R.id.itemAbsent);
			TextView itemReleasedView = (TextView) convertView.findViewById(R.id.itemReleased);
			TextView itemSickView = (TextView) convertView.findViewById(R.id.itemSick);
			TextView itemAverageView = (TextView) convertView.findViewById(R.id.itemAverage);
			TextView itemTotalView = (TextView) convertView.findViewById(R.id.itemTotal);

			itemNameView.setText(Html.fromHtml(gr.getFormText()));
			
			itemAbsentView.setText(mActivity.getString(R.string.label_absent)
					+ ": " + gr.getAbsent());
			
			itemReleasedView.setText(mActivity.getString(R.string.label_released)
					+ ": " + gr.getReleased());
			
			itemSickView.setText(mActivity.getString(R.string.label_sick)
					+ ": " + gr.getSick());
			
			itemAverageView.setText(mActivity.getString(R.string.label_average)
					+ ": " + gr.getAverage());
			
			itemTotalView.setText(mActivity.getString(R.string.label_total)
					+ ": " + gr.getTotal());
		}

		return convertView;
	}

	@Override
	public boolean hasStableIds() {

		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {

		return false;
	}

}
