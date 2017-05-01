/*
 *    Copyright (C) 2010 Stewart Gateley <birbeck@gmail.com>
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ark.android.arkwallpaper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Movie;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.ark.android.arkwallpaper.utils.WallPaperUtils;
import com.ark.android.arkwallpaper.utils.uiutils.BitmapUtil;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class WallpaperSlideshow extends WallpaperService {

    public static final String TAG = "Wallpaper Slideshow";
    public static final String SHARED_PREFS_NAME = "preferences";

    private final Handler mHandler = new Handler();
    private Movie movie;
    private float mXSteps;


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Engine onCreateEngine() {
        return new WallpaperEngine(this);
    }

    class WallpaperEngine extends Engine
            implements OnSharedPreferenceChangeListener {

        // Canvas stuff
        private final Paint mPaint = new Paint();
        private final Matrix mScaler = new Matrix();
        private final Context mContext;
        private int mWidth = 0;
        private int mHeight = 0;
        private int mMinWidth = 0;
        private int mMinHeight = 0;
        private float mXOffset = 0;
        private float mYOffset = 0;
        private boolean mVisible = false;
        private Bitmap mBitmap = null;
        private String mBitmapPath = null;
        private int mIndex = -1;
        private long mLastDrawTime = 0;
        private boolean mStorageReady = true;
        private float mHomePagesCount = 1;

        // Double tap listener
        private final GestureDetector doubleTapDetector;
        private BroadcastReceiver mReceiver;


        private boolean mRandom = false;
        private boolean mRotate = false;
        private boolean mScroll = true;
        private boolean mRecurse = false;
        private boolean mTouchEvents = true;
        private boolean mScreenWake = false;

        private int mNyanDuration;
        float mScaleX;
        float mScaleY;
        int mWhen;
        long mStart;

        private final Runnable mWorker = new Runnable() {
            public void run() {
                    drawFrame();
            }
        };
        private boolean isGif;

        WallpaperEngine(Context context) {
            final Paint paint = mPaint;
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setAntiAlias(true);
            paint.setTextSize(18f);
            mContext = context;


            doubleTapDetector = new GestureDetector(WallpaperSlideshow.this, new SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    if (mTouchEvents) {
                        mLastDrawTime = 0;
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
                if(intent.getBooleanExtra(WallPaperUtils.FORCE_UPDATE, false)){
                    movie = null;
                    mBitmapPath = intent.getStringExtra(WallPaperUtils.FORCE_UPDATE_URI);
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
                mLastDrawTime = 0;
                drawFrame();
            }
        };

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);

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
            mReceiver = new BroadcastReceiver() {
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
                        mHandler.removeCallbacks(mWorker);
                    }
                }
            };
            registerReceiver(mReceiver, filter);

            registerReceiver(changeWallpaperReceiver, new IntentFilter(WallPaperUtils.CHANGE_CURRENT_WALLPAPER_ACTION));
            registerReceiver(changeAlbumReceiver, new IntentFilter(WallPaperUtils.CHANGE_CURRENT_ALBUM_ACTION));

            // Register receiver for screen on events
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

    		/* mStorageReady = (Environment.getExternalStorageState() ==
                Environment.MEDIA_MOUNTED || Environment.getExternalStorageState() ==
        			Environment.MEDIA_CHECKING); */
            setTouchEventsEnabled(mStorageReady);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mHandler.removeCallbacks(mWorker);
            unregisterReceiver(changeWallpaperReceiver);
            unregisterReceiver(changeAlbumReceiver);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            mVisible = visible;
            if (visible) {
                drawFrame();
            } else {
                mHandler.removeCallbacks(mWorker);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format,
                                     int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            mWidth = width;
            mHeight = height;
            mMinWidth = width * 2; // cheap hack for scrolling
            mMinHeight = height;
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
            mHandler.removeCallbacks(mWorker);
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset,
                                     float xStep, float yStep, int xPixels, int yPixels) {
            mXOffset = xOffset;
            mYOffset = yOffset;
            mXSteps = xStep;
            drawFrame();
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            this.doubleTapDetector.onTouchEvent(event);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                              String key) {
            final Resources res = getResources();

//            if(key != null && key.equals(WallPaperUtils.CURRENT_ALBUM_KEY)){
//                mLastDrawTime = 0;
//                drawFrame();
//            }


//    		if (key == null) {
//    			mFolder = sharedPreferences.getString(
//    					res.getString(R.string.preferences_folder_key),
//						Environment.getExternalStorageDirectory()
//								.getAbsolutePath() + "/DCIM/100MEDIA");
//    			mDuration = Integer.valueOf(sharedPreferences.getString(
//    					res.getString(R.string.preferences_duration_key),
//    					res.getString(R.string.preferences_duration_default)));
//    			mRandom = sharedPreferences.getBoolean(
//    					res.getString(R.string.preferences_random_key),
//    					Boolean.valueOf(res.getString(R.string.preferences_random_default)));
//    			mRotate = sharedPreferences.getBoolean(
//    					res.getString(R.string.preferences_rotate_key),
//    					Boolean.valueOf(res.getString(R.string.preferences_rotate_default)));
//    			mScroll = sharedPreferences.getBoolean(
//    					res.getString(R.string.preferences_scroll_key),
//    					Boolean.valueOf(res.getString(R.string.preferences_scroll_default)));
//    			mRecurse = sharedPreferences.getBoolean(
//    					res.getString(R.string.preferences_recurse_key),
//    					Boolean.valueOf(res.getString(R.string.preferences_recurse_default)));
//    			mTouchEvents = sharedPreferences.getBoolean(
//    					res.getString(R.string.preferences_doubletap_key),
//    					Boolean.valueOf(res.getString(R.string.preferences_doubletap_default)));
//    			mScreenWake = sharedPreferences.getBoolean(
//    					res.getString(R.string.preferences_screen_awake_key),
//    					Boolean.valueOf(res.getString(R.string.preferences_screen_awake_default)));
//                mLastDrawTime = 0;
//    		} else if (key.equals(res.getString(R.string.preferences_folder_key))) {
//    			mFolder = sharedPreferences.getString(key,
//    					res.getString(R.string.preferences_folder_default));
//    			mIndex = -1;
//                mLastDrawTime = 0;
//    		} else if (key.equals(res.getString(R.string.preferences_duration_key))) {
//    			mDuration = Integer.parseInt(sharedPreferences.getString(key,
//    					res.getString(R.string.preferences_duration_default)));
//    		} else if (key.equals(res.getString(R.string.preferences_random_key))) {
//    			mRandom = sharedPreferences.getBoolean(key,
//    					Boolean.valueOf(res.getString(R.string.preferences_random_default)));
//    		} else if (key.equals(res.getString(R.string.preferences_rotate_key))) {
//    			mRotate = sharedPreferences.getBoolean(key,
//    					Boolean.valueOf(res.getString(R.string.preferences_rotate_default)));
//    		} else if (key.equals(res.getString(R.string.preferences_scroll_key))) {
//    			mScroll = sharedPreferences.getBoolean(key,
//    					Boolean.valueOf(res.getString(R.string.preferences_scroll_default)));
//    			if (mScroll == true) {
//    				mLastDrawTime = 0;
//    			}
//    		} else if (key.equals(res.getString(R.string.preferences_recurse_key))) {
//    			mRecurse = sharedPreferences.getBoolean(key,
//    					Boolean.valueOf(res.getString(R.string.preferences_recurse_default)));
//    		} else if (key.equals(res.getString(R.string.preferences_doubletap_key))) {
//    			mTouchEvents = sharedPreferences.getBoolean(key,
//    					Boolean.valueOf(res.getString(R.string.preferences_doubletap_default)));
//    		} else if (key.equals(res.getString(R.string.preferences_screen_awake_key))) {
//    			mScreenWake = sharedPreferences.getBoolean(key,
//    					Boolean.valueOf(res.getString(R.string.preferences_screen_awake_default)));
//    		}
        }

        void tick() {
            if (mWhen == -1L) {
                mWhen = 0;
                mStart = SystemClock.uptimeMillis();
            } else {
                long mDiff = SystemClock.uptimeMillis() - mStart;
                mWhen = (int) (mDiff % mNyanDuration);
            }
        }

        void drawFrame() {

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

                // Do we need to get a new image?
                boolean getImage = false;
                if (mBitmapPath == null || (mBitmap == null && !isGif)) {
                    getImage = true;
                } else if (mLastDrawTime == 0) {
                    getImage = true;
                }

                // Get image to draw
                if (getImage) {
                    movie = null;
                    // Read file to bitmap
                    mBitmapPath = WallPaperUtils.getRandomImage();
                    if (mBitmapPath == null || mBitmapPath.isEmpty())
                        throw new NoImagesInFolderException();
                    InputStream stream = getContentResolver().openInputStream(Uri.parse(mBitmapPath));
                    if (getFileExt(mBitmapPath).equalsIgnoreCase("gif")) {
                        isGif = true;
                        if (movie == null) {
                            movie = Movie.decodeStream(stream);
                            mWhen = -1;
                        }
                        mNyanDuration = movie.duration();
                        float prec = movie.width() / mWidth;
                        mScaleX = 3f;
                        mScaleY = 3f;
//						}
                    } else {
                        isGif = false;
                        movie = null;
                        mBitmap = getFillBitmap(mBitmapPath);
                    }
                    // Save the current time
                    mLastDrawTime = System.currentTimeMillis();
                } else if (mBitmap != null && mBitmap.isRecycled()) {
                    if (getFileExt(mBitmapPath).equalsIgnoreCase("gif")) {
                        InputStream stream = getContentResolver().openInputStream(Uri.parse(mBitmapPath));
                        isGif = true;
                        if (movie == null) {
                            movie = Movie.decodeStream(stream);
                            mWhen = -1;
                        }
                        mNyanDuration = movie.duration();
                        float prec = movie.width() / mWidth;
                        mScaleX = 3f;
                        mScaleY = 3f;
//						}
                    } else {
                        isGif = false;
                        movie = null;
                        mBitmap = getFillBitmap(mBitmapPath);
                    }
                }
            } catch (NoImagesInFolderException noie) {
                c.drawColor(Color.BLACK);
                c.translate(0, 30);
                c.drawText("No photos found in selected folder, ",
                        c.getWidth() / 2.0f, (c.getHeight() / 2.0f) - 15, mPaint);
                c.drawText("press Settings to select a folder...",
                        c.getWidth() / 2.0f, (c.getHeight() / 2.0f) + 15, mPaint);
                holder.unlockCanvasAndPost(c);
                return;
            } catch (RuntimeException re) {
                holder.unlockCanvasAndPost(c);
                return;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                c.drawColor(Color.BLACK);
                c.translate(0, 30);
                c.drawText("No photos found in selected folder, ",
                        c.getWidth() / 2.0f, (c.getHeight() / 2.0f) - 15, mPaint);
                c.drawText("press Settings to select a folder...",
                        c.getWidth() / 2.0f, (c.getHeight() / 2.0f) + 15, mPaint);
                holder.unlockCanvasAndPost(c);
            }

            try {
                if (c != null) {
                    int xPos;
                    int yPos;
                    if (mScroll) {
                        xPos = 0 - (int) (mWidth * mXOffset);
                        yPos = 0;
                    } else {
                        xPos = 0;
                        yPos = 0;
                    }
                    try {
                        if (!isGif) {
                            c.drawColor(Color.BLACK);
//							mHomePagesCount = (1/mXSteps) + 1;
//							int newWidth = (int)(mWidth * mHomePagesCount);
//							float ratio = 0;
//							if(mBitmap.getWidth() > newWidth)
//								ratio = ((float)mBitmap.getWidth()/ (float)newWidth);
//							else
//								ratio = newWidth / mBitmap.getWidth();
//
//							int newHeight = (int)(mBitmap.getHeight() / ratio);
//
//							mBitmap = Bitmap.createScaledBitmap(mBitmap, newWidth, newHeight, true);
//
//							Rect src = new Rect(xPos,yPos,mBitmap.getWidth()-1, mBitmap.getHeight()-1);
//							Rect dest = new Rect(xPos,yPos,newWidth-1, newHeight-1);
//							c.drawBitmap(mBitmap, src, dest, null);

//							c.scale(scale ,hScale);
                            float scale = (float)mBitmap.getWidth() / (float) mWidth;
//                            (mHeight / 2 - ((float) mBitmap.getHeight() * scale / 2)) / scale
                            c.drawBitmap(mBitmap, xPos, yPos , mPaint);
                        } else {
                            c.drawColor(Color.BLACK);
                            tick();
                            c.save();
                            float scale = (float) mWidth / (float) movie.width();
                            c.scale(scale, scale);
                            movie.setTime(mWhen);
                            movie.draw(c, 0, (mHeight / 2 - ((float) movie.height() * scale / 2)) / scale);
                            c.restore();

                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            } finally {
                if (c != null) {
                    holder.unlockCanvasAndPost(c);
//					movie.setTime((int) (System.currentTimeMillis() % movie.duration()));
                }
            }

            // Reschedule the next redraw
            mHandler.removeCallbacks(mWorker);
            if (mVisible && isGif) {
                mHandler.postDelayed(mWorker, 20);
            }
        }

        private Bitmap getFitBitmap(String filePath){
            mHomePagesCount = (1 / mXSteps) + 1;
//			mMinWidth = (int)(mWidth * mHomePagesCount);
            int targetWidth = (mScroll) ? mMinWidth : mWidth;
            int targetHeight = (mScroll) ? mMinHeight : mHeight;

            Bitmap bitmap = BitmapUtil.makeBitmap(WallpaperApp.getWallpaperApp(), Math.max(mMinWidth, mMinHeight),
                    mMinWidth * mMinHeight, filePath, null);

            targetHeight = bitmap.getHeight();

            if (bitmap == null) {
                return Bitmap.createBitmap(targetWidth, targetHeight,
                        Bitmap.Config.ARGB_8888);
            }


            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            // Rotate
            if (mRotate) {
                int screenOrientation = getResources().getConfiguration().orientation;
                if (width > height
                        && screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    bitmap = BitmapUtil.rotate(bitmap, 90, mScaler);
                } else if (height > width
                        && screenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                    bitmap = BitmapUtil.rotate(bitmap, -90, mScaler);
                }
            }

            // Scale bitmap
            if (width != targetWidth || height != targetHeight) {
                bitmap = BitmapUtil.transform(mScaler, bitmap,
                        targetWidth, targetHeight, true, true);
            }

            return bitmap;
        }

        private Bitmap getFillBitmap(String filePath){
            int targetWidth = (mScroll) ? mMinWidth : mWidth;
            int targetHeight = (mScroll) ? mMinHeight : mHeight;

            Bitmap bitmap = BitmapUtil.makeBitmap(WallpaperApp.getWallpaperApp(), Math.max(mMinWidth, mMinHeight),
                    mMinWidth * mMinHeight, filePath, null);

            if (bitmap == null) {
                return Bitmap.createBitmap(targetWidth, targetHeight,
                        Bitmap.Config.ARGB_8888);
            }


            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            // Rotate
            if (mRotate) {
                int screenOrientation = getResources().getConfiguration().orientation;
                if (width > height
                        && screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    bitmap = BitmapUtil.rotate(bitmap, 90, mScaler);
                } else if (height > width
                        && screenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                    bitmap = BitmapUtil.rotate(bitmap, -90, mScaler);
                }
            }

            // Scale bitmap
            if (width != targetWidth || height != targetHeight) {
                bitmap = BitmapUtil.transform(mScaler, bitmap,
                        targetWidth, targetHeight, true, true);
            }

            return bitmap;
        }

        private Bitmap getFitBitmapWithoutScroll(String filePath){
            mHomePagesCount = (1 / mXSteps) + 1;
//			mMinWidth = (int)(mWidth * mHomePagesCount);
            int targetWidth = (mScroll) ? mMinWidth : mWidth;
            int targetHeight = (mScroll) ? mMinHeight : mHeight;

            Bitmap bitmap = BitmapUtil.makeBitmap(WallpaperApp.getWallpaperApp(), Math.max(mWidth, mMinHeight),
                    mWidth * mMinHeight, filePath, null);

            targetWidth = mWidth;
            targetHeight = (int)(bitmap.getHeight()/ 1.7);

            if (bitmap == null) {
                return Bitmap.createBitmap(targetWidth, targetHeight,
                        Bitmap.Config.ARGB_8888);
            }


            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            // Rotate
            if (mRotate) {
                int screenOrientation = getResources().getConfiguration().orientation;
                if (width > height
                        && screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    bitmap = BitmapUtil.rotate(bitmap, 90, mScaler);
                } else if (height > width
                        && screenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                    bitmap = BitmapUtil.rotate(bitmap, -90, mScaler);
                }
            }

            // Scale bitmap
            if (width != targetWidth || height != targetHeight) {
                bitmap = BitmapUtil.transform(mScaler, bitmap,
                        targetWidth, targetHeight, true, true);
            }

            return bitmap;
        }

        private Bitmap getFormattedBitmap(String filePath) {
            mHomePagesCount = (1 / mXSteps) + 1;
//			mMinWidth = (int)(mWidth * mHomePagesCount);
            int targetWidth = (mScroll) ? mMinWidth : mWidth;
            int targetHeight = (mScroll) ? mMinHeight : mHeight;

            Bitmap bitmap = BitmapUtil.makeBitmap(WallpaperApp.getWallpaperApp(), Math.max(mMinWidth, mMinHeight),
                    mWidth * mMinHeight, filePath, null);

            targetWidth = mWidth;
            float scale = (float)bitmap.getWidth() / (float) mWidth;
            targetHeight = (int)(bitmap.getHeight()/ scale);

            if (bitmap == null) {
                return Bitmap.createBitmap(targetWidth, targetHeight,
                        Bitmap.Config.ARGB_8888);
            }


            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            // Rotate
            if (mRotate) {
                int screenOrientation = getResources().getConfiguration().orientation;
                if (width > height
                        && screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    bitmap = BitmapUtil.rotate(bitmap, 90, mScaler);
                } else if (height > width
                        && screenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                    bitmap = BitmapUtil.rotate(bitmap, -90, mScaler);
                }
            }

            // Scale bitmap
            if (width != targetWidth || height != targetHeight) {
                bitmap = BitmapUtil.transform(mScaler, bitmap,
                        targetWidth, targetHeight, true, true);
            }

            return bitmap;
        }


        public String getFileExt(String fileName) {
            return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
        }

        public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
            int width = bm.getWidth();
            int height = bm.getHeight();
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            // CREATE A MATRIX FOR THE MANIPULATION
            Matrix matrix = new Matrix();
            // RESIZE THE BIT MAP
            matrix.postScale(scaleWidth, scaleHeight);

            // "RECREATE" THE NEW BITMAP
            Bitmap resizedBitmap = Bitmap.createBitmap(
                    bm, 0, 0, width, height, matrix, false);
            bm.recycle();
            return resizedBitmap;
        }

        public Bitmap scaleBitmapAndKeepRation(Bitmap TargetBmp, int reqHeightInPixels, int reqWidthInPixels) {
            Matrix m = new Matrix();
            m.setRectToRect(new RectF(0, 0, TargetBmp.getWidth(), TargetBmp.getHeight()), new RectF(0, 0, reqWidthInPixels, reqHeightInPixels), Matrix.ScaleToFit.FILL);
            Bitmap scaledBitmap = Bitmap.createBitmap(TargetBmp, 0, 0, TargetBmp.getWidth(), TargetBmp.getHeight(), m, true);
            return scaledBitmap;
        }

        public Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
            int sourceWidth = source.getWidth();
            int sourceHeight = source.getHeight();

            // Compute the scaling factors to fit the new height and width, respectively.
            // To cover the final image, the final scaling will be the bigger
            // of these two.
            float xScale = (float) newWidth / sourceWidth;
            float yScale = (float) newHeight / sourceHeight;
            float scale = Math.max(xScale, yScale);

            // Now get the size of the source bitmap when scaled
            float scaledWidth = scale * sourceWidth;
            float scaledHeight = scale * sourceHeight;

            // Let's find out the upper left coordinates if the scaled bitmap
            // should be centered in the new size give by the parameters
            float left = (newWidth - scaledWidth) / 2;
            float top = (newHeight - scaledHeight) / 2;

            // The target rectangle for the new, scaled version of the source bitmap will now
            // be
            RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

            // Finally, we create a new bitmap of the specified size and draw our new,
            // scaled bitmap onto it.
            Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
            Canvas canvas = new Canvas(dest);
            canvas.drawBitmap(source, null, targetRect, null);

            return dest;
        }

    }

    class NoImagesInFolderException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    class MediaNotReadyException extends Exception {
        private static final long serialVersionUID = 1L;
    }

}