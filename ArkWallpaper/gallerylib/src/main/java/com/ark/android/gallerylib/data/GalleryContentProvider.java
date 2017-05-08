package com.ark.android.gallerylib.data;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.content.UriPermission;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Binder;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.ark.android.arkanalytics.GATrackerManager;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

/**
 *
 * Created by ahmed-basyouni on 4/22/17.
 */

public class GalleryContentProvider extends ContentProvider {

    private static final int GET_PHOTO_FOR_ALBUM = 1;

    private static final int GET_PHOTO_BY_ID = 2;

    private static final int GET_ALBUMS = 3;

    private static final UriMatcher uriMatcher = buildUriMatcher();

    private final HashMap<String, String> allGalleryColumnProjectionMap =
            buildAllImagesColumnProjectionMap();

    private final HashMap<String, String> allAlbumsColumnProjectionMap =
            buildAllAlbumsColumnProjectionMap();

    /**
     * Handle to a new DatabaseHelper.
     */
    private GalleryDBHelper databaseHelper;
    /**
     * Whether we should hold notifyChange() calls due to an ongoing applyBatch operation
     */
    private boolean holdNotifyChange = false;
    /**
     * Set of Uris that should be applied when the ongoing applyBatch operation finishes
     */
    private final LinkedHashSet<Uri> pendingNotifyChange = new LinkedHashSet<>();
    private String TAG = GalleryContentProvider.class.getSimpleName();


    @NonNull
    @Override
    public ContentProviderResult[] applyBatch(@NonNull final ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        holdNotifyChange = true;
        try {
            return super.applyBatch(operations);
        } finally {
            holdNotifyChange = false;
            Context context = getContext();
            if (context != null) {
                ContentResolver contentResolver = context.getContentResolver();
                synchronized (pendingNotifyChange) {
                    Iterator<Uri> iterator = pendingNotifyChange.iterator();
                    while (iterator.hasNext()) {
                        Uri uri = iterator.next();
                        contentResolver.notifyChange(uri, null);
                        iterator.remove();
                    }
                }
            }
        }
    }

    private void notifyChange(Uri uri) {
        if (holdNotifyChange) {
            synchronized (pendingNotifyChange) {
                pendingNotifyChange.add(uri);
            }
        } else {
            Context context = getContext();
            if (context == null) {
                return;
            }
            context.getContentResolver().notifyChange(uri, null);
        }
    }

