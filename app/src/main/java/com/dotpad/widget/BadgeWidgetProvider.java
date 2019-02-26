package com.dotpad.widget;

import com.dotpad.MainActivity;
import com.dotpad.R;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class BadgeWidgetProvider extends AppWidgetProvider {

	public static final String ACTION = "com.dotpad.intent.action.UPDATE";
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

		final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		BadgeWidgetProvider.pushWidgetUpdate(context, remoteViews);
	}

	public static void pushWidgetUpdate(Context context, RemoteViews remoteViews) {

		final Intent intent = new Intent(context, MainActivity.class);
		final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		remoteViews.setOnClickPendingIntent(R.id.widget_main_layout, pendingIntent);
		
		final ComponentName widget = new ComponentName(context, BadgeWidgetProvider.class);
		final AppWidgetManager manager = AppWidgetManager.getInstance(context);
	    manager.updateAppWidget(widget, remoteViews);		
	}
}