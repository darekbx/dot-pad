package com.dotpad.model;

import java.util.Calendar;

import android.graphics.Point;
import android.util.Pair;

public class Dot {

	public int id;
	public String text;
	public int color;
	public int size;
	public Point position;
	public long date;
	public boolean isArchival;
	public boolean isSticked;
	public long reminder = 0;
	public long eventId;
	public long reminderId;
	
	public int index;
	private String mSelection;
	private int mSelectionStart = -1;

	public void setSelection(String needle) {
		mSelectionStart = text.toLowerCase().indexOf(needle);
		mSelection = text.substring(mSelectionStart, mSelectionStart + needle.length());
	}

	public Pair<String, Integer> getSelection() {
		return new Pair<>(mSelection, mSelectionStart);
	}

	public void clearSelection() {
		mSelection = null;
		mSelectionStart = -1;
	}

	public boolean hasSelection() {
		return mSelectionStart != -1;
	}

	public static Dot newDot(String text, int color, int size, long reminder, boolean isSticked, Point position) {

		Dot dot = new Dot();
		dot.text = text;
		dot.color = color;
		dot.size = size;
		dot.position = position;
		dot.reminder = reminder;
		dot.date = Calendar.getInstance().getTimeInMillis();
		dot.isSticked = isSticked;
		
		return dot;
	}
	
	public Calendar getReminder() {
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(this.reminder);
		
		return calendar;
	}
	
	public long[] getTimeSpan(long diffInSeconds) {
		
		long diff[] = new long[] { 0, 0, 0 , 0 };
		
		if (diffInSeconds == 0)
			diffInSeconds = 1;
		
	    diff[3] = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
	    diff[2] = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
	    diff[1] = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
	    diff[0] = (diffInSeconds = (diffInSeconds / 24));
	    
	    if (diff[0] > 0)
	    	return new long[] { diff[0], 0 };
	    
	    if (diff[1] > 0)
	    	return new long[] { diff[1], 1 };
	    
	    if (diff[2] > 0)
	    	return new long[] { diff[2], 2 };
	    
	    if (diff[3] > 0)
	    	return new long[] { diff[3], 3 };
	    
	    return null;
	}
}