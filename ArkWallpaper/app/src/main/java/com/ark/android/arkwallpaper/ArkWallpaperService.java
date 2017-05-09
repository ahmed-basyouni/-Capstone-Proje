package com.ark.android.arkwallpaper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Movie;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.ark.android.arkanalytics.GATrackerManager;
import com.ark.android.arkwallpaper.utils.WallPaperUtils;
import com.ark.android.arkwallpaper.utils.uiutils.BitmapUtil;
import com.ark.android.arkwallpaper.utils.uiutils.GlideBlurringTransformation;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;

import java.io.FileNotFoundException;
import java.io.InputStream;

import static com.ark.android.arkwallpaper.Constants.CHANGE_CURRENT_ALBUM_ACTION;
import static com.ark.android.arkwallpaper.Constants.CHANGE_CURRENT_WALLPAPER_ACTION;
import static com.ark.android.arkwallpaper.Constants.FORCE_UPDATE;
import static com.ark.android.arkwallpaper.Constants.FORCE_UPDATE_URI;

/**
 *  here is where all the magic happen this service is used to change the wallpaper
 * Created by ahmed-basyouni on 5/9/17.
 */

public class ArkWallpaperService extends WallpaperService {

    private final Handler mHandler = new Handler();

    @Override
    public Engine onCreateEngine() {
        return new WallpaperEngine(this);
    }

    class WallpaperEngine extends Engine
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        // Canvas stuff
        private final Paint mPaint = new Paint();
        private final Matrix mScaler = new Matrix();
        private final Context mContext;
        private final SharedPreferences mPrefs;
        private int mWidth = 0;
        private int mHeight = 0;
        private int mMinWidth = 0;
        private float mXOffset = 0;
        private boolean mVisible = false;
        private Bitmap mBitmap = null;
        private String mBitmapPath = null;
        private long mLastDrawTime = 0;
        private boolean mStorageReady = true;
        private boolean mScroll = false;
        private boolean mTouchEvents = false;
        private boolean mScreenWake = false;

        private int mGifDuration;
        float mScaleX;
        float mScaleY;
        int mWhen;
        long mStart;

        private boolean isGif;
        private boolean mFit = true;
        private boolean mFill;
        private int mOriginalWidth;
        private int mOriginalHeight;
        private Movie movie;

        // Double tap listener
        private final GestureDetector doubleTapDetector;

        private final Runnable repeatDrawing = new Runnable() {
            public void run() {
                drawFrame();
            }
        };
        private BroadcastReceiver storageReceiver;

