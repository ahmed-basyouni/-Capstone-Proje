package com.ark.android.arkwallpaper.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.ark.android.arkwallpaper.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 *
 * Created by ahmed-basyouni on 4/17/17.
 */

public class BaseActivity extends AppCompatActivity {

    private FrameLayout mContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.base_activity);
        mContainer = (FrameLayout) findViewById(R.id.container);

        AdView mAdView = (AdView) findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public void setContentView(int layoutResID) {
        View content = LayoutInflater.from(this).inflate(layoutResID,
                mContainer, false);
        mContainer.addView(content);
    }

    @Override
    public void setContentView(View view) {
        mContainer.addView(view);
    }
}
