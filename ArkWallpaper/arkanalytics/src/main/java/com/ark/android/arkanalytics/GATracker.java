package com.ark.android.arkanalytics;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by ahmed-basyouni on 5/8/17.
 */

public class GATracker {

    private static GATracker sInstance;
    private static Tracker mTracker;

    public static synchronized void initialize(Context context) {
        if (sInstance != null) {
            throw new IllegalStateException("Extra call to initialize analytics trackers");
        }

        sInstance = new GATracker(context);
        mTracker = GoogleAnalytics.getInstance(context).newTracker(R.xml.app_tracker);
        mTracker.enableExceptionReporting(true);
    }

    public static synchronized GATracker getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("Call initialize() before getInstance()");
        }

        return sInstance;
    }

    private final Context mContext;

    /**
     * Don't instantiate directly - use {@link #getInstance()} instead.
     */
    private GATracker(Context context) {
        mContext = context.getApplicationContext();
    }

    protected Context getmContext(){
        return mContext;
    }

    public synchronized Tracker get() {
        return mTracker;
    }
}
