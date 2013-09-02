package com.androidproductions.ics.sms.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ServiceStarter extends BroadcastReceiver{
	
	@Override 
    public void onReceive(final Context context, final Intent intent){
    	NotificationService.startService(context, intent);
	}
}

