package com.ark.android.arkwallpaper.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;

import com.ark.android.arkwallpaper.R;

/**
 *
 * Created by ahmed-basyouni on 3/19/17.
 */

public class WidgetService extends Service {

    private static final int LOADER_ID = 1;
    private CursorLoader cursorLoader;
    private Context context;


    private void buildWidget() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName thisWidget = new ComponentName(context,
                WallpaperWidget.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {
            // create some random data

            Log.d("Update", "updated Called");

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);

            // Bind the click intent for the next button on the widget
            final Intent nextIntent = new Intent(context,
                    WallpaperWidget.class);
            nextIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            nextIntent.setAction(WallpaperWidget.ACTION_CLICK);
            final PendingIntent nextPendingIntent = PendingIntent
                    .getBroadcast(context, widgetId, nextIntent,
                            0);
            remoteViews.setOnClickPendingIntent(R.id.refresh, nextPendingIntent);

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.context = this;
        buildWidget();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
