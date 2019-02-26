package com.dotpad;

import com.dotpad.db.DotManager;
import com.dotpad.db.DotManager.Contents;
import com.dotpad.view.StatisticsView;

import android.app.Activity;
import android.os.Bundle;

public class StatisticsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		final DotManager manager = new DotManager(this);
		final StatisticsView view = new StatisticsView(this);
		view.setData(
				manager.colorStatistics(), 
				manager.sizeStatistics(),
				manager.dayStatistics(),
				manager.weekStatistics(),
				manager.monthStatistics(),
				manager.count(Contents.ALL));
		
		this.setContentView(view);

		manager.close();
	}
}