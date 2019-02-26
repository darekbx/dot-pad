package com.dotpad.db;

import java.util.ArrayList;
import java.util.List;

import com.dotpad.R;
import com.dotpad.model.Dot;
import com.dotpad.model.StatisticsItem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Point;
import android.os.Environment;

public class DotManager {

	private SQLiteDatabase mDb;
	private Context mContext;
	
	public enum Contents {
	
		ACTIVE,
		ARCHIVAL,
		ALL
	}

	public DotManager(Context context) {

		final StringBuilder builder = new StringBuilder();
		builder.append(Environment.getExternalStorageDirectory());
		builder.append(context.getString(R.string.db_name));
		
		this.mContext = context;
		this.mDb = SQLiteDatabase.openDatabase(builder.toString(), null, 
				SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		
		//this.update();
	}
	
	public void close() {
	
		if (this.mDb != null)
			this.mDb.close();
	}
	
	public void update() {
		
		List<String> columns = new ArrayList<String>();
		
		Cursor cursor = this.mDb.rawQuery(this.getString(R.string.db_dots_columns), null);
		
		if (cursor.moveToFirst()) {	
			do {
				columns.add(cursor.getString(1));
            }
            while (cursor.moveToNext());
		}
		
		cursor.close();

		final String[] columnsDefined = this.mContext.getResources().getStringArray(R.array.db_dots_columns);
		
		if (!columns.contains(columnsDefined[8]))
			this.mDb.execSQL(this.getString(R.string.db_dots_add_reminder));

		if (!columns.contains(columnsDefined[9]))
			this.mDb.execSQL(this.getString(R.string.db_dots_add_event_id));

		if (!columns.contains(columnsDefined[10]))
			this.mDb.execSQL(this.getString(R.string.db_dots_add_reminder_id));
	}
	
	public void add(Dot dot) {
	
        final String query = this.getString(R.string.db_insert_dot);

        SQLiteStatement stmt = this.mDb.compileStatement(query);
        stmt.bindString(1, dot.text);
        stmt.bindLong(2, dot.color);
        stmt.bindLong(3, dot.size);
        stmt.bindLong(4, dot.position.x);
        stmt.bindLong(5, dot.position.y);
        stmt.bindLong(6, dot.date);
        stmt.bindLong(7, dot.reminder);
        stmt.bindLong(8, dot.eventId);
        stmt.bindLong(9, dot.reminderId);
        stmt.bindLong(10, dot.isSticked ? 1 : 0);

        dot.id = (int)stmt.executeInsert();
        stmt.close();
	}

	public void update(Dot dot) {
	
		final String[] columns = this.mContext.getResources().getStringArray(R.array.db_dots_columns);

		final ContentValues values = new ContentValues(8);
		values.put(columns[1], dot.text);
		values.put(columns[2], dot.color);
		values.put(columns[3], dot.size);
		values.put(columns[4], dot.position.x);
		values.put(columns[5], dot.position.y);
		values.put(columns[8], dot.reminder);
		values.put(columns[9], dot.eventId);
		values.put(columns[10], dot.reminderId);
		values.put(columns[11], dot.isSticked ? 1 : 0);
		
        this.mDb.update(
				this.getString(R.string.db_table_dots),
				values,
				this.getString(R.string.db_where_id),
				new String[]{String.valueOf(dot.id)});
	}

	public void resetDate(Dot dot) {

		final String[] columns = this.mContext.getResources().getStringArray(R.array.db_dots_columns);

		final ContentValues values = new ContentValues(1);
		values.put(columns[6], dot.date);
		
        this.mDb.update(
				this.getString(R.string.db_table_dots),
				values,
				this.getString(R.string.db_where_id),
				new String[]{String.valueOf(dot.id)});
	}
	
	public void setIsArchival(Dot dot, boolean isArchival) {

		final ContentValues values = new ContentValues(1);
		values.put(this.getString(R.string.db_dots_isarchival), isArchival ? 1 : 0);
		
        this.mDb.update(
				this.getString(R.string.db_table_dots),
				values,
				this.getString(R.string.db_where_id),
				new String[]{String.valueOf(dot.id)});
	}

	public void delete(int id) {

		this.mDb.delete(
				this.getString(R.string.db_table_dots),
				this.getString(R.string.db_where_id),
				new String[]{String.valueOf(id)});
	}
	
	public int count(Contents contents) {

		int queryId = -1;
		
		switch (contents) {
		
			case ACTIVE: queryId = R.string.db_dots_count_active; break;
			case ARCHIVAL: queryId = R.string.db_dots_count_archival; break;
			case ALL: queryId = R.string.db_dots_count_all; break;
		}
		
        final String query = this.getString(queryId);
        
        Cursor cursor = this.mDb.rawQuery(query, null);
        int count = -1;
        
        if (cursor.moveToFirst())
        	count = cursor.getInt(0);
        
        cursor.close();
        
        return count;
	}
	
	public void reset() {
		this.mDb.delete(this.getString(R.string.db_table_dots), null, null);
	}

	public List<StatisticsItem> colorStatistics() {
        return this.itemStatistics(R.string.db_dots_group_color, true);
	}

	public List<StatisticsItem> sizeStatistics() {
        return this.itemStatistics(R.string.db_dots_group_size, false);
	}
	
	public float dayStatistics() {
		return this.singleStatistics(R.string.db_dots_group_day);
	}
	
	public float weekStatistics() {
		return this.singleStatistics(R.string.db_dots_group_week);
	}
	
	public float monthStatistics() {
		return this.singleStatistics(R.string.db_dots_group_month);
	}

	public List<Dot> dots(Contents content) {
		return this.dots(content, -1, -1);
	}

	public List<Dot> dots(Contents content, int from, int count) {
		return this.dots(content, from, count, false);
	}

	public List<Dot> dots(Contents content, int from, int count, boolean forceDescending) {
	
		boolean isAll = content == Contents.ALL;
		boolean isArchive = content == Contents.ARCHIVAL;
		
		String where = isAll ? null : this.mContext.getString(R.string.db_where_archival);
		String order = this.mContext.getString(isArchive ? R.string.db_dots_orderby_desc : R.string.db_dots_orderby_asc);
		String[] selectionArgs = null;
		String limit = null;

		if (forceDescending)
			order = this.mContext.getString(R.string.db_dots_orderby_desc);

		if (from != -1 && count != -1)
			limit = this.mContext.getString(R.string.format_limit, from, count);
		
		if (where != null) 
			selectionArgs = new String[] { String.valueOf(isArchive ? 1 : 0) };
		
    	final Cursor cursor = this.mDb.query(this.mContext.getString(R.string.db_table_dots), 
    			null, where, selectionArgs, null, null, order, limit);
    	
        final List<Dot> items = new ArrayList<Dot>();

        if (cursor != null && cursor.moveToFirst()) {

        	int index = 0;
        	
        	if (from != -1)
        		index = from;
        	
            do {
            	
            	Dot dot = this.getFromCursor(cursor);
            	dot.index = ++index;
            	
        		items.add(dot);
            }
            while (cursor.moveToNext());
        }

        cursor.close();

        return items;
	}
	
	private List<StatisticsItem> itemStatistics(int query, boolean isPercent) {

    	final Cursor cursor = this.mDb.rawQuery(this.getString(query), null);
        final List<StatisticsItem> items = new ArrayList<StatisticsItem>();
        final int count = this.count(Contents.ALL);
        
        if (cursor != null && cursor.moveToFirst()) {

            do {
            	
            	if (isPercent) {
            	
            		float percent = (float)cursor.getInt(0) * 100f / (float)count;
            		items.add(new StatisticsItem(cursor.getInt(1), (int)Math.ceil(percent)));
            	}
            	else {
            		items.add(new StatisticsItem(cursor.getInt(1), cursor.getInt(0)));
            	}
            }
            while (cursor.moveToNext());
        }

        cursor.close();

        return items;
	}
	
	
	private float singleStatistics(int query) {

    	final Cursor cursor = this.mDb.rawQuery(this.getString(query), null);
        final int count = cursor.getCount();
        float sum = 0f;
        
        if (cursor != null && cursor.moveToFirst()) {

            do {
            	sum += cursor.getInt(0);
            }
            while (cursor.moveToNext());
        }

        cursor.close();

        return sum / (float)count;
	}

    private Dot getFromCursor(Cursor cursor) {

        final Dot item = new Dot();

        item.id = cursor.getInt(0);
        item.text = cursor.getString(1);
        item.color = cursor.getInt(2);
        item.size = cursor.getInt(3);
        item.position = new Point(cursor.getInt(4), cursor.getInt(5));
        item.date = cursor.getLong(6);
        item.isArchival = cursor.getInt(7) == 1;
		item.reminder = cursor.getLong(8);
		item.eventId = cursor.getLong(9);
		item.reminderId = cursor.getLong(10);
		item.isSticked = cursor.getInt(11) == 1;

        return item;
    }
    
    private String getString(int resourceId) {
        return this.mContext.getString(resourceId);
    }
}