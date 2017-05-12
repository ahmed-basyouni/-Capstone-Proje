/*
 * Copyright 2015 chenupt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ark.android.arkwallpaper.ui.customviews;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ark.android.arkwallpaper.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenupt@gmail.com on 2015/1/31.
 * Description : Tab layout container
 */
public class SpringIndicator extends FrameLayout {

    private static final int INDICATOR_ANIM_DURATION = 3000;

    private float acceleration = 0.5f;
    private float headMoveOffset = 0.6f;
    private float footMoveOffset = 1 - headMoveOffset;
    private float radiusMax;
    private float radiusMin;
    private float radiusOffset;

    private float textSize;
    private int textColorId;
    private int textBgResId;
    private int selectedTextColorId;
    private int indicatorColorId;
    private int indicatorColorsId;
    private int[] indicatorColorArray;

    private LinearLayout tabContainer;
    private SpringView springView;
    private ViewPager viewPager;

    private List<ImageButton> tabs;
    private List<Integer> tabsDrawable;

    private ViewPager.OnPageChangeListener delegateListener;
    private TabClickListener tabClickListener;
    private ObjectAnimator indicatorColorAnim;
    private View currentView;
    private List<String> buttonsDesc;

    public SpringIndicator(Context context) {
        this(context, null);
    }

