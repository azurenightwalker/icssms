package com.androidproductions.libs.sms;

import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Log;

import com.androidproductions.libs.sms.com.androidproductions.libs.sms.constants.Action;
import com.androidproductions.libs.sms.com.androidproductions.libs.sms.constants.MessageType;
import com.androidproductions.libs.sms.com.androidproductions.libs.sms.constants.SmsUri;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/*
 * Transaction:
 *      Designed to encapsulate a single messaging transaction in its entirety without chaining
 *      function calls to get the desired behaviour.
 */
public class Transaction {
    private final Context mContext;

    public Transaction(final Context context)
    {
        mContext = context;
    }

    public void requeueMessage(final Uri uri)
    {
        final ContentValues cv = new ContentValues();
        cv.put("type",MessageType.QUEUED);
        mContext.getContentResolver().update(uri,cv,null,null);
    }

    public List<Uri> queueMessage(final SmsMessage sms)
    {
        final List<Uri> res = new ArrayList<Uri>();
        sms.setThreadId(getOrCreateThreadId(sms.getAddresses()));
        for(final String address : sms.getAddresses())
        {
            final ContentValues cv = new ContentValues();
            cv.put("address", address);
            cv.put("body", sms.getBody());
            cv.put("date", sms.getDate());
            cv.put("read", 1);
            cv.put("type", MessageType.QUEUED);
            cv.put("thread_id", sms.getThreadId());
            res.add(mContext.getContentResolver().insert(SmsUri.QUEUED_URI, cv));
        }
        return res;
    }

    public List<Uri> sendMessage(final SmsMessage message, final Long threadId)
    {
        return sendSmsMessage(message.getBody(), message.getAddresses(),message.getId(), threadId);
    }

    private List<Uri> sendSmsMessage(final String text, final String[] addresses, final Long messageId, Long threadId)
    {
        final List<Uri> res = new ArrayList<Uri>(addresses.length);
        if (threadId == null) {
            threadId = getOrCreateThreadId(addresses);
        }

        for (String address : addresses) {
            res.add(sendSmsMessage(text, address, messageId, threadId));
        }
        return res;
    }

    private Uri sendSmsMessage(final String text, final String address, final Long messageId,Long threadId)
    {
        // save the message for each of the addresses
        final Calendar cal = Calendar.getInstance();
        final ContentValues values = new ContentValues();
        values.put("address", address);
        values.put("body", text);
        values.put("read", 1);

        // attempt to create correct thread id if one is not supplied
        if (threadId == null) {
            threadId = getOrCreateThreadId(address);
        }

        values.put("thread_id", threadId);
        values.put("type", MessageType.OUTBOX);
        final Uri inserted;
        if (messageId != null)
        {
            boolean canSend = false;
            Cursor c = mContext.getContentResolver().query(SmsUri.BASE_URI, null,"_id = ?", new String[] { String.valueOf(messageId)},null,null);
            if (c != null)
            {
                if (c.moveToFirst())
                {
                    canSend = c.getInt(c.getColumnIndex("type")) == MessageType.QUEUED;
                }
            }
            if (!canSend)
                return null;
            mContext.getContentResolver().update(SmsUri.BASE_URI, values,"_id = ?", new String[] { String.valueOf(messageId)});
            inserted = ContentUris.withAppendedId(SmsUri.BASE_URI,messageId);
        }
        else
        {
            values.put("date", cal.getTimeInMillis());
            inserted = mContext.getContentResolver().insert(SmsUri.OUTBOX_URI, values);
        }
        final Intent sendIntent = new Intent(Action.SENT);
        sendIntent.putExtra("SMSURI",inserted);
        final Intent deliveredIntent = new Intent(Action.DELIVERED);
        sendIntent.putExtra("SMSURI",inserted);
        final PendingIntent sentPI = PendingIntent.getBroadcast(mContext, 0, sendIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        final PendingIntent deliveredPI = PendingIntent.getBroadcast(mContext, 0, deliveredIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        final ArrayList<PendingIntent> sPI = new ArrayList<PendingIntent>();
        final ArrayList<PendingIntent> dPI = new ArrayList<PendingIntent>();

        final SmsManager smsManager = SmsManager.getDefault();

        final ArrayList<String> parts = smsManager.divideMessage(text);

        for (String part : parts) {
            sPI.add(sentPI);
            dPI.add(deliveredPI);
        }

        try {
            smsManager.sendMultipartTextMessage(PhoneNumberUtils.stripSeparators(address), null, parts, sPI, dPI);
        } catch (Exception e) {
            e.printStackTrace();
            failedMessage(inserted);
        }
        return inserted;
    }

    public void sentMessage(final Uri uri)
    {
        final ContentValues cv = new ContentValues();
        cv.put("type", MessageType.SENT);
        mContext.getContentResolver().update(uri, cv, null, null);
    }

    public void recievedMessage(final Uri uri)
    {
        final ContentValues cv = new ContentValues();
        cv.put("seen",1);
        mContext.getContentResolver().update(uri,cv,null,null);
    }

    public void failedMessage(final Uri uri)
    {
        final ContentValues cv = new ContentValues();
        cv.put("type",MessageType.FAILED);
        mContext.getContentResolver().update(uri,cv,null,null);
    }

    public long getOrCreateThreadId(final String recipient) {
        return getOrCreateThreadId(new String[] { recipient });
    }

    /**
     * Given the recipients list and subject of an unsaved message,
     * return its thread ID.  If the message starts a new thread,
     * allocate a new thread ID.  Otherwise, use the appropriate
     * existing thread ID.
     *
     * Find the thread ID of the same set of recipients (in
     * any order, without any additions). If one
     * is found, return it.  Otherwise, return a unique thread ID.
     */
    public long getOrCreateThreadId(final String[] recipients) {
        final Uri.Builder uriBuilder = SmsUri.THREAD_URI.buildUpon();

        for (final String recipient : recipients) {
            uriBuilder.appendQueryParameter("recipient", recipient);
        }

        final Uri uri = uriBuilder.build();

        final Cursor cursor = mContext.getContentResolver().query(uri, new String[]{"_id"}, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return cursor.getLong(0);
                } else {
                    Log.e("SMS", "getOrCreateThreadId returned no rows!");
                }
            } finally {
                cursor.close();
            }
        }

        Log.e("SMS", "getOrCreateThreadId failed with uri " + uri.toString());
        throw new IllegalArgumentException("Unable to find or allocate a thread ID.");
    }
}
