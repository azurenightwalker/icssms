package com.androidproductions.ics.sms.service;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import com.androidproductions.ics.sms.transactions.NotificationHelper;
import com.androidproductions.libs.sms.com.androidproductions.libs.sms.constants.SmsUri;

public class NotificationService extends Service{

	private NotificationHandler mContentObserver;
	
	@Override
	public IBinder onBind(final Intent arg0) {
		return null;
	}

	@Override
    public void onCreate() {
        mContentObserver = new NotificationHandler(new Handler());
		getContentResolver().registerContentObserver(SmsUri.BASE_URI, true, mContentObserver);
    }
	
	@Override
    public void onDestroy() {
        getContentResolver().unregisterContentObserver(mContentObserver);
    }
	
	public static void startService(final Context context, final Intent intent) {
        intent.setClass(context, NotificationService.class);
        context.startService(intent);
	}
	
	private class NotificationHandler extends ContentObserver
	{
		public NotificationHandler(final Handler handler) {
			super(handler);
		}
		
		@Override
	    public void onChange(final boolean selfChange) {
	        super.onChange(selfChange);
	        NotificationHelper.getInstance(getApplicationContext()).updateUnreadSms();
	    }
		
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
		@Override
	    public void onChange(final boolean selfChange, final Uri uri) {
	        onChange(selfChange);
	    }
	}
}
