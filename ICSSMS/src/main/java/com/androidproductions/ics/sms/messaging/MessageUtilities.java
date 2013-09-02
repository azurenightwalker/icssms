package com.androidproductions.ics.sms.messaging;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.androidproductions.ics.sms.messaging.sms.SMSMessage;
import com.androidproductions.ics.sms.messaging.sms.SMSUtilities;
import com.androidproductions.ics.sms.utils.AddressUtilities;
import com.androidproductions.libs.sms.com.androidproductions.libs.sms.constants.MessageType;
import com.androidproductions.libs.sms.com.androidproductions.libs.sms.constants.SmsUri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class MessageUtilities {

    private MessageUtilities() {
    }

    public static List<IMessage> GetUnreadMessages(final Context context)
    {
        final ArrayList<IMessage> messages = new ArrayList<IMessage>();
        final Cursor c = context.getContentResolver().query(SmsUri.INBOX_URI, null, "read = '0'",
                null, "date ASC");
        if (c != null)
        {
            if (c.moveToFirst())
            {
                do
                {
                    messages.add(new SMSMessage(context,c));
                } while (c.moveToNext());
            }
            c.close();
        }

        return messages;
    }

    public static List<IMessage> GetUnsentMessages(final Context context)
    {
        final ArrayList<IMessage> messages = new ArrayList<IMessage>();
        final Cursor c = context.getContentResolver().query(SmsUri.BASE_URI, null, "type = ?",
                new String[] { String.valueOf(MessageType.FAILED) }, "date ASC");
        if (c != null)
        {
            if (c.moveToFirst())
            {
                do
                {
                    messages.add(new SMSMessage(context,c));
                } while (c.moveToNext());
            }
            c.close();
        }

        return messages;
    }
	
	public static void SaveDraftMessage(final Context context, final String address, final String message)
	{
		
	}
	
	public static String RetrieveDraftMessage(final Context context, final String address)
	{
		return "";
	}
	
	public static Long SendMessage(final Context context, final String message, final String destination)
	{
		return SMSUtilities.sendSms(context, message, destination);
	}
	
	public static IMessage GenerateMessage(final Context context, final String address, final String message, final int incoming, final long time)
	{
		return SMSUtilities.Generate(context, address, message, incoming, time);
	}

	public static List<IMessage> GetMessageSummary(final Context context)
	{
		final List<IMessage> messages = new ArrayList<IMessage>();
        try
        {
            final Cursor c = context.getContentResolver().query(Uri.parse("content://mms-sms/conversations?simple=true"), null, null,
                    null, null);
            if (c != null)
            {
                if (c.moveToFirst())
                {

                    do
                    {
                        if (c.getInt(c.getColumnIndex("message_count")) > 0)
                            messages.add(SMSUtilities.generateMessageFromSummary(context,c));
                    } while (c.moveToNext());
                }
                c.close();
            }
        }
        catch(Exception e)
        {
            // Simple conversations not supported
            try
            {
                final Cursor c = context.getContentResolver().query(SmsUri.CONVERSATIONS_URI, new String[] {"*"}, null,
                        null, null);
                if (c != null)
                {
                    processCursor(context, messages, c);
                    c.close();
                }
            }
            catch (Exception ex)
            {
                try
                {
                    // Now were really custom..
                    final Cursor c = context.getContentResolver().query(SmsUri.SMS_ONLY_CONVERSATIONS_URI, new String[] {"*"}, null,
                            null, null);
                    if (c != null)
                    {
                        processCursor(context, messages, c);
                        c.close();
                    }
                }
                catch(Exception exc)
                {
                    // There has got to be a better way..
                    final Cursor c = context.getContentResolver().query(SmsUri.SMS_ONLY_CONVERSATIONS_URI, null, null,
                            null, null);
                    if (c != null)
                    {
                        processCursor(context, messages, c);
                        c.close();
                    }
                }
            }
        }
        Collections.sort(messages, new Comparator<IMessage>() {
            public int compare(final IMessage m1, final IMessage m2) {
                return m2.getDate().compareTo(m1.getDate());
            }
        });
		
		return messages;
	}

	private static void processCursor(final Context context, final List<IMessage> messages,
			final Cursor c) {
		if (c.moveToFirst())
		{
			do
			{
				final String where = "thread_id = ?";
				final String[] vals = new String[] { String.valueOf(c.getLong(0))};
				final Cursor c2 = context.getContentResolver().query(SmsUri.BASE_URI,new String[] {"*"}, where,
						vals, "date DESC");
                if (c2 != null)
                {
                    if (c2.moveToFirst())
                    {
                        final int addressCol = c2.getColumnIndex("address");
                        final String address = AddressUtilities.StandardiseNumber(c2.getString(addressCol),context);
                        final SMSMessage sms = new SMSMessage(context,c2, address);
                        sms.SummaryCount = c2.getCount();
                        messages.add(sms);
                    }
                    c2.close();
                }
			} while (c.moveToNext());
		}
	}

    public static List<IMessage> GetMessages(final Context context, final long threadId, final int max)
    {
        return GetMessages(context, threadId, max,null);
    }

	public static List<IMessage> GetMessages(final Context context, final long threadId, final int max, final Long date)
	{
		final ArrayList<IMessage> messages = new ArrayList<IMessage>();
        final String where;
        final String[] vals;
        if (date == null)
        {
            where = "thread_id = ?";
            vals = new String[] { String.valueOf(threadId) };
        }
        else
        {
            where = "thread_id = ? and date < ?";
            vals = new String[] { String.valueOf(threadId), String.valueOf(date) };
        }

        final Cursor c = context.getContentResolver().query(SmsUri.BASE_URI,
                null, where, vals, "date DESC LIMIT "+max);
        if (c != null)
        {
            if (c.moveToFirst())
            {
                do
                {
                    messages.add(new SMSMessage(context,c));
                } while (c.moveToNext());
            }
            c.close();
        }
        Collections.sort(messages, new Comparator<IMessage>() {
            public int compare(final IMessage m1, final IMessage m2) {
                return m1.getDate().compareTo(m2.getDate());
            }
        });
        return messages.subList(Math.max(0,messages.size()-max), messages.size());
	}

}
