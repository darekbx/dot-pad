package com.dotpad;

import java.util.ArrayList;
import java.util.Calendar;

import com.dotpad.db.DotManager;
import com.dotpad.db.DotManager.Contents;
import com.dotpad.dialogs.DotDialog;
import com.dotpad.logic.CalendarManager;
import com.dotpad.logic.adapters.DotAdapter;
import com.dotpad.logic.listeners.CacheListener;
import com.dotpad.logic.listeners.DotDialogListener;
import com.dotpad.logic.listeners.MainActivityListener;
import com.dotpad.model.Dot;
import com.dotpad.view.DotBoardView;
import com.dotpad.widget.BadgeWidgetProvider;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.RemoteViews;

public class MainActivity 
	extends Activity 
	implements MainActivityListener {

	private DotManager mManager;
	private DotAdapter mAdapter;
	private DotBoardView mDotBoardView;
	
	private boolean mShowingAll = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// override targetSdkVersion if we want to see options menu at the bottom 
		this.getApplicationInfo().targetSdkVersion = 10;
		
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.activity_main);
		
		this.mManager = new DotManager(this);
		
		this.mAdapter = new DotAdapter(this, R.layout.dot_item, new ArrayList<Dot>());
		this.mAdapter.registerDataSetObserver(this.mObserver);
		
		this.mDotBoardView = (DotBoardView)this.findViewById(R.id.main_dot_view);
		this.mDotBoardView.setAdapter(this.mAdapter);
		this.mDotBoardView.setListener(this);

		this.startService(new Intent(this, PebbleService.class));
    }
	
	@Override
	protected void onDestroy() {

		if (this.mManager != null)
			this.mManager.close();
		
		if (this.mAdapter != null)
			this.mAdapter.unregisterDataSetObserver(this.mObserver);
		
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		
		if (this.mAdapter != null) {

			this.mAdapter.clear();
			this.mAdapter.addAll(this.mManager.dots(Contents.ACTIVE));
			this.mAdapter.notifyDataSetChanged();
		}
		
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		
		super.onPause();

		this.mDotBoardView.setCanDrag(false);
		//this.updateWidget();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		this.getMenuInflater().inflate(R.menu.dots_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		
			case R.id.menu_archive:
				this.startActivity(new Intent(this, ArchiveActivity.class));
				return true;

			case R.id.menu_statistics:
				this.startActivity(new Intent(this, StatisticsActivity.class));
				return true;

			case R.id.menu_all:
				
				this.mShowingAll = !this.mShowingAll;
				item.setTitle(this.mShowingAll
						? R.string.menu_all_off 
						: R.string.menu_all_on);

				this.mAdapter.clear();
				this.mAdapter.addAll(this.mManager.dots(this.mShowingAll ? Contents.ALL : Contents.ACTIVE));
				
				this.mDotBoardView.refresh();
				
				return true;

			case R.id.menu_list:
				Intent intent = new Intent(this, ArchiveActivity.class);
				intent.putExtra(this.getString(R.string.active_key), true);
				this.startActivity(intent);
				break;
				
			case R.id.menu_drag:
				
				item.setTitle(this.mDotBoardView.canDrag() 
						? R.string.menu_drag_on 
						: R.string.menu_drag_off);
				this.mDotBoardView.setCanDrag(!this.mDotBoardView.canDrag());
				
				return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onAdd(final Point position) {
		
		new DotDialog(this, new DotDialogListener() {
			
			@Override
			public void onAdd(String note, int color, int size, boolean isSticked, long reminder) {
				
				Dot dot = Dot.newDot(note, color, size, reminder, isSticked, position);
				MainActivity.this.addDot(dot);
			}

			@Override
			public void onReset(Dot dot) { }

			@Override
			public void onUpdate(Dot dot) {
				MainActivity.this.saveDot(dot);				
			}
		}).show();
	}

	@Override
	public void onTap(final Dot dot) {

		if (dot == null)
			return;

		new DotDialog(this, dot, new DotDialogListener() {
			
			@Override
			public void onAdd(String note, int color, int size, boolean isSticked, long reminder) {
				
				dot.text = note;
				dot.color = color;
				dot.size = size;
				dot.reminder = reminder;
				dot.isSticked = isSticked;
				
				MainActivity.this.saveDot(dot);
			}

			@Override
			public void onReset(Dot dot) {
				MainActivity.this.resetDot(dot);
			}

			@Override
			public void onUpdate(Dot dot) {
				MainActivity.this.saveDot(dot);
			}
		}).show();
	}

	@Override
	public void onDelete(final Dot dot) {

		if (dot == null)
			return;

		this.deleteReminder(dot);
		
		this.mManager.setIsArchival(dot, true);
	
		this.mAdapter.remove(dot);
		this.mAdapter.notifyDataSetChanged();

		this.mDotBoardView.deleteNotify();
	}

	@Override
	public void onUpdate(Dot dot) {
		this.saveDot(dot);
	}
	
	private void addDot(Dot dot) {

		this.setReminder(dot);
		
		this.mManager.add(dot);

		this.mAdapter.add(dot);
		this.mAdapter.notifyDataSetChanged();

		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	private void saveDot(Dot dot) {

		this.setReminder(dot);
		
		this.mManager.update(dot);

		this.mAdapter.update(dot);
		this.mAdapter.notifyDataSetChanged();
	}
	
	private void resetDot(Dot dot) {
	
		dot.date = Calendar.getInstance().getTimeInMillis();
		
		this.mManager.resetDate(dot);

		this.mAdapter.update(dot);
		this.mAdapter.notifyDataSetChanged();
	}
	
	private void setReminder(Dot dot) {

		ContentResolver contentResolver = this.getContentResolver();
		CalendarManager calendarManager = new CalendarManager();
		long calendarId = calendarManager.getCalendarId(this, contentResolver);
		
		if (dot.reminder != 0) {

			if (dot.eventId != 0)
				calendarManager.deleteReminder(contentResolver, this, dot.eventId, dot.reminderId);
			
			long[] ids = calendarManager.addReminder(contentResolver, calendarId, dot);
			
			dot.eventId = ids[0];
			dot.reminderId = ids[1];
		}
		else {

			calendarManager.deleteReminder(contentResolver, this, dot.eventId, dot.reminderId);

			dot.eventId = 0;
			dot.reminderId = 0;
		}
	}
	
	private void deleteReminder(Dot dot) {

		ContentResolver contentResolver = this.getContentResolver();
		CalendarManager calendarManager = new CalendarManager();
		calendarManager.deleteReminder(contentResolver, this, dot.eventId, dot.reminderId);
	}
	
	private void updateWidget() {

		MainActivity.this.mDotBoardView.getWidgetImage(new CacheListener() {
			
			@Override
			public void onGetBitmap(Bitmap bitmap) {
				
				final RemoteViews remoteViews = new RemoteViews(
						MainActivity.this.getPackageName(), R.layout.widget_layout);
				remoteViews.setImageViewBitmap(R.id.widget_image_view, bitmap);

				BadgeWidgetProvider.pushWidgetUpdate(
						MainActivity.this.getApplicationContext(), remoteViews);
			}
		});
	}
	
	private DataSetObserver mObserver = new DataSetObserver() {
		
		@Override
		public void onChanged() {
			
			mDotBoardView.refresh();
			//updateWidget();
		}
	};
}