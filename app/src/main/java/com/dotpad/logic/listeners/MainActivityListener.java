package com.dotpad.logic.listeners;

import android.graphics.Point;

import com.dotpad.model.Dot;

public interface MainActivityListener {

	public void onAdd(Point position);
	public void onTap(Dot dot);
	public void onDelete(Dot dot);
	public void onUpdate(Dot dot);
}