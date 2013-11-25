package com.shinymetal.gradereport.objects;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import android.content.ContentValues;
import android.database.Cursor;

import com.shinymetal.gradereport.db.Database;

public class Week extends FormTimeInterval {
	
	private final static String ID_NAME = "ID";
	private final static String FORMID_NAME = "FORMID";
	private final static String FORMTEXT_NAME = "FORMTEXT";
	private final static String SCHEDULEID_NAME = "SCHEDULEID";
	private final static String START_NAME = "START";
	private final static String STOP_NAME = "STOP";
	private final static String LOADED_NAME = "LOADED";
	
	public static final String TABLE_NAME = "WEEK";
	public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " ("
			+ ID_NAME + " INTEGER PRIMARY KEY ASC, "
			+ FORMID_NAME + " TEXT NOT NULL, "
			+ SCHEDULEID_NAME + " INTEGER REFERENCES SCHEDULE (ID), "
			+ FORMTEXT_NAME + " TEXT NOT NULL, "
			+ START_NAME + " INTEGER NOT NULL, "
			+ STOP_NAME + " INTEGER NOT NULL, "
			+ LOADED_NAME + " INTEGER NOT NULL, "
			+ " UNIQUE ( " + START_NAME + ", " + STOP_NAME + ", " + SCHEDULEID_NAME + "));";
	

	private final static String SELECTION_GET_BY_DATE = START_NAME
			+ " <= ? AND " + STOP_NAME + " >= ? AND " + SCHEDULEID_NAME
			+ " = ?";
	private final static String[] COLUMNS_GET_BY_DATE = new String[] {
			FORMID_NAME, FORMTEXT_NAME, START_NAME, STOP_NAME, LOADED_NAME,
			ID_NAME };
	private final static String SELECTION_GET_BY_FORM_ID = FORMID_NAME
			+ " = ? AND " + SCHEDULEID_NAME + " = ?";
	private final static String[] COLUMNS_GET_BY_FORM_ID = new String[] {
			FORMTEXT_NAME, START_NAME, STOP_NAME, LOADED_NAME, ID_NAME };
	private final static String SELECTION_GET_SET = SCHEDULEID_NAME + " = ?";
	private final static String[] COLUMNS_GET_SET = new String[] {
			FORMTEXT_NAME, FORMID_NAME, START_NAME, STOP_NAME, LOADED_NAME,
			ID_NAME };

	private boolean mLoaded;
	
	private static volatile Week mLastWeek;

	private long mScheduleId;
	private long mRowId;
	
	public Week () {
		
		mLoaded = false;
	}

	@Override
	public void setStart (Date day) {
		
		super.setStart(getWeekStart(day));
		super.setStop(getWeekStop(day));		
	}
	
	@Override
	public void setStop (Date day) {
		
		super.setStart(getWeekStart(day));
		super.setStop(getWeekStop(day));		
	}
	
	public Week setLoaded () { mLoaded = true; return this; }	
	public boolean getLoaded () { return mLoaded; }

