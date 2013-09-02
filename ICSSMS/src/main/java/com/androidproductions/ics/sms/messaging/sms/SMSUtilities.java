package com.androidproductions.ics.sms.messaging.sms;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.androidproductions.ics.sms.Constants;
import com.androidproductions.ics.sms.receivers.SmsUpdateReceiver;

public final class SMSUtilities {
    private SMSUtilities() {
    }

    public static SMSMessage generateMessageFromSummary(final Context context, final Cursor c) {
		String address = "";
        final String recs = c.getString(c.getColumnIndex("recipient_ids"));
        if (recs != null)
        {
            for (final String recipient : recs.split(" "))
            {
                final Cursor c2 = context.getContentResolver().query(Uri.parse("content://mms-sms/canonical-address/"+recipient),null, null,
                        null, null);
                if (c2 != null)
                {
                    if (c2.moveToFirst()) {
                        address = c2.getString(0);
                    }
                    c2.close();
                }
            }
        }
		final SMSMessage message = new SMSMessage(context, address, 1, c.getString(c.getColumnIndex("snippet")), c.getLong(c.getColumnIndex("date")));
		message.HasAttachment = c.getInt(c.getColumnIndex("has_attachment")) == 1;
		message.SummaryCount = c.getInt(c.getColumnIndex("message_count"));
		message.Read = c.getInt(c.getColumnIndex("read"));
		message.ThreadId = c.getLong(0);
		return message;
	}
	
	public static Long sendSms(final Context context, final String text, final String number)
	{
		if (number != null)
		{
			final SMSMessage message = new SMSMessage(context, number, 0, text, System.currentTimeMillis());
            message.Read = 1;
            final long threadId = message.queueSending();
            final Intent intent  = new Intent(context, SmsUpdateReceiver.class);
            intent.setAction(Constants.ACTION_SEND_MESSAGE);
            context.sendBroadcast(intent);
            return threadId;
		}
		return 0L;
	}
	
	public static SMSMessage Generate(final Context context, final String address, final String message, final int incoming, final long time)
	{
		return new SMSMessage(context, address,incoming,message,time);
	}

}
