package com.shinymetal.gradereport.objects;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.shinymetal.gradereport.db.Database;
import com.shinymetal.gradereport.utils.GshisLoader;

import android.content.ContentValues;
import android.database.Cursor;

public class Pupil extends FormSelectableField {	
	
	private final static String ID_NAME = "ID";
	private final static String FORMID_NAME = "FORMID";
	private final static String FORMTEXT_NAME = "FORMTEXT";
	private final static String USERNAME_NAME = "USERNAME";
	
	public final static String TABLE_NAME = "PUPIL";
	public final static String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " ("
			+ ID_NAME + " INTEGER PRIMARY KEY ASC, "
			+ FORMID_NAME + " TEXT, "
			+ FORMTEXT_NAME	+ " TEXT, "
			+ USERNAME_NAME + " TEXT, "
			+ " UNIQUE (" + FORMID_NAME + ", " + USERNAME_NAME + "));";
	
	private final static String SELECTION_GET_BY_FORM_ID = FORMID_NAME + " = ? AND " + USERNAME_NAME + " = ?";
	private final static String[] COLUMNS_GET_BY_FORM_ID = new String[] {FORMTEXT_NAME, ID_NAME};
	private final static String SELECTION_GET_BY_FORM_NAME = FORMTEXT_NAME + " = ? AND " + USERNAME_NAME + " = ?";
	private final static String[] COLUMNS_GET_BY_FORM_NAME = new String[] {FORMID_NAME, ID_NAME};
	private final static String SELECTION_GET_SET = USERNAME_NAME + " = ?";
	private final static String[] COLUMNS_GET_SET = new String[] {FORMTEXT_NAME, FORMID_NAME, ID_NAME};
	
	private long mRowId;	
	private static volatile Pupil mLastPupil;

	public Pupil(String n) {

		setFormText(n);
	}

	public Pupil(String n, String fId) {

		this(n);
		setFormId(fId);
	}
	
	public long getRowId () { return mRowId; }
	
	public long insert() {
		
        ContentValues values = new ContentValues();
        
        values.put(FORMID_NAME, getFormId());
        values.put(FORMTEXT_NAME, getFormText());
        values.put(USERNAME_NAME, GshisLoader.getInstance().getLogin());

        return mRowId = Database.getWritable().insert(TABLE_NAME, null, values);		
	}
	
	public static synchronized Pupil getByFormId(String fId) {
		
		// Elementary caching
		if (mLastPupil != null && mLastPupil.getFormId().equals(fId))
			return mLastPupil;
		
        String[] args = new String[] { fId, GshisLoader.getInstance().getLogin() };

		Cursor c = Database.getReadable().query(TABLE_NAME,
				COLUMNS_GET_BY_FORM_ID, SELECTION_GET_BY_FORM_ID, args, null,
				null, null);
        c.moveToFirst();
        if (c.getCount() <= 0) {
        	
        	c.close();
        	return null;
        }
        
        Pupil p = new Pupil(c.getString(c.getColumnIndex(FORMTEXT_NAME)), fId);
        p.mRowId = c.getLong(c.getColumnIndex(ID_NAME));

        c.close();
        return mLastPupil = p; 
	}
	
	public static synchronized Pupil getByFormName(String name) {
		
		if (name == null)
			return null;
		
		// Elementary caching
		if (mLastPupil != null && mLastPupil.getFormText().equals(name))
			return mLastPupil;
		
        String[] args = new String[] { name, GshisLoader.getInstance().getLogin() };

		Cursor c = Database.getReadable().query(TABLE_NAME,
				COLUMNS_GET_BY_FORM_NAME, SELECTION_GET_BY_FORM_NAME, args,
				null, null, null);
        c.moveToFirst();
        if (c.getCount() <= 0) {
        	
        	c.close();
        	return null;
        }
        
        Pupil p = new Pupil(name, c.getString(c.getColumnIndex(FORMID_NAME)));
        p.mRowId = c.getLong(c.getColumnIndex(ID_NAME));
        
        c.close();
        return mLastPupil = p; 
	}
	
	public static final Set<Pupil> getSet() {
		
		Set<Pupil> set = new HashSet<Pupil> ();
		String login = GshisLoader.getInstance().getLogin();
		
		if (login == null) return set;
		
        String[] args = new String[] { login };
		Cursor c = Database.getReadable().query(TABLE_NAME, COLUMNS_GET_SET,
				SELECTION_GET_SET, args, null, null, null);

		c.moveToFirst();
		while (!c.isAfterLast()) {

			Pupil p = new Pupil(c.getString(c.getColumnIndex(FORMTEXT_NAME)),
					c.getString(c.getColumnIndex(FORMID_NAME)));
			p.mRowId = c.getLong(c.getColumnIndex(ID_NAME));
			set.add(p);
			
			c.moveToNext();
		}
		
		c.close();
		return set;
	}

	public final Set<Schedule> getScheduleSet() {
		
		return Schedule.getSet(this);
	}

	public Pupil addSchedule(Schedule s) {

		s.insert(this);
		return this;
	}

	public Schedule getScheduleByFormId(String fId) {
	
		return Schedule.getByFormId(this, fId);
	}

	public Schedule getScheduleBySchoolYear(String schoolYear) {

		return Schedule.getBySchoolYear(this, schoolYear);
	}

	public String toString() {
		return getFormText() + " f: " + getFormId ();
	}

	public Schedule getScheduleByDate(Date day) {

		return Schedule.getByDate(this, day);
	}
}
