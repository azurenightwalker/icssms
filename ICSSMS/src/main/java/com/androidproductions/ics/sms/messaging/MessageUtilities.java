package com.androidproductions.ics.sms.messaging;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.androidproductions.ics.sms.messaging.sms.SMSUtilities;
import com.androidproductions.ics.sms.utils.AddressUtilities;
import com.androidproductions.libs.sms.InternalTransaction;
import com.androidproductions.libs.sms.Transaction;
import com.androidproductions.libs.sms.com.androidproductions.libs.sms.constants.MessageType;
import com.androidproductions.libs.sms.com.androidproductions.libs.sms.constants.SmsUri;
import com.androidproductions.libs.sms.com.androidproductions.libs.sms.readonly.ConversationSummary;
import com.androidproductions.libs.sms.com.androidproductions.libs.sms.readonly.IMessageView;
import com.androidproductions.libs.sms.com.androidproductions.libs.sms.readonly.SmsMessageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class MessageUtilities {

    private MessageUtilities() {
    }

    public static List<IMessageView> GetUnreadMessages(final Context context)
    {
        final ArrayList<IMessageView> messages = new ArrayList<IMessageView>();
        final Cursor c = context.getContentResolver().query(SmsUri.INBOX_URI, null, "read = '0'",
                null, "date ASC");
        if (c != null)
        {
            if (c.moveToFirst())
            {
                do
                {
                    messages.add(new SmsMessageView(context,c));
                } while (c.moveToNext());
            }
            c.close();
        }

        return messages;
    }

    public static List<IMessageView> GetUnsentMessages(final Context context)
    {
        final ArrayList<IMessageView> messages = new ArrayList<IMessageView>();
        final Cursor c = context.getContentResolver().query(SmsUri.BASE_URI, null, "type = ?",
                new String[] { String.valueOf(MessageType.FAILED) }, "date ASC");
        if (c != null)
        {
            if (c.moveToFirst())
            {
                do
                {
                    messages.add(new SmsMessageView(context,c));
                } while (c.moveToNext());
            }
            c.close();
        }

        return messages;
    }

    public static void SaveDraftMessage(final Context context, final String address, final String message)
    {
        DeleteMessageDraft(context, address);
        if (!message.equals(""))
            new InternalTransaction(context).SaveDraft(new SmsMessageView(context,address,message,System.currentTimeMillis()));
    }

    private static void DeleteMessageDraft(final Context context, final String address) {
        final long tid = new Transaction(context).getOrCreateThreadId(address);
        final Cursor c = context.getContentResolver().query(SmsUri.DRAFT_URI,null,"thread_id = ?",
                new String[] { String.valueOf(tid) },"date DESC LIMIT 1");
        if (c != null)
        {
            if (c.moveToFirst())
            {
                context.getContentResolver().delete(
                        ContentUris.withAppendedId(SmsUri.BASE_URI, c.getLong(c.getColumnIndex("_id"))),   // the user dictionary content URI
                        null,                    // the column to select on
                        null                      // the value to compare to
                );
            }
            c.close();
        }
    }

    public static String RetrieveDraftMessage(final Context context, final String address)
    {
        String draft = "";
        final long tid = new Transaction(context).getOrCreateThreadId(address);
        final Cursor c = context.getContentResolver().query(SmsUri.DRAFT_URI,null,"thread_id = ?",
                new String[] { String.valueOf(tid) },"date DESC LIMIT 1");
        if (c != null)
        {
            if (c.moveToFirst())
            {
                draft = c.getString(c.getColumnIndex("body"));
            }
            c.close();
        }
        return draft;
    }
	
	public static Long SendMessage(final Context context, final String message, final String destination)
	{
		return SMSUtilities.sendSms(context, message, destination);
	}
	
	public static IMessageView GenerateMessage(final Context context, final String address, final String message, final long time)
	{
		return SMSUtilities.Generate(context, address, message, time);
	}

	public static List<ConversationSummary> GetMessageSummary(final Context context)
	{
		final List<ConversationSummary> messages = new ArrayList<ConversationSummary>();
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
        Collections.sort(messages, new Comparator<ConversationSummary>() {
            public int compare(final ConversationSummary m1, final ConversationSummary m2) {
                return m2.getDate().compareTo(m1.getDate());
            }
        });
		
		return messages;
	}

	private static void processCursor(final Context context, final List<ConversationSummary> messages,
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
                        final int bodyCol = c2.getColumnIndex("body");
                        final int dateCol = c2.getColumnIndex("date");
                        final String address = AddressUtilities.StandardiseNumber(c2.getString(addressCol),context);
                        final ConversationSummary sms =
                                new ConversationSummary(context,c2,address,
                                        c2.getString(bodyCol),c2.getLong(dateCol),c2.getCount());
                        messages.add(sms);
                    }
                    c2.close();
                }
			} while (c.moveToNext());
		}
	}

    public static List<IMessageView> GetMessages(final Context context, final long threadId, final long contactId, final int max)
    {
        return GetMessages(context, threadId, contactId, max,null);
    }

	public static List<IMessageView> GetMessages(final Context context, final long threadId, final long contactId, final int max, final Long date)
	{
		final ArrayList<IMessageView> messages = new ArrayList<IMessageView>();
        final String where;
        final String[] vals;
        if (date == null)
        {
            where = "thread_id = ? and type != ? ";
            vals = new String[] { String.valueOf(threadId), String.valueOf(MessageType.DRAFT) };
        }
        else
        {
            where = "thread_id = ? and date < ? and type != ? ";
            vals = new String[] { String.valueOf(threadId), String.valueOf(date), String.valueOf(MessageType.DRAFT) };
        }

        final Cursor c = context.getContentResolver().query(SmsUri.BASE_URI,
                null, where, vals, "date DESC LIMIT "+max);
        if (c != null)
        {
            if (c.moveToFirst())
            {
                do
                {
                    messages.add(new SmsMessageView(context,c,contactId));
                } while (c.moveToNext());
            }
            c.close();
        }
        Collections.sort(messages, new Comparator<IMessageView>() {
            public int compare(final IMessageView m1, final IMessageView m2) {
                return m1.getDate().compareTo(m2.getDate());
            }
        });
        return messages.subList(Math.max(0,messages.size()-max), messages.size());
	}

}
