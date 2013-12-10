package com.shinymetal.gradereportlt;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.shinymetal.gradereport.utils.NetLogger;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LogListAdapter extends BaseAdapter {
	
	private Cursor mCursor;
	private static final SimpleDateFormat mFormat = new SimpleDateFormat(
			"HH:mm ", Locale.ENGLISH);
	
	public LogListAdapter (Cursor cursor) {
		
		mCursor = cursor;
	}

	@Override
	public int getCount() {

		if (mCursor == null || mCursor.isClosed())
			return 0;
		
		return mCursor.getCount();
	}

	@Override
	public Object getItem(int position) {
		
		if (mCursor == null || mCursor.isClosed())
			return null;
		
		mCursor.moveToPosition(position);
		
		if (!mCursor.isAfterLast())		
			return mCursor;
		
		return null;
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) parent.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.log_list_item, null);
		}

		TextView itemDate = (TextView) convertView.findViewById(R.id.logDate);
		TextView itemText = (TextView) convertView.findViewById(R.id.logText);
		
		if (getItem(position) != null) {
			
			Date d = new Date(mCursor.getLong(mCursor.getColumnIndex(NetLogger.EVENTTIME_NAME)));
			
			itemDate.setText(mFormat.format(d));			
			itemText.setText(mCursor.getString(mCursor.getColumnIndex(NetLogger.EVENTTEXT_NAME)));
		}
		
		return convertView;
	}
}
