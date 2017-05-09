package com.ark.android.arkwallpaper.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import com.ark.android.arkwallpaper.Constants;
import com.ark.android.arkwallpaper.WallpaperApp;
import com.ark.android.gallerylib.data.GallaryDataBaseContract;

import java.util.ArrayList;
import java.util.List;

import static com.ark.android.arkwallpaper.Constants.CURRENT_ALBUM_INDEX;

/**
 *
 * Created by ahmed-basyouni on 5/9/17.
 */

public class WidgetUtils {

    private static final String DEFAULT_PREF_NAME = "defaultPrefName";

    private static SharedPreferences getSharedPreferences(String key) {
        return WallpaperApp.getWallpaperApp().getSharedPreferences(key, Context.MODE_PRIVATE);
    }

    public static void setChangeMode(Constants.CHANGE_MODE changeMode, String key){
        getSharedPreferences(key).edit().putInt(Constants.CHANGE_MODE_KEY, changeMode.ordinal()).apply();
    }

    public static int getChangeMode(String key){
        return getSharedPreferences(key).getInt(Constants.CHANGE_MODE_KEY, 0);
    }

    public static void setSelectedAlbum(String albumName, String key){
        getSharedPreferences(key).edit().putString(Constants.SELECTED_ALBUM_KEY, albumName).apply();
    }

    public static String getSelectedAlbum(String key){
        return getSharedPreferences(key).getString(Constants.SELECTED_ALBUM_KEY, null);
    }

    public static void chooseNextAlbum() {

        Cursor cursor = WallpaperApp.getWallpaperApp().getContentResolver()
                .query(GallaryDataBaseContract.AlbumsTable.CONTENT_URI,
                        new String[]{GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_NAME},
                        null, null, null);

        if(cursor != null && cursor.getCount() > 1){
            int index = getCurrentAlbumIndex();
            if(index >= cursor.getCount())
                index = 0;
            cursor.moveToPosition(index);
            setCurrentAlbumIndex(index + 1);
            String albumName = cursor.getString(cursor.getColumnIndex(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_NAME));
            if(!albumName.equals(WallPaperUtils.getCurrentAlbum())){
                WallPaperUtils.changeAlbumBroadCast(albumName);
            }
        }
        if(cursor != null)
            cursor.close();
    }

    private static int getCurrentAlbumIndex() {
        return getSharedPreferences(DEFAULT_PREF_NAME).getInt(CURRENT_ALBUM_INDEX, 0);
    }

    private static void setCurrentAlbumIndex(int index) {
        getSharedPreferences(DEFAULT_PREF_NAME).edit().putInt(CURRENT_ALBUM_INDEX, index).apply();
    }

    public static void clearPref(String prefName) {
        getSharedPreferences(prefName).edit().clear().apply();
    }
}
