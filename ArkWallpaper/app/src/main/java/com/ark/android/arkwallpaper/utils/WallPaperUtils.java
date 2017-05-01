package com.ark.android.arkwallpaper.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.ark.android.arkwallpaper.WallpaperApp;
import com.ark.android.arkwallpaper.WallpaperObserverService;
import com.ark.android.arkwallpaper.WallpaperSlideshow;
import com.ark.android.arkwallpaper.data.manager.InternalFileSaveDataLayer;
import com.ark.android.arkwallpaper.data.model.WallPaperObject;
import com.ark.android.gallerylib.data.GallaryDataBaseContract;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by ahmed-basyouni on 4/30/17.
 */

public class WallPaperUtils {

    public static final String CURRENT_ALBUM_KEY = "com.ark.android.arkwallpaper.utils.currentAlbumKey";
    public static final String CURRENT_WALLPAPER_KEY = "com.ark.android.arkwallpaper.utils.currentWallpaperKey";
    public static final String CURRENT_WALLPAPER_ID_KEY =
            "com.ark.android.arkwallpaper.utils.currentWallpaperIdKey";
    public static final String WALLPAPER_ALBUM_ID =
            "com.ark.android.arkwallpaper.utils.currentAlbumIdKey";
    public static final String CHANGE_CURRENT_WALLPAPER_ACTION = "com.ark.android.arkwallpaper.utils.changeCurrentWallpaper";
    public static final String FORCE_UPDATE = "com.ark.android.arkwallpaper.utils.foceUpdate";
    public static final String FORCE_UPDATE_URI = "com.ark.android.arkwallpaper.utils.forceUpdateUri";
    public static final String CHANGE_CURRENT_ALBUM_ACTION = "com.ark.android.arkwallpaper.utils.changeCurrentAlbum";

    public static String getCurrentAlbum() {
        return getSharedPreferences()
                .getString(CURRENT_ALBUM_KEY, null);
    }

    public static String getCurrentWallpaper() {
        return getSharedPreferences()
                .getString(CURRENT_WALLPAPER_KEY, null);
    }

    public static int getCurrentWallpaperId() {
        return getSharedPreferences()
                .getInt(CURRENT_WALLPAPER_ID_KEY, 0);
    }

    private static void setCurrentWallpaperId(int currentWallpaperId) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        sharedPreferences.edit().putInt(CURRENT_WALLPAPER_ID_KEY, currentWallpaperId).apply();
    }

    public static void setCurrentAlbumId(int currentAlbumId) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        sharedPreferences.edit().putInt(WALLPAPER_ALBUM_ID, currentAlbumId).apply();
    }

    public static int getCurrentAlbumId() {
        return getSharedPreferences()
                .getInt(WALLPAPER_ALBUM_ID, -1);
    }

    private static void setCurrentWallpaper(Uri currentWallpaper) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        sharedPreferences.edit().putString(CURRENT_WALLPAPER_KEY, currentWallpaper.toString()).apply();
    }

    public static void setCurrentAlbum(String currentAlbumName) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        sharedPreferences.edit().putString(CURRENT_ALBUM_KEY, currentAlbumName).apply();
    }

    private static SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(WallpaperApp.getWallpaperApp());
    }


    public static String getRandomImage() {

        if(getCurrentAlbum() == null)
            return null;

        Cursor cursor = WallpaperApp.getWallpaperApp().getContentResolver()
                .query(GallaryDataBaseContract.GalleryTable.CONTENT_URI,
                        new String[]{GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI,
                                GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME,
                                GallaryDataBaseContract.GalleryTable._ID},
                        GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME + " = ?",
                        new String[]{getCurrentAlbum()}, null);

        String imageUri = "";

        if (cursor != null) {
            int randomIndex = new Random().nextInt(cursor.getCount());
            cursor.moveToPosition(randomIndex);
            imageUri = cursor.getString(cursor.getColumnIndex(GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI));
            setCurrentWallpaper(Uri.parse(imageUri));
            setCurrentWallpaperId(cursor.getInt(cursor.getColumnIndex(GallaryDataBaseContract.GalleryTable._ID)));
            cursor.close();

        } else {
            setCurrentWallpaper(null);
            setCurrentWallpaperId(-1);
        }

        return imageUri;
    }

    public static void changeCurrentWallpaperId(String forceUri){
        Cursor cursor = WallpaperApp.getWallpaperApp().getContentResolver()
                .query(GallaryDataBaseContract.GalleryTable.CONTENT_URI,
                        new String[]{GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME,
                                GallaryDataBaseContract.GalleryTable._ID},
                        GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI + " = ?",
                        new String[]{forceUri}, null);

        if (cursor != null) {
            cursor.moveToPosition(0);
            setCurrentWallpaper(Uri.parse(forceUri));
            setCurrentWallpaperId(cursor.getInt(cursor.getColumnIndex(GallaryDataBaseContract.GalleryTable._ID)));
            setCurrentAlbum(cursor.getString(cursor.getColumnIndex(GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME)));
            cursor.close();
        }
    }

    public static boolean isLiveWallpaperActive() {
        ActivityManager manager = (ActivityManager) WallpaperApp.getWallpaperApp()
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (WallpaperSlideshow.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void changeWallpaperBroadCast(String forceUri){
        Intent intent = new Intent(WallPaperUtils.CHANGE_CURRENT_WALLPAPER_ACTION);
        if(forceUri != null){
            intent.putExtra(FORCE_UPDATE, true);
            intent.putExtra(FORCE_UPDATE_URI, forceUri);
        }

        WallpaperApp.getWallpaperApp().sendBroadcast(intent);
    }

    public static void changeAlbumBroadCast(String albumName){
        Intent intent = new Intent(WallPaperUtils.CHANGE_CURRENT_ALBUM_ACTION);
        setCurrentAlbum(albumName);
        WallpaperApp.getWallpaperApp().sendBroadcast(intent);
    }

    public static boolean isObservableServiceRunning() {
        ActivityManager manager = (ActivityManager) WallpaperApp.getWallpaperApp()
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (WallpaperObserverService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
