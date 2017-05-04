package com.ark.android.arkwallpaper.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ark.android.arkwallpaper.R;
import com.ark.android.arkwallpaper.utils.IOUtils;
import com.ark.android.arkwallpaper.utils.WallPaperUtils;
import com.ark.android.arkwallpaper.utils.uiutils.GlideContentProviderLoader;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 *
 * Created by ahmed-basyouni on 5/4/17.
 */

public class LastImageInfoActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String IMAGE_URI = "image_uri";
    public static final int REQUEST_ID = 235;
    @BindView(R.id.expandedImageView)
    ImageView expandedImageView;
    @BindView(R.id.setAsWallpaper)
    TextView setAsWallpaper;
    @BindView(R.id.downloadImage)
    TextView downloadImage;
    @BindView(R.id.deleteImage)
    TextView deleteImage;
    private String imageUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.large_image_view);

        ButterKnife.bind(this);

        changeVisibility();
        setClickListeners();

        imageUri = "";
        if(getIntent().getExtras() != null && getIntent().getExtras().get(IMAGE_URI) != null)
            imageUri = getIntent().getExtras().getString(IMAGE_URI);
        else
            imageUri = WallPaperUtils.getCurrentWallpaper();

        Glide.with(this)
                .using(new GlideContentProviderLoader(this))
                .load(Uri.parse(imageUri))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(expandedImageView);
    }

    private void setClickListeners() {
        expandedImageView.setOnClickListener(this);
        setAsWallpaper.setOnClickListener(this);
        deleteImage.setOnClickListener(this);
        downloadImage.setOnClickListener(this);
    }

    private void changeVisibility() {
        expandedImageView.setVisibility(View.VISIBLE);
        setAsWallpaper.setVisibility(getIntent().getExtras() != null ? View.VISIBLE : View.GONE);
        downloadImage.setVisibility(View.VISIBLE);
        deleteImage.setVisibility(getIntent().getExtras() != null ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.deleteImage:
                deleteImage();
                break;

            case R.id.setAsWallpaper:
                WallPaperUtils.changeWallpaperBroadCast(imageUri);
                break;

            case R.id.downloadImage:
                saveImage();
                break;

            case R.id.expandedImageView:
                ActivityCompat.finishAfterTransition(this);
                break;
        }
    }

    private void deleteImage() {
        Intent intent = new Intent();
        intent.putExtra(IMAGE_URI, imageUri);
        setResult(Activity.RESULT_OK, intent);
        this.finish();
    }

    private void saveImage() {
        try {
            IOUtils.exportFile(new File(Uri.parse(imageUri).getPath()));
            Toast.makeText(this, getString(R.string.imageSaved), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