	public static Date getWeekStart (Date day) {
		
		Calendar cal = Calendar.getInstance();				
		
        cal.setTime(day);
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DATE, -1);
        }
        
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        return cal.getTime();
	}
	
	public static Date getWeekStop (Date day) {
		
		Calendar cal = Calendar.getInstance();				
		
        cal.setTime(day);
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            cal.add(Calendar.DATE, 1);
        }

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
	}
	
	public String toString() {
		return getStart().toString() + " - " + getStop().toString() + " l: "
				+ mLoaded + " f: " + getFormId() + " ScheduleId: "
				+ mScheduleId + " mRowId: " + mRowId;
	}

	public long update() {

        ContentValues values = new ContentValues();
		
        values.put(FORMID_NAME, getFormId());
        values.put(FORMTEXT_NAME, getFormText());        
    	values.put(START_NAME, getStart().getTime());
    	values.put(STOP_NAME, getStop().getTime());
    	values.put(LOADED_NAME, getLoaded());
    	
    	String selection = ID_NAME + " = ?";
        String[] args = new String[] { "" + mRowId };
		
    	return Database.getWritable().update(TABLE_NAME, values, selection, args);		
	}

	public long insert(Schedule schedule) {
		
        ContentValues values = new ContentValues();
        
        values.put(FORMID_NAME, getFormId());
        values.put(FORMTEXT_NAME, getFormText());
        values.put(SCHEDULEID_NAME, mScheduleId = schedule.getRowId());
        values.put(LOADED_NAME, getLoaded());
    	values.put(START_NAME, getStart().getTime());
    	values.put(STOP_NAME, getStop().getTime());	

        return mRowId = Database.getWritable().insert(TABLE_NAME, null, values);		
	}

	public static synchronized Week getByDate(Schedule schedule, Date day) {
		
		long date = day.getTime();
		
		// Elementary caching
		if (mLastWeek != null && mLastWeek.getStart().getTime() <= date
				&& mLastWeek.getStop().getTime() >= date
				&& mLastWeek.mScheduleId == schedule.getRowId()) {
			
			return mLastWeek;
		}
        
        String[] args = new String[] { "" + date, "" + date, "" + schedule.getRowId() };

		Cursor c = Database.getReadable().query(TABLE_NAME,
				COLUMNS_GET_BY_DATE, SELECTION_GET_BY_DATE, args, null, null, null);
        c.moveToFirst();
        if (c.getCount() <= 0) {
        	
        	c.close();
        	return null;
        }
		
		Week w = new Week();
		
		w.setFormId(c.getString(c.getColumnIndex(FORMID_NAME)));
		w.setFormText(c.getString(c.getColumnIndex(FORMTEXT_NAME)));
		w.mScheduleId = schedule.getRowId();
		
		long start = c.getLong(c.getColumnIndex(START_NAME));
		w.setStart(new Date(start));		
		long stop = c.getLong(c.getColumnIndex(STOP_NAME));
		w.setStop(new Date(stop));
		w.mRowId = c.getLong(c.getColumnIndex(ID_NAME));
		
		if (c.getInt(c.getColumnIndex(LOADED_NAME)) > 0)
			w.setLoaded();

		c.close();
		return mLastWeek = w;
	}

	public static synchronized Week getByFormId(Schedule schedule, String formId) {
		
		// Elementary caching
		if (mLastWeek != null && mLastWeek.getFormId().equals(formId)
				&& mLastWeek.mScheduleId == schedule.getRowId()) {
			
			return mLastWeek;
		}
        
        String[] args = new String[] { formId, "" + schedule.getRowId() };

		Cursor c = Database.getReadable().query(TABLE_NAME,
				COLUMNS_GET_BY_FORM_ID, SELECTION_GET_BY_FORM_ID, args, null,
				null, null);
        c.moveToFirst();
        if (c.getCount() <= 0) {
        	
        	c.close();
        	return null;
        }
        
        Week w = new Week();
		
		w.setFormText(c.getString(c.getColumnIndex(FORMTEXT_NAME)));
		w.setFormId(formId);
		w.mScheduleId = schedule.getRowId();
		
		long start = c.getLong(c.getColumnIndex(START_NAME));
		w.setStart(new Date(start));		
		long stop = c.getLong(c.getColumnIndex(STOP_NAME));
		w.setStop(new Date(stop));
		w.mRowId = c.getLong(c.getColumnIndex(ID_NAME));
		
		if (c.getInt(c.getColumnIndex(LOADED_NAME)) > 0)
			w.setLoaded();
		
		c.close();
		return mLastWeek = w;
	}

	public static Set<Week> getSet(Schedule schedule) {
		
		Set<Week> set = new HashSet<Week> ();		
        String[] args = new String[] { "" + schedule.getRowId() };

		Cursor c = Database.getReadable().query(TABLE_NAME, COLUMNS_GET_SET,
				SELECTION_GET_SET, args, null, null, null);

		c.moveToFirst();
		while (!c.isAfterLast()) {

			Week w = new Week();
			
			w.setFormId(c.getString(c.getColumnIndex(FORMID_NAME)));
			w.setFormText(c.getString(c.getColumnIndex(FORMTEXT_NAME)));
			w.mScheduleId = schedule.getRowId();
			
			long start = c.getLong(c.getColumnIndex(START_NAME));
			w.setStart(new Date(start));			
			long stop = c.getLong(c.getColumnIndex(STOP_NAME));
			w.setStop(new Date(stop));
			w.mRowId = c.getLong(c.getColumnIndex(ID_NAME));
			
			if (c.getInt(c.getColumnIndex(LOADED_NAME)) > 0)
				w.setLoaded();
			
			set.add(w);
			c.moveToNext();
		}
		
		c.close();
		return set;
	}
}
