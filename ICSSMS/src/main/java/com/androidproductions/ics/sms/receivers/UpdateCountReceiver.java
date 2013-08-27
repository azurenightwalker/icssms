package com.androidproductions.ics.sms.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.androidproductions.ics.sms.utils.ApexHelper;

//TODO: This doesnt seem to be hooked up correctly
public class UpdateCountReceiver extends BroadcastReceiver {

	private static final String ACTION_UPDATE_COUNTER = "com.anddoes.launcher.UPDATE_COUNTER";
	
	@Override
    public void onReceive(Context context, Intent intent) {
		if (intent != null && ACTION_UPDATE_COUNTER.equals(intent.getAction())) {
            ApexHelper.getInstance(context).update();
        }
	}
}
