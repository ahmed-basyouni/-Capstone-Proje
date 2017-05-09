package com.ark.android.onlinesourcelib.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ark.android.onlinesourcelib.downloader.FiveHundredPxDownloader;

/**
 * Created by ahmed-basyouni on 4/30/17.
 */
public class FivePxManager {
    private static FivePxManager ourInstance = new FivePxManager();

    public static FivePxManager getInstance() {
        return ourInstance;
    }

    private FivePxManager() {
    }

    public int getOffset(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt("FivePxOffset" , 0);
    }

    public void setOffset(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int currentOffset = sharedPreferences.getInt("FivePxOffset", 0);
        sharedPreferences.edit().putInt("FivePxOffset", currentOffset+ FiveHundredPxDownloader.DOWNLOAD_LIMIT).apply();
    }

    public void restOffset(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putInt("FivePxOffset", 0).apply();
    }
}
