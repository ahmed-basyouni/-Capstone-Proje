package com.ark.android.arkwallpaper.presenter.presenterImp;

import android.support.v4.view.ViewPager;

import com.ark.android.arkwallpaper.presenter.contract.HomeContract;

/**
 * Created by ahmed-basyouni on 4/22/17.
 */

public class HomePresenter implements HomeContract.IHomePresenter {

    private final HomeContract.IHomeView iHomeView;

    public HomePresenter(HomeContract.IHomeView iHomeView){
        this.iHomeView = iHomeView;
        iHomeView.setupPager();
        iHomeView.setupIndicator();
        setPagerListener();
    }

    private void setPagerListener() {
        this.iHomeView.getIndicator().setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                HomePresenter.this.iHomeView.getHomeFragment().get(position).onFragmentSelected();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}
