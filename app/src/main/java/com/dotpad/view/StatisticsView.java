package com.dotpad.view;

import java.util.List;

import com.dotpad.R;
import com.dotpad.logic.CircleUtils;
import com.dotpad.model.StatisticsItem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.view.View;

public class StatisticsView extends View {

	private static final int RESOLUTION = 100; 

	private Context mContext;
	private Paint mPiePaint;
	private Paint mLinePaint;
	
	private List<StatisticsItem> mColors;
	private List<StatisticsItem> mSizes;
	private float mDay;
	private float mWeek;
	private float mMonth;
	private int mCount;
	
	public StatisticsView(Context context) {
		
		super(context);

		this.mContext = context;
		
		this.mPiePaint = new Paint();
		this.mPiePaint.setAntiAlias(true);
		this.mPiePaint.setColor(Color.RED);
		this.mPiePaint.setStyle(Style.FILL);
		
		this.mLinePaint = new Paint();
		this.mLinePaint.setAntiAlias(true);
		this.mLinePaint.setColor(Color.BLACK);
		this.mLinePaint.setStyle(Style.STROKE);
		this.mLinePaint.setStrokeWidth(4f);
	}

	public void setData(
			List<StatisticsItem> colors,
			List<StatisticsItem> sizes,
			float day,
			float week,
			float month,
			int count) {
	
		this.mColors = colors;
		this.mSizes = sizes;
		this.mDay = day;
		this.mWeek = week;
		this.mMonth = month;
		this.mCount = count;
		
		this.invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {

		int width = this.getWidth() / 2 - 40;
		
		canvas.drawColor(Color.WHITE);
		canvas.save();
		
		this.drawColorPie(canvas, this.mColors, width);
		this.drawTextValues(canvas);
	}
	
	private void drawColorPie(Canvas canvas, List<StatisticsItem> items, int size) {

		canvas.translate(size + 40, size + 40);
		
		final int centerX = 0;
		final int centerY = 0;

		int start = 0;

		for (StatisticsItem item : items) {

			this.mPiePaint.setColor(item.key);
			this.drawPart(canvas, centerX, centerY, size, 
					start, start + item.percent);
			
			start += item.percent;
		}
		
		canvas.drawCircle(centerX, centerY, size, this.mLinePaint);
		canvas.restore();
	}
	
	private void drawTextValues(Canvas canvas) {

		this.mLinePaint.setStyle(Style.FILL);
		this.mLinePaint.setStrokeWidth(1f);
		this.mLinePaint.setTextSize(24);
		this.mLinePaint.setTypeface(Typeface.MONOSPACE);
		
		canvas.translate(40, 740);

		canvas.drawText(this.mContext.getString(R.string.format_day, this.mDay), 0, 0, this.mLinePaint);
		canvas.drawText(this.mContext.getString(R.string.format_week, this.mWeek), 0, 30, this.mLinePaint);
		canvas.drawText(this.mContext.getString(R.string.format_month, this.mMonth), 0, 60, this.mLinePaint);
		canvas.drawText(this.mContext.getString(R.string.format_count, this.mCount), 0, 90, this.mLinePaint);

		canvas.drawText(this.mContext.getString(R.string.size_title), 0, 130, this.mLinePaint);
		
		int top = 160;
		
		for (StatisticsItem size : this.mSizes) {
			
			canvas.drawText(this.mContext.getString(R.string.format_size, (float)size.key, (float)size.percent),
					0, top, this.mLinePaint);
			
			top += 30;
		}
	}
	
	private void drawPart(Canvas canvas, int centerX, int centerY, 
			int size, int from, int to) {

		final Path path = new Path();
		path.moveTo(centerX, centerY);
		
		int x, y;
		
		for (int i = from; i <= to; i++) {
			
			x = CircleUtils.calculateXSegment(centerX, size, i, RESOLUTION);
			y = CircleUtils.calculateYSegment(centerY, size, i, RESOLUTION);
			
			path.lineTo(x, y);
		}
		
		canvas.drawPath(path,  this.mPiePaint);
	}
}