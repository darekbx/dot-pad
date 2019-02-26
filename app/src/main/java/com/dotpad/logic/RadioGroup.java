package com.dotpad.logic;

import com.dotpad.logic.listeners.RadioGroupListener;
import com.dotpad.logic.listeners.RadioItemListener;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class RadioGroup 
	extends LinearLayout 
	implements RadioItemListener {

	private RadioGroupListener mListener;
	private Object mValue = null;
	
	public RadioGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setOnCheckedColorChanged(RadioGroupListener listener) {
		this.mListener = listener;
	}

	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {

		RadioItemBase radioColorView = (RadioItemBase)child;
		radioColorView.setOnCheckedChanged(this);
		
		if (radioColorView.isChecked())
			this.mValue = radioColorView.getValue();
		
		super.addView(child, index, params);
	}

	public Object getCheckedValue() {
		return this.mValue;
	}
	
	public void setCheckedValue(Object value) {
	
		this.mValue = value;

		final int childCount = this.getChildCount();
		
		for (int i = 0; i < childCount; i++) {
			
			RadioItemBase view = (RadioItemBase)this.getChildAt(i);
			view.setChecked(view.getValue().equals(value));
		}
	}
	
	@Override
	public void onCheckedChanged(Object value, boolean checked) {

		if (checked) {
		
			this.mValue = value;
			this.uncheckAll(value);
			
			if (this.mListener != null)
				this.mListener.onCheckedColorChanged(value);
		}
	}
	
	private void uncheckAll(Object withoutValue) {
	
		final int childCount = this.getChildCount();
		
		for (int i = 0; i < childCount; i++) {
			
			RadioItemBase view = (RadioItemBase)this.getChildAt(i);
			
			if (view.getValue().equals(withoutValue))
				continue;
			
			view.setChecked(false);
		}
	}
}