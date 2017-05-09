
package com.ark.android.arkwallpaper.ui.activity;

import android.accounts.Account;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ark.android.arkwallpaper.R;
import com.ark.android.arkwallpaper.ui.adapter.AlbumAdapter;
import com.ark.android.gallerylib.CancelReason;
import com.ark.android.gallerylib.ChooserActivity;
import com.ark.android.gallerylib.data.GallaryDataBaseContract;
import com.ark.android.onlinesourcelib.syncadapter.FiveHundredSyncAdapter;
import com.ark.android.onlinesourcelib.Account.FivePxGenericAccountService;
import com.ark.android.onlinesourcelib.syncUtils.FivePxSyncUtils;
import com.ark.android.onlinesourcelib.Account.TumblrGenericAccountService;
import com.ark.android.onlinesourcelib.syncadapter.TumblrSyncAdapter;
import com.ark.android.onlinesourcelib.syncUtils.TumblrSyncUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlbumActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private static final int CHOOSER_ACTIVITY_REQUEST = 500;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.albumList)
    RecyclerView albumList;
    @BindView(R.id.floatingMenu)
    FloatingActionButton floatingMenu;
    @BindView(R.id.addFolder)
    FloatingActionButton addFolder;
    @BindView(R.id.addImage)
    FloatingActionButton addImage;
    @BindView(R.id.progressbar)
    ProgressBar progressBar;
    @BindView(R.id.setAsWallpaper)
    TextView setWallpaper;
    @BindView(R.id.refreshFolder)
    ImageButton refreshButton;
    @BindView(R.id.syncProgressIndicator)
    ProgressBar syncProgressIndicator;
    @BindView(R.id.emptyView)
    TextView emptyView;


    private Animation fabOpen;
    private Animation fabClose;
    private Animation rotateForward;
    private Animation rotateBackward;
    private boolean isFabOpen;
    private View.OnClickListener backClick;
    private int mAlbumtype;

    private Object mSyncTumblrObserverHandle;
    private Object mSync500PxObserverHandle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlbumActivity.this.finish();
            }
        });

        mAlbumtype = getIntent().getExtras().getInt("type", GallaryDataBaseContract.AlbumsTable.ALBUM_TYPE_GALLERY);

        if (mAlbumtype == GallaryDataBaseContract.AlbumsTable.ALBUM_TYPE_GALLERY)
            refreshButton.setVisibility(View.GONE);

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAlbumtype == GallaryDataBaseContract.AlbumsTable.ALBUM_TYPE_TUMBLR) {
                    Bundle bundle = new Bundle();
                    bundle.putString("albumName", getIntent().getExtras().getString("tumblrBlog"));
                    TumblrSyncUtils.getInstance().TriggerRefresh(bundle);

                } else if (mAlbumtype == GallaryDataBaseContract.AlbumsTable.ALBUM_TYPE_PX) {
                    Bundle bundle = new Bundle();
                    bundle.putString(FiveHundredSyncAdapter.CAT_KEY, getIntent().getExtras().getString("fivePxCat"));
                    FivePxSyncUtils.getInstance().TriggerRefresh(bundle);
                }
            }
        });

        getSupportActionBar().setTitle(getIntent().getExtras().getString("albumName"));

        getSupportLoaderManager().initLoader(0, null, this);

        fabOpen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotateForward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotateBackward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);

        floatingMenu.setOnClickListener(this);
        addFolder.setOnClickListener(this);
        addImage.setOnClickListener(this);
        if (getIntent().getExtras().getString("albumName").equals(FiveHundredSyncAdapter.ALBUM_NAME))
            progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAlbumtype == GallaryDataBaseContract.AlbumsTable.ALBUM_TYPE_TUMBLR) {
            mSyncTumblrStatusObserver.onStatusChanged(0);
            // Watch for sync state changes
            final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                    ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
            mSyncTumblrObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncTumblrStatusObserver);

        } else if (mAlbumtype == GallaryDataBaseContract.AlbumsTable.ALBUM_TYPE_PX) {
            mSyncFivePxStatusObserver.onStatusChanged(0);
            // Watch for sync state changes
            final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                    ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
            mSync500PxObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncFivePxStatusObserver);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSyncTumblrObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncTumblrObserverHandle);
            mSyncTumblrObserverHandle = null;
        }

        if (mSync500PxObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSync500PxObserverHandle);
            mSync500PxObserverHandle = null;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, GallaryDataBaseContract.GalleryTable.CONTENT_URI
                , new String[]{BaseColumns._ID, GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI, GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME}, GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME + " = ?"
                , new String[]{getIntent().getExtras().getString("albumName")}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.getCount() > 0) {
            progressBar.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
            setupAdapter(data);
        }else{
            albumList.setAdapter(null);
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    private void setupAdapter(Cursor data) {
        data.moveToFirst();
        List<Uri> images = new ArrayList<>();
        while (!data.isAfterLast()) {
            if (data.getString(data.getColumnIndex(GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME)).equalsIgnoreCase(TumblrSyncAdapter.ALBUM_NAME) ||
                    data.getString(data.getColumnIndex(GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME)).equalsIgnoreCase(FiveHundredSyncAdapter.ALBUM_NAME)) {
                floatingMenu.setVisibility(View.GONE);
            }
            images.add(Uri.parse(data.getString(data.getColumnIndex(GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI))));
            data.moveToNext();
        }

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);

        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int mod = position % 3;

                if (mod == 0 || mod == 1)
                    return 1;
                else
                    return 2;
            }
        });
        albumList.setLayoutManager(gridLayoutManager);
