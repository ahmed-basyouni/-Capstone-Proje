package com.ark.android.arkwallpaper.utils.uiutils;

/**
 * Created by ahmed-basyouni on 4/17/17.
 */


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.renderscript.RSRuntimeException;

import com.ark.android.arkanalytics.GATrackerManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.crashlytics.android.Crashlytics;

public class GlideBlurringTransformation implements Transformation<Bitmap> {

    private static final float DEFAULT_BRIGHT = 0;
    private static int MAX_RADIUS = 25;
    private static int DEFAULT_DOWN_SAMPLING = 1;
    private static float DEFAULT_DESATURATED = 0.0f;
    private final float mdesaturateAmount;
    private final float mBright;

    private Context mContext;
    private BitmapPool mBitmapPool;

    private int mRadius;
    private int mSampling;

    public GlideBlurringTransformation(Context context) {
        this(context, Glide.get(context).getBitmapPool(), MAX_RADIUS, DEFAULT_DOWN_SAMPLING, DEFAULT_DESATURATED, DEFAULT_BRIGHT);
    }

    public GlideBlurringTransformation(Context context, BitmapPool pool) {
        this(context, pool, MAX_RADIUS, DEFAULT_DOWN_SAMPLING, DEFAULT_DESATURATED,DEFAULT_BRIGHT);
    }

    public GlideBlurringTransformation(Context context, BitmapPool pool, int radius) {
        this(context, pool, radius, DEFAULT_DOWN_SAMPLING, DEFAULT_DESATURATED,DEFAULT_BRIGHT);
    }

    public GlideBlurringTransformation(Context context, int radius) {
        this(context, Glide.get(context).getBitmapPool(), radius, DEFAULT_DOWN_SAMPLING, DEFAULT_DESATURATED,DEFAULT_BRIGHT);
    }

    public GlideBlurringTransformation(Context context, int radius, float desaturateAmount) {
        this(context, Glide.get(context).getBitmapPool(), radius, DEFAULT_DOWN_SAMPLING, desaturateAmount,DEFAULT_BRIGHT);
    }

    public GlideBlurringTransformation(Context context, int radius, float desaturateAmount, float bright) {
        this(context, Glide.get(context).getBitmapPool(), radius, DEFAULT_DOWN_SAMPLING, desaturateAmount,bright);
    }

    public GlideBlurringTransformation(Context context, int radius, int sampling) {
        this(context, Glide.get(context).getBitmapPool(), radius, sampling, DEFAULT_DESATURATED, DEFAULT_BRIGHT);
    }

    public GlideBlurringTransformation(Context context, BitmapPool pool, int radius, int sampling, float desaturateAmount, float bright) {
        mContext = context.getApplicationContext();
        mBitmapPool = pool;
        mRadius = radius;
        mdesaturateAmount = desaturateAmount;
        mSampling = sampling;
        mBright = bright;
    }

    @Override
    public Resource<Bitmap> transform(Resource<Bitmap> resource, int outWidth, int outHeight) {
        Bitmap source = resource.get();

        int width = source.getWidth();
        int height = source.getHeight();
        int scaledWidth = width / mSampling;
        int scaledHeight = height / mSampling;

        Bitmap bitmap = mBitmapPool.get(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        canvas.scale(1 / (float) mSampling, 1 / (float) mSampling);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(source, 0, 0, paint);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            try {
                ImageBlurrer imageBlurrer = new ImageBlurrer(mContext, bitmap);
                bitmap = imageBlurrer.blurBitmap(mRadius, mdesaturateAmount, -mBright);
                imageBlurrer.destroy();
            } catch (RSRuntimeException e) {
                GATrackerManager.getInstance().trackException(e);
//                Crashlytics.logException(e);
                bitmap = FastBlur.blur(bitmap, mRadius, true);
            }
        } else {
            bitmap = FastBlur.blur(bitmap, mRadius, true);
        }

        return BitmapResource.obtain(bitmap, mBitmapPool);
    }

    @Override
    public String getId() {
        return "BlurTransformation(radius=" + mRadius + ", sampling=" + mSampling + ")";
    }
}