    public SpringIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
    }

    public void setTabsDrawable(List<Integer> tabsDrawable) {
        this.tabsDrawable = tabsDrawable;
    }

    private void initAttrs(AttributeSet attrs) {
        textColorId = R.color.si_default_text_color;
        selectedTextColorId = R.color.si_default_text_color_selected;
        indicatorColorId = R.color.si_default_indicator_bg;
        textSize = getResources().getDimension(R.dimen.si_default_text_size);
        radiusMax = getResources().getDimension(R.dimen.si_default_radius_max);
        radiusMin = getResources().getDimension(R.dimen.si_default_radius_min);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SpringIndicator);
        textColorId = a.getResourceId(R.styleable.SpringIndicator_siTextColor, textColorId);
        selectedTextColorId = a.getResourceId(R.styleable.SpringIndicator_siSelectedTextColor, selectedTextColorId);
        textSize = a.getDimension(R.styleable.SpringIndicator_siTextSize, textSize);
        textBgResId = a.getResourceId(R.styleable.SpringIndicator_siTextBg, 0);
        indicatorColorId = a.getResourceId(R.styleable.SpringIndicator_siIndicatorColor, indicatorColorId);
        indicatorColorsId = a.getResourceId(R.styleable.SpringIndicator_siIndicatorColors, 0);
        radiusMax = a.getDimension(R.styleable.SpringIndicator_siRadiusMax, radiusMax);
        radiusMin = a.getDimension(R.styleable.SpringIndicator_siRadiusMin, radiusMin);
        a.recycle();

        if (indicatorColorsId != 0) {
            indicatorColorArray = getResources().getIntArray(indicatorColorsId);
        }
        radiusOffset = radiusMax - radiusMin;
    }


    public void setViewPager(final ViewPager viewPager) {
        this.viewPager = viewPager;
        initSpringView();
        setUpListener();
    }


    private void initSpringView() {
        addPointView();
        addTabContainerView();
        addTabItems();
    }

    private void addPointView() {
        springView = new SpringView(getContext());
        springView.setIndicatorColor(getResources().getColor(indicatorColorId));
        addView(springView);
    }

    public static Bitmap tintImage(Bitmap bitmap, int color) {
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        Bitmap bitmapResult = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapResult);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return bitmapResult;
    }

    private void addTabContainerView() {
        tabContainer = new LinearLayout(getContext());
        tabContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
        tabContainer.setOrientation(LinearLayout.HORIZONTAL);
        tabContainer.setGravity(Gravity.CENTER);
        addView(tabContainer);
    }

    private void addTabItems() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        tabs = new ArrayList<>();
        for (int i = 0; i < viewPager.getAdapter().getCount(); i++) {
            ImageButton imageButton = new ImageButton(getContext());
            imageButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            imageButton.setLayoutParams(layoutParams);
            imageButton.setTag(i);
            if(buttonsDesc != null)
                imageButton.setContentDescription(buttonsDesc.get(i));
            final int position = i;
            imageButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (tabClickListener == null || tabClickListener.onTabClick(position)) {
                        viewPager.setCurrentItem(position);
                    }
                }
            });
            tabs.add(imageButton);
            tabContainer.addView(imageButton);
        }
    }

    /**
     * Set current point position.
     */
    private void createPoints() {
        View view = tabs.get(viewPager.getCurrentItem());
        currentView = view;
        springView.getHeadPoint().setX(view.getX() + view.getWidth() / 2);
        springView.getHeadPoint().setY(view.getY() + view.getHeight() / 2);
        springView.getFootPoint().setX(view.getX() + view.getWidth() / 2);
        springView.getFootPoint().setY(view.getY() + view.getHeight() / 2);
        springView.animCreate();
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed || currentView == null) createPoints();
        setSelectedTextColor(viewPager.getCurrentItem());
    }


    private void setUpListener() {
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setSelectedTextColor(position);
                if (delegateListener != null) {
                    delegateListener.onPageSelected(position);
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position < tabs.size() - 1) {
                    // radius
                    float radiusOffsetHead = 0.5f;
                    if (positionOffset < radiusOffsetHead) {
                        springView.getHeadPoint().setRadius(radiusMin);
                    } else {
                        springView.getHeadPoint().setRadius(((positionOffset - radiusOffsetHead) / (1 - radiusOffsetHead) * radiusOffset + radiusMin));
                    }
                    float radiusOffsetFoot = 0.5f;
                    if (positionOffset < radiusOffsetFoot) {
                        springView.getFootPoint().setRadius((1 - positionOffset / radiusOffsetFoot) * radiusOffset + radiusMin);
                    } else {
                        springView.getFootPoint().setRadius(radiusMin);
                    }

                    // x
                    float headX = 1f;
                    if (positionOffset < headMoveOffset) {
                        float positionOffsetTemp = positionOffset / headMoveOffset;
                        headX = (float) ((Math.atan(positionOffsetTemp * acceleration * 2 - acceleration) + (Math.atan(acceleration))) / (2 * (Math.atan(acceleration))));
                    }
                    springView.getHeadPoint().setX(getTabX(position) - headX * getPositionDistance(position));
                    float footX = 0f;
                    if (positionOffset > footMoveOffset) {
                        float positionOffsetTemp = (positionOffset - footMoveOffset) / (1 - footMoveOffset);
                        footX = (float) ((Math.atan(positionOffsetTemp * acceleration * 2 - acceleration) + (Math.atan(acceleration))) / (2 * (Math.atan(acceleration))));
                    }
                    springView.getFootPoint().setX(getTabX(position) - footX * getPositionDistance(position));

                    // reset radius
                    if (positionOffset == 0) {
                        springView.getHeadPoint().setRadius(radiusMax);
                        springView.getFootPoint().setRadius(radiusMax);
                    }
                } else {
                    springView.getHeadPoint().setX(getTabX(position));
                    springView.getFootPoint().setX(getTabX(position));
                    springView.getHeadPoint().setRadius(radiusMax);
                    springView.getFootPoint().setRadius(radiusMax);
                }

                // set indicator colors
                // https://github.com/TaurusXi/GuideBackgroundColorAnimation
                if (indicatorColorsId != 0) {
                    float length = (position + positionOffset) / viewPager.getAdapter().getCount();
                    int progress = (int) (length * INDICATOR_ANIM_DURATION);
                    seek(progress);
                }

                springView.postInvalidate();
                if (delegateListener != null) {
                    delegateListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (delegateListener != null) {
                    delegateListener.onPageScrollStateChanged(state);
                }
            }
        });
    }


    private float getPositionDistance(int position) {
        float tarX = tabs.get(position + 1).getX();
        float oriX = tabs.get(position).getX();
        return oriX - tarX;
    }

    private float getTabX(int position) {
        return tabs.get(position).getX() + tabs.get(position).getWidth() / 2;
    }

    private void setSelectedTextColor(int position) {
        for(int x=0; x < tabs.size(); x++){
            if(x != position)
                ((ImageButton)tabs.get(x).findViewWithTag(x)).setImageResource(tabsDrawable.get(x));
        }
        ((ImageButton)tabs.get(position).findViewWithTag(position)).setImageBitmap(tintImage(BitmapFactory.decodeResource(getResources()
                , tabsDrawable.get(position)) , getResources().getColor(R.color.colorPrimary)));
    }

    private void createIndicatorColorAnim() {
        indicatorColorAnim = ObjectAnimator.ofInt(springView, "indicatorColor", indicatorColorArray);
        indicatorColorAnim.setEvaluator(new ArgbEvaluator());
        indicatorColorAnim.setDuration(INDICATOR_ANIM_DURATION);
    }

    private void seek(long seekTime) {
        if (indicatorColorAnim == null) {
            createIndicatorColorAnim();
        }
        indicatorColorAnim.setCurrentPlayTime(seekTime);
    }

    public List<ImageButton> getTabs() {
        return tabs;
    }

    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        this.delegateListener = listener;
    }

    public void setOnTabClickListener(TabClickListener listener) {
        this.tabClickListener = listener;
    }


    public void setButtonsDesc(List<String> buttonsDesc) {
        this.buttonsDesc = buttonsDesc;
    }
}