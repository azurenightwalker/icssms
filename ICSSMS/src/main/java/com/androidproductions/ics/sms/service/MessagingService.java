package com.androidproductions.ics.sms.service;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.androidproductions.ics.sms.Constants;
import com.androidproductions.ics.sms.R;
import com.androidproductions.ics.sms.SmsDialog;
import com.androidproductions.ics.sms.SmsNotify;
import com.androidproductions.ics.sms.messaging.sms.SMSMessage;
import com.androidproductions.ics.sms.preferences.ConfigurationHelper;
import com.androidproductions.ics.sms.receivers.MyPhoneStateListener;
import com.androidproductions.ics.sms.receivers.SmsUpdateReceiver;
import com.androidproductions.ics.sms.transactions.NotificationHelper;
import com.androidproductions.ics.sms.utils.LogHelper;

import java.util.ArrayList;

import static android.content.Intent.ACTION_BOOT_COMPLETED;

public class MessagingService extends Service{

	private static final String TAG = "ICSSMSService";
    private ServiceHandler mServiceHandler;

	// Indicates next message can be picked up and sent out.
    private static final String EXTRA_MESSAGE_SENT_SEND_NEXT ="SendNextMsg";


    private static final Handler mToastHandler = new Handler();

    private static final String SMS_URI = "SMSURI";
    
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
		
