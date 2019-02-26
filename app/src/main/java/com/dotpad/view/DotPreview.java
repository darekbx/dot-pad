package com.dotpad.view;

import com.dotpad.model.Dot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.View;

public class DotPreview extends View {

	private Paint mPaint;
	private Dot mDot;
	
	public DotPreview(Context context, AttributeSet attrs) {
		
		super(context, attrs);
		
		this.mPaint = new Paint();
		this.mPaint.setAntiAlias(true);
		this.mPaint.setTextSize(24f);
		this.mPaint.setTextAlign(Align.CENTER);
	}

	public void setDot(Dot dot) {
		this.mDot = dot;
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {

		if (this.mDot != null) {
		
			this.mPaint.setColor(this.mDot.color);
			
			int width = this.getWidth();
			int value = width / 2;
			
			canvas.drawCircle(value, value + 2, value - 2, this.mPaint);

			this.mPaint.setColor(Color.WHITE);
			
			canvas.drawText(String.valueOf(this.mDot.size), value, value + 10, this.mPaint);
		}
		
		super.onDraw(canvas);
	}
}