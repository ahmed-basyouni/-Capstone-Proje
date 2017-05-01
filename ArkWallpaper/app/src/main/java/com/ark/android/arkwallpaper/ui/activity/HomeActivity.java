package com.ark.android.arkwallpaper.ui.activity;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 *
 * Created by ahmed-basyouni on 4/17/17.
 */

public class HomeActivity extends BaseActivity implements HomeContract.IHomeView{

    @BindView(R.id.toolbar) Toolbar toolBar;
    @BindView(R.id.indicator) SpringIndicator springIndicator;
    @BindView(R.id.homePager) ViewPager viewPager;

    public static final String FROM_SPLASH = "fromsplashScreen";

    private HomePagerAdapter homePagerAdapter;
    private int REQUEST_SET_LIVE_WALLPAPER = 543;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        ButterKnife.bind(this);
        setSupportActionBar(toolBar);
        new HomePresenter(this);

        startObservableService();

        if(!WallPaperUtils.isLiveWallpaperActive() && getIntent().getExtras().getBoolean(FROM_SPLASH, false)){
            Snackbar snackBar = Snackbar.make(findViewById(R.id.snackContainer), "This is main activity", Snackbar.LENGTH_INDEFINITE)
                    .setAction("CLOSE", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent();
                            intent.setAction(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                            String pkg = getPackageName();
                            String cls = WallpaperSlideshow.class.getCanonicalName();
                            intent.putExtra(
                                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                                    new ComponentName(pkg, cls));
//                            ComponentName component = new ComponentName("com.ark.android.arkwallpaper", "com.ark.android.arkwallpaper" + ".WallpaperSlideshow");
//                            Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
//                            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component);
                            startActivityForResult(intent, REQUEST_SET_LIVE_WALLPAPER);
                        }
                    })
                    .setActionTextColor(getResources().getColor(android.R.color.holo_red_light));

            View view = snackBar.getView();
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)view.getLayoutParams();
            params.gravity = Gravity.TOP;
            view.setLayoutParams(params);
            snackBar.show();
        }
    }

    private void startObservableService() {
        if(!WallPaperUtils.isObservableServiceRunning()){
            startService(new Intent(this, WallpaperObserverService.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
    public List<HomeContract.OnHomePagerChange> getHomeFragment() {
        return homePagerAdapter.getFragmentsList();
    }

    @Override
    public void setupPager() {
        List<HomeContract.OnHomePagerChange> fragments = new ArrayList<>();
        fragments.add(new ChangeFragment());
        fragments.add(new AlbumsFragment());
        fragments.add(new SettingsFragment());
        homePagerAdapter = new HomePagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(homePagerAdapter);
    }
}
