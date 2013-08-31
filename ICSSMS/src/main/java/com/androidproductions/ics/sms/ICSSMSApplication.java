package com.androidproductions.ics.sms;

import android.app.Application;
import com.androidproductions.ics.sms.data.ImageCache;
import com.androidproductions.ics.sms.preferences.ConfigurationHelper;

public class ICSSMSApplication extends Application {
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        ImageCache.initInstance(Constants.MIN_CACHE_SIZE,getApplicationContext());
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level > TRIM_MEMORY_UI_HIDDEN)
            ImageCache.initInstance(Constants.MIN_CACHE_SIZE,getApplicationContext());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ImageCache.initInstance(Constants.CACHE_SIZE,getApplicationContext());
        ConfigurationHelper.initInstance(getApplicationContext());
    }
}
