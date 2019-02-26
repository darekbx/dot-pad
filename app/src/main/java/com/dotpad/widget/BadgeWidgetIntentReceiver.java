package com.dotpad.widget;

import com.dotpad.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class BadgeWidgetIntentReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		if (intent.getAction().equals(BadgeWidgetProvider.ACTION))
			this.updateWidgetPictureAndButtonListener(context);
	}

	private void updateWidgetPictureAndButtonListener(Context context) {
		
		final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		BadgeWidgetProvider.pushWidgetUpdate(context.getApplicationContext(), remoteViews);
	}
}