        WallpaperEngine(Context context) {
            final Paint paint = mPaint;
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setAntiAlias(true);
            paint.setTextSize(18f);
            mContext = context;

            mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            mPrefs.registerOnSharedPreferenceChangeListener(this);

            // Read the preferences
            onSharedPreferenceChanged(mPrefs, null);

            doubleTapDetector = new GestureDetector(ArkWallpaperService.this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    if (mTouchEvents) {
                        mLastDrawTime = 0;
                        showNotificationToast();
                        drawFrame();
                        return true;
                    }
                    return false;
                }
            });
        }

        BroadcastReceiver changeWallpaperReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                showNotificationToast();
                if(intent.getBooleanExtra(FORCE_UPDATE, false)){
                    movie = null;
                    mBitmapPath = intent.getStringExtra(FORCE_UPDATE_URI);
                    WallPaperUtils.changeCurrentWallpaperId(mBitmapPath);
                    if(mBitmap != null)
                        mBitmap.recycle();
                    drawFrame();
                }else {
                    mLastDrawTime = 0;
                    drawFrame();
                }
            }
        };

        BroadcastReceiver changeAlbumReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                showNotificationToast();
                mLastDrawTime = 0;
                drawFrame();
            }
        };

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);

            if(!isPreview()) {
                WallPaperUtils.setLiveWallpaperIsRunning(true);
                registerReceiver(changeWallpaperReceiver, new IntentFilter(CHANGE_CURRENT_WALLPAPER_ACTION));
                registerReceiver(changeAlbumReceiver, new IntentFilter(CHANGE_CURRENT_ALBUM_ACTION));
                registerScreenWakeReceiver();
            }

            registerStorageReceiver();
        }

        private void registerScreenWakeReceiver() {
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    System.out.println(Intent.ACTION_SCREEN_ON);
                    if (mScreenWake) {
                        mLastDrawTime = 0;
                        drawFrame();
                    }
                }
            }, new IntentFilter(Intent.ACTION_SCREEN_ON));
        }

        private void registerStorageReceiver() {
            // Register receiver for media events
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
            filter.addAction(Intent.ACTION_MEDIA_CHECKING);
            filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            filter.addAction(Intent.ACTION_MEDIA_EJECT);
            filter.addAction(Intent.ACTION_MEDIA_NOFS);
            filter.addAction(Intent.ACTION_MEDIA_REMOVED);
            filter.addAction(Intent.ACTION_MEDIA_SHARED);
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            filter.addDataScheme("file");
            storageReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action.equals(Intent.ACTION_MEDIA_MOUNTED)
                            || action.equals(Intent.ACTION_MEDIA_CHECKING)) {
                        mStorageReady = true;
                        setTouchEventsEnabled(true);
                        drawFrame();
                    } else {
                        mStorageReady = false;
                        setTouchEventsEnabled(false);
                        mHandler.removeCallbacks(repeatDrawing);
                    }
                }
            };
            registerReceiver(storageReceiver, filter);

            setTouchEventsEnabled(mStorageReady);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if(!isPreview())
                WallPaperUtils.setLiveWallpaperIsRunning(false);
            mHandler.removeCallbacks(repeatDrawing);
            mPrefs.unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            mVisible = visible;
            if (visible) {
                drawFrame();
            } else {
                mHandler.removeCallbacks(repeatDrawing);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format,
                                     int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            mWidth = width;
            mHeight = height;
            mMinWidth = width * 2; // cheap hack for scrolling
            if (mBitmap != null) {
                mBitmap.recycle();
            }
            drawFrame();
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            mLastDrawTime = 0;
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mVisible = false;
            mHandler.removeCallbacks(repeatDrawing);
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset,
                                     float xStep, float yStep, int xPixels, int yPixels) {
            mXOffset = xOffset;
            drawFrame();
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            this.doubleTapDetector.onTouchEvent(event);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(key == null) {
                mFit = WallPaperUtils.getDisplayMode() == Constants.DISPLAY_MODE.FIT.ordinal();
                mFill = WallPaperUtils.getDisplayMode() == Constants.DISPLAY_MODE.FILL.ordinal();
                mScroll = WallPaperUtils.isScrolling();
                mTouchEvents = WallPaperUtils.isChangedWithDoubleTap();
                mScreenWake = WallPaperUtils.isChangeWithUnlock();
            }else {
                if(key.equals(Constants.RANDOM_ORDER_KEY)) {
                    mLastDrawTime = 0;
                    drawFrame();
                }else if(key.equals(Constants.CHANGE_SCROLLING_KEY) && movie == null){
                    mScroll = WallPaperUtils.isScrolling();
                    if(mBitmap != null)
                        mBitmap.recycle();
                    drawFrame();
                }else if(key.equals(Constants.GREY_SCALE_KEY) || key.equals(Constants.DIM_KEY) || key.equals(Constants.BLURRING_KEY)) {
                    if (mBitmap != null)
                        mBitmap.recycle();
                    drawFrame();
                }else if(key.equals(Constants.CHANGE_DISPLAY_MODE_KEY) && movie == null){
                    if(WallPaperUtils.getDisplayMode() == Constants.DISPLAY_MODE.FIT.ordinal()){
                        mFit = true;
                        mFill = false;
                    }else{
                        mFit = false;
                        mFill = true;
                    }
                    if(mBitmap != null)
                        mBitmap.recycle();
                    drawFrame();
                }else if(key.equals(Constants.CHANGE_WITH_UNLOCK_KEY)){
                    mScreenWake = WallPaperUtils.isChangeWithUnlock();
                }else if(key.equals(Constants.CHANGE_WITH_DOUBLE_TAP_KEY)){
                    mTouchEvents = WallPaperUtils.isChangedWithDoubleTap();
                }
            }
        }

        void tick() {
            if (mWhen == -1L) {
                mWhen = 0;
                mStart = SystemClock.uptimeMillis();
            } else {
                long mDiff = SystemClock.uptimeMillis() - mStart;
                mWhen = (int) (mDiff % mGifDuration);
            }
        }

        void drawFrame(){

            final SurfaceHolder holder = getSurfaceHolder();
            Canvas c = null;

            String state = Environment.getExternalStorageState();
            if (!state.equals(Environment.MEDIA_MOUNTED) &&
                    !state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
                return;
            }

            try {
                // Lock the canvas for writing
                c = holder.lockCanvas();

                boolean loadNewImage = false;
                if (mBitmapPath == null || (mBitmap == null && !isGif)) {
                    loadNewImage = true;
                } else if (mLastDrawTime == 0) {
                    loadNewImage = true;
                }

                // Get image to draw
                if (loadNewImage) {
                    movie = null;
                    mBitmapPath = WallPaperUtils.getImages();
                    if (mBitmapPath == null || mBitmapPath.isEmpty())
                        throw new FileNotFoundException();
                    if (isGif()) {
                        configGif();
                    } else {
                        isGif = false;
                        movie = null;
                        mBitmap = getFormattedBitmap(mBitmapPath);
                    }
                    // Save the current time
                    mLastDrawTime = System.currentTimeMillis();
                } else if (mBitmap != null && mBitmap.isRecycled()) {
                    if (isGif()) {
                        configGif();
                    } else {
                        isGif = false;
                        movie = null;
                        mBitmap = getFormattedBitmap(mBitmapPath);
                    }
                }
            } catch (RuntimeException re) {
                GATrackerManager.getInstance().trackException(re);
                holder.unlockCanvasAndPost(c);
                return;
            } catch (FileNotFoundException e) {
                GATrackerManager.getInstance().trackException(e);
                e.printStackTrace();
                drawNoImagesFound(c);
                holder.unlockCanvasAndPost(c);
                return;
            }

            try {
                if (c != null) {
                    int xPos = 0;
                    int yPos;
                    if (mScroll) {
                        xPos = 10 - (int) (mWidth * mXOffset);
                    }

                    try {
                        if (!isGif) {
                            yPos = getYPos();
                            c.drawColor(Color.BLACK);
                            c.drawBitmap(mBitmap, xPos, yPos , mPaint);
                        } else {
                            c.drawColor(Color.BLACK);
                            tick();
                            c.save();
                            float scale = (float) mWidth / (float) movie.width();
                            c.scale(scale, scale);
                            movie.setTime(mWhen);
                            float movieY = (mHeight / 2 - ((float) movie.height() * scale / 2)) / scale;
                            movie.draw(c, 0, movieY);
                            c.restore();

                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            } finally {
                if (c != null) {
                    holder.unlockCanvasAndPost(c);
                }
            }

            // Reschedule the next redraw
            mHandler.removeCallbacks(repeatDrawing);
            if (mVisible && isGif) {
                mHandler.postDelayed(repeatDrawing, 50);
            }
        }

        private boolean isGif() {
            return getFileExt(mBitmapPath).equalsIgnoreCase("gif");
        }

        private int getYPos() {
            int yPos = 0;
            if(mFill || mOriginalHeight > mOriginalWidth) {
                yPos = 0;
            }else if(mFit && mScroll){
                float scale;
                if(mBitmap.getWidth() > mMinWidth)
                    scale = (float)mBitmap.getWidth() / (float) mMinWidth;
                else
                    scale = (float) mMinWidth / (float)mBitmap.getWidth();

                yPos = (int) ((mHeight / 2 - ((float) mBitmap.getHeight() * scale / 2)) / scale);
            }else if(mFit){
                float scale;
                if(mBitmap.getWidth() > mWidth)
                    scale = (float)mBitmap.getWidth() / (float) mWidth;
                else
                    scale = (float) mWidth / (float)mBitmap.getWidth();

                yPos = (int) ((mHeight / 2 - ((float) mBitmap.getHeight() * scale / 2)) / scale);
            }
            return yPos;
        }

        private void configGif() throws FileNotFoundException {
            isGif = true;
            if (movie == null) {
                InputStream stream = getContentResolver().openInputStream(Uri.parse(mBitmapPath));
                movie = Movie.decodeStream(stream);
                mWhen = -1;
            }
            mGifDuration = movie.duration();
            mScaleX = 3f;
            mScaleY = 3f;
        }

        private void drawNoImagesFound(Canvas c) {
            c.drawColor(Color.BLACK);
            c.translate(0, 30);
            c.drawText("No photos found in selected folder, ",
                    c.getWidth() / 2.0f, (c.getHeight() / 2.0f) - 15, mPaint);
            c.drawText("press Settings to select a folder...",
                    c.getWidth() / 2.0f, (c.getHeight() / 2.0f) + 15, mPaint);
        }

        private String getFileExt(String fileName) {
            return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
        }

        private void showNotificationToast() {
            Toast.makeText(ArkWallpaperService.this, getString(R.string.changeWallpaper), Toast.LENGTH_SHORT).show();
        }

        private Bitmap getFormattedBitmap(String file) {
            int targetWidth = (mScroll)? mMinWidth: mWidth;
            int targetHeight = mHeight;

            Bitmap bitmap = BitmapUtil.makeBitmap(WallpaperApp.getWallpaperApp(), Math.max(mMinWidth, mHeight),
                    mMinWidth * mHeight, file, null);

            if (bitmap == null) {
                return Bitmap.createBitmap(targetWidth, targetHeight,
                        Bitmap.Config.ARGB_8888);
            }

            if(mFit && mScroll && bitmap.getWidth() > bitmap.getHeight()){
                targetWidth = mMinWidth;
                targetHeight = bitmap.getHeight();

            }else if(mFit && bitmap.getWidth() > bitmap.getHeight()){

                targetWidth = mWidth;
                float scale;
                if(bitmap.getWidth() > mWidth)
                    scale = (float)bitmap.getWidth() / (float) ((mScroll) ? mMinWidth : mWidth);
                else
                    scale = (float) ((mScroll) ? mMinWidth : mWidth) / (float)bitmap.getWidth();

                targetHeight = (int)(bitmap.getHeight()/ scale);
            }else if(mFit && mScroll && bitmap.getHeight() > bitmap.getWidth()){
                targetWidth = mWidth;
                targetHeight = mHeight;

            }

            mOriginalWidth = bitmap.getWidth();
            mOriginalHeight = bitmap.getHeight();
            // Scale bitmap
            if (mOriginalWidth != targetWidth || mOriginalHeight != targetHeight) {
                bitmap = BitmapUtil.transform(mScaler, bitmap,
                        targetWidth, targetHeight, true, true);
            }

            GlideBlurringTransformation glideBlurringTransformation = new GlideBlurringTransformation(mContext, WallPaperUtils.getCurrentBlurring(), WallPaperUtils.getCurrentGreyScale(), WallPaperUtils.getCurrentDim());

            bitmap =  glideBlurringTransformation.transform(BitmapResource.obtain(bitmap, Glide.get(mContext).getBitmapPool()), bitmap.getWidth(), bitmap.getHeight()).get();

            return bitmap;
        }
    }
}
