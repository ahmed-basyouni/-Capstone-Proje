package com.ark.android.arkwallpaper.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by ahmed-basyouni on 4/22/17.
 */

public class HomePagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragmentsList;

    public HomePagerAdapter(FragmentManager fm, List<Fragment> fragmentsList) {
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

    public List<Fragment> getFragmentsList() {
        return fragmentsList;
    }
}
