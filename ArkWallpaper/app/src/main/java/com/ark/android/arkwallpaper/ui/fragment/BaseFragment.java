package com.ark.android.arkwallpaper.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.ark.android.arkanalytics.GATrackerManager;

/**
 *
 * Created by ahmed-basyouni on 5/8/17.
 */

public abstract class BaseFragment extends Fragment {

    protected abstract String getFragmentTitle();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GATrackerManager.getInstance().trackScreenView(getFragmentTitle());
    }
}
