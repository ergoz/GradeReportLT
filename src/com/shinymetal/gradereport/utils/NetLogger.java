package com.shinymetal.gradereport.utils;

import java.util.Date;

import com.shinymetal.gradereport.db.Database;

import android.content.ContentValues;
import android.database.Cursor;

public class NetLogger {
	
	public final static String ID_NAME = "ID";
	public final static String EVENTTIME_NAME = "TIME";
	public final static String EVENTTEXT_NAME = "EVENT";
	
	public static final String TABLE_NAME = "NETLOG";
	public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " ("
			+ ID_NAME + " INTEGER PRIMARY KEY ASC, "
			+ EVENTTIME_NAME + " INTEGER NOT NULL, "
			+ EVENTTEXT_NAME + " INTEGER NOT NULL);";
	public static final String SELECTION_OLDER_THAN = EVENTTIME_NAME + " <= ?";
	public static final String[] COLUMNS_GET_ALL = new String[] { ID_NAME, EVENTTIME_NAME, EVENTTEXT_NAME};
	
	public static long add(String event) {
		
		 ContentValues values = new ContentValues();
		 
		 values.put(EVENTTIME_NAME, (new Date()).getTime());
		 values.put(EVENTTEXT_NAME, event);
		 
		 return Database.getWritable().insert(TABLE_NAME, null, values);
	}
	
	public static long trim (int seconds) {
		
		long millis = (new Date()).getTime() - (seconds * 1000);
		String[] args = new String[] { "" + millis };
		
		return Database.getWritable().delete(TABLE_NAME, SELECTION_OLDER_THAN, args);
	}
	
	public static Cursor getCursorAll() {
	
		return Database.getReadable().query(TABLE_NAME,	
			COLUMNS_GET_ALL, null, null, null, null, EVENTTIME_NAME + " DESC");
	}

}
