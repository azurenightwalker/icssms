package com.androidproductions.ics.sms.transactions;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationCompat.InboxStyle;
import com.androidproductions.ics.sms.Constants;
import com.androidproductions.ics.sms.R;
import com.androidproductions.ics.sms.SmsDialog;
import com.androidproductions.ics.sms.SmsNotify;
import com.androidproductions.ics.sms.messaging.IMessage;
import com.androidproductions.ics.sms.messaging.MessageUtilities;
import com.androidproductions.ics.sms.preferences.ConfigurationHelper;
import com.androidproductions.ics.sms.ICSSMSActivity_;
import com.androidproductions.ics.sms.SmsViewer_;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NotificationHelper {

    private final Context mContext;
    private final NotificationManager mNotificationManager;
    private PendingIntent contentIntent;
    private List<IMessage> messages;
    private int messageSize;
    private final ConfigurationHelper configurationHelper;

    private static NotificationHelper mInstance;
    private boolean alertOnce;

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
        messages = new ArrayList<IMessage>();
        messageSize = 0;
        configurationHelper = ConfigurationHelper.getInstance();
    }

    public void updateUnreadSms() {
        notifyUnreadMessages(MessageUtilities.GetUnreadMessages(mContext));
    }

    private void notifyUnreadMessages(List<IMessage> smss) {
        alertOnce = shouldAlertOnce(smss);
        messages = smss;
        messageSize = messages.size();
        if (configurationHelper.getBooleanValue(ConfigurationHelper.NOTIFICATIONS_ENABLED))
        {
            if (messageSize > 0)
            {
                mNotificationManager.notify(Constants.NOTIFICATION_ID, buildNotification());
                configurationHelper.setBooleanValue(ConfigurationHelper.NOTIFICATION_SHOWING, true);
            }
            else
            {
                mNotificationManager.cancel(Constants.NOTIFICATION_ID);
                configurationHelper.setBooleanValue(ConfigurationHelper.NOTIFICATION_SHOWING, false);
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

    private Notification buildNotification()
    {
        // Simple case?
        if (messageSize == 1)
            return buildNotification(messages.get(0));

        // Find all contacts
        List<String> numbers = new ArrayList<String>();
        HashMap<String, ArrayList<IMessage>> groupedMessages = new HashMap<String,ArrayList<IMessage>>();
        for (IMessage s : messages)
        {
            if (!numbers.contains(s.getAddress()))
            {
                numbers.add(s.getAddress());
                groupedMessages.put(s.getAddress(), new ArrayList<IMessage>());
            }
            groupedMessages.get(s.getAddress()).add(s);
        }

        IMessage first = messages.get(0);
        IMessage last = messages.get(messageSize-1);

        final Intent multiIntent = new Intent(mContext, ICSSMSActivity_.class);
        multiIntent.putExtra(Constants.NOTIFICATION_STATE_UPDATE, true);
        multiIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        contentIntent = PendingIntent.getActivity(mContext, 0, multiIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Builder builder = buildBaseNotification();
        Intent dialogIntent;
        if (configurationHelper.getStringValue(ConfigurationHelper.DIALOG_TYPE).equals("2"))
            dialogIntent = new Intent(mContext, SmsNotify.class);
        else
            dialogIntent = new Intent(mContext, SmsDialog.class);
        dialogIntent.putExtra(Constants.SMS_RECEIVE_LOCATION, last.getAddress());
        dialogIntent.putExtra(Constants.SMS_MESSAGE, last.getText());
        dialogIntent.putExtra(Constants.SMS_TIME, last.getDate());
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// |Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent dialogpending = PendingIntent.getActivity(mContext, 0, dialogIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        String quickReply = mContext.getResources().getString(R.string.quickReply);
        String showMore = mContext.getResources().getString(R.string.showMore);
        builder.addAction(R.drawable.ic_go, quickReply, dialogpending);
        Intent convoIntent = new Intent(mContext,ICSSMSActivity_.class);
        PendingIntent convoOpen = PendingIntent.getActivity(mContext, 0, convoIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.addAction(R.drawable.ic_go, showMore, convoOpen);
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

    private Notification buildNotification(IMessage sms)
    {
        String name = sms.getContactName();
        String contentText = getContent();
        final Intent singleIntent = new Intent(mContext, SmsViewer_.class);
        singleIntent.putExtra(Constants.SMS_RECEIVE_LOCATION, sms.getAddress());
        singleIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        contentIntent = PendingIntent.getActivity(mContext, 0, singleIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Builder builder = buildBaseNotification();
        Intent dialogIntent;
        if (configurationHelper.getStringValue(ConfigurationHelper.DIALOG_TYPE).equals("2"))
            dialogIntent = new Intent(mContext, SmsNotify.class);
        else
            dialogIntent = new Intent(mContext, SmsDialog.class);
        dialogIntent.putExtra(Constants.SMS_RECEIVE_LOCATION, sms.getAddress());
        dialogIntent.putExtra(Constants.SMS_MESSAGE, sms.getText());
        dialogIntent.putExtra(Constants.SMS_TIME, System.currentTimeMillis());
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// |Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent dialogpending = PendingIntent.getActivity(mContext, 0, dialogIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        String open = mContext.getResources().getString(R.string.openConvo);
        String quickReply = mContext.getResources().getString(R.string.quickReply);
        builder.addAction(R.drawable.ic_go, quickReply, dialogpending);
        builder.addAction(R.drawable.ic_go, open, contentIntent);
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
            String notAllSent = mContext.getResources().getString(R.string.notAllSent);
            String sendingFailed = mContext.getResources().getString(R.string.sendingFailed);
            @SuppressWarnings("deprecation")
            Notification notify = builder.setAutoCancel(true)
                    .setContentText(notAllSent)
                    .setContentTitle(sendingFailed)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setSmallIcon(R.drawable.ic_launcher_sms)
                    .setTicker(notAllSent)
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

    private Builder buildBaseNotification() {
        Builder builder = new Builder(mContext);
        IMessage first = messages.get(0);
        builder.setAutoCancel(true)
                .setContentText(getContent())
                .setContentTitle(first.getContactName())
                .setSmallIcon(getIcon())
                .setTicker(getTickerTitle())
                .setWhen(System.currentTimeMillis())
                .setContentIntent(getIntent())
                .setLargeIcon(first.getContactPhoto())
                .setNumber(messageSize)
                .setOnlyAlertOnce(alertOnce);

        int defaults = 0;
        if (setVibrate(builder))
            defaults |= Notification.DEFAULT_VIBRATE;
        if (setSound(builder))
            defaults |= Notification.DEFAULT_SOUND;
        if (setLights(builder))
            defaults |= Notification.DEFAULT_LIGHTS;
        if (defaults > 0)
        {
            if (defaults == 7)
                builder.setDefaults(Notification.DEFAULT_ALL);
            else
                builder.setDefaults(defaults);
        }

        return builder;
    }


    int getIcon()
    {
        return configurationHelper.getBooleanValue(ConfigurationHelper.ALTERNATIVE_ICON) ?
                R.drawable.stat_notify_sms : R.drawable.ic_launcher_sms;
    }

    String getTickerTitle()
    {
        if (configurationHelper.getBooleanValue(ConfigurationHelper.PRIVATE_NOTIFICATIONS)) {
            return messageSize + " new message" + (messageSize == 1 ? "" : "s");
        }
        else
        {
            IMessage mess;
            if (messageSize == 1)
                mess = messages.get(0);
            else
                mess = messages.get(messageSize-1);
            return mess.getContactName() + ": " + mess.getText();

        }
    }

    String getContent()
    {
        if (messageSize == 1)
        {
            if (configurationHelper.getBooleanValue(ConfigurationHelper.PRIVATE_NOTIFICATIONS)) {
                return "You have 1 new message";
            }
            return messages.get(0).getText();
        }
        else
        {
            // Find all contacts
            List<String> numbers = new ArrayList<String>();
            for (IMessage s : messages)
            {
                if (!numbers.contains(s.getAddress()))
                {
                    numbers.add(s.getAddress());
                }
            }
            int contactCount = numbers.size();
            if (contactCount == 1)
                return "You have "+messageSize+" new messages";
            else
                return "+ " + (contactCount - 1) + " other"
                        + (contactCount > 2 ? "s" : "" );
        }
    }

    PendingIntent getIntent()
    {
        Intent contentIntent;
        if (messageSize == 1)
        {
            contentIntent = new Intent(mContext, SmsViewer_.class);
            contentIntent.putExtra(Constants.SMS_RECEIVE_LOCATION, messages.get(0).getAddress());
        }
        else
        {
            contentIntent = new Intent(mContext, ICSSMSActivity_.class);
            contentIntent.putExtra(Constants.NOTIFICATION_STATE_UPDATE, true);
        }
        contentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(mContext, 0, contentIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    Boolean setVibrate(Builder builder)
    {
        if (configurationHelper.getBooleanValue(ConfigurationHelper.VIBRATION))
            return true;
        builder.setVibrate(new long[] {0L});
        return false;
    }

    Boolean setSound(Builder builder)
    {
        if (!configurationHelper.getBooleanValue(ConfigurationHelper.CUSTOM_SOUND))
            return true;
        String sound = configurationHelper.getStringValue(ConfigurationHelper.NOTIFICATION_SOUND);
        if (sound.equals(""))
            return true;
        builder.setSound(Uri.parse(sound));
        return false;
    }

    Boolean setLights(Builder builder)
    {
        return true;
    }
}
