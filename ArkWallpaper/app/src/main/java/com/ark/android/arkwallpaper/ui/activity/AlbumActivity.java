
package com.ark.android.arkwallpaper.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ark.android.arkwallpaper.R;
import com.ark.android.arkwallpaper.ui.adapter.AlbumAdapter;
import com.ark.android.arkwallpaper.ui.adapter.AlbumsAdapter;
import com.ark.android.arkwallpaper.utils.uiutils.GlideContentProviderLoader;
import com.ark.android.gallerylib.CancelReason;
import com.ark.android.gallerylib.ChooserActivity;
import com.ark.android.gallerylib.data.GallaryDataBaseContract;
import com.bumptech.glide.Glide;

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
    private Animation fabOpen;
    private Animation fabClose;
    private Animation rotateForward;
    private Animation rotateBackward;
    private boolean isFabOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlbumActivity.this.finish();
            }
        });

        getSupportLoaderManager().initLoader(0, null, this);

        fabOpen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotateForward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotateBackward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);

        floatingMenu.setOnClickListener(this);
        addFolder.setOnClickListener(this);
        addImage.setOnClickListener(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, GallaryDataBaseContract.GalleryTable.CONTENT_URI
                , new String[]{BaseColumns._ID, GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI}, GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME + " = ?"
                , new String[]{getIntent().getExtras().getString("albumName")}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.getCount() > 0)
            setupAdapter(data);
    }

    private void setupAdapter(Cursor data) {
        data.moveToFirst();
        List<Uri> images = new ArrayList<>();
        while (!data.isAfterLast()) {
            images.add(Uri.parse(data.getString(data.getColumnIndex(GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI))));
            data.moveToNext();
        }

        albumList.setLayoutManager(new GridLayoutManager(this, 2));
        albumList.setAdapter(new AlbumAdapter(images, this));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_CANCELED && data != null){
            int cancelReason = data.getIntExtra(ChooserActivity.CANCEL_REASON, CancelReason.REASON_CANCEL);

            switch (cancelReason){
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
        }
    }

    void showSnackBar(String msg){
        if(isFabOpen)
            animateFAB();
        Snackbar snack = Snackbar.make(findViewById(R.id.coordinateLayout), msg, Snackbar.LENGTH_LONG);
        View view = snack.getView();
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)view.getLayoutParams();
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
}
