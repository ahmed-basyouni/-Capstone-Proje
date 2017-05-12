package com.ark.android.arkwallpaper.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.ark.android.arkwallpaper.Constants;
import com.ark.android.arkwallpaper.R;
import com.ark.android.arkwallpaper.WallpaperApp;
import com.ark.android.arkwallpaper.utils.WallPaperUtils;
import com.ark.android.arkwallpaper.utils.WidgetUtils;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;


/**
 *
 * Created by ahmed-basyouni on 3/18/17.
 */

public class WallpaperWidget extends AppWidgetProvider {

    public static final String ACTION_CLICK = "ACTION_CLICK";
    public static final String PREFERENCE_NAME = "com.ark.android.arkwallpaper.widget.pref";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        if (appWidgetIds != null) {
            int N = appWidgetIds.length;

            for (int mAppWidgetId : appWidgetIds) {

                Intent intent = new Intent(context, WidgetService.class);

                intent.putExtra(EXTRA_APPWIDGET_ID, mAppWidgetId);
                context.startService(intent);
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        int widgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, -1);
        String prefName =  "Pref" + widgetId;
        if(action.equals(ACTION_CLICK)){
            Constants.CHANGE_MODE change_mode = Constants.CHANGE_MODE.values()[WidgetUtils.getChangeMode(prefName)];
            dealWithChangeMode(change_mode, prefName);
        }else if(action.equals("android.appwidget.action.APPWIDGET_DELETED")){
            WidgetUtils.clearPref(prefName);
        }
        super.onReceive(context, intent);
    }

    private void dealWithChangeMode(Constants.CHANGE_MODE change_mode, String prefName) {
        switch (change_mode){
            case NEXT_WALLPAPER:
                WallPaperUtils.changeWallpaperBroadCast(null);
                break;
            case NEXT_ALBUM:
                WidgetUtils.chooseNextAlbum();
                break;
            case SELECT_ALBUM:
                String albumName = WidgetUtils.getSelectedAlbum(prefName);
                WallPaperUtils.changeAlbumBroadCast(albumName);
                break;
        }
    }
}
