package com.ark.android.onlinesourcelib;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.ark.android.gallerylib.data.GallaryDataBaseContract;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ahmed-basyouni on 4/25/17.
 */

public class FiveHundredSyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String ALBUM_NAME = "500Px";
    private final ContentResolver mContentResolver;
    private final String CACHE_FOLDER = "500pxFolder";
    private ArrayList<ContentProviderOperation> operations = new ArrayList<>();

    public FiveHundredSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    public FiveHundredSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        List<FiveHundredPxService.Photo> photos = FiveHundredPxDownloader.get500PXPhotos();
        if (photos != null) {
            checkCacheFolder();
            for (FiveHundredPxService.Photo photo : photos) {
                if (!photo.nsfw)
                    downloadImage(photo);
            }

            try {
                getContext().getContentResolver().applyBatch(GallaryDataBaseContract.GALLERY_AUTHORITY, operations);
            } catch (RemoteException | OperationApplicationException e) {
                e.printStackTrace();
            }
        }
    }

    private void downloadImage(FiveHundredPxService.Photo photo) {
        try {
            URL url = new URL(photo.image_url);
            URLConnection conn = url.openConnection();
            Bitmap bitmap = BitmapFactory.decodeStream(conn.getInputStream());
            Uri imageUri = saveImage(bitmap, photo.id);
            if (imageUri != null) {

                ContentValues values = new ContentValues();
                values.put(GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI, imageUri.toString());
                values.put(GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME, ALBUM_NAME);

                operations.add(ContentProviderOperation.newInsert(GallaryDataBaseContract.GalleryTable.CONTENT_URI)
                        .withValues(values).build());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Uri saveImage(Bitmap bitmap, int id) {
        File cacheFolder = getContext().getDir(CACHE_FOLDER, Context.MODE_PRIVATE);
        File imageFile = new File(cacheFolder.getAbsolutePath() + File.separator + id + ".jpg");
        try {
            FileOutputStream out = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return Uri.fromFile(imageFile);
    }

    private void checkCacheFolder() {
        File cacheFolder = getContext().getDir(CACHE_FOLDER, Context.MODE_PRIVATE);
        if (!cacheFolder.exists())
            cacheFolder.mkdirs();
        else {
            deleteCacheContent(cacheFolder);
        }
    }

    private void deleteCacheContent(File fileOrDirectory) {
        for (File child : fileOrDirectory.listFiles())
            child.delete();

    }
}
