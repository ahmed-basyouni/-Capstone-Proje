package com.ark.android.arkwallpaper.data.model;

import android.database.Cursor;
import android.net.Uri;

import com.ark.android.gallerylib.data.GallaryDataBaseContract;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by ahmed-basyouni on 4/22/17.
 */

public class AlbumObject implements Serializable {

    private String albumName;
    private Uri albumImage;
    private boolean enabled;
    private int type;
    private int count;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public Uri getAlbumImage() {
        return albumImage;
    }

    public void setAlbumImage(Uri albumImage) {
        this.albumImage = albumImage;
    }

    public static List<AlbumObject> getAlbumsFromCursor(Cursor cursor){
        List<AlbumObject> albums = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            AlbumObject albumObject = new AlbumObject();
            albumObject.setAlbumName(cursor.getString(cursor.getColumnIndex(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_NAME)));
            albumObject.setAlbumImage(Uri.parse(cursor.getString(cursor.getColumnIndex(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_IMAGE_URI))));
            albumObject.setEnabled(cursor.getInt(cursor.getColumnIndex(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_ENABLED)) == 1);
            albumObject.setType(cursor.getInt(cursor.getColumnIndex(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_TYPE)));
            albumObject.setCount(cursor.getInt(cursor.getColumnIndex(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_COUNT)));
            albums.add(albumObject);
            cursor.moveToNext();
        }
        return albums;
    }
}
