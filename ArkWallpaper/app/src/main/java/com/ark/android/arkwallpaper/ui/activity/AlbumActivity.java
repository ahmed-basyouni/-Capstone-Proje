
package com.ark.android.arkwallpaper.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ark.android.arkwallpaper.R;
import com.ark.android.arkwallpaper.ui.adapter.AlbumsAdapter;
import com.ark.android.arkwallpaper.utils.uiutils.GlideContentProviderLoader;
import com.ark.android.gallerylib.ChooserActivity;
import com.ark.android.gallerylib.data.GallaryDataBaseContract;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlbumActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.albumList)
    RecyclerView albumList;

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

//        startActivityForResult(new Intent(this, ChooserActivity.class)
//                .putExtra(ChooserActivity.ALBUM_NAME, getIntent().getExtras().getString("albumName"))
//                .putExtra(ChooserActivity.CHOSEN_SOURCE, ChooserActivity.CHOOSE_FOLDER), 500);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, GallaryDataBaseContract.GalleryTable.CONTENT_URI
                , new String[]{BaseColumns._ID, GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI}, GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME + " = ?"
                , new String[]{getIntent().getExtras().getString("albumName")}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data != null && data.getCount() > 0)
            setupAdapter(data);
    }

    private void setupAdapter(Cursor data) {
        data.moveToFirst();
        List<Uri> images = new ArrayList<>();
        while (!data.isAfterLast()){
            images.add(Uri.parse(data.getString(data.getColumnIndex(GallaryDataBaseContract.GalleryTable.COLUMN_NAME_URI))));
            data.moveToNext();
        }

        albumList.setLayoutManager(new GridLayoutManager(this, 2));
        albumList.setAdapter(new AlbumAdapter(images, this));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumHolder>{

        private final List<Uri> images;
        private final Activity mContext;
        private int width;

        public AlbumAdapter(List<Uri> imagesUri, Activity activity){
            this.images = imagesUri;
            this.mContext = activity;
            DisplayMetrics metrics = new DisplayMetrics();
            mContext.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            width = metrics.widthPixels;
        }

        @Override
        public AlbumHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(mContext).inflate(R.layout.album_single_item, parent, false);
            return new AlbumHolder(rootView);
        }

        @Override
        public void onBindViewHolder(AlbumHolder holder, int position) {

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.imageHolder.getLayoutParams();

            params.width = (width/2) - 24;
            params.height = (width/2) - 24;
            holder.imageHolder.setLayoutParams(params);

            Glide.with(mContext)
                    .using(new GlideContentProviderLoader(mContext))
                    .load(images.get(position))
                    .override(params.width, params.height)
                    .into(holder.albumSingleImage);
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        class AlbumHolder extends RecyclerView.ViewHolder{

            @BindView(R.id.imageHolder)
            CardView imageHolder;
            @BindView(R.id.albumSingleImage)
            ImageView albumSingleImage;

            public AlbumHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }


}
