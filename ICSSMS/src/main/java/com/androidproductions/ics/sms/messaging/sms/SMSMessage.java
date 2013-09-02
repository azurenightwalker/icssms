package com.androidproductions.ics.sms.messaging.sms;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.androidproductions.libs.sms.com.androidproductions.libs.sms.constants.SmsUri;
import com.androidproductions.libs.sms.Transaction;

public class SMSMessage extends SMSMessageBase{

	public SMSMessage(final Context con, final Cursor c) {
		super(con, c);
	}
	public SMSMessage(final Context con, final Cursor c, final String address) {
		super(con, c,address);
	}
	public SMSMessage(final Context con, final String address, final int type, final String body, final long date)
	{
		super(con,address,type,body,date);
	}
	public SMSMessage(final Context con, final Object[] msgs) {
		super(con, msgs);
	}

	public SMSMessage getPrevious() {
		final Cursor c = mContext.getContentResolver().query(SmsUri.SENT_URI, null, "thread_id = ?",
                new String[] { String.valueOf(new Transaction(mContext).getOrCreateThreadId(Address)) }, "date DESC");
		if (c != null) {
            if (c.moveToFirst())
            {
                final SMSMessage message = new SMSMessage(mContext, c);
                c.close();
                return message;
            }
            c.close();
        }
		return null;
	}
	
	public Uri saveIncoming(final boolean read) {
        return saveMessage(read);
    }
	
	Uri saveMessage(final boolean read) {
        // Store the message in the content provider.
        final ContentValues values = buildContentValues(read);
        findName();
        final ContentResolver resolver = mContext.getContentResolver();
        return resolver.insert(SmsUri.INBOX_URI, values);
    }
	
	private ContentValues buildContentValues(final boolean read) {
        // Store the message in the content provider.
        final ContentValues values = new ContentValues();

        values.put("body", Body);
        values.put("address", Address);

        // Use now for the timestamp to avoid confusion with clock
        // drift between the handset and the SMSC.
        // Check to make sure the system is giving us a non-bogus time.
        //Calendar buildDate = new GregorianCalendar(2011, 8, 18);    // 18 Sep 2011
        //Calendar nowDate = new GregorianCalendar();
        //long now = System.currentTimeMillis();
        //nowDate.setTimeInMillis(now);
        values.put("date", Date);
        //values.put(Inbox.DATE_SENT, DateSent);
        values.put("protocol", Protocol);
        values.put("read", read ? 1 : 0);
        values.put("seen", 0);
        values.put("type", Type);
        values.put("subject", Subject);
        values.put("reply_path_present", ReplyPathPresent);
        values.put("service_center", ServiceCentre);
        
        if (Address != null) {
            final Long threadId = new Transaction(mContext).getOrCreateThreadId(Address);
            values.put("thread_id", threadId);
        }
        
        return values;
    }
}
