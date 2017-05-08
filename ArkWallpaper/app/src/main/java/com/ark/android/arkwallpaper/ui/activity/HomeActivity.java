package com.ark.android.arkwallpaper.ui.activity;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.ark.android.arkwallpaper.R;
import com.ark.android.arkwallpaper.WallpaperObserverService;
import com.ark.android.arkwallpaper.WallpaperSlideshow;
import com.ark.android.arkwallpaper.presenter.contract.HomeContract;
import com.ark.android.arkwallpaper.presenter.presenterImp.HomePresenter;
import com.ark.android.arkwallpaper.ui.BaseActivity;
import com.ark.android.arkwallpaper.ui.adapter.HomePagerAdapter;
import com.ark.android.arkwallpaper.ui.customviews.SpringIndicator;
import com.ark.android.arkwallpaper.ui.fragment.AlbumsFragment;
import com.ark.android.arkwallpaper.ui.fragment.ChangeFragment;
import com.ark.android.arkwallpaper.ui.fragment.SettingsFragment;
import com.ark.android.arkwallpaper.utils.WallPaperUtils;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 *
 * Created by ahmed-basyouni on 4/17/17.
 */

public class HomeActivity extends BaseActivity implements HomeContract.IHomeView {

    @BindView(R.id.toolbar)
    Toolbar toolBar;
    @BindView(R.id.indicator)
    SpringIndicator springIndicator;
    @BindView(R.id.homePager)
    ViewPager viewPager;
    @BindView(R.id.warningText)
    TextView warningText;

    public static final String FROM_SPLASH = "fromsplashScreen";

    private HomePagerAdapter homePagerAdapter;
    private int REQUEST_SET_LIVE_WALLPAPER = 543;
    private Spring warningSpring;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        ButterKnife.bind(this);
        setSupportActionBar(toolBar);
        new HomePresenter(this);

        startObservableService();

        SpringSystem springSystem = SpringSystem.create();

        warningSpring = springSystem.createSpring();

        // Add a listener to observe the motion of the spring.
        warningSpring.addListener(new SimpleSpringListener() {

            @Override
            public void onSpringUpdate(Spring spring) {
                // You can observe the updates in the spring
                // state by asking its current value in onSpringUpdate.
                float value = (float) spring.getCurrentValue();
                float scale = Math.abs(1f - (value * 0.5f));
                warningText.setScaleX(scale);
                warningText.setScaleY(scale);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!WallPaperUtils.isLiveWallpaperActive() && getIntent().getExtras().getBoolean(FROM_SPLASH, false)) {
            warningText.setVisibility(View.VISIBLE);
            warningSpring.setEndValue(4);
        }
    }

    private void startObservableService() {
        if (!WallPaperUtils.isObservableServiceRunning()) {
            startService(new Intent(this, WallpaperObserverService.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SET_LIVE_WALLPAPER && resultCode != Activity.RESULT_CANCELED && WallPaperUtils.isLiveWallpaperActive())
            warningText.setVisibility(View.GONE);
    }

    @Override
    public void setupIndicator() {
        TypedArray tabsdrawables = getResources().obtainTypedArray(R.array.tabs_drawables);
        springIndicator.setTabsDrawable(tabsdrawables);
        springIndicator.setViewPager(viewPager);
    }

    @Override
    public ViewPager getHomePager() {
        return viewPager;
    }

    @Override
    public SpringIndicator getIndicator() {
        return springIndicator;
    }


    @Override
    public void setupPager() {
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new ChangeFragment());
        fragments.add(new AlbumsFragment());
        fragments.add(new SettingsFragment());
        homePagerAdapter = new HomePagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(homePagerAdapter);
    }

    public void setLiveWallpaper(View view) {
        warningSpring.setEndValue(2);
        Intent intent = new Intent();
        intent.setAction(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        String pkg = getPackageName();
        String cls = WallpaperSlideshow.class.getCanonicalName();
        intent.putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(pkg, cls));
        startActivityForResult(intent, REQUEST_SET_LIVE_WALLPAPER);
    }
}
