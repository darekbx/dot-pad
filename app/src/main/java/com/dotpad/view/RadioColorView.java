package com.dotpad.view;

import com.dotpad.R;
import com.dotpad.R.color;
import com.dotpad.logic.RadioItemBase;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;

public class RadioColorView extends RadioItemBase {

	private Paint mPaint;
	private Paint mBorderPaint;
	
	public RadioColorView(Context context, AttributeSet attrs) {
		
		super(context, attrs);
		
		TypedArray attrArray = context.obtainStyledAttributes(attrs, 
				R.styleable.RadioItemView);
		
		this.mPaint = new Paint();
		this.mPaint.setAntiAlias(true);
		this.mPaint.setColor(attrArray.getColor(0, color.red));

		this.mBorderPaint = new Paint();
		this.mBorderPaint.setAntiAlias(true);
		this.mBorderPaint.setColor(Color.BLACK);
		this.mBorderPaint.setStyle(Style.STROKE);
		this.mBorderPaint.setStrokeWidth(3);
		
		this.setChecked(attrArray.getBoolean(1, false));
		
		attrArray.recycle();
	}

	@Override
	public Object getValue() {
		return this.mPaint.getColor();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		
		final int x = this.getWidth() / 2;
		final int y = this.getHeight() / 2;
		final int r = x - 8;
		
		canvas.drawCircle(x, y, r, this.mPaint);
		
		if (this.isChecked())
			canvas.drawCircle(x, y, r, this.mBorderPaint);
	}
}