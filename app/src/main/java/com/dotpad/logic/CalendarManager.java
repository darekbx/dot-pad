package com.dotpad.logic;

import java.util.TimeZone;

import com.dotpad.R;
import com.dotpad.model.Dot;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;

public class CalendarManager {
	
	public long[] addReminder(ContentResolver contentResolver, long calendarId, Dot dot) {

		final long oneHour = 60 * 60 * 1000;
		
        ContentValues values = new ContentValues();
        values.put(Events.DTSTART, dot.reminder);
        values.put(Events.DTEND, dot.reminder + oneHour);
        values.put(Events.TITLE, dot.text);
        values.put(Events.DESCRIPTION, dot.text);
        values.put(Events.EVENT_COLOR, dot.color);
        values.put(Events.CALENDAR_ID, calendarId);
        values.put(Events.HAS_ALARM, true);
        values.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().getDisplayName());
        
        Uri eventUri = contentResolver.insert(Events.CONTENT_URI, values);
        long createdEventId = Long.parseLong(eventUri.getLastPathSegment());
        
         ContentValues reminders = new ContentValues();
        reminders.put(Reminders.EVENT_ID, createdEventId);
        reminders.put(Reminders.METHOD, Reminders.METHOD_ALERT);
        reminders.put(Reminders.MINUTES, 0);

        Uri reminderUri = contentResolver.insert(Reminders.CONTENT_URI, reminders);
        long createdReminderId = Long.parseLong(reminderUri.getLastPathSegment());
        
        return new long[] { createdEventId, createdReminderId };
	}
	
	public long getCalendarId(Context context, ContentResolver contentResolver) {
	
		long calendarId = -1;
		
		Cursor cursor = contentResolver.query(
				Calendars.CONTENT_URI, 
				new String[] { Calendars._ID }, 
				context.getString(R.string.format_calendar_selection, Calendars.ACCOUNT_NAME), 
				new String[] { context.getString(R.string.google_account) }, 
				null);
		
		if (cursor != null) {
		
			if (cursor.moveToFirst()) 
				calendarId = cursor.getLong(0);
			
			cursor.close();
		}
		
		return calendarId;
	}
	
	public void deleteReminder(ContentResolver contentResolver, 
			Context context, long eventId, long reminderId) {

		contentResolver.delete(ContentUris.withAppendedId(
				Uri.parse(context.getString(R.string.calendar_events)), eventId), null, null);
		contentResolver.delete(ContentUris.withAppendedId(
				Uri.parse(context.getString(R.string.calendar_reminders)), reminderId), null, null);
	}
}