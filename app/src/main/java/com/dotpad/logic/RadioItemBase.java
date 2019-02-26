package com.dotpad.logic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.dotpad.logic.listeners.RadioItemListener;

public abstract class RadioItemBase extends View {

	private RadioItemListener mListener;
	private boolean mIsChecked;
	
	public RadioItemBase(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setOnCheckedChanged(RadioItemListener listener) {
		this.mListener = listener;
	}

	public void setChecked(boolean checked) {
	
		this.mIsChecked = checked;
		this.invalidate();
	}
	
	public boolean isChecked() {
		return this.mIsChecked;
	}
	
	public abstract Object getValue();
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			
			this.setChecked(true);
			this.mListener.onCheckedChanged(this.getValue(), this.mIsChecked);
			
			return true;
		}
		
		return super.onTouchEvent(event);
	}
}