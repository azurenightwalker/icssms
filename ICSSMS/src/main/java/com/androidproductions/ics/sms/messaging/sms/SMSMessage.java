package com.androidproductions.ics.sms.messaging.sms;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.androidproductions.ics.sms.Constants;

public class SMSMessage extends SMSMessageBase{

	public SMSMessage(Context con, Cursor c) {
		super(con, c);
	}
	public SMSMessage(Context con, Cursor c, String address) {
		super(con, c,address);
	}
	public SMSMessage(Context con, String address, int type, String body, long date)
	{
		super(con,address,type,body,date);
	}
	public SMSMessage(Context con, Object[] msgs) {
		super(con, msgs);
	}
	
	public long queueSending()
	{
		ContentValues values = new ContentValues();
        values.put("address", Address);
        values.put("body", Body);
        values.put("date", Date);
        values.put("read", 1);
        values.put("type", Constants.MESSAGE_TYPE_QUEUED);
        uri = mContext.getContentResolver().insert(Constants.SMS_QUEUED_URI, values);
        Cursor c = mContext.getContentResolver().query(uri,null,null,null,null);
        if (c != null)
        {
            c.moveToFirst();
            long id =  c.getLong(1);
            c.close();
            return id;
        }
        return 0L;
	}
	
	public void markSendingFailed()
	{
		ContentValues values = new ContentValues();
        values.put("type", Constants.MESSAGE_TYPE_FAILED);
        mContext.getContentResolver().update(uri, values,null,null);
	}
	
	public void moveToOutbox()
	{
		ContentValues values = new ContentValues();
        values.put("type", Constants.MESSAGE_TYPE_OUTBOX);
        mContext.getContentResolver().update(uri, values,null,null);
	}
	
	public static boolean moveToFolder(Context context,Uri uri, int folder)
	{
		try
		{
		ContentValues values = new ContentValues();
        values.put("type", folder);
        context.getContentResolver().update(uri, values,null,null);
		if (folder == Constants.MESSAGE_TYPE_SENT)
		{
	        Cursor c = context.getContentResolver().query(uri,null,null,null,null);
            if (c != null)
            {
                if (c.moveToFirst())
                {
                    SMSMessage s = new SMSMessage(context, c);
                    s.Read = 1;
                    s.saveIncoming(true);
                    context.getContentResolver().delete(uri, null,null);
                }
                c.close();
            }
        }
			
        return true;
		} catch(Exception e) { e.printStackTrace(); return false; }
	}
	public SMSMessage getPrevious() {
		Cursor c = mContext.getContentResolver().query(Constants.SMS_SENT_URI, null, "thread_id = ?", new String[] { String.valueOf(getOrCreateThreadId(mContext, Address)) }, "date DESC");
		if (c != null) {
            if (c.moveToFirst())
            {
                SMSMessage message = new SMSMessage(mContext, c);
                c.close();
                return message;
            }
            c.close();
        }
		return null;
	}
	
	public Uri saveIncoming(boolean read) {
        return saveMessage(read);
    }
	
	Uri saveMessage(boolean read) {
        // Store the message in the content provider.
        ContentValues values = buildContentValues(read);
        findName();
        ContentResolver resolver = mContext.getContentResolver();
        return resolver.insert(Constants.SMS_INBOX_URI, values);
    }
	
	private ContentValues buildContentValues(boolean read) {
        // Store the message in the content provider.
        ContentValues values = new ContentValues();

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
            Long threadId = getOrCreateThreadId(mContext, Address);
            values.put("thread_id", threadId);
        }
        
        return values;
    }
	
	public static long getOrCreateThreadId (Context context, String recipient)
	{
	    Uri THREAD_ID_CONTENT_URI= Uri.parse("content://mms-sms/threadID");
	    String[] ID_PROJECTION ={BaseColumns._ID};
        Uri.Builder uriBuilder=THREAD_ID_CONTENT_URI.buildUpon();
        uriBuilder.appendQueryParameter("recipient",recipient);
        Uri uri=uriBuilder.build();
        if (uri != null)
        {
            Cursor cursor=context.getContentResolver().query(uri,ID_PROJECTION,null ,null ,null );
            if (cursor!=null )
            {
                try
                {
                    if (cursor.moveToFirst())
                    {
                        return cursor.getLong(0);
                    }
                    else
                    {
                        Log.e("","getOrCreateThreadId returned no rows ! ");
                    }
                }
                finally
                {
                    cursor.close();
                }
            }
        }
        throw new IllegalArgumentException("Unable to find or allocate a thread ID . ");
    }
}
