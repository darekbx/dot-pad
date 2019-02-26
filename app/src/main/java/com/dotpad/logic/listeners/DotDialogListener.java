package com.dotpad.logic.listeners;

import com.dotpad.model.Dot;

public interface DotDialogListener {
	void onAdd(String note, int color, int size, boolean isSticked, long reminder);
	void onUpdate(Dot dot);
	void onReset(Dot dot);
}