package com.ark.android.arkwallpaper.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import com.ark.android.arkwallpaper.R;
import com.ark.android.arkwallpaper.ui.customviews.AnimatedSvgView;
import com.ark.android.arkwallpaper.utils.LogoPaths;
import com.ark.android.arkwallpaper.utils.uiutils.GlideBlurringTransformation;
import com.bumptech.glide.Glide;

public class SplashActivity extends AppCompatActivity {

    private View mSubtitleView;
    private AnimatedSvgView mLogoView;
    private float mInitialLogoOffset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        mInitialLogoOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16,
                getResources().getDisplayMetrics());
        mSubtitleView = findViewById(R.id.logo_subtitle);
        ImageView splashImageView = (ImageView) findViewById(R.id.splashBg);
        Glide.with(this)
                .load(R.drawable.splash)
                .bitmapTransform(new GlideBlurringTransformation(this))
                .into(splashImageView);
        mLogoView = (AnimatedSvgView) findViewById(R.id.animated_logo);
        mLogoView.setGlyphStrings(LogoPaths.GLYPHS);
        Handler mHandler = new Handler();
        mLogoView.setOnStateChangeListener(new AnimatedSvgView.OnStateChangeListener() {
            @Override
            public void onStateChange(int state) {
                if (state == AnimatedSvgView.STATE_FILL_STARTED) {
                    mSubtitleView.setAlpha(0);
                    mSubtitleView.setVisibility(View.VISIBLE);
                    mSubtitleView.setTranslationY(-mSubtitleView.getHeight());
                    float originalScale = mSubtitleView.getScaleY();
                    mSubtitleView.setScaleY(mSubtitleView.getScaleY() * 1.5f);

                    AnimatorSet set = new AnimatorSet();
                    Interpolator interpolator = new OvershootInterpolator();
                    Interpolator bounceInterpolator = new BounceInterpolator();
                    ObjectAnimator a1 = ObjectAnimator.ofFloat(mLogoView, View.TRANSLATION_Y, 0);
                    ObjectAnimator a2 = ObjectAnimator.ofFloat(mSubtitleView,
                            View.TRANSLATION_Y, 0);
                    ObjectAnimator a3 = ObjectAnimator.ofFloat(mSubtitleView, View.ALPHA, 1);
                    ObjectAnimator a4 = ObjectAnimator.ofFloat(mSubtitleView, View.SCALE_Y, originalScale);
                    a1.setInterpolator(interpolator);
                    a2.setInterpolator(interpolator);
                    a4.setInterpolator(bounceInterpolator);
                    set.setDuration(700).playTogether(a1, a2, a3,a4);
                    set.start();

                    set.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                            SplashActivity.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            SplashActivity.this.finish();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                }
            }
        });
        reset();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                start();
            }
        }, 700);
    }

    public void start() {
        mLogoView.start();
    }

    public void reset() {
        mLogoView.reset();
        mLogoView.setTranslationY(mInitialLogoOffset);
        mSubtitleView.setVisibility(View.INVISIBLE);
    }

}
