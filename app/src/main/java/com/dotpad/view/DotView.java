package com.dotpad.view;

import java.util.Calendar;

import com.dotpad.R;
import com.dotpad.logic.listeners.DotViewListener;
import com.dotpad.model.Dot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DotView extends View {

	private static final int SIZE_RATIO = 16;
	private static final int STICKED_DOT_ALPHA = 160;

	private DotViewListener mListener;
	private Point mFirstPositon;
	private Point mLastPositon;
	private Paint mPaint;
	private Paint mPaintShadow;
	private Paint mPaintText;
	private Paint mReminderPaint;
	private Dot mDot;

	private static boolean sIsShadowEnabled;
	
	public DotView(Context context, AttributeSet attrs) {
		
		super(context, attrs);
		
		this.mPaint = new Paint();
		this.mPaint.setAntiAlias(true);
		
		this.mPaintShadow = new Paint(this.mPaint);
		this.mPaintShadow.setColor(Color.BLACK);
		
		this.mPaintText = new Paint(this.mPaint);
		this.mPaintText.setColor(Color.argb(200, 255, 255, 255));
		this.mPaintText.setTextSize(16);
		this.mPaintText.setTypeface(Typeface.MONOSPACE);
		this.mPaintText.setTextAlign(Align.CENTER);
		
		this.mReminderPaint = new Paint(this.mPaint);
		this.mReminderPaint.setColor(Color.WHITE);
		this.mReminderPaint.setStyle(Style.STROKE);
		this.mReminderPaint.setStrokeWidth(4f);
	}
	
	public void setDot(Dot dot) {
		mDot = dot;
		setDotColor();
	}
	
	public Dot getDot() {
		return mDot;
	}
	
	public void setListener(DotViewListener listener) {
		mListener = listener;
	}

	public void setShadowEnabled(boolean enabled) {
		DotView.sIsShadowEnabled = enabled;
		invalidate();
	}

	private void setDotColor() {
		if (mDot.isSticked) {
			setStickedColor();
		} else {
			mPaint.setColor(mDot.color);
		}
	}

	private void setStickedColor() {
		int red = Color.red(mDot.color);
		int green = Color.green(mDot.color);
		int blue = Color.blue(mDot.color);
		mPaint.setColor(Color.argb(STICKED_DOT_ALPHA, red, green,  blue));
	}
	
	@Override
	protected void onDraw(Canvas canvas) {

		super.onDraw(canvas);
		
		if (this.mDot != null) {

			final long now = Calendar.getInstance().getTimeInMillis();
			final int x = this.getWidth() / 2;
			final int y = this.getHeight() / 2;
			final int r = this.getSize() / 2;

			if (DotView.sIsShadowEnabled)
				canvas.drawCircle(x, y, r, this.mPaintShadow);
			
			canvas.drawCircle(x, y, r - 8, this.mPaint);
			
			final long[] timeSpan = this.mDot.getTimeSpan((now - this.mDot.date) / 1000);
			
			if (timeSpan != null && !DotView.sIsShadowEnabled) {
				
				canvas.drawText(this.getContext().getString(
							R.string.format_time_span, 
							timeSpan[0], 
							this.getSpanUnit((int)timeSpan[1])
						), 
						x, y + 5, this.mPaintText);
			}
			
			if (this.mDot.reminder != 0) {

				final int divisor = 1000 / 60;
				final long minutesRemianingSpan = (this.mDot.reminder - now) / divisor;
				final long minutesSpan = (this.mDot.reminder - this.mDot.date) / divisor;
				final float percent = (minutesSpan - minutesRemianingSpan) * 100 / Math.max(1, minutesSpan);
				
				this.drawPart(canvas, x, y, r - 14, percent);
			}
		}
	}

	private void drawPart(Canvas canvas, int centerX, int centerY, int size, float percent) {

		canvas.save();
		canvas.rotate(-90, centerX, centerY);
		
		int padding = 12;
		
		canvas.drawArc(
				new RectF(padding, padding, this.getWidth() - padding, this.getHeight() - padding), 
				0f, percent * 3.6f, false, this.mReminderPaint);

		canvas.restore();
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (((DotBoardView)this.getParent()).canDrag()) {
			
			Point position = new Point((int)event.getX(), (int)event.getY());
			
			switch (event.getAction()) {
			
				case MotionEvent.ACTION_DOWN:
					this.mFirstPositon = position;
					break;
					
				case MotionEvent.ACTION_UP:
					this.mLastPositon = position;
	
					if (this.mLastPositon != null) {
	
						final int[] positionDelta = this.positionDelta();
						
						this.mListener.onDrop(this, this.mLastPositon, 
								(positionDelta[0] > 10 || positionDelta[1] > 10));
						
						return true;
					}
					
					break;
					
				case MotionEvent.ACTION_MOVE:
					this.mListener.onDrag(this, position);
					break;
			}
		}
		
		return super.onTouchEvent(event);
	}
	
	private int[] positionDelta() {
	 
		return new int[] {
			Math.abs(this.mFirstPositon.x - this.mLastPositon.x),
			Math.abs(this.mFirstPositon.y - this.mLastPositon.y)
		};
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		this.setMeasuredDimension(this.getSize(), this.getSize());
	}
	
	private int getSize() {
		return this.mDot.size * SIZE_RATIO;
	}
	
	private String getSpanUnit(int index) {
		return this.getContext().getResources().getStringArray(R.array.span_units)[index];
	}
}