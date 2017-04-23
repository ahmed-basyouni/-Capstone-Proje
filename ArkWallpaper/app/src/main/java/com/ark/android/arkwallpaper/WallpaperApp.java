package com.ark.android.arkwallpaper;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ahmed-basyouni on 4/23/17.
 */

public class WallpaperApp extends Application {

    private static WallpaperApp instance;

    //get MAx number of threads the device can handle (taken from AsyncTask.java)
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;

    private ExecutorService executorService;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        executorService = Executors.newFixedThreadPool(CORE_POOL_SIZE);
    }

    public static WallpaperApp getWallpaperApp() {
        return instance;
    }

    /**
     * a method to run any runnable in background from the pool in {@link #executorService}
     *
     * @param runnable
     */
    public void runInBackGround(Runnable runnable) {
        executorService.submit(runnable);
    }

    /**
     * a method to run any runnable on UI thread using {@link #handler}
     *
     * @param runnable
     */
    public void runOnUI(Runnable runnable) {
        handler.post(runnable);
    }
}