//        albumList.addItemDecoration(new GridSeparator(2, 8, true));
        albumList.setAdapter(new AlbumAdapter(images, this));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED && data != null && requestCode == CHOOSER_ACTIVITY_REQUEST) {
            int cancelReason = data.getIntExtra(ChooserActivity.CANCEL_REASON, CancelReason.REASON_CANCEL);

            switch (cancelReason) {
                case CancelReason.REASON_PERMISSION:
                    showSnackBar(getString(R.string.permission_denied));
                    break;
                case CancelReason.REASON_FOLDER_ACTIVITY_NOT_FOUND:
                    showSnackBar(getString(R.string.gallery_add_folder_error));
                    break;
                case CancelReason.REASON_IMAGE_ACTIVITY_NOT_FOUND:
                    showSnackBar(getString(R.string.gallery_add_photos_error));
                    break;
            }
        }else if(resultCode == Activity.RESULT_OK && data != null && requestCode == LastImageInfoActivity.REQUEST_ID){
            showDeleteDialog(data.getStringExtra(LastImageInfoActivity.IMAGE_URI));
        }
    }

    void showSnackBar(String msg) {
        if (isFabOpen)
            animateFAB();
        Snackbar snack = Snackbar.make(findViewById(R.id.coordinateLayout), msg, Snackbar.LENGTH_LONG);
        View view = snack.getView();
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
        params.gravity = Gravity.BOTTOM;
        view.setLayoutParams(params);
        snack.show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.floatingMenu:
                animateFAB();
                break;
            case R.id.addFolder:
                startChooserActivity(true);
                animateFAB();
                break;
            case R.id.addImage:
                startChooserActivity(false);
                animateFAB();
                break;
        }
    }

    private void animateFAB() {

        if (isFabOpen) {
            floatingMenu.startAnimation(rotateBackward);
            addFolder.startAnimation(fabClose);
            addImage.startAnimation(fabClose);
            addImage.setClickable(false);
            addFolder.setClickable(false);
            isFabOpen = false;
        } else {
            floatingMenu.startAnimation(rotateForward);
            addFolder.startAnimation(fabOpen);
            addImage.startAnimation(fabOpen);
            addFolder.setClickable(true);
            addImage.setClickable(true);
            isFabOpen = true;
        }
    }

    void startChooserActivity(boolean isFolder) {
        startActivityForResult(new Intent(this, ChooserActivity.class)
                .putExtra(ChooserActivity.ALBUM_NAME, getIntent().getExtras().getString("albumName"))
                .putExtra(ChooserActivity.CHOSEN_SOURCE, isFolder
                        ? ChooserActivity.CHOOSE_FOLDER : ChooserActivity.CHOOSE_IMAGE), CHOOSER_ACTIVITY_REQUEST);
    }

    public void setBackClickable(View.OnClickListener backClickable) {
        this.backClick = backClickable;
    }

    @Override
    public void onBackPressed() {
        if (backClick != null && findViewById(R.id.expandedImageView).getVisibility() == View.VISIBLE) {
            backClick.onClick(null);
        } else {
            super.onBackPressed();
        }
    }

    public int getmAlbumtype() {
        return mAlbumtype;
    }

    private SyncStatusObserver mSyncFivePxStatusObserver = new SyncStatusObserver() {
        /** Callback invoked with the sync adapter status changes. */
        @Override
        public void onStatusChanged(int which) {
            runOnUiThread(new Runnable() {
                /**
                 * The SyncAdapter runs on a background thread. To update the UI, onStatusChanged()
                 * runs on the UI thread.
                 */
                @Override
                public void run() {
                    // Create a handle to the account that was created by
                    // SyncService.CreateSyncAccount(). This will be used to query the system to
                    // see how the sync status has changed.
                    Account account = FivePxGenericAccountService.GetAccount(FivePxSyncUtils.ACCOUNT_TYPE);
                    if (account == null) {
                        // GetAccount() returned an invalid value. This shouldn't happen, but
                        // we'll set the status to "not refreshing".
                        setRefreshActionButtonState(false);
                        return;
                    }

                    // Test the ContentResolver to see if the sync adapter is active or pending.
                    // Set the state of the refresh button accordingly.
                    boolean syncActive = ContentResolver.isSyncActive(
                            account, GallaryDataBaseContract.GALLERY_AUTHORITY);
                    boolean syncPending = ContentResolver.isSyncPending(
                            account, GallaryDataBaseContract.GALLERY_AUTHORITY);
                    setRefreshActionButtonState(syncActive || syncPending);
                }
            });
        }
    };

    private SyncStatusObserver mSyncTumblrStatusObserver = new SyncStatusObserver() {
        /** Callback invoked with the sync adapter status changes. */
        @Override
        public void onStatusChanged(int which) {
            runOnUiThread(new Runnable() {
                /**
                 * The SyncAdapter runs on a background thread. To update the UI, onStatusChanged()
                 * runs on the UI thread.
                 */
                @Override
                public void run() {
                    // Create a handle to the account that was created by
                    // SyncService.CreateSyncAccount(). This will be used to query the system to
                    // see how the sync status has changed.
                    Account account = TumblrGenericAccountService.GetAccount(TumblrSyncUtils.ACCOUNT_TYPE);
                    if (account == null) {
                        // GetAccount() returned an invalid value. This shouldn't happen, but
                        // we'll set the status to "not refreshing".
                        setRefreshActionButtonState(false);
                        return;
                    }

                    // Test the ContentResolver to see if the sync adapter is active or pending.
                    // Set the state of the refresh button accordingly.
                    boolean syncActive = ContentResolver.isSyncActive(
                            account, GallaryDataBaseContract.GALLERY_AUTHORITY);
                    boolean syncPending = ContentResolver.isSyncPending(
                            account, GallaryDataBaseContract.GALLERY_AUTHORITY);
                    setRefreshActionButtonState(syncActive || syncPending);
                }
            });
        }
    };

    public void setRefreshActionButtonState(boolean refreshing) {
        if (refreshing) {
            syncProgressIndicator.setVisibility(View.VISIBLE);
            refreshButton.setEnabled(false);
        } else {
            syncProgressIndicator.setVisibility(View.GONE);
            refreshButton.setEnabled(true);
        }
    }

    public void deleteImage(String imageUri) {
        showDeleteDialog(imageUri);
    }

    private void showDeleteDialog(final String imageUri) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete))
                .setMessage(getString(R.string.image_delete_confirmation))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        getContentResolver().delete(GallaryDataBaseContract.GalleryTable.CONTENT_URI
                                , GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI + " = ?", new String[]{imageUri});
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
    }
}
