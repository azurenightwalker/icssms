package com.androidproductions.ics.sms.messaging;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.androidproductions.ics.sms.Constants;
import com.androidproductions.ics.sms.messaging.sms.SMSMessage;
import com.androidproductions.ics.sms.messaging.sms.SMSUtilities;
import com.androidproductions.ics.sms.utils.AddressUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class MessageUtilities {

    public static List<IMessage> GetUnreadMessages(Context context)
    {
        ArrayList<IMessage> messages = new ArrayList<IMessage>();
        Cursor c = context.getContentResolver().query(Constants.SMS_INBOX_URI, null, "read = '0'",
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

    public static List<IMessage> GetUnsentMessages(Context context)
    {
        ArrayList<IMessage> messages = new ArrayList<IMessage>();
        Cursor c = context.getContentResolver().query(Constants.SMS_URI, null, "type = ?",
                new String[] { String.valueOf(Constants.MESSAGE_TYPE_FAILED) }, "date ASC");
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
	
	public static void SaveDraftMessage(Context context, String address, String message)
	{
		
	}
	
	public static String RetrieveDraftMessage(Context context, String address)
	{
		return "";
	}
	
	public static Long SendMessage(Context context, String message, String destination)
	{
		return SMSUtilities.sendSms(context, message, destination);
	}
	
	public static IMessage GenerateMessage(Context context, String address, String message, int incoming, long time)
	{
		return SMSUtilities.Generate(context, address, message, incoming, time);
	}

	public static List<IMessage> GetMessageSummary(Context context)
	{
		List<IMessage> messages = new ArrayList<IMessage>();
        try
        {
            Cursor c = context.getContentResolver().query(Uri.parse("content://mms-sms/conversations?simple=true"), null, null,
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
                Cursor c = context.getContentResolver().query(Constants.SMS_CONVERSATIONS_URI, new String[] {"*"}, null,
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
                    Cursor c = context.getContentResolver().query(Constants.SMS_ONLY_CONVERSATIONS_URI, new String[] {"*"}, null,
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
                    Cursor c = context.getContentResolver().query(Constants.SMS_ONLY_CONVERSATIONS_URI, null, null,
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
            public int compare(IMessage m1, IMessage m2) {
                return m2.getDate().compareTo(m1.getDate());
            }
        });
		
		return messages;
	}

	private static void processCursor(Context context, List<IMessage> messages,
			Cursor c) {
		if (c.moveToFirst())
		{
			do
			{
				String where = "thread_id = ?";
				String[] vals = new String[] { String.valueOf(c.getLong(0))};
				Cursor c2 = context.getContentResolver().query(Constants.SMS_URI,new String[] {"*"}, where,
						vals, "date DESC");
                if (c2 != null)
                {
                    if (c2.moveToFirst())
                    {
                        int addressCol = c2.getColumnIndex("address");
                        String address = AddressUtilities.StandardiseNumber(c2.getString(addressCol),context);
                        SMSMessage sms = new SMSMessage(context,c2, address);
                        sms.SummaryCount = c2.getCount();
                        messages.add(sms);
                    }
                    c2.close();
                }
			} while (c.moveToNext());
		}
	}

    public static List<IMessage> GetMessages(Context context, long threadId, int max)
    {
        return GetMessages(context, threadId, max,null);
    }

	public static List<IMessage> GetMessages(Context context, long threadId, int max, Long date)
	{
		ArrayList<IMessage> messages = new ArrayList<IMessage>();
        String where;
        String[] vals;
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

        Cursor c = context.getContentResolver().query(Constants.SMS_URI,
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
            public int compare(IMessage m1, IMessage m2) {
                return m1.getDate().compareTo(m2.getDate());
            }
        });
        return messages.subList(Math.max(0,messages.size()-max), messages.size());
	}

}
