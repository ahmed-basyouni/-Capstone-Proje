package com.ark.android.arkwallpaper.presenter.contract;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import com.ark.android.arkwallpaper.ui.customviews.SpringIndicator;

import java.util.List;

/**
 *
 * Created by ahmed-basyouni on 4/22/17.
 */

public interface HomeContract {

    interface IHomeView{
        void setupPager();
        void setupIndicator();
        ViewPager getHomePager();
        SpringIndicator getIndicator();
        List<OnHomePagerChange> getHomeFragment();
    }

    interface IHomePresenter{

    }

    interface OnHomePagerChange{
        void onFragmentSelected();
    }
}
