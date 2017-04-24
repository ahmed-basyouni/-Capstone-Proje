package com.ark.android.arkwallpaper.ui.adapter;

import android.app.Activity;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ark.android.arkwallpaper.R;
import com.ark.android.arkwallpaper.utils.uiutils.GlideContentProviderLoader;
import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 *
 * Created by ahmed-basyouni on 4/24/17.
 */

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumHolder>{

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