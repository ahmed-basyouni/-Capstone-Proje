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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;

import com.ark.android.arkanalytics.GATrackerManager;
import com.ark.android.gallerylib.data.GallaryDataBaseContract;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by ahmed-basyouni on 4/25/17.
 */

public class FiveHundredSyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String ALBUM_NAME = "500Px";
    private final ContentResolver mContentResolver;
    private final String CACHE_FOLDER = "500pxFolder";
    public static final String CAT_KEY = "fivePxCat";
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
        if(!extras.isEmpty()) {
            if (extras.getBoolean("isPer", false))
                FivePxManager.getInstance().restOffset(getContext());
            List<FiveHundredPxService.Photo> photos = FiveHundredPxDownloader.get500PXPhotos(extras.getString(CAT_KEY),
                    FivePxManager.getInstance().getOffset(getContext()));
            operations.clear();
            if (photos != null) {
                checkCacheFolder(photos);
                for (FiveHundredPxService.Photo photo : photos) {
                    if (!photo.nsfw)
                        downloadImage(photo);
                }

                try {
                    if(!operations.isEmpty()) {
                        getContext().getContentResolver().delete(GallaryDataBaseContract.GalleryTable.CONTENT_URI, GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME + " = ?"
                                , new String[]{ALBUM_NAME});
                        getContext().getContentResolver().applyBatch(GallaryDataBaseContract.GALLERY_AUTHORITY, operations);
                    }
                    FivePxManager.getInstance().setOffset(getContext());
                } catch (RemoteException | OperationApplicationException e) {
                    GATrackerManager.getInstance().trackException(e);
                    e.printStackTrace();
                }
            }
        }
    }

    private void downloadImage(FiveHundredPxService.Photo photo) {
        try {
            URL url = new URL(photo.image_url);
            URLConnection conn = url.openConnection();
            String fileName = "";

            int index = photo.image_url.lastIndexOf("/");
            fileName = photo.image_url.substring(index + 1) + ".jpg";

            conn.connect();
            Uri imageUri = saveImage(conn, fileName);
            if (imageUri != null) {
                ContentValues values = new ContentValues();
                values.put(GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI, imageUri.toString());
                values.put(GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME, ALBUM_NAME);

                operations.add(ContentProviderOperation.newInsert(GallaryDataBaseContract.GalleryTable.CONTENT_URI)
                        .withValues(values).build());
            }
        } catch (IOException e) {
            GATrackerManager.getInstance().trackException(e);
            e.printStackTrace();
        }
    }

    private Uri saveImage(URLConnection urlConnection, String fileName) {
        File cacheFolder = getContext().getDir(CACHE_FOLDER, Context.MODE_PRIVATE);
        File imageFile = new File(cacheFolder.getAbsolutePath() + File.separator + fileName);
        try {
            FileOutputStream out = new FileOutputStream(imageFile);

            //this will be used in reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();

            //create a buffer...
            byte[] buffer = new byte[1024];
            int bufferLength = 0; //used to store a temporary size of the buffer

            //now, read through the input buffer and write the contents to the file
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                //add the data in the buffer to the file in the file output stream (the file on the sd card
                out.write(buffer, 0, bufferLength);
            }
            //close the output stream when done
            out.close();
        } catch (Exception e) {
            GATrackerManager.getInstance().trackException(e);
            e.printStackTrace();
            return null;
        }

        return Uri.fromFile(imageFile);
    }

    private void checkCacheFolder(List<FiveHundredPxService.Photo> photos) {
        File cacheFolder = getContext().getDir(CACHE_FOLDER, Context.MODE_PRIVATE);
        if (!cacheFolder.exists())
            cacheFolder.mkdirs();
        else {
            deleteCacheContent(cacheFolder, photos);
        }
    }

    private void deleteCacheContent(File fileOrDirectory, List<FiveHundredPxService.Photo> photos) {
        for (File child : fileOrDirectory.listFiles()) {
            boolean fileAlredyExist = false;
            int position = -1;
            String fileName = "";
            for (int x = 0; x < photos.size(); x++) {
                int index = photos
                        .get(x).image_url.lastIndexOf("/");
                fileName = photos
                        .get(x).image_url.substring(index + 1) + ".jpg";
                if (child.getName().equals(fileName)) {
                    fileAlredyExist = true;
                    position = x;
                    break;
                }
            }
            if(position != -1){
                File cacheFolder = getContext().getDir(CACHE_FOLDER, Context.MODE_PRIVATE);
                File imageFile = new File(cacheFolder.getAbsolutePath() + File.separator + fileName);
                Uri fileUri = Uri.fromFile(imageFile);
                //fix bug where file exist but it doesn't exist in database
                Cursor cursor = getContext().getContentResolver().query(GallaryDataBaseContract.GalleryTable.CONTENT_URI
                        ,new String[]{GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI}, GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI + " = ?"
                        , new String[]{fileUri.toString()}, null);
                if(cursor == null || cursor.getCount() == 0){
                    ContentValues values = new ContentValues();
                    values.put(GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI, fileUri.toString());
                    values.put(GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME, ALBUM_NAME);

                    operations.add(ContentProviderOperation.newInsert(GallaryDataBaseContract.GalleryTable.CONTENT_URI)
                            .withValues(values).build());
                }
                if(cursor != null)
                    cursor.close();
                photos.remove(position);
            }else {
                child.delete();
            }
        }

    }
}
