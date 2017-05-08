package com.ark.android.arkwallpaper.utils;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.ark.android.arkwallpaper.WallpaperApp;
import com.ark.android.arkwallpaper.WallpaperObserverService;
import com.ark.android.gallerylib.data.GallaryDataBaseContract;

import java.util.Random;

import static com.ark.android.arkwallpaper.Constants.*;

/**
 *
 * Created by ahmed-basyouni on 4/30/17.
 */

public class WallPaperUtils {

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

    private static void setCurrentWallpaper(String currentWallpaper) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        sharedPreferences.edit().putString(CURRENT_WALLPAPER_KEY, currentWallpaper).apply();
    }

    public static void setCurrentAlbum(String currentAlbumName) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        sharedPreferences.edit().putString(CURRENT_ALBUM_KEY, currentAlbumName).apply();
    }

    private static SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(WallpaperApp.getWallpaperApp());
    }


    public static String getImages() {

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

        if (cursor != null && cursor.getCount() > 0) {
            int index;
            if(isRandomOrder()) {
                setCurrentPicIndex(0);
                index = new Random().nextInt(cursor.getCount());
            }else{
                index = getCurrentPicIndex() < cursor.getCount() ? getCurrentPicIndex() : 0;
                setCurrentPicIndex(index + 1);
            }
            cursor.moveToPosition(index);
            imageUri = cursor.getString(cursor.getColumnIndex(GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI));
            setCurrentWallpaper(imageUri);
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
            setCurrentWallpaper(forceUri);
            setCurrentWallpaperId(cursor.getInt(cursor.getColumnIndex(GallaryDataBaseContract.GalleryTable._ID)));
            setCurrentAlbum(cursor.getString(cursor.getColumnIndex(GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME)));
            cursor.close();
        }
    }

    private static void setCurrentPicIndex(int currentPicIndex) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        sharedPreferences.edit().putInt(CURRENT_PIC_INDEX_KEY, currentPicIndex).apply();
    }

    private static int getCurrentPicIndex() {
        return getSharedPreferences()
                .getInt(CURRENT_PIC_INDEX_KEY, 0);
    }

    public static boolean isLiveWallpaperActive() {
        return getSharedPreferences()
                .getBoolean(WALLPAPER_IS_RUNNING, false);
    }

    public static void changeWallpaperBroadCast(String forceUri){
        Intent intent = new Intent(CHANGE_CURRENT_WALLPAPER_ACTION);
        if(forceUri != null){
            intent.putExtra(FORCE_UPDATE, true);
            intent.putExtra(FORCE_UPDATE_URI, forceUri);
        }

        WallpaperApp.getWallpaperApp().sendBroadcast(intent);
    }

    public static void changeAlbumBroadCast(String albumName){
        Intent intent = new Intent(CHANGE_CURRENT_ALBUM_ACTION);
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

    public static void setLiveWallpaperIsRunning(boolean running) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        sharedPreferences.edit().putBoolean(WALLPAPER_IS_RUNNING, running).apply();
    }

    public static void setChangeWithDoubleTap(boolean isChecked) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        sharedPreferences.edit().putBoolean(CHANGE_WITH_DOUBLE_TAP_KEY, isChecked).apply();
    }

    public static boolean isChangedWithDoubleTap(){
        return getSharedPreferences().getBoolean(CHANGE_WITH_DOUBLE_TAP_KEY, false);
    }

    public static void setScrolling(boolean scroll) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        sharedPreferences.edit().putBoolean(CHANGE_SCROLLING_KEY, scroll).apply();
    }

    public static boolean isScrolling(){
        return getSharedPreferences().getBoolean(CHANGE_SCROLLING_KEY, false);
    }

    public static void setDisplayMode(DISPLAY_MODE displayMode) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        sharedPreferences.edit().putInt(CHANGE_DISPLAY_MODE_KEY, displayMode.ordinal()).apply();
    }

    public static int getDisplayMode(){
        return getSharedPreferences().getInt(CHANGE_DISPLAY_MODE_KEY, DISPLAY_MODE.FIT.ordinal());
    }

    public static void setRandomOrder(boolean isRandom) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        sharedPreferences.edit().putBoolean(RANDOM_ORDER_KEY, isRandom).apply();
    }

    public static boolean isRandomOrder() {
        return getSharedPreferences()
                .getBoolean(RANDOM_ORDER_KEY, false);
    }

    public static void setChangeWithUnlock(boolean isChecked) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        sharedPreferences.edit().putBoolean(CHANGE_WITH_UNLOCK_KEY, isChecked).apply();
    }

    public static boolean isChangeWithUnlock(){
        return getSharedPreferences().getBoolean(CHANGE_WITH_UNLOCK_KEY, false);
    }

    private static void setChangeWithInterval(boolean isChecked) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        sharedPreferences.edit().putBoolean(CHANGE_WITH_INTERVAL_KEY, isChecked).apply();
        if(!isChecked)
            cancelAlarm();
    }

    private static void cancelAlarm() {
        AlarmManager manager = (AlarmManager) WallpaperApp.getWallpaperApp().getSystemService(Context.ALARM_SERVICE);
        manager.cancel(getAlarmPendingIntent());
    }

    public static boolean isChangeWithInterval(){
        return getSharedPreferences().getBoolean(CHANGE_WITH_INTERVAL_KEY, false);
    }

    public static void setChangeInterval(int interval, INTERVAL_MODE mode){
        SharedPreferences sharedPreferences = getSharedPreferences();
        sharedPreferences.edit().putLong(CHANGE_WITH_INTERVAL_KEY, interval * mode.getValue()).apply();
        setChangeWithInterval(true);
        setChangeWallpaperUnit(mode);
        setChangeWallpaperInterval(interval);
        updateChangeAlarm(interval * mode.getValue());
    }

    public static int getChangeWallpaperUnit(){
        return getSharedPreferences().getInt(CHANGE_Unit_KEY, 0);
    }

    private static void setChangeWallpaperUnit(INTERVAL_MODE mode) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        sharedPreferences.edit().putInt(CHANGE_Unit_KEY, mode.ordinal()).apply();
    }

    public static int getChangeWallpaperInterval(){
        return getSharedPreferences().getInt(CHANGE_INTERVAL_KEY, 30);
    }

    private static void setChangeWallpaperInterval(int interval) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        sharedPreferences.edit().putInt(CHANGE_INTERVAL_KEY, interval).apply();
    }

    public static float getCurrentGreyScale(){
        return getSharedPreferences().getFloat(GREY_SCALE_KEY, 0.0f);
    }

    public static void setCurrentGreyScale(float greyScale) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        sharedPreferences.edit().putFloat(GREY_SCALE_KEY, greyScale).apply();
    }

    public static int getCurrentBlurring(){
        return getSharedPreferences().getInt(BLURRING_KEY, 0);
    }

    public static void setCurrentBlurring(int blurring) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        sharedPreferences.edit().putInt(BLURRING_KEY, blurring).apply();
    }

    public static int getCurrentDim(){
        return getSharedPreferences().getInt(DIM_KEY, 0);
    }

    public static void setCurrentDim(int dim) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        sharedPreferences.edit().putInt(DIM_KEY, dim).apply();
    }

    public static void updateChangeAlarm(long interval) {
        AlarmManager manager = (AlarmManager) WallpaperApp.getWallpaperApp().getSystemService(Context.ALARM_SERVICE);
        cancelAlarm();
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval,
                interval , getAlarmPendingIntent());
    }

    private static PendingIntent getAlarmPendingIntent() {
        Intent alarmIntent = new Intent(CHANGE_CURRENT_WALLPAPER_ACTION);
        return PendingIntent.getBroadcast(WallpaperApp.getWallpaperApp(), ALARM_ID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    public static void checkDeletedAlbum(String albumName) {
        if(getCurrentAlbum().equals(albumName))
            setCurrentAlbum(null);
    }
}
