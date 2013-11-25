package com.shinymetal.gradereport.objects;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import android.content.ContentValues;
import android.database.Cursor;

import com.shinymetal.gradereport.db.Database;

public class Schedule extends FormTimeInterval {
	
	private final static String ID_NAME = "ID";
	private final static String FORMID_NAME = "FORMID";
	private final static String FORMTEXT_NAME = "FORMTEXT";
	private final static String PUPILID_NAME = "PUPILID";
	private final static String START_NAME = "START";
	private final static String STOP_NAME = "STOP";
	
	public final static String TABLE_NAME = "SCHEDULE";
	public final static String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " ("
			+ ID_NAME + " INTEGER PRIMARY KEY ASC, "
			+ FORMID_NAME + " TEXT NOT NULL, "
			+ PUPILID_NAME + " INTEGER REFERENCES PUPIL (ID), "
			+ FORMTEXT_NAME + " TEXT NOT NULL, "
			+ START_NAME + " INTEGER NOT NULL, "
			+ STOP_NAME + " INTEGER NOT NULL, "
			+ " UNIQUE ( " + START_NAME + ", " + STOP_NAME + ", " + PUPILID_NAME + "));";
	
	private final static String SELECTION_UPDATE = ID_NAME + " = ?";
	private final static String SELECTION_GET_BY_FORM_ID = FORMID_NAME + " = ? AND " + PUPILID_NAME + " = ?";
	private final static String[] COLUMNS_GET_BY_FORM_ID = new String[] {FORMTEXT_NAME, START_NAME, STOP_NAME, ID_NAME};
	private final static String[] COLUMNS_GET_SET = new String[] {FORMTEXT_NAME, FORMID_NAME, START_NAME, STOP_NAME, ID_NAME};
	private final static String SELECTION_GET_SET = PUPILID_NAME + " = ?";	
	private final static String SELECTION_GET_BY_SCHOOL_YEAR = FORMTEXT_NAME + " = ? AND " + PUPILID_NAME + " = ?";
	private final static String[] COLUMNS_GET_BY_SCHOOL_YEAR = new String[] {FORMID_NAME, START_NAME, STOP_NAME, ID_NAME};
	private final static String SELECTION_GET_BY_DATE = START_NAME + " <= ? AND " + STOP_NAME + " >= ? AND "
			+ PUPILID_NAME + " = ?";
	private final static String[] COLUMNS_GET_BY_DATE = new String[] {FORMID_NAME, FORMTEXT_NAME, START_NAME, STOP_NAME, ID_NAME};
	
	@SuppressWarnings("unused")
	private long mPupilId;
	private long mRowId;
	
	public Schedule(String formId, String schoolYear) {

		setFormId(formId);
		setFormText(schoolYear);
	}
	
	public long getRowId () { return mRowId; }
	
	public long insert(Pupil p) {		

        ContentValues values = new ContentValues();
        
        values.put(FORMID_NAME, getFormId());
        values.put(FORMTEXT_NAME, getFormText());
        values.put(PUPILID_NAME, mPupilId = p.getRowId());
        
    	values.put(START_NAME, getStart().getTime());
    	values.put(STOP_NAME, getStop().getTime());	

        return mRowId = Database.getWritable().insert(TABLE_NAME, null, values);	
	}
	
	public long update() {
		
        ContentValues values = new ContentValues();
        
        values.put(FORMID_NAME, getFormId());
        values.put(FORMTEXT_NAME, getFormText());        
    	values.put(START_NAME, getStart().getTime());
    	values.put(STOP_NAME, getStop().getTime());	
        String[] args = new String[] { "" + mRowId };
		
    	return Database.getWritable().update(TABLE_NAME, values, SELECTION_UPDATE, args);
	}
	
	public static Schedule getByFormId(Pupil p, String fId) {

        String[] args = new String[] { fId, "" + p.getRowId() };

		Cursor c = Database.getReadable().query(TABLE_NAME,
				COLUMNS_GET_BY_FORM_ID, SELECTION_GET_BY_FORM_ID, args, null,
				null, null);
        c.moveToFirst();
        if (c.getCount() <= 0) {
        	
        	c.close();
        	return null;
        }
		
		Schedule s = new Schedule (fId, c.getString(c.getColumnIndex(FORMTEXT_NAME)));
		long start = c.getLong(c.getColumnIndex(START_NAME));
		s.setStart(new Date(start));		
		long stop = c.getLong(c.getColumnIndex(STOP_NAME));
		s.setStop(new Date(stop));
		s.mRowId = c.getLong(c.getColumnIndex(ID_NAME));
		
		c.close();
		return s;
	}
	