    @Override
    public boolean onCreate() {
        databaseHelper = new GalleryDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (uriMatcher.match(uri) == GET_PHOTO_BY_ID ||
                uriMatcher.match(uri) == GET_PHOTO_FOR_ALBUM) {
            return queryGalleyImages(uri, projection, selection, selectionArgs, sortOrder);
        } else if (uriMatcher.match(uri) == GET_ALBUMS) {
            return queryAlbums(uri, projection, selection, selectionArgs, sortOrder);
        } else {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    private Cursor queryAlbums(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        ContentResolver contentResolver = getContext() != null ? getContext().getContentResolver() : null;
        if (contentResolver == null) {
            return null;
        }
        final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(GallaryDataBaseContract.AlbumsTable.TABLE_NAME);
        qb.setProjectionMap(allAlbumsColumnProjectionMap);
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String orderBy;
        if (TextUtils.isEmpty(sortOrder))
            orderBy = GallaryDataBaseContract.AlbumsTable.DEFAULT_SORT_ORDER;
        else
            orderBy = sortOrder;
        final Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy, null);
        c.setNotificationUri(contentResolver, uri);
        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        if (uriMatcher.match(uri) == GET_PHOTO_FOR_ALBUM)
            return insertGalleryImage(uri, values);
        else if (uriMatcher.match(uri) == GET_ALBUMS)
            return insertAlbum(uri, values);
        else {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    private Uri insertAlbum(Uri uri, ContentValues values) {
        Context context = getContext();
        if (context == null) {
            return null;
        }
        if (values == null) {
            throw new IllegalArgumentException("Invalid ContentValues: must not be null");
        }

        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        long rowId = db.insert(GallaryDataBaseContract.AlbumsTable.TABLE_NAME,
                GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_NAME, values);
        // If the insert succeeded, the row ID exists.
        if (rowId > 0) {
            // Creates a URI with the chosen photos ID pattern and the new row ID appended to it.
            final Uri albumUri = ContentUris.withAppendedId(GallaryDataBaseContract.AlbumsTable.CONTENT_URI, rowId);
            notifyChange(albumUri);
            return albumUri;
        }
        // If the insert didn't succeed, then the rowID is <= 0
        throw new SQLException("Failed to insert row into " + uri);
    }

    private Uri insertGalleryImage(Uri uri, ContentValues values) {
        Context context = getContext();
        if (context == null) {
            return null;
        }
        if (values == null) {
            throw new IllegalArgumentException("Invalid ContentValues: must not be null");
        }
        if (!values.containsKey(GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI))
            throw new IllegalArgumentException("Initial values must contain URI " + values);
        String imageUri = "";
        boolean isTreeUri = false;
        if (values.getAsString(GallaryDataBaseContract.GalleryTable.COLUMN_PARENT_URI) != null
                && !values.getAsString(GallaryDataBaseContract.GalleryTable.COLUMN_PARENT_URI).equalsIgnoreCase("")) {
            imageUri = values.getAsString(GallaryDataBaseContract.GalleryTable.COLUMN_PARENT_URI);
            isTreeUri = true;
        } else
            imageUri = values.getAsString(GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI);
        // Check if it is a tree URI (i.e., a whole directory of images)
        Uri uriToTake = Uri.parse(imageUri);
        if (isTreeUri) {
            try {
                context.getContentResolver().takePersistableUriPermission(uriToTake, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException ignored) {
                GATrackerManager.getInstance().trackException(ignored);
                ignored.printStackTrace();
                // You can't persist URI permissions from your own app, so this fails.
                // We'll still have access to it directly
            }
        } else if(ContentResolver.SCHEME_CONTENT.equals(uriToTake.getScheme())){
            boolean haveUriPermission = context.checkUriPermission(uriToTake,
                    Binder.getCallingPid(), Binder.getCallingUid(),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION) == PackageManager.PERMISSION_GRANTED;
            // If we only have permission to this URI via URI permissions (rather than directly,
            // such as if the URI is from our own app), it is from an external source and we need
            // to make sure to gain persistent access to the URI's content
            if (haveUriPermission) {
                boolean persistedPermission = false;
                // Try to persist access to the URI, saving us from having to store a local copy
                if (DocumentsContract.isDocumentUri(context, uriToTake)) {
                    try {
                        context.getContentResolver().takePersistableUriPermission(uriToTake,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        persistedPermission = true;
                        // If we have a persisted URI permission, we don't need a local copy
                    } catch (SecurityException ignored) {
                        GATrackerManager.getInstance().trackException(ignored);
                        // If we don't have FLAG_GRANT_PERSISTABLE_URI_PERMISSION (such as when using ACTION_GET_CONTENT),
                        // this will fail. We'll need to make a local copy (handled below)
                    }
                }
            } else {
                // On API 25 and lower, we don't get URI permissions to URIs
                // from our own package so we manage those URI permissions manually
                ContentResolver resolver = context.getContentResolver();
                resolver.call(uriToTake, "takePersistableUriPermission",
                        uriToTake.toString(), null);
            }
        }

        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        long rowId = db.insert(GallaryDataBaseContract.GalleryTable.TABLE_NAME,
                GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI, values);
        // If the insert succeeded, the row ID exists.
        if (rowId > 0) {
            if(values.containsKey(GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME)){
                updateAlbumImage(values.getAsString(GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME));
            }
            // Creates a URI with the chosen photos ID pattern and the new row ID appended to it.
            final Uri chosenPhotoUri = ContentUris.withAppendedId(GallaryDataBaseContract.GalleryTable.CONTENT_URI, rowId);
            notifyChange(chosenPhotoUri);
            return chosenPhotoUri;
        }
        // If the insert didn't succeed, then the rowID is <= 0
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public ParcelFileDescriptor openFile(@NonNull final Uri uri, @NonNull final String mode) throws FileNotFoundException {
        if (uriMatcher.match(uri) == GET_PHOTO_BY_ID) {
            return openGalleryFile(uri, mode);
        } else {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    private ParcelFileDescriptor openGalleryFile(Uri uri, String mode) throws FileNotFoundException {
        if (!mode.equals("r")) {
            throw new IllegalArgumentException("Only reading chosen photos is allowed");
        }
        String[] projection = {GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI};
        Cursor data = queryGalleyImages(uri, projection, null, null, null);
        if (data == null) {
            return null;
        }
        if (!data.moveToFirst()) {
            data.close();
            throw new FileNotFoundException("Unable to load " + uri);
        }
        String imageUri = data.getString(0);
        data.close();
        // Assume we have persisted URI permission to the imageUri and can read the image directly from the imageUri
        try {
            return getContext().getContentResolver().openFileDescriptor(Uri.parse(imageUri), mode);
        } catch (SecurityException | IllegalArgumentException e) {
            GATrackerManager.getInstance().trackException(e);
            Log.d(TAG, "Unable to load " + uri + ", deleting the row", e);
            deleteGalleryImage(uri, null, null);
            throw new FileNotFoundException("No permission to load " + uri);
        }
    }


    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        if (uriMatcher.match(uri) == GET_PHOTO_FOR_ALBUM ||
                uriMatcher.match(uri) == GET_PHOTO_BY_ID) {
            return deleteGalleryImage(uri, selection, selectionArgs);
        } else if (uriMatcher.match(uri) == GET_ALBUMS) {
            return deleteAlbum(uri, selection, selectionArgs);
        } else {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    private int deleteAlbum(Uri uri, String selection, String[] selectionArgs) {
        Context context = getContext();
        if (context == null) {
            return 0;
        }
// Opens the database object in "write" mode.
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        // We can't just simply delete the rows as that won't free up the space occupied by the
        // chosen image files for each row being deleted. Instead we have to query
        // and manually delete each chosen image file
        String[] projection = new String[]{
                GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI, GallaryDataBaseContract.GalleryTable.COLUMN_PARENT_URI};
        Cursor rowsToDelete = queryGalleyImages(GallaryDataBaseContract.GalleryTable.CONTENT_URI
                , projection, selection, selectionArgs, null);
        if (rowsToDelete == null) {
            return 0;
        }
        rowsToDelete.moveToFirst();
        while (!rowsToDelete.isAfterLast()) {
            String imageUri = "";
            if(rowsToDelete.getString(rowsToDelete.getColumnIndex(GallaryDataBaseContract.GalleryTable.COLUMN_PARENT_URI)) != null &&
                    rowsToDelete.getString(rowsToDelete.getColumnIndex(GallaryDataBaseContract.GalleryTable.COLUMN_PARENT_URI)).equalsIgnoreCase(""))
                imageUri = rowsToDelete.getString(rowsToDelete.getColumnIndex(GallaryDataBaseContract.GalleryTable.COLUMN_PARENT_URI));
            else
                imageUri = rowsToDelete.getString(rowsToDelete.getColumnIndex(GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI));
            Uri uriToRelease = Uri.parse(imageUri);
            ContentResolver contentResolver = context.getContentResolver();
            if(ContentResolver.SCHEME_CONTENT.equals(uriToRelease.getScheme())) {
                boolean haveUriPermission = context.checkUriPermission(uriToRelease,
                        Binder.getCallingPid(), Binder.getCallingUid(),
                        Intent.FLAG_GRANT_READ_URI_PERMISSION) == PackageManager.PERMISSION_GRANTED;
                if (haveUriPermission) {
                    // Try to release any persisted URI permission for the imageUri
                    List<UriPermission> persistedUriPermissions = contentResolver.getPersistedUriPermissions();
                    for (UriPermission persistedUriPermission : persistedUriPermissions) {
                        if (persistedUriPermission.getUri().equals(uriToRelease)) {
                            contentResolver.releasePersistableUriPermission(
                                    uriToRelease, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            break;
                        }
                    }
                } else {
                    // On API 25 and lower, we don't get URI permissions to URIs
                    // from our own package so we manage those URI permissions manually
                    contentResolver.call(uriToRelease, "releasePersistableUriPermission",
                            uriToRelease.toString(), null);
                }
            }
            rowsToDelete.moveToNext();
        }

        db.delete(GallaryDataBaseContract.GalleryTable.TABLE_NAME, selection, selectionArgs);
        int count = db.delete(GallaryDataBaseContract.AlbumsTable.TABLE_NAME, selection, selectionArgs);
        if (count > 0) {
            notifyChange(uri);
        }
        return count;
    }

    private int deleteGalleryImage(Uri uri, String selection, String[] selectionArgs) {
        Context context = getContext();
        if (context == null) {
            return 0;
        }
// Opens the database object in "write" mode.
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        // We can't just simply delete the rows as that won't free up the space occupied by the
        // chosen image files for each row being deleted. Instead we have to query
        // and manually delete each chosen image file
        String[] projection = new String[]{
                GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI, GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME, GallaryDataBaseContract.GalleryTable._ID};
        Cursor rowsToDelete = queryGalleyImages(uri, projection, selection, selectionArgs, null);
        if (rowsToDelete == null) {
            return 0;
        }
        rowsToDelete.moveToFirst();
        String albumName = null;
        if(rowsToDelete.getCount() > 0){
            albumName = rowsToDelete.getString(rowsToDelete.getColumnIndex(GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME));
        }
        while (!rowsToDelete.isAfterLast()) {
            String imageUri = rowsToDelete.getString(0);
            if(uri.toString().equals(GallaryDataBaseContract.GalleryTable.CONTENT_URI.toString())){
                if(rowsToDelete.getInt(rowsToDelete.getColumnIndex(GallaryDataBaseContract.GalleryTable._ID)) != -1){
                    String deletedImageUri = uri.toString() + "/" + rowsToDelete.getInt(rowsToDelete.getColumnIndex(GallaryDataBaseContract.GalleryTable._ID));
                    notifyChange(Uri.parse(deletedImageUri));
                }
            }
            Uri uriToRelease = Uri.parse(imageUri);
            ContentResolver contentResolver = context.getContentResolver();
            if(ContentResolver.SCHEME_CONTENT.equals(uriToRelease.getScheme())) {
                boolean haveUriPermission = context.checkUriPermission(uriToRelease,
                        Binder.getCallingPid(), Binder.getCallingUid(),
                        Intent.FLAG_GRANT_READ_URI_PERMISSION) == PackageManager.PERMISSION_GRANTED;
                if (haveUriPermission) {
                    // Try to release any persisted URI permission for the imageUri
                    List<UriPermission> persistedUriPermissions = contentResolver.getPersistedUriPermissions();
                    for (UriPermission persistedUriPermission : persistedUriPermissions) {
                        if (persistedUriPermission.getUri().equals(uriToRelease)) {
                            contentResolver.releasePersistableUriPermission(
                                    uriToRelease, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            break;
                        }
                    }
                } else {
                    // On API 25 and lower, we don't get URI permissions to URIs
                    // from our own package so we manage those URI permissions manually
                    contentResolver.call(uriToRelease, "releasePersistableUriPermission",
                            uriToRelease.toString(), null);
                }
            }
            rowsToDelete.moveToNext();
        }
        String finalSelection = selection;
        if (uriMatcher.match(uri) == GET_PHOTO_BY_ID) {

            finalSelection = DatabaseUtils.concatenateWhere(selection,
                    BaseColumns._ID + "=" + uri.getLastPathSegment());
        }

        int count = db.delete(GallaryDataBaseContract.GalleryTable.TABLE_NAME, finalSelection, selectionArgs);
        if (count > 0) {
            notifyChange(uri);
        }
        if(albumName != null)
            updateAlbumImage(albumName);
        return count;
    }

    private void updateAlbumImage(String albumName){
        String[] projection = new String[]{
                GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI, GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME};
        Cursor rowsToDelete = queryGalleyImages(GallaryDataBaseContract.GalleryTable.CONTENT_URI, projection
                , GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME + " = ?", new String[]{albumName}, null);
        ContentValues contentValues = new ContentValues();
        if (rowsToDelete == null || rowsToDelete.getCount() == 0) {
            contentValues.put(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_IMAGE_URI, "");
        }else if(rowsToDelete.getCount() > 0){
            rowsToDelete.moveToPosition(0);
            contentValues.put(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_IMAGE_URI, rowsToDelete.getString(rowsToDelete.getColumnIndex(GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI)));
        }
        contentValues.put(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_COUNT, rowsToDelete == null ? 0 : rowsToDelete.getCount());
        updateAlbum(GallaryDataBaseContract.AlbumsTable.CONTENT_URI, contentValues,GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_NAME + " = ?", new String[]{albumName});
    }

    private Cursor queryGalleyImages(Uri uri, String[] projection, String selection, String[] selectionArgs, final String sortOrder) {
        ContentResolver contentResolver = getContext() != null ? getContext().getContentResolver() : null;
        if (contentResolver == null) {
            return null;
        }
        final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(GallaryDataBaseContract.GalleryTable.TABLE_NAME);
        qb.setProjectionMap(allGalleryColumnProjectionMap);
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        if (uriMatcher.match(uri) == GET_PHOTO_BY_ID) {
            // If the incoming URI is for a single chosen photo identified by its ID, appends "_ID = <chosenPhotoId>"
            // to the where clause, so that it selects that single chosen photo
            qb.appendWhere(BaseColumns._ID + "=" + uri.getLastPathSegment());
        }
        String orderBy;
        if (TextUtils.isEmpty(sortOrder))
            orderBy = GallaryDataBaseContract.GalleryTable.DEFAULT_SORT_ORDER;
        else
            orderBy = sortOrder;
        final Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy, null);
        c.setNotificationUri(contentResolver, uri);
        return c;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if(uriMatcher.match(uri) == GET_ALBUMS)
            return updateAlbum(uri, values, selection, selectionArgs);
        else
            throw new UnsupportedOperationException("Updates are not allowed");
    }

    private int updateAlbum(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int update = db.update(GallaryDataBaseContract.AlbumsTable.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        notifyChange(uri);
        return update;
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(GallaryDataBaseContract.GALLERY_AUTHORITY, GallaryDataBaseContract.GalleryTable.TABLE_NAME,
                GET_PHOTO_FOR_ALBUM);
        matcher.addURI(GallaryDataBaseContract.GALLERY_AUTHORITY, GallaryDataBaseContract.GalleryTable.TABLE_NAME + "/#",
                GET_PHOTO_BY_ID);
        matcher.addURI(GallaryDataBaseContract.GALLERY_AUTHORITY, GallaryDataBaseContract.AlbumsTable.TABLE_NAME,
                GET_ALBUMS);
        return matcher;
    }

    private static HashMap<String, String> buildAllImagesColumnProjectionMap() {
        final HashMap<String, String> allColumnProjectionMap = new HashMap<>();
        allColumnProjectionMap.put(BaseColumns._ID, BaseColumns._ID);
        allColumnProjectionMap.put(GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI,
                GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI);
        allColumnProjectionMap.put(GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME,
                GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME);
        allColumnProjectionMap.put(GallaryDataBaseContract.GalleryTable.COLUMN_PARENT_URI,
                GallaryDataBaseContract.GalleryTable.COLUMN_PARENT_URI);
        return allColumnProjectionMap;
    }

    private HashMap<String, String> buildAllAlbumsColumnProjectionMap() {
        final HashMap<String, String> allColumnProjectionMap = new HashMap<>();
        allColumnProjectionMap.put(BaseColumns._ID, BaseColumns._ID);
        allColumnProjectionMap.put(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_NAME,
                GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_NAME);
        allColumnProjectionMap.put(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_IMAGE_URI,
                GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_IMAGE_URI);
        allColumnProjectionMap.put(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_ENABLED,
                GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_ENABLED);
        allColumnProjectionMap.put(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_TYPE,
                GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_TYPE);
        allColumnProjectionMap.put(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_COUNT,
                GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_COUNT);
        allColumnProjectionMap.put(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_TUMBLR_BLOG_NAME,
                GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_TUMBLR_BLOG_NAME);
        allColumnProjectionMap.put(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_Five_PX_CATEGORY,
                GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_Five_PX_CATEGORY);
        return allColumnProjectionMap;
    }
}
