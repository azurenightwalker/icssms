package com.androidproductions.ics.sms.receivers;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.preference.PreferenceManager;

import com.androidproductions.ics.sms.Constants;
import com.androidproductions.ics.sms.service.MessagingService;
import com.androidproductions.ics.sms.service.NotificationService;

public class SmsUpdateReceiver extends BroadcastReceiver {
	private static final Object mStartingServiceSync = new Object();
	private static PowerManager.WakeLock mStartingService;
	public static final String UPDATE_TYPE = "UPDATE_TYPE";
	public static final String SMS_SENT = "SMS_SENT";
	public static final String SMS_ID = "_ID";
	public static final String SMS_URI = "SMSURI";
	@Override
    public void onReceive(Context context, Intent intent) {
		intent.setClass(context, MessagingService.class);
        intent.putExtra("result", getResultCode());
    	beginStartingService(context, intent);
    	NotificationService.startService(context, intent);
    	if (Constants.SMS_RECEIVED_ACTION.equals(intent.getAction()) && PreferenceManager.getDefaultSharedPreferences(context).getBoolean("DisableOtherNotifications", true))
    		abortBroadcast();
	}
	
	private static void beginStartingService(Context context, Intent intent) {
        synchronized (mStartingServiceSync) {
            if (mStartingService == null) {
                PowerManager pm =
                    (PowerManager)context.getSystemService(Context.POWER_SERVICE);
                mStartingService = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "StartingAlertService");
                mStartingService.setReferenceCounted(false);
            }
            mStartingService.acquire();
            context.startService(intent);
        }
    }

    /**
     * Called back by the service when it has finished processing notifications,
     * releasing the wake lock if the service is now stopping.
     */
    public static void finishStartingService(Service service, int startId) {
        synchronized (mStartingServiceSync) {
            if (mStartingService != null) {
                if (service.stopSelfResult(startId)) {
                    mStartingService.release();
                }
            }
        }
    }
}
