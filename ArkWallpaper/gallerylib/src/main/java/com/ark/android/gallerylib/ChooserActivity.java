package com.ark.android.gallerylib;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ark.android.gallerylib.data.GallaryDataBaseContract;
import com.ark.android.gallerylib.util.GallaryUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * Created by ahmed-basyouni on 4/21/17.
 */

public class ChooserActivity extends AppCompatActivity {

    public static final int CHOOSE_FOLDER = 0x01;
    public static final int CHOOSE_IMAGE = 0x02;

    public static final String CANCEL_REASON = "com.ark.android.gallarylib.result_cancel_reason";
    public static final String ALBUM_NAME = "com.ark.android.gallarylib.albumName";

    public static final String CHOSEN_SOURCE = "com.ark.android.gallarylib.choosen_source";
    private static final int REQUEST_STORAGE_PERMISSION = 3;
    private static final int FOLDER_CHOOSER_REQUEST = 0x07;
    private static final int IMAGE_CHOOSER_REQUEST = 0x08;
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private String TAG = ChooserActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkIntent();
        checkPermissions();
    }

    private void checkIntent() {
        if(getIntent().getExtras() == null)
            throw new RuntimeException(getString(R.string.activityOpenedWithoutExtra));

        if(getIntent().getExtras().get(CHOSEN_SOURCE) == null)
            throw new RuntimeException(getString(R.string.activityOpenedWithoutSource));

        if(getIntent().getExtras().get(ALBUM_NAME) == null)
            throw new RuntimeException(getString(R.string.activityOpenedWithoutAlbum));
    }

    private void checkPermissions() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            ActivityCompat.requestPermissions(ChooserActivity.this, new String[] {
                    Manifest.permission.READ_EXTERNAL_STORAGE }, REQUEST_STORAGE_PERMISSION);
        }else{
            openChooser();
        }
    }

    private void openChooser() {
        if(getIntent().getExtras().getInt(CHOSEN_SOURCE, CHOOSE_FOLDER) == CHOOSE_FOLDER){
            openFoldersChooser();
        }else if(getIntent().getExtras().getInt(CHOSEN_SOURCE, CHOOSE_FOLDER) == CHOOSE_IMAGE){
            openImageChooser();
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        try {
            startActivityForResult(intent, IMAGE_CHOOSER_REQUEST);
        } catch (ActivityNotFoundException e) {
            setCancelResult(CancelReason.REASON_IMAGE_ACTIVITY_NOT_FOUND);
        }
    }

    private void openFoldersChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        try {
            startActivityForResult(intent, FOLDER_CHOOSER_REQUEST);
            
        }catch (ActivityNotFoundException ex){
            setCancelResult(CancelReason.REASON_FOLDER_ACTIVITY_NOT_FOUND);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_STORAGE_PERMISSION && checkPermissionGranted()){
            openChooser();
        }else{
            setCancelResult(CancelReason.REASON_PERMISSION);
        }
    }

    private boolean checkPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void setCancelResult(int reason){
        Intent data = new Intent();
        data.putExtra(CANCEL_REASON , reason);
        setResult(Activity.RESULT_CANCELED, data);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        Uri parentUri = null;
        if (requestCode != FOLDER_CHOOSER_REQUEST && requestCode != IMAGE_CHOOSER_REQUEST) {
            return;
        }

        if (resultCode != RESULT_OK || result == null) {
            setCancelResult(CancelReason.REASON_CANCEL);
            return;
        }

        final Set<Uri> uris = new HashSet<>();
        ClipData clipData = result.getClipData();
        if (result.getData() != null && requestCode == FOLDER_CHOOSER_REQUEST) {
            uris.addAll(GallaryUtils.getImagesFromTreeUri(ChooserActivity.this, result.getData(), Integer.MAX_VALUE));
            parentUri = result.getData();
        }else if(result.getData() != null && requestCode == IMAGE_CHOOSER_REQUEST && clipData == null){
            uris.add(result.getData());
        }

        if (clipData != null) {
            int count = clipData.getItemCount();
            for (int i = 0; i < count; i++) {
                Uri uri = clipData.getItemAt(i).getUri();
                if (uri != null) {
                    uris.add(uri);
                }
            }
        }

        if (uris.isEmpty()) {
            return;
        }
        // Update chosen URIs
        final Uri finalParentUri = parentUri;
        runOnHandlerThread(new Runnable() {
            @Override
            public void run() {
                ArrayList<ContentProviderOperation> operations = new ArrayList<>();
                for (Uri uri : uris) {
                    ContentValues values = new ContentValues();
                    values.put(GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI, uri.toString());
                    values.put(GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME, getIntent().getExtras().getString(ALBUM_NAME));
                    if(finalParentUri != null){
                        values.put(GallaryDataBaseContract.GalleryTable.COLUMN_PARENT_URI, finalParentUri.toString());
                    }
                    operations.add(ContentProviderOperation.newInsert(GallaryDataBaseContract.GalleryTable.CONTENT_URI)
                            .withValues(values).build());
                }
                try {
                    getContentResolver().applyBatch(GallaryDataBaseContract.GALLERY_AUTHORITY, operations);
                } catch (RemoteException | OperationApplicationException e) {
                    Log.e(TAG, "Error writing uris to ContentProvider", e);
                }finally {
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            }
        });
    }

    private void runOnHandlerThread(Runnable runnable) {
        if (mHandlerThread == null) {
            mHandlerThread = new HandlerThread(this.getClass().getSimpleName());
            mHandlerThread.start();
            mHandler = new Handler(mHandlerThread.getLooper());
        }
        mHandler.post(runnable);
    }
}
