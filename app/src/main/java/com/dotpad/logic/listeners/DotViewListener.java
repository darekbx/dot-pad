package com.dotpad.logic.listeners;

import android.graphics.Point;

import com.dotpad.view.DotView;

public interface DotViewListener {

	public void onDrop(DotView view, Point position, boolean hasMoved);
	public void onDrag(DotView view, Point position);
}