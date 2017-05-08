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
    }
}
