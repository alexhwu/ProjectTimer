package com.cooltofu.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class TimerDbAdapter {

	// Database fields
	public static final String KEY_ROWID = "_id";
	public static final String KEY_LABEL = "label";
	public static final String KEY_SECONDS = "seconds";
	public static final String KEY_TIMESTAMP = "timestamp";
	public static final String KEY_IS_ON = "is_on";
	private static final String DB_TABLE = "timer";
	
	private Context context;
	private SQLiteDatabase db;
	private TimerDbHelper dbHelper;

	public TimerDbAdapter(Context context) {
		this.context = context;
		
	}

	public TimerDbAdapter open() throws SQLException {
		
		dbHelper = new TimerDbHelper(context);
		db = dbHelper.getWritableDatabase();
	
		return this;
	}

	public void close() {
		dbHelper.close();
	}

	
/**
	 * Create a new timer If the timer is successfully created return the new
	 * rowId for that timer, otherwise return a -1 to indicate failure.
	 */

	public long createTimer(String label, int seconds, long timestamp, boolean isOn) {
		ContentValues values = createContentValues(label, seconds, timestamp, isOn);

		return db.insert(DB_TABLE, null, values);
	}

	
/**
	 * Update the timer
	 */

	public boolean updateTimer(long rowId, String label, int seconds, long timestamp, boolean isOn) {
		ContentValues values = createContentValues(label, seconds, timestamp, isOn);

		return db.update(DB_TABLE, values, KEY_ROWID + "=" + rowId, null) > 0;
	}

	
/**
	 * Deletes timer
	 */

	public boolean deleteTimer(long rowId) {
		return db.delete(DB_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	
	
/**
	 * Return a Cursor over the list of all timer in the database
	 * 
	 * @return Cursor over all notes
	 */

	public Cursor fetchAllTimers() {
		return db.query(DB_TABLE, new String[] { KEY_ROWID, KEY_LABEL, KEY_SECONDS, KEY_TIMESTAMP, KEY_IS_ON }, null, null, null, null, null);
	}

	
/**
	 * Return a Cursor positioned at the defined timer
	 */

	public Cursor fetchTimer(long rowId) throws SQLException {
		Cursor mCursor = db.query(true, DB_TABLE, new String[] { KEY_ROWID, KEY_LABEL, KEY_SECONDS, KEY_IS_ON }, KEY_ROWID + "="
				+ rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	private ContentValues createContentValues(String label, int seconds, long timestamp, boolean isOn) {
		ContentValues values = new ContentValues();
		values.put(KEY_LABEL, label);
		values.put(KEY_SECONDS, seconds);
		values.put(KEY_TIMESTAMP, timestamp);
		values.put(KEY_IS_ON, isOn);
		return values;
	}

}