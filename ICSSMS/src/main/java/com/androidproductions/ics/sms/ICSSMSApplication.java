package com.androidproductions.ics.sms;

import android.app.Application;

import com.androidproductions.ics.sms.preferences.ConfigurationHelper;
import com.androidproductions.ics.sms.utils.LogHelper;
import com.androidproductions.libs.sms.com.androidproductions.libs.sms.readonly.ImageCache;

public class ICSSMSApplication extends Application {
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        ImageCache.initInstance(Constants.MIN_CACHE_SIZE,getApplicationContext(),R.drawable.ic_contact_picture);
    }

    @Override
    public void onTrimMemory(final int level) {
        super.onTrimMemory(level);
        if (level > TRIM_MEMORY_UI_HIDDEN)
            ImageCache.initInstance(Constants.MIN_CACHE_SIZE,getApplicationContext(),R.drawable.ic_contact_picture);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ImageCache.initInstance(Constants.CACHE_SIZE, getApplicationContext(), R.drawable.ic_contact_picture);
        ConfigurationHelper.initInstance(getApplicationContext());
        LogHelper.setDebug(LogHelper.DEBUG_LEVEL_WARNING);
    }
}
