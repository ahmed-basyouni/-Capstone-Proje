package com.ark.android.arkwallpaper.ui.activity;

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
import com.ark.android.arkwallpaper.ui.BaseActivity;
import com.ark.android.arkwallpaper.ui.customviews.SpringIndicator;
import com.ark.android.arkwallpaper.ui.fragment.ChangeFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 *
 * Created by ahmed-basyouni on 4/17/17.
 */

public class HomeActivity extends BaseActivity {

    @BindView(R.id.toolbar) Toolbar toolBar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        ButterKnife.bind(this);
        setSupportActionBar(toolBar);
        SpringIndicator springIndicator = (SpringIndicator) findViewById(R.id.indicator);
        ViewPager viewPager = (ViewPager) findViewById(R.id.homePager);
        viewPager.setAdapter(new HomePagerAdapter(getSupportFragmentManager()));
        springIndicator.setViewPager(viewPager);
    }


//    private Bitmap generateSourceImage() {
//        mImageFillPaint.setColor(getResources().getColor(R.color.colorPrimaryDark));
//        mImageFillPaint.setAntiAlias(true);
//        Bitmap bitmap = Bitmap.createBitmap(600, 600,
//                Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        mTempRectF.set(0, 0, 600, 600);
//        canvas.drawOval(mTempRectF, mImageFillPaint);
//        Typeface plain = Typeface.createFromAsset(getAssets(), "fonts/brush.ttf");
//        Paint paint = new Paint();
//        paint.setTypeface(plain);
//        paint.setColor(Color.WHITE);
//        paint.setTextSize(100);
//        float textWidth = paint.measureText("Change", 0, "Change".length());
//        int xPos = (canvas.getWidth() / 2) - ((int)textWidth/2);
//        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ;
//        //((textPaint.descent() + textPaint.ascent()) / 2) is the distance from the baseline to the center.
//
//        canvas.drawText("Change", xPos,yPos, paint);
//        return bitmap;
//    }


    public class HomePagerAdapter extends FragmentPagerAdapter{

        public HomePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return new ChangeFragment();
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return getString(R.string.change_tab);
                case 1:
                    return getString(R.string.albums_tab);
                case 2:
                    return getString(R.string.settings_tab);
            }
            return "";
        }
    }
}
