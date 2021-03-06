package com.androidproductions.ics.sms.receivers;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;

import com.androidproductions.ics.sms.Constants;
import com.androidproductions.logging.LogHelper;

public class MyPhoneStateListener extends PhoneStateListener {
    private final Context context;
   
   public MyPhoneStateListener(final Context con)
   {
	   context = con;
   }
   
   @Override
   public void onCallStateChanged(final int state, final String incomingNumber){
        switch(state){
        case TelephonyManager.CALL_STATE_IDLE:
              LogHelper.getInstance().i("MyPhoneStateListener->onCallStateChanged() -> CALL_STATE_IDLE " + incomingNumber);
              break;
        case TelephonyManager.CALL_STATE_OFFHOOK:
              LogHelper.getInstance().i("MyPhoneStateListener->onCallStateChanged() -> CALL_STATE_OFFHOOK "+incomingNumber);
              break;
        case TelephonyManager.CALL_STATE_RINGING:
              LogHelper.getInstance().i("MyPhoneStateListener->onCallStateChanged() -> CALL_STATE_RINGING "+incomingNumber);
              break;
        default:
           LogHelper.getInstance().i("MyPhoneStateListener->onCallStateChanged() -> default -> "+Integer.toString(state));
           break;
        }
   }
   
   @Override
   public void onServiceStateChanged (final ServiceState serviceState){
      switch(serviceState.getState()){
           case ServiceState.STATE_IN_SERVICE:
                LogHelper.getInstance().i("MyPhoneStateListener->onServiceStateChanged() -> STATE_IN_SERVICE");
                serviceState.setState(ServiceState.STATE_IN_SERVICE);
                final Intent intent  = new Intent(context, SmsUpdateReceiver.class);
                intent.setAction(Constants.ACTION_SERVICE_STATE_IN_SERVICE);
                context.sendBroadcast(intent);
                break;
           case ServiceState.STATE_OUT_OF_SERVICE:
                LogHelper.getInstance().i("MyPhoneStateListener->onServiceStateChanged() -> STATE_OUT_OF_SERVICE");
                serviceState.setState(ServiceState.STATE_OUT_OF_SERVICE);
                break;
           case ServiceState.STATE_EMERGENCY_ONLY:
              LogHelper.getInstance().i("MyPhoneStateListener->onServiceStateChanged() -> STATE_EMERGENCY_ONLY");
              serviceState.setState(ServiceState.STATE_EMERGENCY_ONLY);
              break;
           case ServiceState.STATE_POWER_OFF:
              LogHelper.getInstance().i("MyPhoneStateListener->onServiceStateChanged() -> STATE_POWER_OFF");
              serviceState.setState(ServiceState.STATE_POWER_OFF);
              break;
           default:
              LogHelper.getInstance().i("MyPhoneStateListener->onServiceStateChanged() -> default -> "+Integer.toString(serviceState.getState()));
              break;
        }
   }
}