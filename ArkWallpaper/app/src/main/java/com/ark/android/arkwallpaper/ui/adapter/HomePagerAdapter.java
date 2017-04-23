package com.ark.android.arkwallpaper.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.ark.android.arkwallpaper.R;
import com.ark.android.arkwallpaper.presenter.contract.HomeContract;
import com.ark.android.arkwallpaper.ui.fragment.ChangeFragment;
import com.ark.android.arkwallpaper.ui.fragment.SettingsFragment;

import java.util.List;

/**
 * Created by ahmed-basyouni on 4/22/17.
 */

public class HomePagerAdapter extends FragmentPagerAdapter {

    private List<HomeContract.OnHomePagerChange> fragmentsList;

    public HomePagerAdapter(FragmentManager fm, List<HomeContract.OnHomePagerChange> fragmentsList) {
        super(fm);
        this.fragmentsList = fragmentsList;
    }

    @Override
    public Fragment getItem(int position) {
        return (Fragment) fragmentsList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentsList.size();
    }

    public List<HomeContract.OnHomePagerChange> getFragmentsList() {
        return fragmentsList;
    }
}
