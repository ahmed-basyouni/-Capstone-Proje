package com.ark.android.arkwallpaper.ui.activity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ark.android.arkanalytics.GATrackerManager;
import com.ark.android.arkwallpaper.R;
import com.ark.android.arkwallpaper.utils.WallPaperUtils;
import com.ark.android.arkwallpaper.utils.uiutils.GlideBlurringTransformation;
import com.ark.android.arkwallpaper.utils.uiutils.GlideContentProviderLoader;
import com.ark.android.arkwallpaper.utils.uiutils.ImageBlurrer;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 *
 * Created by ahmed-basyouni on 5/7/17.
 */

public class CustomizeWallpaperActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    @BindView(R.id.expandedImageView)
    ImageView expandedImageView;
    @BindView(R.id.blurringValue)
    SeekBar blurringValue;
    @BindView(R.id.dimValue)
    SeekBar dimValue;
    @BindView(R.id.greyValue)
    SeekBar greyScaleValue;
    @BindView(R.id.blurringTitle)
    TextView blurringTitle;
    @BindView(R.id.dimTitle)
    TextView dimTitle;
    @BindView(R.id.greyTitle)
    TextView greyTitle;
    @BindView(R.id.customizeSectionTitle)
    TextView customizeSectionTitle;
    private Bitmap customizedBitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.customize_activity);
        GATrackerManager.getInstance().trackScreenView(getString(R.string.customizeActivity));
        ButterKnife.bind(this);

        changeVisibility();
        setClickListeners();

        String imageUri = WallPaperUtils.getCurrentWallpaper();

        Glide.with(this)
                .using(new GlideContentProviderLoader(this))
                .load(Uri.parse(imageUri))
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(new SimpleTarget<Bitmap>(500, 500) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                        customizedBitmap = resource;
                        expandedImageView.setImageBitmap(resource);
                        setseekerBarColors(resource);
                        setSeekBarsValue();
                    }
                });

        setSeekBarsListener();
    }

    private void setseekerBarColors(Bitmap resource) {
        Palette p = Palette.from(resource).generate();
        if(p.getVibrantSwatch() != null) {

            blurringValue.getProgressDrawable().setColorFilter(p.getVibrantSwatch().getTitleTextColor(), PorterDuff.Mode.MULTIPLY);
            blurringValue.getThumb().setColorFilter(p.getVibrantSwatch().getTitleTextColor(), PorterDuff.Mode.MULTIPLY);

            dimValue.getProgressDrawable().setColorFilter(p.getVibrantSwatch().getTitleTextColor(), PorterDuff.Mode.MULTIPLY);
            dimValue.getThumb().setColorFilter(p.getVibrantSwatch().getTitleTextColor(), PorterDuff.Mode.MULTIPLY);

            greyScaleValue.getProgressDrawable().setColorFilter(p.getVibrantSwatch().getTitleTextColor(), PorterDuff.Mode.MULTIPLY);
            greyScaleValue.getThumb().setColorFilter(p.getVibrantSwatch().getTitleTextColor(), PorterDuff.Mode.MULTIPLY);

            blurringTitle.setTextColor(p.getVibrantSwatch().getTitleTextColor());
            dimTitle.setTextColor(p.getVibrantSwatch().getTitleTextColor());
            greyTitle.setTextColor(p.getVibrantSwatch().getTitleTextColor());
            customizeSectionTitle.setTextColor(p.getVibrantSwatch().getTitleTextColor());
        }
    }

    private void setSeekBarsValue() {
        blurringValue.setProgress(WallPaperUtils.getCurrentBlurring());
        dimValue.setProgress(WallPaperUtils.getCurrentDim());
        greyScaleValue.setProgress((int) (WallPaperUtils.getCurrentGreyScale() * 100));
        changeImageValues(blurringValue.getProgress(), dimValue.getProgress(), greyScaleValue.getProgress());
    }

    private void setSeekBarsListener() {
        blurringValue.setOnSeekBarChangeListener(this);
        dimValue.setOnSeekBarChangeListener(this);
        greyScaleValue.setOnSeekBarChangeListener(this);
    }

    private void setClickListeners() {
        expandedImageView.setOnClickListener(this);
    }

    private void changeVisibility() {
        expandedImageView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.expandedImageView:
                ActivityCompat.finishAfterTransition(this);
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    private void changeImageValues(int blur, int dim, int greyScale) {

        GlideBlurringTransformation glideBlurringTransformation = new GlideBlurringTransformation(this, blur, (float) greyScale / 100, dim);

        expandedImageView.setImageBitmap(glideBlurringTransformation.transform(BitmapResource.obtain(customizedBitmap, Glide.get(this).getBitmapPool()), 200, 100).get());


    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.blurringValue:
                changeImageValues(blurringValue.getProgress(), dimValue.getProgress(), greyScaleValue.getProgress());
                WallPaperUtils.setCurrentBlurring(blurringValue.getProgress());
                break;
            case R.id.dimValue:
                changeImageValues(blurringValue.getProgress(), dimValue.getProgress(), greyScaleValue.getProgress());
                WallPaperUtils.setCurrentDim(dimValue.getProgress());
                break;
            case R.id.greyValue:
                changeImageValues(blurringValue.getProgress(), dimValue.getProgress(), greyScaleValue.getProgress());
                WallPaperUtils.setCurrentGreyScale((float)greyScaleValue.getProgress() / 100);
                break;

        }
    }
}
