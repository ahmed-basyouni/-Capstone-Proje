package com.ark.android.onlinesourcelib;

/**
 * Created by ahmed-basyouni on 4/25/17.
 */

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/** Service to handle sync requests.
 *
 * <p>This service is invoked in response to Intents with action android.content.SyncAdapter, and
 * returns a Binder connection to SyncAdapter.
 *
 * <p>For performance, only one sync adapter will be initialized within this application's context.
 *
 * <p>Note: The SyncService itself is not notified when a new sync occurs. It's role is to
 * manage the lifecycle of our {@link } and provide a handle to said SyncAdapter to the
 * OS on request.
 */
public class SyncService extends Service {
    private static final String TAG = "SyncService";

    private static final Object sSyncAdapterLock = new Object();
    private static FiveHundredSyncAdapter sSyncAdapter = null;

    /**
     * Thread-safe constructor, creates static {@link } instance.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service created");
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new FiveHundredSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    /**
     * Logging-only destructor.
     */
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroyed");
    }

    /**
     * Return Binder handle for IPC communication with {@link FiveHundredSyncAdapter}.
     *
     * <p>New sync requests will be sent directly to the SyncAdapter using this channel.
     *
     * @param intent Calling intent
     * @return Binder handle for {@link FiveHundredSyncAdapter}
     */
    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}