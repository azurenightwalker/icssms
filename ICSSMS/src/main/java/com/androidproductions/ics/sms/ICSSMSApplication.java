package com.androidproductions.ics.sms;

import android.app.Application;
import com.androidproductions.ics.sms.data.ImageCache;
import com.androidproductions.ics.sms.preferences.ConfigurationHelper;

/**
 * Created by Scott on 31/08/13.
 */
public class ICSSMSApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ImageCache.initInstance(Constants.CACHE_SIZE,getApplicationContext());
        ConfigurationHelper.initInstance(getApplicationContext());
    }
}
