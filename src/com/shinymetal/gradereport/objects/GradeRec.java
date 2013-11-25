package com.shinymetal.gradereport.objects;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.shinymetal.gradereport.db.Database;

import android.content.ContentValues;
import android.database.Cursor;

public class GradeRec extends FormTimeInterval {	
	 
	public static final String TABLE_NAME = "GRADE";
	
	private final static String ID_NAME = "ID";
	private final static String FORMTEXT_NAME = "FORMTEXT";
	private final static String SCHEDULEID_NAME = "SCHEDULEID";
	private final static String START_NAME = "START";
	private final static String STOP_NAME = "STOP";
	private final static String ABSENT_NAME = "ABSENT";
	private final static String RELEASED_NAME = "RELEASED";
	private final static String SICK_NAME = "SICK";
	private final static String AVERAGE_NAME = "AVERAGE";
	private final static String TOTAL_NAME = "TOTAL";	

	public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " ("
			+ ID_NAME + " INTEGER PRIMARY KEY ASC, "
			+ SCHEDULEID_NAME + " INTEGER REFERENCES SCHEDULE (ID), "
			+ FORMTEXT_NAME + " TEXT NOT NULL, "
			+ START_NAME + " INTEGER NOT NULL, "
			+ STOP_NAME + " INTEGER NOT NULL, "			
			+ ABSENT_NAME + " INTEGER, "
			+ RELEASED_NAME + " INTEGER, "
			+ SICK_NAME + " INTEGER, "
			+ AVERAGE_NAME + " REAL, "
			+ TOTAL_NAME + " INTEGER, "
			+ " UNIQUE ( " + FORMTEXT_NAME + ", "+ START_NAME + ", " + STOP_NAME + ", " + SCHEDULEID_NAME + "));";
	
	private final static String SELECTION_GET_ALL = SCHEDULEID_NAME + " = ?";
	private final static String[] COLUMNS_GET_ALL = new String[] { ID_NAME,
		SCHEDULEID_NAME, FORMTEXT_NAME, START_NAME, STOP_NAME, ABSENT_NAME, RELEASED_NAME, SICK_NAME, AVERAGE_NAME, TOTAL_NAME };
	public static final String SELECTION_UPDATE = ID_NAME + " = ?";
	public static final String SELECTION_GET_ALL_BY_DATE = SCHEDULEID_NAME + " = ? AND " + START_NAME + " = ?";
	public static final String SELECTION_GET_BY_DATE_TEXT = SCHEDULEID_NAME
			+ " = ? AND " + START_NAME + " = ? AND " + FORMTEXT_NAME + " = ?";

	protected int mAbsent;
	protected int mReleased;
	protected int mSick;
	protected float mAverage;
	protected int mTotal;
	
	@SuppressWarnings("unused")
	private long mScheduleId;
	private long mRowId;
	
	public GradeRec () {
		
	}
	
	public GradeRec addMarcRec (MarkRec rec) {
		
		rec.insert(this);
		return this;
	}
	
	public String toString() {
		
		String res = getFormText() + ": ";		
		res += mTotal + " ( abs: " + mAbsent + ", rel: " + mReleased;
		res += ", mSick: " + mSick + ", av: " + mAverage + ", t: " + mTotal + ") m:";
		
		for (MarkRec rec : MarkRec.getSet(this))
			res += " " + rec.getMarks () + " (" + rec.getComment () + ")"; 
		
		return res;
	}
	
	public int getAbsent() {
		return mAbsent;
	}
	
	public void setAbsent(int absent) {
		this.mAbsent = absent;
	}
	
	public int getReleased() {
		return mReleased;
	}
	
	public void setReleased(int released) {
		this.mReleased = released;
	}
	
	public int getSick() {
		return mSick;
	}
	
	public void setSick(int sick) {
		this.mSick = sick;
	}
	
	public float getAverage() {
		return mAverage;
	}
	
	public void setAverage(float average) {
		this.mAverage = average;
	}
	
	public int getTotal() {
		return mTotal;
	}
	
	public void setTotal(int total) {
		this.mTotal = total;
	}

	public long insert(Schedule schedule) {

		ContentValues values = new ContentValues();
        
        values.put(FORMTEXT_NAME, getFormText());
        values.put(SCHEDULEID_NAME, mScheduleId = schedule.getRowId());
    	values.put(START_NAME, getStart().getTime());
    	values.put(STOP_NAME, getStop().getTime());
    	values.put(ABSENT_NAME, getAbsent());
    	values.put(RELEASED_NAME, getReleased());
    	values.put(SICK_NAME, getSick());    	
    	values.put(AVERAGE_NAME, getAverage());
    	values.put(TOTAL_NAME, getTotal());

        return mRowId = Database.getWritable().insert(TABLE_NAME, null, values);		
	}
	
