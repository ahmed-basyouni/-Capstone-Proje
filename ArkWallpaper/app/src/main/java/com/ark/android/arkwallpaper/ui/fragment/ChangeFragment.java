package com.ark.android.arkwallpaper.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ark.android.arkwallpaper.R;
import com.ark.android.arkwallpaper.presenter.contract.HomeContract;

/**
 * Created by ahmed-basyouni on 4/17/17.
 */

public class ChangeFragment extends Fragment implements HomeContract.OnHomePagerChange{

    @Override
    public void onFragmentSelected() {

    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change, container, false);
    }
}
