package com.shinymetal.gradereport.objects;

import java.util.HashSet;
import java.util.Set;

import android.content.ContentValues;
import android.database.Cursor;

import com.shinymetal.gradereport.db.Database;

public class MarkRec {

	public static final String TABLE_NAME = "MARK";

	private final static String ID_NAME = "ID";
	private final static String GRADEID_NAME = "GRADEID";
	private final static String MARKS_NAME = "MARKS";
	private final static String COMMENT_NAME = "COMMENT";

	public static final String TABLE_CREATE = "CREATE TABLE "
			+ TABLE_NAME + " (" + ID_NAME + " INTEGER PRIMARY KEY ASC, "
			+ GRADEID_NAME + " INTEGER REFERENCES GRADE (ID), "
			+ MARKS_NAME + " TEXT NOT NULL, "
			+ COMMENT_NAME + " TEXT NOT NULL, "
			+ " UNIQUE ( " + GRADEID_NAME + ", "
			+ COMMENT_NAME + "));";
	
	private final static String SELECTION_GET_ALL = GRADEID_NAME + " = ?";
	private final static String[] COLUMNS_GET_ALL = new String[] { ID_NAME,
			GRADEID_NAME, MARKS_NAME, COMMENT_NAME };
	private final static String SELECTION_GET_BY_COMMENT = GRADEID_NAME
			+ " = ? AND " + COMMENT_NAME + " = ?";
	private final static String[] COLUMNS_GET_BY_COMMENT = COLUMNS_GET_ALL;

	private String mMarks;
	private String mComment;
	
	private long mGradeId;
	private long mRowId;

	public String getMarks() {
		return mMarks;
	}

	public void setMarks(String marks) {
		mMarks = marks;
	}

	public String getComment() {
		return mComment;
	}

	public void setComment(String comment) {
		mComment = comment;
	}

	public MarkRec() {
		
	}
	
	public MarkRec(String marks, String comment) {

		mMarks = marks;
		mComment = comment;
	}
	
	public long update() {

        ContentValues values = new ContentValues();
		
        values.put(GRADEID_NAME, mGradeId);
        values.put(MARKS_NAME, mMarks);        
    	values.put(COMMENT_NAME, mComment);
    	
    	String selection = ID_NAME + " = ?";
        String[] args = new String[] { "" + mRowId };
		
    	return Database.getWritable().update(TABLE_NAME, values, selection, args);		
	}

	public long insert(GradeRec gr) {
		
        ContentValues values = new ContentValues();
        
        values.put(GRADEID_NAME, gr.getRowId());
        values.put(MARKS_NAME, mMarks);        
    	values.put(COMMENT_NAME, mComment);
    	
        return mRowId = Database.getWritable().insert(TABLE_NAME, null, values);		
	}

	public static Cursor getCursor(GradeRec gr) {

        String[] args = new String[] { "" + gr.getRowId()};

		return Database.getReadable().query(TABLE_NAME,
				COLUMNS_GET_ALL, SELECTION_GET_ALL, args, null,
				null, ID_NAME);
	}
	
	public static MarkRec getFromCursor(Cursor c) {
		
		int fieldPos;
		
		if(c == null || c.isAfterLast()) {
			
			return null;
		}
		
		MarkRec m = new MarkRec ();
		
		if ((fieldPos = c.getColumnIndex(ID_NAME)) != -1)
			m.mRowId = c.getLong(fieldPos);
		
		if ((fieldPos = c.getColumnIndex(COMMENT_NAME)) != -1)
			m.setComment(c.getString(fieldPos));
		
		if ((fieldPos = c.getColumnIndex(MARKS_NAME)) != -1)
			m.setMarks(c.getString(fieldPos));

		if ((fieldPos = c.getColumnIndex(ID_NAME)) != -1)
			m.mRowId = c.getLong(fieldPos);
		
		return m;
	}
	
	public static MarkRec getByComment(GradeRec gr, String comment) {
		
        String[] args = new String[] { "" + gr.getRowId(), "" + comment };
        
		Cursor c = Database.getReadable().query(TABLE_NAME,
				COLUMNS_GET_BY_COMMENT, SELECTION_GET_BY_COMMENT, args, null,
				null, null);
        c.moveToFirst();
        if (c.getCount() <= 0) {
        	
        	c.close();
        	return null;
        }
		
        MarkRec m = new MarkRec();
		
		m.setComment(c.getString(c.getColumnIndex(COMMENT_NAME)));
		m.setMarks(c.getString(c.getColumnIndex(MARKS_NAME)));
		m.mGradeId = gr.getRowId();
		m.mRowId = c.getLong(c.getColumnIndex(ID_NAME));
		
		c.close();
		return m;
	}
	
	public static Set<MarkRec> getSet(GradeRec gr) {

		Set<MarkRec> set = new HashSet<MarkRec> (); 
		String[] args = new String[] { "" + gr.getRowId() };

		Cursor c = Database.getReadable().query(TABLE_NAME, COLUMNS_GET_ALL,
				SELECTION_GET_ALL, args, null, null, null);

		c.moveToFirst();
		while (!c.isAfterLast()) {

			MarkRec m = new MarkRec();
			
			m.setComment(c.getString(c.getColumnIndex(COMMENT_NAME)));
			m.setMarks(c.getString(c.getColumnIndex(MARKS_NAME)));
			m.mGradeId = gr.getRowId();
			m.mRowId = c.getLong(c.getColumnIndex(ID_NAME));
			
			set.add(m);
			c.moveToNext();
		}
		
		c.close();
		return set;
	}
}
