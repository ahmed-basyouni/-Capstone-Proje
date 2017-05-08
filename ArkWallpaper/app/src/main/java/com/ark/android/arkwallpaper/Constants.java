package com.ark.android.arkwallpaper;

/**
 * Created by ahmed-basyouni on 5/2/17.
 */

public final class Constants {

    public static final String CURRENT_ALBUM_KEY = "com.ark.android.arkwallpaper.utils.currentAlbumKey";
    public static final String CURRENT_WALLPAPER_KEY = "com.ark.android.arkwallpaper.utils.currentWallpaperKey";
    public static final String CURRENT_WALLPAPER_ID_KEY =
            "com.ark.android.arkwallpaper.utils.currentWallpaperIdKey";
    public static final String WALLPAPER_ALBUM_ID =
            "com.ark.android.arkwallpaper.utils.currentAlbumIdKey";
    public static final String CHANGE_CURRENT_WALLPAPER_ACTION = "com.ark.android.arkwallpaper.utils.changeCurrentWallpaper";
    public static final String FORCE_UPDATE = "com.ark.android.arkwallpaper.utils.foceUpdate";
    public static final String FORCE_UPDATE_URI = "com.ark.android.arkwallpaper.utils.forceUpdateUri";
    public static final String CHANGE_CURRENT_ALBUM_ACTION = "com.ark.android.arkwallpaper.utils.changeCurrentAlbum";
    public static final String WALLPAPER_IS_RUNNING = "com.ark.android.arkwallpaper.utils.wallpaperIsRunning";
    public static final String CHANGE_WITH_DOUBLE_TAP_KEY = "com.ark.android.arkwallpaper.utils.changeWithDoubleTap";
    public static final String CHANGE_WITH_UNLOCK_KEY = "com.ark.android.arkwallpaper.utils.changeWithUnlock";
    public static final String CHANGE_WITH_INTERVAL_KEY = "com.ark.android.arkwallpaper.utils.changeWithInterval";
    public static final String CHANGE_Unit_KEY = "com.ark.android.arkwallpaper.utils.changeUnit";
    public static final String CHANGE_INTERVAL_KEY = "com.ark.android.arkwallpaper.utils.changeValue";
    public static final String CURRENT_PIC_INDEX_KEY = "com.ark.android.arkwallpaper.utils.currentImageIndex";
    public static final String CHANGE_SCROLLING_KEY = "com.ark.android.arkwallpaper.utils.scrolling";
    public static final String CHANGE_DISPLAY_MODE_KEY = "com.ark.android.arkwallpaper.utils.displayMode";
    public static final String RANDOM_ORDER_KEY = "com.ark.android.arkwallpaper.utils.changeOrder";
    public static final String GREY_SCALE_KEY = "com.ark.android.arkwallpaper.utils.greyScale";
    public static final String BLURRING_KEY = "com.ark.android.arkwallpaper.utils.blurring";
    public static final String DIM_KEY = "com.ark.android.arkwallpaper.utils.dim";

    public static final int ALARM_ID = 0x0a;

    public enum DISPLAY_MODE{
        FIT,
        FILL
    }

    public enum INTERVAL_MODE{
        TIME_MIN_INTERVAL(1000 * 60),
        TIME_HOUR_INTERVAL(1000 * 60 * 60),
        TIME_DAY_INTERVAL(1000 * 60 * 60 * 24);

        private final long interval;
        INTERVAL_MODE(long interval) { this.interval = interval; }
        public long getValue() { return interval; }
    }

}
