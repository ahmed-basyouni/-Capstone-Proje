
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
import android.util.Log;

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
 * Created by ahmed-basyouni on 4/25/17.
 */

public class TumblrSyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String ALBUM_NAME = "Tumblr";
    private final ContentResolver mContentResolver;
    private final String CACHE_FOLDER = "TumblerFolder";
    private final String TAG = TumblrSyncAdapter.class.getSimpleName();
    private ArrayList<ContentProviderOperation> operations = new ArrayList<>();

    public TumblrSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    public TumblrSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        if(!extras.isEmpty()){
            if(extras.getBoolean("isPer", false))
                TumblrManager.getInstance().restOffset(getContext());
            List<TumblrService.Post> photos = TumblrDownloader.getTumblrPhotos(extras.getString("albumName"), TumblrManager.getInstance().getOffset(getContext()));
            operations.clear();
            if (photos != null) {
                checkCacheFolder(photos);
                for (TumblrService.Post post : photos) {
//                if (!photo.nsfw)
                    downloadImage(post);
                }

                try {
                    if(!operations.isEmpty()) {
                        getContext().getContentResolver().delete(GallaryDataBaseContract.GalleryTable.CONTENT_URI, GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME + " = ?"
                                , new String[]{ALBUM_NAME});
                        getContext().getContentResolver().applyBatch(GallaryDataBaseContract.GALLERY_AUTHORITY, operations);
                    }
                    TumblrManager.getInstance().setOffset(getContext());
                } catch (RemoteException | OperationApplicationException e) {
                    GATrackerManager.getInstance().trackException(e);
                    e.printStackTrace();
                }
            }
        }
    }

    private void downloadImage(TumblrService.Post photo) {
        try {
            Log.d(TAG, "start downloading " + photo.photos.get(0).original_size.url);
            URL url = new URL(photo.photos.get(0).original_size.url);
            URLConnection conn = url.openConnection();
            String fileName = "";
            String raw = conn.getHeaderField("Content-Disposition");

            if (raw != null && raw.contains("=")) {
                fileName = raw.split("=")[1]; //getting value after '='
            } else {
                int index = photo.photos.get(0).original_size.url.lastIndexOf("/");
                fileName = photo.photos.get(0).original_size.url.substring(index+1);
            }
            conn.connect();
            Uri imageUri = saveImage(conn, fileName);
            if (imageUri != null) {
                Log.d(TAG, "saving");
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
            while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
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

    private void checkCacheFolder(List<TumblrService.Post> photos) {
        File cacheFolder = getContext().getDir(CACHE_FOLDER, Context.MODE_PRIVATE);
        if (!cacheFolder.exists())
            cacheFolder.mkdirs();
        else {
            deleteCacheContent(cacheFolder, photos);
        }
    }

    private void deleteCacheContent(File fileOrDirectory, List<TumblrService.Post> photos) {
        for (File child : fileOrDirectory.listFiles()) {
            boolean fileAlredyExist = false;
            int position = -1;
            String fileName = "";
            for(int x = 0 ; x < photos.size(); x++){
                int index = photos
                        .get(x).photos.get(0).original_size.url.lastIndexOf("/");
                fileName = photos
                        .get(x).photos.get(0).original_size.url.substring(index+1);
                if(child.getName().equals(fileName)) {
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

    @Override
    public void onSyncCanceled() {
        super.onSyncCanceled();
    }
}
