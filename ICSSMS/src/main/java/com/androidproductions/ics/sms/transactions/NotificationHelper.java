package com.androidproductions.ics.sms.transactions;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationCompat.InboxStyle;
import com.androidproductions.ics.sms.Constants;
import com.androidproductions.ics.sms.ICSSMSActivity_;
import com.androidproductions.ics.sms.SmsViewer_;
import com.androidproductions.ics.sms.R;
import com.androidproductions.ics.sms.SmsDialog;
import com.androidproductions.ics.sms.SmsNotify;
import com.androidproductions.ics.sms.messaging.IMessage;
import com.androidproductions.ics.sms.messaging.MessageUtilities;
import com.androidproductions.ics.sms.preferences.ConfigurationHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NotificationHelper {
	
	private final Context mContext;
	private final NotificationManager mNotificationManager;
	private final SharedPreferences mPrefs;
	private PendingIntent contentIntent;
	private List<IMessage> messages;
    private final ConfigurationHelper configurationHelper;
	
	private static NotificationHelper mInstance;

	public static NotificationHelper getInstance(Context context)
	{
		if (mInstance != null) return mInstance;
		mInstance = new NotificationHelper(context);
		return mInstance;
	}
	
	private NotificationHelper(Context context)
	{
		mContext = context;
		mNotificationManager = (NotificationManager) mContext.getSystemService(
				Context.NOTIFICATION_SERVICE);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		messages = new ArrayList<IMessage>();
        configurationHelper = ConfigurationHelper.getInstance(mContext);
	}
	
	public void updateUnreadSms() {
	    notifyUnreadMessages(MessageUtilities.GetUnreadMessages(mContext));
	}

    private void notifyUnreadMessages(List<IMessage> smss) {
		messages = smss;
		if (mPrefs.getBoolean("Notification", true))
		{
			if (smss.size() > 0)
			{
				mNotificationManager.notify(Constants.NOTIFICATION_ID, buildNotification(smss,
                        shouldAlertOnce(smss)));
				mPrefs.edit().putBoolean(Constants.NOTIFICATION_SHOWING_KEY, true).apply();
			}
			else
			{
				mNotificationManager.cancel(Constants.NOTIFICATION_ID);
				mPrefs.edit().putBoolean(Constants.NOTIFICATION_SHOWING_KEY, false).apply();
			}
		}
	}
	
	private boolean shouldAlertOnce(List<IMessage> smss) {
		if (messages.size() < smss.size())
			return false;
		for(IMessage mess : smss)
		{
			boolean found = false;
			for(IMessage mess2 : messages)
			{
				if (mess2.getAddress().equals(mess.getAddress()) && mess.getText().equals(mess2.getText()))
					found = true;
			}
			if (!found)
				return false;
		}
		return true;
	}

	private Notification buildNotification(List<IMessage> smss,boolean alertOnce)
	{
		// Simple case?
		int smsCount = smss.size();
		if (smsCount == 1)
			return buildNotification(smss.get(0),alertOnce);
		
		// Find all contacts
		List<String> numbers = new ArrayList<String>();
		HashMap<String, ArrayList<IMessage>> groupedMessages = new HashMap<String,ArrayList<IMessage>>();
		for (IMessage s : smss)
		{
			if (!numbers.contains(s.getAddress()))
			{
				numbers.add(s.getAddress());
				groupedMessages.put(s.getAddress(), new ArrayList<IMessage>());
			}
			groupedMessages.get(s.getAddress()).add(s);
		}

		IMessage first = smss.get(0);
		IMessage last = smss.get(smsCount-1);
		int contactCount = groupedMessages.size();
		String contentText;
		if (contactCount == 1)
			contentText = "You have "+smsCount+" new messages";
		else
			contentText = "+ " + (numbers.size() - 1) + " other"
			                     + (numbers.size() > 2 ? "s" : "" );
		
		String tickerTitle = last.getContactName() + ": " + last.getText();
		if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("PrivateNotifications", false))
			tickerTitle = "New message from " + last.getContactName();
		
		final Intent multiIntent = new Intent(mContext, ICSSMSActivity_.class);
		multiIntent.putExtra(Constants.NOTIFICATION_STATE_UPDATE, true);
		multiIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		contentIntent = PendingIntent.getActivity(mContext, 0, multiIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		int icon = mPrefs.getBoolean("AlternateIcon", false) ? R.drawable.stat_notify_sms : R.drawable.ic_launcher_sms;
		Builder builder = buildBaseNotification(
				contentText,
				first.getContactName(),
				tickerTitle,
				System.currentTimeMillis(),
				icon,
				first.getContactPhoto(),
				smsCount,
				contentIntent,
				alertOnce);
		Intent dialogIntent;
		if (PreferenceManager.getDefaultSharedPreferences(mContext).getString("DialogType","0").equals("2"))
			dialogIntent = new Intent(mContext, SmsNotify.class);
		else
			dialogIntent = new Intent(mContext, SmsDialog.class);
		dialogIntent.putExtra(Constants.SMS_RECEIVE_LOCATION, last.getAddress());
		dialogIntent.putExtra(Constants.SMS_MESSAGE, last.getText());
		dialogIntent.putExtra(Constants.SMS_TIME, last.getDate());
		dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// |Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent dialogpending = PendingIntent.getActivity(mContext, 0, dialogIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		builder.addAction(R.drawable.ic_go, "Quick Reply", dialogpending);
		Intent convoIntent = new Intent(mContext,ICSSMSActivity_.class);
		PendingIntent convoOpen = PendingIntent.getActivity(mContext, 0, convoIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		builder.addAction(R.drawable.ic_go, "Show More", convoOpen);
		// Messages from more than 1 person
		// Inbox style
		int i = 0;
		int v= 0;
		InboxStyle big = new NotificationCompat.InboxStyle(builder);
		for(String item : groupedMessages.keySet())
		{
			for (IMessage sms : groupedMessages.get(item))
			{
				if (i < 6)
				{
					String name = sms.getContactName();
					if (i < 6)
					{
						big.addLine(name + ": " + sms.getText());
						i++;
					}
					else
						v++;
				}
				else 
					v++;
			}
		}	
		if (v > 0)
			big.setSummaryText("+ " + v + " other"
	                 + (v > 2 ? "s" : "" ));
		return big.build();
	}
	
	private Notification buildNotification(IMessage sms, boolean alertOnce)
	{
		String name = sms.getContactName();
		String tickerTitle = name + ": " + sms.getText();
		String contentText = sms.getText();
		if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("PrivateNotifications", false)) {
			tickerTitle = "New message from " + name;
			contentText = "You have 1 new message";
		}
		final Intent singleIntent = new Intent(mContext, SmsViewer_.class);
		singleIntent.putExtra(Constants.SMS_RECEIVE_LOCATION, sms.getAddress());
		singleIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		contentIntent = PendingIntent.getActivity(mContext, 0, singleIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		int icon = mPrefs.getBoolean("AlternateIcon", false) ? R.drawable.stat_notify_sms : R.drawable.ic_launcher_sms;
		Builder builder = buildBaseNotification(
				contentText,
                name,
				tickerTitle,
				System.currentTimeMillis(),
				icon,
				sms.getContactPhoto(),
				1,
				contentIntent,
				alertOnce);
		Intent dialogIntent;
		if (PreferenceManager.getDefaultSharedPreferences(mContext).getString("DialogType","0").equals("2"))
			dialogIntent = new Intent(mContext, SmsNotify.class);
		else
			dialogIntent = new Intent(mContext, SmsDialog.class);
		dialogIntent.putExtra(Constants.SMS_RECEIVE_LOCATION, sms.getAddress());
		dialogIntent.putExtra(Constants.SMS_MESSAGE, sms.getText());
		dialogIntent.putExtra(Constants.SMS_TIME, System.currentTimeMillis());
		dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// |Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent dialogpending = PendingIntent.getActivity(mContext, 0, dialogIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		builder.addAction(R.drawable.ic_go, "Quick Reply", dialogpending);
		builder.addAction(R.drawable.ic_go, "Open", contentIntent);
		BigTextStyle big = new NotificationCompat.BigTextStyle(builder);
		big.bigText(contentText);
		return big.build();
	}

	public void notifySendFailed() {
		Cursor c = mContext.getContentResolver().query(Constants.SMS_FAILED_URI, null, null, null, null);
        int smsCount = 1;
        if (c != null)
        {
            smsCount = c.getCount();
            c.close();
        }
		if (smsCount > 0)
		{
			Builder builder = new Builder(mContext);
			final Intent multiIntent = new Intent(mContext, ICSSMSActivity_.class);
			multiIntent.putExtra(Constants.NOTIFICATION_STATE_UPDATE, true);
			multiIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, multiIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	        @SuppressWarnings("deprecation")
			Notification notify = builder.setAutoCancel(true)
	        							 .setContentText("Not all messages were successfully sent.")
							        	 .setContentTitle("Message sending failed")
							        	 .setDefaults(Notification.DEFAULT_ALL)
								         .setSmallIcon(R.drawable.ic_launcher_sms)
								         .setTicker("Not all messages were successfully sent")
								         .setWhen(System.currentTimeMillis())
								         .setContentIntent(contentIntent)
								         .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(),android.R.drawable.ic_dialog_alert))
								         .setNumber(smsCount)
								         .getNotification();
	        mNotificationManager.notify(Constants.NOTIFICATION_SENDFAILED_ID, notify);
		}
		else
		{
			mNotificationManager.cancel(Constants.NOTIFICATION_SENDFAILED_ID);
		}
	}
	
	public void cancelSendFailed() {
		mNotificationManager.cancel(Constants.NOTIFICATION_SENDFAILED_ID);
	}

	private Builder buildBaseNotification(String text,
			String title, String ticker, long when, int icon, Bitmap largeIcon,
			int count,PendingIntent contentIntent ,boolean alertOnce) {
		Builder builder = new Builder(mContext);
		builder.setAutoCancel(true)
			   .setContentText(text)
			   .setContentTitle(title)
		       .setSmallIcon(icon)
		       .setTicker(ticker)
		       .setWhen(when)
		       .setContentIntent(contentIntent)
		       .setLargeIcon(largeIcon)
		       .setNumber(count)
		       .setOnlyAlertOnce(alertOnce);
        int defaults = Notification.DEFAULT_LIGHTS;
        if (configurationHelper.getBooleanValue(ConfigurationHelper.VIBRATION))
            defaults |= Notification.DEFAULT_VIBRATE;
		String sound = mPrefs.getString("NotificationSound", "");
		if (mPrefs.getBoolean("CustomSound", true) && !sound.equals(""))
			builder.setSound(Uri.parse(sound));
		else
            defaults |= Notification.DEFAULT_SOUND;
		builder.setDefaults(defaults);
		return builder;
	}
}
