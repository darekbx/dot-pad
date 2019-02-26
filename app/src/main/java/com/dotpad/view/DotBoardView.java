package com.dotpad.view;

import java.util.Timer;
import java.util.TimerTask;

import com.dotpad.R;
import com.dotpad.logic.adapters.DotAdapter;
import com.dotpad.logic.listeners.CacheListener;
import com.dotpad.logic.listeners.DotViewListener;
import com.dotpad.logic.listeners.MainActivityListener;
import com.dotpad.model.Dot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;

public class DotBoardView 
	extends AdapterView<DotAdapter> 
	implements OnClickListener, OnLongClickListener, DotViewListener {

	private MainActivityListener mListener;
	private DotAdapter mAdapter;
	private Paint mPaint;
	private Paint mRingPaint;
	private Paint mMessagePaint;
	private Point mScreenSize;
	private Paint mDragPaint;
	private Paint mDragViewPaint;
	private Point mDragPosition;
	private DotView mDraggedView;
	
	private boolean mHideRings = false;
	private boolean mLongClicked = false;
	private boolean mCanDrag = false;
	
	public DotBoardView(Context context, AttributeSet attrs) {
		
		super(context, attrs);

		this.setDrawingCacheEnabled(true);
		this.setWillNotDraw(false);
		
		this.mPaint = new Paint();
		this.mPaint.setAntiAlias(true);
		this.mPaint.setColor(Color.WHITE);
		this.mPaint.setStrokeWidth(15);
		this.mPaint.setStyle(Style.STROKE);
		this.mPaint.setPathEffect(new DashPathEffect(new float[] { 40, 20 }, 10));
		
		this.mRingPaint = new Paint();
		this.mRingPaint.setAntiAlias(true);
		this.mRingPaint.setColor(Color.rgb(220, 220, 220));
		this.mRingPaint.setStrokeWidth(1);
		this.mRingPaint.setStyle(Style.STROKE);
		//this.mRingPaint.setPathEffect(new DashPathEffect(new float[] { 10, 20 }, 5)); // overdraw!

		this.mMessagePaint = new Paint();
		this.mMessagePaint.setAntiAlias(true);
		this.mMessagePaint.setColor(Color.rgb(200, 200, 200));
		this.mMessagePaint.setTextSize(18);
		
		this.mDragPaint = new Paint();
		this.mDragPaint.setAntiAlias(true);
		this.mDragPaint.setColor(Color.BLACK);
		this.mDragPaint.setStrokeWidth(5);
		this.mDragPaint.setStyle(Style.STROKE);
		this.mDragPaint.setPathEffect(new DashPathEffect(new float[] { 10, 10 }, 10));

		this.mDragViewPaint = new Paint();
		this.mDragViewPaint.setAntiAlias(true);
		
		this.mScreenSize = new Point();
		((WindowManager)this.getContext().getSystemService(Context.WINDOW_SERVICE))
			.getDefaultDisplay().getSize(this.mScreenSize);
	}

	public void setListener(MainActivityListener listener) {
		this.mListener = listener;
	}
	
	public void refresh() {
	
		this.removeAllViewsInLayout();
		this.requestLayout();
	}
	
	public void deleteNotify() {

		this.mPaint.setColor(this.getResources().getColor(R.color.red));
		this.invalidate();

		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {

				DotBoardView.this.post(new Runnable() {

					@Override
					public void run() {

						DotBoardView.this.mPaint.setColor(Color.WHITE);
						DotBoardView.this.invalidate();
					}
				});
			}
		}, 200);
	}
	
	public boolean canDrag() {
		return this.mCanDrag;
	}
	
	public void setCanDrag(boolean canDrag) {

		this.mCanDrag = canDrag;
	
		this.mPaint.setColor(canDrag ? this.getResources().getColor(R.color.green) : Color.WHITE);
		this.invalidate();
	}
	
	public void getWidgetImage(final CacheListener listener) {

		this.setViewShadowEnabled(true);
		this.mHideRings = true;

		this.destroyDrawingCache();
		this.buildDrawingCache();
		
		final DotBoardView _this = this;
		
		this.post(new Runnable() {

			@Override
			public void run() {

				final Bitmap cache = _this.getDrawingCache();
				final double ratio = (double) cache.getHeight() / (double) cache.getWidth();
				final int newWidth = 128;

				listener.onGetBitmap(Bitmap.createScaledBitmap(cache, newWidth, (int) (ratio * newWidth), true));

				_this.mHideRings = false;
				_this.setViewShadowEnabled(false);
				_this.refresh();
			}
		});
	}
	
	private void setViewShadowEnabled(boolean enabled) {

		final int childCount = this.mAdapter.getCount();
		
		for (int i = 0; i < childCount; i++) { 
		
			DotView view = (DotView)this.mAdapter.getView(i, null, this);
			view.setShadowEnabled(enabled);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {

		if (this.mPaint.getColor() != Color.WHITE)
			canvas.drawRect(0, 0, this.getWidth(), this.getHeight(), this.mPaint);

		if (!this.mHideRings) {

			// today / tommorow
			canvas.drawCircle(0, 0, 400, this.mRingPaint);
			
			// next days
			canvas.drawCircle(0, 0, 700, this.mRingPaint);

			// in the feauture
			canvas.drawCircle(0, 0, 1000, this.mRingPaint);
		}
		
		if (this.mDragPosition != null) {

			final int r = 100;
			canvas.drawCircle(
					this.mDragPosition.x, this.mDragPosition.y, 
					r, this.mDragPaint);
			
			canvas.drawCircle(
					this.mDragPosition.x, this.mDragPosition.y, 
					this.mDraggedView.getMeasuredWidth() / 2, this.mDragViewPaint);
		}
		
		if (this.mAdapter != null) {
		
			String message = String.valueOf(countDots());
			float width = this.mMessagePaint.measureText(message, 0, message.length());
			
			canvas.drawText(message, this.getWidth() - width - 5, this.getHeight() - 10, this.mMessagePaint);
		}
		
		super.onDraw(canvas);
	}

	private int countDots() {
		int count = 0;
		for (Dot dot : mAdapter.getItems()) {
			if (dot.isSticked) {
				continue;
			}
			count++;
		}
		return count;
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		Point position = new Point((int)event.getX(), (int)event.getY());
		
		switch (event.getAction()) {
		
			case MotionEvent.ACTION_UP:
				
				if (this.mLongClicked) {
					
					this.mLongClicked = false;
					return true;
				}
				
				if (!this.mCanDrag)
					DotBoardView.this.mListener.onAdd(position);
				
				return false;
				
			case MotionEvent.ACTION_MOVE:
				
				int color = this.getColor(event);

				if (color != -1) {

					View dotView = this.dotViewFromPosition(color, position.x, position.y);

					if (dotView != null) {

						dotView.layout(
								position.x,
								position.y,
								position.x + dotView.getMeasuredWidth(),
								position.y + dotView.getMeasuredHeight());
					}
				}
				
				return false;
		}
		
		return true;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

		super.onLayout(changed, left, top, right, bottom);

		if (changed || this.getChildCount() == 0) {
			
			final int childCount = this.mAdapter.getCount();
	
			for (int i = 0; i < childCount; i++) { 
			
				Dot dot = this.mAdapter.getItem(i);

				DotView view = (DotView)this.mAdapter.getView(i, null, this);
				view.setOnClickListener(this);
				view.setOnLongClickListener(this);
				view.setListener(this);
				
				view.measure(-1, -1);
				view.layout(
						dot.position.x, 
						dot.position.y, 
						dot.position.x + view.getMeasuredWidth(), 
						dot.position.y + view.getMeasuredHeight());
	
				this.addViewInLayout(view, i, null, true);
			}
			
			this.invalidate();
		}
	}

	private DotView dotViewFromPosition(int color, int x, int y) {

		final int childCount = this.getChildCount();

		for (int i = 0; i < childCount; i++) { 

			Dot dot = this.mAdapter.getItem(i);
			
			if (dot.color == color) {
			
				View view = this.getChildAt(i);
				Rect viewRectangle = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());

				if (viewRectangle.contains(x, y))
					return (DotView)view;
			}
		}
		
		return null;
	}

	private int getColor(MotionEvent e) {
		
		this.setDrawingCacheEnabled(false);
		this.setDrawingCacheEnabled(true);

		final Bitmap cache = this.getDrawingCache();

		if (e.getX() < cache.getWidth() && e.getY() < cache.getHeight())
			return cache.getPixel((int) e.getX(), (int) e.getY());
		else
			return -1;
	}
	
	@Override
	public DotAdapter getAdapter() {
		return this.mAdapter;
	}

	@Override
	public void setAdapter(DotAdapter adapter) {

		this.mAdapter = adapter;
		this.refresh();
	}

	@Override
	public View getSelectedView() { return null; }

	@Override
	public void setSelection(int position) { }

	@Override
	public boolean onLongClick(View v) {

		if (this.mCanDrag)
			return false;

		this.mListener.onDelete(((DotView)v).getDot());
		this.mLongClicked = true;

		v.setVisibility(View.GONE);
		
		return true;
	}

	@Override
	public void onClick(View v) {

		if (this.mCanDrag)
			return;
		
		this.mListener.onTap(((DotView)v).getDot());
	}

	@Override
	public void onDrop(DotView view, Point position, boolean hasMoved) {

		if (!this.mCanDrag)
			return;

		if (hasMoved) {
			
			view.getDot().position = this.fixPosition(view, position);
			this.mListener.onUpdate(view.getDot());
		}

		this.mDragPosition = null;
		this.invalidate();
	}

	@Override
	public void onDrag(DotView view, Point position) {

		Point realPosition = this.fixPosition(view, position);
		realPosition.x += view.getMeasuredWidth() / 2;
		realPosition.y += view.getMeasuredHeight() / 2;		
		
		this.mDragViewPaint.setColor(view.getDot().color);
		
		this.mDraggedView = view;
		this.mDragPosition = realPosition;
		this.invalidate();
	}
	
	private Point fixPosition(DotView view, Point position) {

		Point dotPosition = new Point(view.getDot().position);
		dotPosition.x = dotPosition.x + position.x;
		dotPosition.y = dotPosition.y + position.y;
		
		dotPosition.x = Math.min(dotPosition.x, this.mScreenSize.x - view.getMeasuredWidth());
		dotPosition.y = Math.min(dotPosition.y, this.mScreenSize.y - view.getMeasuredHeight() - 50);
		
		dotPosition.x = Math.max(dotPosition.x, 0);
		dotPosition.y = Math.max(dotPosition.y, 0);
		
		return dotPosition;
	}
}