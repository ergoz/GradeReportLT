package com.shinymetal.gradereport.objects;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import android.content.ContentValues;
import android.database.Cursor;

import com.shinymetal.gradereport.db.Database;

public class Lesson extends FormTimeInterval {
	
	private final static String ID_NAME = "ID";
	private final static String FORMID_NAME = "FORMID";
	private final static String FORMTEXT_NAME = "FORMTEXT";
	private final static String SCHEDULEID_NAME = "SCHEDULEID";
	private final static String START_NAME = "START";
	private final static String STOP_NAME = "STOP";
	private final static String NUMBER_NAME = "NUMBER";
	private final static String TEACHER_NAME = "TEACHER";
	private final static String THEME_NAME = "THEME";
	private final static String HOMEWORK_NAME = "HOMEWORK";
	private final static String MARKS_NAME = "MARKS";
	private final static String COMMENT_NAME = "COMMENT";
	
	public static final String TABLE_NAME = "LESSON";
	public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " ("
			+ ID_NAME + " INTEGER PRIMARY KEY ASC, "
			+ FORMID_NAME + " TEXT NOT NULL, "
			+ SCHEDULEID_NAME + " INTEGER REFERENCES SCHEDULE (ID), "
			+ FORMTEXT_NAME + " TEXT NOT NULL, "
			+ START_NAME + " INTEGER NOT NULL, "
			+ STOP_NAME + " INTEGER NOT NULL, "
			+ NUMBER_NAME + " INTEGER NOT NULL, "
			+ TEACHER_NAME + " TEXT, "
			+ THEME_NAME + " TEXT, "
			+ HOMEWORK_NAME + " TEXT, "
			+ MARKS_NAME + " TEXT, "
			+ COMMENT_NAME + " TEXT, "
			+ " UNIQUE ( " + START_NAME + ", " + STOP_NAME + ", " + SCHEDULEID_NAME + "));";
	
	public static final String SELECTION_UPDATE = ID_NAME + " = ?";
	public static final String SELECTION_GET_BY_START = START_NAME + " <= ? AND " + STOP_NAME + " >= ? AND "
			+ SCHEDULEID_NAME + " = ?";
	public static final String RAWQUERY_EXISTS_BY_START = "SELECT 1 FROM "
			+ TABLE_NAME + " WHERE " + SELECTION_GET_BY_START;
	public static final String[] COLUMNS_GET_BY_START = new String[] { FORMID_NAME, FORMTEXT_NAME,
			START_NAME, STOP_NAME, ID_NAME, SCHEDULEID_NAME, NUMBER_NAME, TEACHER_NAME,
			THEME_NAME, HOMEWORK_NAME, MARKS_NAME, COMMENT_NAME };
	public static final String SELECTION_GET_ALL_BY_DATE = START_NAME + " >= ? AND " + STOP_NAME + " <= ? AND "
			+ SCHEDULEID_NAME + " = ?";
	public static final String[] COLUMNS_GET_ALL_BY_DATE = new String[] { FORMID_NAME, FORMTEXT_NAME,
			START_NAME, STOP_NAME, ID_NAME, SCHEDULEID_NAME, NUMBER_NAME, TEACHER_NAME,
			THEME_NAME, HOMEWORK_NAME, MARKS_NAME, COMMENT_NAME };
	public static final String SELECTION_GET_BY_NUMBER = START_NAME + " >= ? AND " + STOP_NAME + " <= ? AND "
			+ SCHEDULEID_NAME + " = ? AND " + NUMBER_NAME + " = ?";
	public static final String[] COLUMNS_GET_BY_NUMBER = new String[] { FORMID_NAME, FORMTEXT_NAME,
			START_NAME, STOP_NAME, ID_NAME, SCHEDULEID_NAME, NUMBER_NAME, TEACHER_NAME,
			THEME_NAME, HOMEWORK_NAME, MARKS_NAME, COMMENT_NAME };
	public static final String SELECTION_GET_SET = SCHEDULEID_NAME + " = ?";
	public static final String[] COLUMNS_GET_SET = new String[] {FORMID_NAME, FORMTEXT_NAME,
			START_NAME, STOP_NAME, ID_NAME, SCHEDULEID_NAME, NUMBER_NAME, TEACHER_NAME,
			THEME_NAME, HOMEWORK_NAME, MARKS_NAME, COMMENT_NAME};

	protected String teacher;
	
	protected String theme;
	protected String homework;
	protected String marks;
	protected String comment;
	
	protected int number;
	
	private long mScheduleId;
	private long mRowId;

	public String getTeacher() {
		return teacher;
	}

	public String getTheme() {
		return theme;
	}

	public String getHomework() {
		return homework;
	}

	public String getMarks() {
		return marks;
	}
	
	public String getComment() {
		return comment;
	}

