package com.ark.android.arkwallpaper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ark.android.arkwallpaper.utils.WallPaperUtils;

/**
 * Created by ahmed-basyouni on 5/3/17.
 */

public class DeviceBootReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            /* Setting the alarm here */
            long modeValue = Constants.INTERVAL_MODE.values()[WallPaperUtils.getChangeWallpaperUnit()].getValue();
            int interval = WallPaperUtils.getChangeWallpaperInterval();
            WallPaperUtils.updateChangeAlarm(interval * modeValue);


        }
    }
}
