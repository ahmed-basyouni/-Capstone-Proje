package com.ark.android.arkwallpaper;

import android.app.Service;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import com.ark.android.arkwallpaper.utils.WallPaperUtils;
import com.ark.android.gallerylib.data.GallaryDataBaseContract;

/**
 *
 * Created by ahmed-basyouni on 4/30/17.
 */

public class WallpaperObserverService extends Service {

    private ContentObserver mContentObserver;

    @Override
    public void onCreate() {
        super.onCreate();

        mContentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                if(TextUtils.isDigitsOnly(uri.getLastPathSegment())) {
                    if(Integer.parseInt(uri.getLastPathSegment()) == WallPaperUtils.getCurrentWallpaperId() && WallPaperUtils.isLiveWallpaperActive()){
                        //User remove current wallpaper
                        setNewWallpaper();

                    }else if(WallPaperUtils.getCurrentWallpaperId() == -1 && WallPaperUtils.isLiveWallpaperActive()){

                        setNewWallpaper();
                    }
                }
            }
        };
        // Make any changes since the last time the GalleryArtSource was created
        mContentObserver.onChange(false, GallaryDataBaseContract.GalleryTable.CONTENT_URI);
        getContentResolver().registerContentObserver(GallaryDataBaseContract.GalleryTable.CONTENT_URI, true, mContentObserver);

    }

    private void setNewWallpaper() {
        WallPaperUtils.changeWallpaperBroadCast(null);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(mContentObserver);
    }
}
