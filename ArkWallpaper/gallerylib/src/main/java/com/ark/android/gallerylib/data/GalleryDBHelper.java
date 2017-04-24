package com.ark.android.gallerylib.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 *
 * Created by ahmed-basyouni on 4/22/17.
 */

public class GalleryDBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "gallery_DataBase.db";

    public GalleryDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createGalleryTable(db);
        createAlbumsTable(db);
    }

    private void createAlbumsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + GallaryDataBaseContract.AlbumsTable.TABLE_NAME + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_NAME + " TEXT NOT NULL,"
                + GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_IMAGE_URI + " TEXT NOT NULL,"
                + GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_ENABLED + " Integer,"
                + GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_COUNT + " Integer,"
                + GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_TYPE + " Integer,"
                + "UNIQUE (" + GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_NAME + ") ON CONFLICT REPLACE)");
    }

    private void createGalleryTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + GallaryDataBaseContract.GalleryTable.TABLE_NAME + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI + " TEXT NOT NULL,"
                + GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME + " TEXT NOT NULL,"
                + GallaryDataBaseContract.GalleryTable.COLUMN_PARENT_URI + " TEXT,"
                + "UNIQUE (" + GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI + ") ON CONFLICT REPLACE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + GallaryDataBaseContract.GalleryTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + GallaryDataBaseContract.AlbumsTable.TABLE_NAME);
        createGalleryTable(db);
        createAlbumsTable(db);
    }
}
