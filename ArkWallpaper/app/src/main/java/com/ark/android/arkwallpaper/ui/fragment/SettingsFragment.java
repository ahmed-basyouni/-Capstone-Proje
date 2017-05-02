package com.ark.android.arkwallpaper.ui.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ark.android.arkwallpaper.R;
import com.ark.android.arkwallpaper.presenter.contract.HomeContract;
import com.ark.android.arkwallpaper.utils.uiutils.GlideContentProviderLoader;
import com.ark.android.gallerylib.ChooserActivity;
import com.ark.android.gallerylib.data.GallaryDataBaseContract;
import com.bumptech.glide.Glide;

/**
 * Created by ahmed-basyouni on 4/22/17.
 */

public class SettingsFragment extends Fragment implements HomeContract.OnHomePagerChange {
    @Override
    public void onFragmentSelected() {

    }

    ImageView galleryImage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_change, container, false);
//        startActivityForResult(new Intent(getActivity(), ChooserActivity.class)
//                .putExtra(ChooserActivity.ALBUM_NAME , "a")
//                .putExtra(ChooserActivity.CHOSEN_SOURCE, ChooserActivity.CHOOSE_FOLDER), 500);
        return rootView;
    }
}