	public static final Set<Schedule> getSet(Pupil p) {
		
		Set<Schedule> set = new HashSet<Schedule> (); 

        String[] args = new String[] { "" + p.getRowId() };
		
		Cursor c = Database.getReadable().query(TABLE_NAME, COLUMNS_GET_SET,
				SELECTION_GET_SET, args, null, null, null);

		c.moveToFirst();
		while (!c.isAfterLast()) {

			String formText = c.getString(c.getColumnIndex(FORMTEXT_NAME));
			String formId = c.getString(c.getColumnIndex(FORMID_NAME));
			
			Schedule s = new Schedule (formId, formText);
			s.mPupilId = p.getRowId();
			s.setStart(new Date(c.getInt(c.getColumnIndex(START_NAME))));
			s.setStop(new Date(c.getInt(c.getColumnIndex(STOP_NAME))));
			s.mRowId = c.getLong(c.getColumnIndex(ID_NAME));
			
			set.add(s);
			c.moveToNext();
		}
		
		c.close();
		return set;
	}
	
	public static Schedule getBySchoolYear(Pupil p, String schoolYear) {

        String[] args = new String[] { schoolYear, "" + p.getRowId() };
        
		Cursor c = Database.getReadable().query(TABLE_NAME,
				COLUMNS_GET_BY_SCHOOL_YEAR, SELECTION_GET_BY_SCHOOL_YEAR, args,
				null, null, null);
        c.moveToFirst();
        if (c.getCount() <= 0) {
        	
        	c.close();
        	return null;
        }
		
		Schedule s = new Schedule (c.getString(c.getColumnIndex(FORMID_NAME)), schoolYear);
		s.mPupilId = p.getRowId();
		long start = c.getLong(c.getColumnIndex(START_NAME));
		s.setStart(new Date(start));		
		long stop = c.getLong(c.getColumnIndex(STOP_NAME));
		s.setStop(new Date(stop));
		s.mRowId = c.getLong(c.getColumnIndex(ID_NAME));
		
		c.close();
		return s;
	}
	
	public static Schedule getByDate(Pupil p, Date day) {
		
		long date = day.getTime();
        String[] args = new String[] { "" + date, "" + date, "" + p.getRowId() };

		Cursor c = Database.getReadable().query(TABLE_NAME,
				COLUMNS_GET_BY_DATE, SELECTION_GET_BY_DATE, args, null, null,
				null);
        c.moveToFirst();
        if (c.getCount() <= 0) {
        	
        	c.close();
        	return null;
        }
		
		Schedule s = new Schedule(c.getString(c.getColumnIndex(FORMID_NAME)),
				c.getString(c.getColumnIndex(FORMTEXT_NAME)));
		s.mPupilId = p.getRowId();
		long start = c.getLong(c.getColumnIndex(START_NAME));
		s.setStart(new Date(start));		
		long stop = c.getLong(c.getColumnIndex(STOP_NAME));
		s.setStop(new Date(stop));
		s.mRowId = c.getLong(c.getColumnIndex(ID_NAME));
		
		c.close();
		return s;
	}

	public Schedule addLesson(Lesson l) throws IllegalStateException {

		l.insert(this);
		return this;
	}
	
	public Schedule addSemester(GradeSemester s) {

		s.insert(this);
		return this;
	}
	
	public GradeSemester getSemester(Date day) {

		return GradeSemester.getByDate(this, day);
	}
	
	public GradeSemester getSemesterByNumber(int number) {

		return GradeSemester.getByNumber(this, number);
	}
	
	public GradeSemester getSemester(String formId) {

		return GradeSemester.getByFormId(this, formId);
	}

	public Schedule addGradeRec(GradeRec gr) {

		gr.insert(this);
		return this;
	}

	public Lesson getLessonByStart(Date start) {
		
		return Lesson.getByStart(this, start);
	}
	
	public boolean existsLessonByStart(Date start) {
		
		return Lesson.existsByStart(this, start);
	}
	
	public Cursor getCursorLessonsByDate(Date date) {
		
		Date start;
		Calendar cal = Calendar.getInstance();				
		
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        start = cal.getTime();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
		
		return Lesson.getCursorAllByDate(this, start, cal.getTime());
	}
	
	public Cursor getCursorGradesByDate(Date date) {
	
		return GradeRec.getCursorByStart(this, date);
	}	
	
	public Lesson getLessonByNumber(Date date, int number) {
		
		Date start;
		Calendar cal = Calendar.getInstance();				
		
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        start = cal.getTime();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);

		return Lesson.getByNumber(this, start, cal.getTime(), number);
	}

	public Schedule addWeek(Week w) {
		
		w.insert(this);
		return this;
	}

	public Week getWeek(Date day) {
		
		Week w = Week.getByDate(this, day);
		return w;
	}
	
	public Week getWeek(String formId) {

		Week w = Week.getByFormId(this, formId);
		return w;
	}

	public final Set<GradeSemester> getSemesterSet() {
		
		return GradeSemester.getSet(this);
	}

	public final Set<Week> getWeekSet() {
		
		return Week.getSet(this);
	}
	public final Set<Lesson> getLessonSet() {
		
		return Lesson.getSet(this);
	}

	public final Set<GradeRec> getGradeRecSet() {
		
		return GradeRec.getSet(this);
	}
	
	public GradeRec getGradeRecByDateText(Date day, String text) {
		
		return GradeRec.getByDateText(this, day, text);
	}
}
