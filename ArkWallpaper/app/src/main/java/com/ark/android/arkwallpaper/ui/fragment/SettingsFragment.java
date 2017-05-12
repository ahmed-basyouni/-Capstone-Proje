package com.ark.android.arkwallpaper.ui.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.ark.android.arkanalytics.GATrackerManager;
import com.ark.android.arkwallpaper.Constants;
import com.ark.android.arkwallpaper.R;
import com.ark.android.arkwallpaper.presenter.contract.HomeContract;
import com.ark.android.arkwallpaper.ui.activity.CustomizeWallpaperActivity;
import com.ark.android.arkwallpaper.ui.activity.LastImageInfoActivity;
import com.ark.android.arkwallpaper.utils.WallPaperUtils;
import com.ark.android.arkwallpaper.utils.uiutils.GlideBlurringTransformation;
import com.ark.android.arkwallpaper.utils.uiutils.GlideContentProviderLoader;
import com.ark.android.gallerylib.ChooserActivity;
import com.ark.android.gallerylib.data.GallaryDataBaseContract;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 *
 * Created by ahmed-basyouni on 4/22/17.
 */

public class SettingsFragment extends BaseFragment  implements SharedPreferences.OnSharedPreferenceChangeListener
        , CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    @BindView(R.id.displayModeFill)
    RadioButton displayModeFill;
    @BindView(R.id.displayModeFit)
    RadioButton displayModeFit;
    @BindView(R.id.imageScrolling)
    CheckBox imageScrolling;
    @BindView(R.id.randomOrder)
    CheckBox randomOrder;
    @BindView(R.id.lastWallpaperContainer)
    CardView customizationContainer;
    @BindView(R.id.lastSetImage)
    ImageView lastSetImage;
    @BindView(R.id.displayModeOption)
    RadioGroup displayModeOption;
    private Bitmap mBitmap;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        ButterKnife.bind(this, rootView);
        checkWallpaper();
        checkCheckBoxesState();
        setCheckBoxesListener();
        customizationContainer.setOnClickListener(this);
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
        return rootView;
    }

    private void setCheckBoxesListener() {
        imageScrolling.setOnCheckedChangeListener(this);
        randomOrder.setOnCheckedChangeListener(this);
        displayModeOption.setOnCheckedChangeListener(this);
    }

    private void checkCheckBoxesState() {
        randomOrder.setChecked(WallPaperUtils.isRandomOrder());
        imageScrolling.setChecked(WallPaperUtils.isScrolling());
        Boolean fit = WallPaperUtils.getDisplayMode() == Constants.DISPLAY_MODE.FIT.ordinal();
        if(fit)
            displayModeFit.setChecked(true);
        else
            displayModeFill.setChecked(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key != null && (key.equals(Constants.CURRENT_WALLPAPER_KEY)|| key.equals(Constants.GREY_SCALE_KEY) ||
                key.equals(Constants.BLURRING_KEY) || key.equals(Constants.DIM_KEY))) {
            mBitmap = null;
            checkWallpaper();
        }
    }

    private void checkWallpaper() {
        if (WallPaperUtils.getCurrentWallpaper() != null) {

            customizationContainer.setVisibility(View.VISIBLE);
            if(mBitmap == null){
                Glide.with(getActivity())
                        .using(new GlideContentProviderLoader(getActivity()))
                        .load(Uri.parse(WallPaperUtils.getCurrentWallpaper()))
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(new SimpleTarget<Bitmap>(500, 500) {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                mBitmap = resource;
                                GlideBlurringTransformation glideBlurringTransformation = new GlideBlurringTransformation(getActivity(), WallPaperUtils.getCurrentBlurring(), (float) WallPaperUtils.getCurrentGreyScale(), WallPaperUtils.getCurrentDim());

                                lastSetImage.setImageBitmap(glideBlurringTransformation.transform(BitmapResource.obtain(resource, Glide.get(getActivity()).getBitmapPool()), 200, 100).get());
                            }
                        });
            }else{
                GlideBlurringTransformation glideBlurringTransformation = new GlideBlurringTransformation(getActivity(), WallPaperUtils.getCurrentBlurring(), WallPaperUtils.getCurrentGreyScale(), WallPaperUtils.getCurrentDim());

                lastSetImage.setImageBitmap(glideBlurringTransformation.transform(BitmapResource.obtain(mBitmap, Glide.get(getActivity()).getBitmapPool()), 200, 100).get());

            }

        } else {
            customizationContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.imageScrolling:
                changeScrollingOption(isChecked);
                GATrackerManager.getInstance().trackEvent(getString(R.string.settings)
                        , getString(R.string.changeScrolling),
                        String.valueOf(isChecked));
                break;
            case R.id.randomOrder:
                changeRandomOption(isChecked);
                GATrackerManager.getInstance().trackEvent(getString(R.string.settings)
                        , getString(R.string.changeOrder),
                        String.valueOf(isChecked));
                break;
        }
    }

    private void changeDisplayOption(Constants.DISPLAY_MODE display_mode) {
        WallPaperUtils.setDisplayMode(display_mode);
    }

    private void changeRandomOption(boolean isChecked) {
        WallPaperUtils.setRandomOrder(isChecked);
    }

    private void changeScrollingOption(boolean isChecked) {
        WallPaperUtils.setScrolling(isChecked);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId){
            case R.id.displayModeFit:
                changeDisplayOption(Constants.DISPLAY_MODE.FIT);
                GATrackerManager.getInstance().trackEvent(getString(R.string.settings)
                        , getString(R.string.changeDisplayMode),
                        getString(R.string.fit));
                break;
            case R.id.displayModeFill:
                changeDisplayOption(Constants.DISPLAY_MODE.FILL);
                GATrackerManager.getInstance().trackEvent(getString(R.string.settings)
                        , getString(R.string.changeDisplayMode),
                        getString(R.string.fill));
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.lastWallpaperContainer){
            Intent intent = new Intent(getActivity(), CustomizeWallpaperActivity.class);

            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(getActivity(), lastSetImage, getString(R.string.expandedImageView));
//            startActivity(intent, options.toBundle());
            getActivity().startActivityFromFragment(this, intent, LastImageInfoActivity.REQUEST_ID, options.toBundle());
        }

    }

    @Override
    protected String getFragmentTitle() {
        return getString(R.string.settingsFragment);
    }
}