	public int getNumber() {
		return number;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public void setHomework(String homework) {
		this.homework = homework;
	}

	public void setMarks(String marks) {
		this.marks = marks;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public void setTeacher(String teacher) {
		this.teacher = teacher;
	}
	
	public void setNumber(int number) {
		this.number = number;
	}

	public Lesson() {
	}

	public String toString() {

		String res = "" + number + " " + getStart().toString() + " - "
				+ getStop().toString() + " " + getFormText() + " / " + teacher
				+ ", H: " + homework + ", M: " + marks + ", C: " + comment + " mRowId = "
				+ mRowId + " mScheduleId = " + mScheduleId;
		return res;
	}

	public long insert(Schedule schedule) {
		
        ContentValues values = new ContentValues();
        
        values.put(FORMID_NAME, getFormId());
        values.put(FORMTEXT_NAME, getFormText());
        values.put(SCHEDULEID_NAME, mScheduleId = schedule.getRowId());
    	values.put(START_NAME, getStart().getTime());
    	values.put(STOP_NAME, getStop().getTime());
    	values.put(NUMBER_NAME, getNumber());
    	values.put(TEACHER_NAME, getTeacher());
    	values.put(THEME_NAME, getTheme());
    	values.put(HOMEWORK_NAME, getHomework());
    	values.put(MARKS_NAME, getMarks());
    	values.put(COMMENT_NAME, getComment());

        return mRowId = Database.getWritable().insert(TABLE_NAME, null, values);		
	}
	
	public long update() {
		
        ContentValues values = new ContentValues();

        values.put(FORMID_NAME, getFormId());
        values.put(FORMTEXT_NAME, getFormText());        
    	values.put(START_NAME, getStart().getTime());
    	values.put(STOP_NAME, getStop().getTime());
    	values.put(NUMBER_NAME, getNumber());
    	values.put(TEACHER_NAME, getTeacher());
    	values.put(THEME_NAME, getTheme());
    	values.put(HOMEWORK_NAME, getHomework());
    	values.put(MARKS_NAME, getMarks());
    	values.put(COMMENT_NAME, getComment());
    	
        String[] args = new String[] { "" + mRowId };
    	return Database.getWritable().update(TABLE_NAME, values, SELECTION_UPDATE, args);		
	}
	
	public static boolean existsByStart(Schedule schedule, Date startTime) {

		long date = startTime.getTime();
		String[] args = new String[] { "" + date, "" + date,
				"" + schedule.getRowId() };
		Cursor c = Database.getReadable().rawQuery(RAWQUERY_EXISTS_BY_START, args);

		boolean exists = (c.getCount() > 0);
		c.close();
		return exists;
	}
	
	public static Lesson getByStart(Schedule schedule, Date startTime) {

		long date = startTime.getTime();
		String[] args = new String[] { "" + date, "" + date, "" + schedule.getRowId() };

		Cursor c = Database.getReadable().query(TABLE_NAME,
				COLUMNS_GET_BY_START, SELECTION_GET_BY_START, args, null, null,
				null);
        c.moveToFirst();
        if (c.getCount() <= 0) {
        	
        	c.close();
        	return null;
        }
		
		Lesson l = new Lesson();
		
		l.setFormId(c.getString(c.getColumnIndex(FORMID_NAME)));
		l.setFormText(c.getString(c.getColumnIndex(FORMTEXT_NAME)));
		l.mScheduleId = schedule.getRowId();
		long start = c.getLong(c.getColumnIndex(START_NAME));
		l.setStart(new Date(start));		
		long stop = c.getLong(c.getColumnIndex(STOP_NAME));
		l.setStop(new Date(stop));
		l.mRowId = c.getLong(c.getColumnIndex(ID_NAME));
		l.setNumber(c.getInt(c.getColumnIndex(NUMBER_NAME)));
		l.setTeacher(c.getString(c.getColumnIndex(TEACHER_NAME)));
		l.setTheme(c.getString(c.getColumnIndex(THEME_NAME)));
		l.setHomework(c.getString(c.getColumnIndex(HOMEWORK_NAME)));
		l.setMarks(c.getString(c.getColumnIndex(MARKS_NAME)));
		l.setComment(c.getString(c.getColumnIndex(COMMENT_NAME)));
		
		c.close();
		return l;
	}
	
	public static Cursor getCursorAllByDate(Schedule schedule, Date start, Date stop) {

		long date1 = start.getTime();
		long date2 = stop.getTime();
		
        String[] args = new String[] { "" + date1, "" + date2, "" + schedule.getRowId()};

		return Database.getReadable().query(TABLE_NAME,
				COLUMNS_GET_ALL_BY_DATE, SELECTION_GET_ALL_BY_DATE, args, null,
				null, NUMBER_NAME);
	}
	
	public static Lesson getFromCursor(Cursor c) {
		
		int fieldPos;
		
		if(c == null || c.isAfterLast()) {
			
			return null;
		}
		
		Lesson l = new Lesson();
		
		if ((fieldPos = c.getColumnIndex(FORMID_NAME)) != -1)
			l.setFormId(c.getString(fieldPos));

		if ((fieldPos = c.getColumnIndex(FORMTEXT_NAME)) != -1)
			l.setFormText(c.getString(fieldPos));
		
		if ((fieldPos = c.getColumnIndex(TEACHER_NAME)) != -1)
			l.setTeacher(c.getString(fieldPos));
		
		if ((fieldPos = c.getColumnIndex(THEME_NAME)) != -1)
			l.setTheme(c.getString(fieldPos));
		
		if ((fieldPos = c.getColumnIndex(HOMEWORK_NAME)) != -1)
			l.setHomework(c.getString(fieldPos));
		
		if ((fieldPos = c.getColumnIndex(MARKS_NAME)) != -1)
			l.setMarks(c.getString(fieldPos));
		
		if ((fieldPos = c.getColumnIndex(COMMENT_NAME)) != -1)
			l.setComment(c.getString(fieldPos));

		if ((fieldPos = c.getColumnIndex(SCHEDULEID_NAME)) != -1)
			l.mScheduleId = c.getLong(fieldPos);
		
		if ((fieldPos = c.getColumnIndex(START_NAME)) != -1)
			l.setStart(new Date(c.getLong(fieldPos)));
		
		if ((fieldPos = c.getColumnIndex(STOP_NAME)) != -1)
			l.setStop(new Date(c.getLong(fieldPos)));
		
		if ((fieldPos = c.getColumnIndex(ID_NAME)) != -1)
			l.mRowId = c.getLong(fieldPos);
		
		if ((fieldPos = c.getColumnIndex(NUMBER_NAME)) != -1)
			l.setNumber(c.getInt(fieldPos));
	
		return l;
	}

	public static Lesson getByNumber(Schedule schedule, Date start, Date stop, int number) {

		long date1 = start.getTime();
		long date2 = stop.getTime();
        String[] args = new String[] { "" + date1, "" + date2, "" + schedule.getRowId(), "" + number };
        
		Cursor c = Database.getReadable().query(TABLE_NAME,
				COLUMNS_GET_BY_NUMBER, SELECTION_GET_BY_NUMBER, args, null,
				null, null);
        c.moveToFirst();
        if (c.getCount() <= 0) {
        	
        	c.close();
        	return null;
        }
		
		Lesson l = new Lesson();
		
		l.setFormId(c.getString(c.getColumnIndex(FORMID_NAME)));
		l.setFormText(c.getString(c.getColumnIndex(FORMTEXT_NAME)));
		l.mScheduleId = schedule.getRowId();
		long start1 = c.getLong(c.getColumnIndex(START_NAME));
		l.setStart(new Date(start1));		
		long stop1 = c.getLong(c.getColumnIndex(STOP_NAME));
		l.setStop(new Date(stop1));
		l.mRowId = c.getLong(c.getColumnIndex(ID_NAME));
		l.setNumber(c.getInt(c.getColumnIndex(NUMBER_NAME)));
		l.setTeacher(c.getString(c.getColumnIndex(TEACHER_NAME)));
		l.setTheme(c.getString(c.getColumnIndex(THEME_NAME)));
		l.setHomework(c.getString(c.getColumnIndex(HOMEWORK_NAME)));
		l.setMarks(c.getString(c.getColumnIndex(MARKS_NAME)));
		l.setComment(c.getString(c.getColumnIndex(COMMENT_NAME)));
		
		c.close();
		return l;
	}

	public static Set<Lesson> getSet(Schedule schedule) {

		Set<Lesson> set = new HashSet<Lesson> (); 
        String[] args = new String[] { "" + schedule.getRowId() };
        
		Cursor c = Database.getReadable().query(TABLE_NAME, COLUMNS_GET_SET,
				SELECTION_GET_SET, args, null, null, null);

		c.moveToFirst();
		while (!c.isAfterLast()) {
			
			Lesson l = new Lesson();
			
			l.setFormId(c.getString(c.getColumnIndex(FORMID_NAME)));
			l.setFormText(c.getString(c.getColumnIndex(FORMTEXT_NAME)));
			l.mScheduleId = schedule.getRowId();
			long start = c.getLong(c.getColumnIndex(START_NAME));
			l.setStart(new Date(start));		
			long stop = c.getLong(c.getColumnIndex(STOP_NAME));
			l.setStop(new Date(stop));
			l.mRowId = c.getLong(c.getColumnIndex(ID_NAME));
			l.setNumber(c.getInt(c.getColumnIndex(NUMBER_NAME)));
			l.setTeacher(c.getString(c.getColumnIndex(TEACHER_NAME)));
			l.setTheme(c.getString(c.getColumnIndex(THEME_NAME)));
			l.setHomework(c.getString(c.getColumnIndex(HOMEWORK_NAME)));
			l.setMarks(c.getString(c.getColumnIndex(MARKS_NAME)));
			l.setComment(c.getString(c.getColumnIndex(COMMENT_NAME)));

			set.add(l);
			c.moveToNext();
		}
		
		c.close();
		return set;
	}
}
