package com.androidproductions.ics.sms;

import android.net.Uri;

public class Constants {
	
	public static final Uri SMS_URI= Uri.parse("content://sms/");
	public static final Uri SMS_INBOX_URI = Uri.parse("content://sms/inbox");
	public static final Uri SMS_FAILED_URI = Uri.parse("content://sms/failed");
	public static final Uri SMS_QUEUED_URI = Uri.parse("content://sms/queued"); 
	public static final Uri SMS_SENT_URI = Uri.parse("content://sms/sent"); 
	public static final Uri SMS_DRAFT_URI = Uri.parse("content://sms/draft");
	public static final Uri SMS_OUTBOX_URI = Uri.parse("content://sms/outbox");
	public static final Uri SMS_UNDELIVERED_URI = Uri.parse("content://sms/undelivered");
	public static final Uri SMS_All_URI = Uri.parse("content://sms/all");
	public static final Uri SMS_CONVERSATIONS_URI = Uri.parse("content://mms-sms/conversations");
	public static final Uri SMS_ONLY_CONVERSATIONS_URI = Uri.parse("content://sms/conversations");
	
	public static final String SMS_RECEIVE_LOCATION = "SmsReceiveLocation";
	public static final String SMS_MESSAGE = "SmsMessage";
	public static final String SMS_TIME = "SmsTime";
	public static final String MESSAGE_TYPE = "MessageType";
	
	public static final int MAX_MESSAGE_COUNT = 25;
	
	public static final int MESSAGE_TYPE_ALL = 0;
    public static final int MESSAGE_TYPE_INBOX = 1;
    public static final int MESSAGE_TYPE_SENT = 2;
    public static final int MESSAGE_TYPE_DRAFT = 3;
    public static final int MESSAGE_TYPE_OUTBOX = 4;
    public static final int MESSAGE_TYPE_FAILED = 5; // for failed outgoing messages
    public static final int MESSAGE_TYPE_QUEUED = 6;
	
    public static final int NOTIFICATION_ID = 9533;
    public static final int NOTIFICATION_SENDFAILED_ID = 9534;
    
	public static final String NOTIFICATION_SHOWING_KEY = "NotificationShowing";
	public static final String NOTIFICATION_STATE_UPDATE = "NotificationStateUpdate";
	public static final String SHOW_SMILEY_KEY = "ShowSmileyKey";

    public static final String TAG ="ICSSMS";
	
	public static final String SMS_RECEIVED_ACTION ="android.provider.Telephony.SMS_RECEIVED";

    public static final String ACTION_SEND_MESSAGE = "com.androidproductions.ics.sms.transactions.SEND_MESSAGE";
	
    public static final String MESSAGE_SENT_ACTION = "com.androidproductions.ics.sms.transactions.MESSAGE_SENT";

    public static final String ACTION_SERVICE_STATE_CHANGED = "com.androidproductions.ics.sms.transactions.SERVICE_STATE_CHANGED";
	public static final String ACTION_SERVICE_STATE_IN_SERVICE = "com.androidproductions.ics.sms.transactions.SERVICE_STATE_IN_SERVICE";
	
	public static final int THEME_HOLO = 0;
	public static final int THEME_HOLO_BLUE = 1;
	public static final int THEME_HOLO_GREEN = 2;
	public static final int THEME_HOLO_RED = 3;
	public static final int THEME_HOLO_PURPLE = 4;
	public static final int THEME_HOLO_ORANGE = 5;
    public static final int THEME_HOLO_LIGHT = 6;
    public static final int THEME_SMOOTH = 7;
	
	public static final String PROJECT_ID = "1079086356494";
	public static final String API_KEY = "AIzaSyD4UuXBPFW8NyvmDYO5VJq5Aofv2nhQgZ0";
	
	
}
