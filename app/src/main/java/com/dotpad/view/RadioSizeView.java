package com.dotpad.view;

import com.dotpad.R;
import com.dotpad.logic.RadioItemBase;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;

public class RadioSizeView extends RadioItemBase {

	private static final int SIZE_RATIO = 4;
	
	private Paint mPaint;
	private Paint mBorderPaint;
	
	private int mSize;
	
	public RadioSizeView(Context context, AttributeSet attrs) {
		
		super(context, attrs);

		TypedArray attrArray = context.obtainStyledAttributes(attrs, 
				R.styleable.RadioItemView);
		
		this.mPaint = new Paint();
		this.mPaint.setAntiAlias(true);
		this.mPaint.setColor(Color.GRAY);

		this.mBorderPaint = new Paint();
		this.mBorderPaint.setAntiAlias(true);
		this.mBorderPaint.setColor(Color.BLACK);
		this.mBorderPaint.setStyle(Style.STROKE);
		this.mBorderPaint.setStrokeWidth(3);
		
		this.setChecked(attrArray.getBoolean(1, false));
		this.mSize = attrArray.getInt(2, 10);
		
		attrArray.recycle();
	}

	@Override
	public Object getValue() {
		return this.mSize;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		
		final int x = this.getWidth() / 2;
		final int y = this.getHeight() / 2;
		final int r = this.mSize * SIZE_RATIO;
		
		canvas.drawCircle(x, y, r, this.mPaint);
		
		if (this.isChecked())
			canvas.drawCircle(x, y, r, this.mBorderPaint);
	}
}