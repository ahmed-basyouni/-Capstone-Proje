package com.ark.android.arkwallpaper.data.manager;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.ark.android.arkwallpaper.WallpaperApp;
import com.ark.android.arkwallpaper.data.model.AlbumObject;
import com.ark.android.arkwallpaper.presenter.contract.AlbumFragmentContract;
import com.ark.android.gallerylib.data.GallaryDataBaseContract;

import java.util.List;

/**
 *
 * Created by ahmed-basyouni on 4/22/17.
 */

public class AlbumsManager implements AlbumFragmentContract.IAlbumsModel{

    private AlbumFragmentContract.IAlbumsPresenter iAlbumsPresenter;
    private List<AlbumObject> albums;


    @Override
    public void editAlbumName(String oldName, String newName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_NAME, newName);
        WallpaperApp.getWallpaperApp().getContentResolver().update(GallaryDataBaseContract.AlbumsTable.CONTENT_URI, contentValues
                , GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_NAME + " = ?" , new String[]{oldName});
    }

    @Override
    public void deleteAlbum(String albumName) {
        WallpaperApp.getWallpaperApp().getContentResolver().delete(GallaryDataBaseContract.AlbumsTable.CONTENT_URI
                , GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_NAME + " = ?" , new String[]{albumName});
    }

    @Override
    public void addAlbum(String albumName, int type, String fivePxCategoryName, String tumblrBlogName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_NAME, albumName);
        contentValues.put(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_IMAGE_URI, "");
        contentValues.put(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_ENABLED, 1);
        contentValues.put(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_COUNT, 0);
        contentValues.put(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_TYPE, type);
        contentValues.put(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_TUMBLR_BLOG_NAME, tumblrBlogName);
        contentValues.put(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_Five_PX_CATEGORY, fivePxCategoryName);
        WallpaperApp.getWallpaperApp().getContentResolver().insert(GallaryDataBaseContract.AlbumsTable.CONTENT_URI, contentValues);
    }

    @Override
    public void enableAlbum(boolean enabled, String albumName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_ENABLED, enabled? 1 : 0);
        WallpaperApp.getWallpaperApp().getContentResolver().update(GallaryDataBaseContract.AlbumsTable.CONTENT_URI, contentValues
                , GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_NAME + " = ?" , new String[]{albumName});
    }
}
