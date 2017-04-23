package com.ark.android.arkwallpaper.ui.activity;

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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;

import com.ark.android.arkwallpaper.R;
import com.ark.android.arkwallpaper.presenter.contract.HomeContract;
import com.ark.android.arkwallpaper.presenter.presenterImp.HomePresenter;
import com.ark.android.arkwallpaper.ui.BaseActivity;
import com.ark.android.arkwallpaper.ui.adapter.HomePagerAdapter;
import com.ark.android.arkwallpaper.ui.customviews.SpringIndicator;
import com.ark.android.arkwallpaper.ui.fragment.AlbumsFragment;
import com.ark.android.arkwallpaper.ui.fragment.ChangeFragment;
import com.ark.android.arkwallpaper.ui.fragment.SettingsFragment;

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

    private HomePagerAdapter homePagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        ButterKnife.bind(this);
        setSupportActionBar(toolBar);
        new HomePresenter(this);
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