	public long update() {

		ContentValues values = new ContentValues();
		
        values.put(FORMTEXT_NAME, getFormText());
    	values.put(START_NAME, getStart().getTime());
    	values.put(STOP_NAME, getStop().getTime());
    	values.put(ABSENT_NAME, getAbsent());
    	values.put(RELEASED_NAME, getReleased());
    	values.put(SICK_NAME, getSick());    	
    	values.put(AVERAGE_NAME, getAverage());
    	values.put(TOTAL_NAME, getTotal());

    	String[] args = new String[] { "" + mRowId };
    	return Database.getWritable().update(TABLE_NAME, values, SELECTION_UPDATE, args);
	}
	
	public static Cursor getCursorByStart(Schedule schedule, Date day) {
		
		long date = day.getTime();
        String[] args = new String[] { "" + schedule.getRowId(), "" + date};

		return Database.getReadable().query(TABLE_NAME,
				COLUMNS_GET_ALL, SELECTION_GET_ALL_BY_DATE, args, null,
				null, FORMTEXT_NAME);
	}
	
	public static GradeRec getFromCursor(Cursor c) {

		int fieldPos;

		if (c == null || c.isAfterLast()) {

			return null;
		}

		GradeRec gr = new GradeRec();
		
		if ((fieldPos = c.getColumnIndex(SCHEDULEID_NAME)) != -1)
			gr.mScheduleId = c.getLong(fieldPos);
		
		if ((fieldPos = c.getColumnIndex(ID_NAME)) != -1)
			gr.mRowId = c.getLong(fieldPos);
		
		if ((fieldPos = c.getColumnIndex(START_NAME)) != -1)
			gr.setStart(new Date(c.getLong(fieldPos)));
		
		if ((fieldPos = c.getColumnIndex(STOP_NAME)) != -1)
			gr.setStop(new Date(c.getLong(fieldPos)));
		
		if ((fieldPos = c.getColumnIndex(FORMTEXT_NAME)) != -1)
			gr.setFormText(c.getString(fieldPos));
		
		if ((fieldPos = c.getColumnIndex(ABSENT_NAME)) != -1)
			gr.setAbsent(c.getInt(fieldPos));
		
		if ((fieldPos = c.getColumnIndex(RELEASED_NAME)) != -1)
			gr.setReleased(c.getInt(fieldPos));
		
		if ((fieldPos = c.getColumnIndex(SICK_NAME)) != -1)
			gr.setSick(c.getInt(fieldPos));
		
		if ((fieldPos = c.getColumnIndex(AVERAGE_NAME)) != -1)			
			gr.setAverage(c.getFloat(fieldPos));
		
		if ((fieldPos = c.getColumnIndex(TOTAL_NAME)) != -1)
			gr.setTotal(c.getInt(fieldPos));

		return gr;
	}

	public static GradeRec getByDateText(Schedule schedule, Date day, String text) {
		
		long date = day.getTime();
        String[] args = new String[] { "" + schedule.getRowId(), "" + date, text};
		
		Cursor c = Database.getReadable().query(TABLE_NAME, COLUMNS_GET_ALL,
				SELECTION_GET_BY_DATE_TEXT, args, null, null, null);
	
		c.moveToFirst();
        if (c.getCount() <= 0) {
        	
        	c.close();
        	return null;
        }
        
        GradeRec rec = getFromCursor(c);
        c.close();
        return rec;
	}
	
	public static Set<GradeRec> getSet(Schedule schedule) {

		Set<GradeRec> set = new HashSet<GradeRec> (); 
		String[] args = new String[] { "" + schedule.getRowId() };

		Cursor c = Database.getReadable().query(TABLE_NAME, COLUMNS_GET_ALL,
				SELECTION_GET_ALL, args, null, null, null);

		c.moveToFirst();
		while (!c.isAfterLast()) {

			GradeRec gr = new GradeRec();
			
			gr.mScheduleId = schedule.getRowId();
			gr.mRowId = c.getLong(c.getColumnIndex(ID_NAME));
			long start1 = c.getLong(c.getColumnIndex(START_NAME));
			gr.setStart(new Date(start1));		
			long stop1 = c.getLong(c.getColumnIndex(STOP_NAME));
			gr.setStop(new Date(stop1));
			gr.setFormText(c.getString(c.getColumnIndex(FORMTEXT_NAME)));
			
			gr.setAbsent(c.getInt(c.getColumnIndex(ABSENT_NAME)));
			gr.setReleased(c.getInt(c.getColumnIndex(RELEASED_NAME)));
			gr.setSick(c.getInt(c.getColumnIndex(SICK_NAME)));
			gr.setAverage(c.getFloat(c.getColumnIndex(AVERAGE_NAME)));
			gr.setTotal(c.getInt(c.getColumnIndex(TOTAL_NAME)));
			
			set.add(gr);
			c.moveToNext();
		}
		
		c.close();
		return set;
	}
	
	public Cursor getCursorMarks() {
		
		return MarkRec.getCursor(this); 
	}
	
	public MarkRec getMarkRecByComment(String comment) {
		
		return MarkRec.getByComment(this, comment);
	}

	public long getRowId() {

		return mRowId;
	}
}