        HandlerThread thread = new HandlerThread(TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        Looper mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper,this);
    }
	
	public int onStartCommand(final Intent intent, int flags, int startId)
	{
        int mResultCode = intent != null ? intent.getIntExtra("result", 0) : 0;

        if (intent != null) {
            LogHelper.t(this, intent.getAction());
        }
        else {
            LogHelper.w("Starting to process message with no intent");
        }
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        msg.arg2 = mResultCode;
        mServiceHandler.sendMessage(msg);
        return Service.START_STICKY;
	}
	
	@SuppressWarnings("deprecation")
    @Override
	public void onStart(Intent intent, int startId) {
		onStartCommand(intent, startId, 0);
	}
	
	@SuppressWarnings("unused")
	private static String translateResultCode(int resultCode) {
        switch (resultCode) {
            case Activity.RESULT_OK:
                return "Activity.RESULT_OK";
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                return "SmsManager.RESULT_ERROR_GENERIC_FAILURE";
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                return "SmsManager.RESULT_ERROR_RADIO_OFF";
            case SmsManager.RESULT_ERROR_NULL_PDU:
                return "SmsManager.RESULT_ERROR_NULL_PDU";
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                return "SmsManager.RESULT_ERROR_NO_SERVICE";
            default:
                return "Unknown error code";
        }
    }
	
	
	private static final class ServiceHandler extends Handler {
		private final Service context;
        private final ConfigurationHelper mConfig;
        @SuppressWarnings("unused")
		private boolean mSending;
        private MyPhoneStateListener phoneListener;
        public ServiceHandler(Looper looper, Service con) {
            super(looper);
            context = con;
            mConfig = ConfigurationHelper.getInstance();
        }

        /**
         * Handle incoming transaction requests.
         * The incoming requests are initiated by the MMSC Server or by the MMS Client itself.
         */
        @Override
        public void handleMessage(Message msg) {
            int serviceId = msg.arg1;
            int resultCode = msg.arg2;
            Intent intent = (Intent)msg.obj;
            if (intent != null) {
                String action = intent.getAction();
                int error = intent.getIntExtra("errorCode", 0);
                if (Constants.MESSAGE_SENT_ACTION.equals(action)) {
                    handleSmsSent(intent, error,resultCode);
                } else if (Constants.SMS_RECEIVED_ACTION.equals(action)) {
                    handleSmsRecieved(intent);
                } else if (ACTION_BOOT_COMPLETED.equals(action)) {
                    handleBootCompleted();
                } else if (Constants.ACTION_SERVICE_STATE_IN_SERVICE.equals(action)) {
                    handleServiceStateChanged();
                } else if (Constants.ACTION_SEND_MESSAGE.endsWith(action)) {
                    handleSendMessage();
                }
            }
            // NOTE: We MUST not call stopSelf() directly, since we need to
            // make sure the wake lock acquired by AlertReceiver is released.
            SmsUpdateReceiver.finishStartingService(context, serviceId);
        }
        
        
        private void handleServiceStateChanged() {
        	unRegisterForServiceStateChanges();
        	sendFirstQueuedMessage();
        	NotificationHelper.getInstance(context).cancelSendFailed();
		}

		private void handleBootCompleted() {
			sendFirstQueuedMessage();
		}

		private void handleSendMessage() {
			sendFirstQueuedMessage();
		}

		private void handleSmsSent(Intent intent, int error, int resultCode) {
            //noinspection ConstantConditions
            Uri uri = (Uri)intent.getExtras().get(SMS_URI);
	        mSending = false;
	        boolean sendNextMsg = intent.getBooleanExtra(EXTRA_MESSAGE_SENT_SEND_NEXT, false);

	        if (resultCode == Activity.RESULT_OK) {
	            if (!SMSMessage.moveToFolder(context, uri, Constants.MESSAGE_TYPE_SENT)) {
	                Log.e(TAG, "handleSmsSent: failed to move message " + uri + " to sent folder");
	            }
	            if (sendNextMsg) {
	                sendFirstQueuedMessage();
	            }

	            // Update the notification for failed messages since they may be deleted.
	            //MessagingNotification.updateSendFailedNotification(this);
	        } else if ((resultCode == SmsManager.RESULT_ERROR_RADIO_OFF) ||
	                (resultCode == SmsManager.RESULT_ERROR_NO_SERVICE)) {
	            // We got an error with no service or no radio. Register for state changes so
	            // when the status of the connection/radio changes, we can try to send the
	            // queued up messages.
	            registerForServiceStateChanges();
	            // We couldn't send the message, put in the queue to retry later.
	            SMSMessage.moveToFolder(context, uri, Constants.MESSAGE_TYPE_QUEUED);
	            mToastHandler.post(new Runnable() {
	                public void run() {
	                    Toast.makeText(context, context.getString(R.string.message_queued),
	                            Toast.LENGTH_SHORT).show();
	                }
	            });
	            
	        } else {
	            messageFailedToSend(uri, error);
	            if (sendNextMsg) {
	                sendFirstQueuedMessage();
	            }
	        }
	    }

	    private void sendFirstQueuedMessage() {
	    	mSending = true;
	    	Cursor c = context.getContentResolver().query(Constants.SMS_QUEUED_URI, null, null,
					null, "date ASC");
	    	if (c!= null)
            {
                if (c.moveToFirst())
                {
                    int addressCol = c.getColumnIndex("address");
                    String address = c.getString(addressCol);
                    sendMessage(new SMSMessage(context,c, address));
                }
                c.close();
            }
		}
	    
		private void sendMessage(SMSMessage message) {
			SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> messages = smsManager.divideMessage(message.Body);

            // Sanitise number
	        String mDest = PhoneNumberUtils.stripSeparators(message.Address);
	        
	        int messageCount = messages.size();
	        message.moveToOutbox();
	        ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>(messageCount);
	        ArrayList<PendingIntent> recIntents = new ArrayList<PendingIntent>(messageCount);
	        for (int i = 0; i < messageCount; i++) {
	            Intent intent  = new Intent(message.mContext,
	                    SmsUpdateReceiver.class);
	            intent.putExtra(SmsUpdateReceiver.UPDATE_TYPE, SmsUpdateReceiver.SMS_SENT);
	            intent.putExtra(SmsUpdateReceiver.SMS_URI, message.uri);
	            intent.putExtra(SmsUpdateReceiver.SMS_ID, message.ID);
	            intent.setAction("com.androidproductions.ics.sms.transactions.MESSAGE_SENT");
	            
	            int requestCode = 0;
	            if (i == messageCount -1) {
	                // Changing the requestCode so that a different pending intent
	                // is created for the last fragment with
	                // EXTRA_MESSAGE_SENT_SEND_NEXT set to true.
	                requestCode = 1;
	                intent.putExtra(EXTRA_MESSAGE_SENT_SEND_NEXT, true);
	            }
	            sentIntents.add(PendingIntent.getBroadcast(message.mContext, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT));
	            recIntents.add(null);
	        }
	        try {
	            smsManager.sendMultipartTextMessage(mDest, null, messages, sentIntents, recIntents);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	            message.markSendingFailed();
	        }
		}

		private void messageFailedToSend(Uri uri, int error) {
            Log.e(Constants.TAG,"Message sending failed with error code:"+error);
			SMSMessage.moveToFolder(context, uri, Constants.MESSAGE_TYPE_FAILED);
	        NotificationHelper.getInstance(context).notifySendFailed();
	    }

		public void handleSmsRecieved(Intent intent)
        {
            //noinspection ConstantConditions
            SMSMessage sms = new SMSMessage(context, (Object[]) intent.getExtras().get("pdus"));

            // Save message if needed
            if (mConfig.getBooleanValue(ConfigurationHelper.DISABLE_OTHER_NOTIFICATIONS))
                sms.saveIncoming(false);

            // Display Dialog
            if (mConfig.getBooleanValue(ConfigurationHelper.DIALOG_ENABLED))
                displayDialog(context, sms);
            context.sendBroadcast(new Intent("com.androidproductions.ics.sms.UPDATE_DIALOG"));
        }
        
        private void displayDialog(Context context, SMSMessage sms) {
        	Intent dialogIntent;
    		if (mConfig.getStringValue(ConfigurationHelper.DIALOG_TYPE).equals("2"))
    			dialogIntent = new Intent(context, SmsNotify.class);
    		else
    			dialogIntent = new Intent(context, SmsDialog.class);
    		dialogIntent.putExtra(Constants.SMS_RECEIVE_LOCATION, sms.Address);
    		dialogIntent.putExtra(Constants.SMS_MESSAGE, sms.Body);
    		dialogIntent.putExtra(Constants.MESSAGE_TYPE, "SMS");
    		dialogIntent.putExtra(Constants.SMS_TIME, System.currentTimeMillis());
    		dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// |Intent.FLAG_ACTIVITY_SINGLE_TOP);
    		context.startActivity(dialogIntent);
    	}
        
        private void registerForServiceStateChanges() {
        	phoneListener = new MyPhoneStateListener(context);
            TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            telephony.listen(phoneListener, PhoneStateListener.LISTEN_SERVICE_STATE);
        }

        private void unRegisterForServiceStateChanges() {
        	TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            telephony.listen(phoneListener, PhoneStateListener.LISTEN_NONE);
        }
    }
	
	

}
