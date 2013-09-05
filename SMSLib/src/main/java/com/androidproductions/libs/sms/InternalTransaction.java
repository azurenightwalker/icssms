package com.androidproductions.libs.sms;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import com.androidproductions.libs.sms.com.androidproductions.libs.sms.constants.MessageType;
import com.androidproductions.libs.sms.com.androidproductions.libs.sms.constants.SmsUri;
import com.androidproductions.libs.sms.com.androidproductions.libs.sms.readonly.IMessageView;

/*
 * InternalTransaction:
 *      Designed to encapsulate a single messaging transaction in its entirety without chaining
 *      function calls to get the desired behaviour.
 */
public class InternalTransaction {
    private final Context mContext;

    public InternalTransaction(final Context context)
    {
        mContext = context;
    }

    public boolean DeleteMessage(final IMessageView message)
    {
        return (!message.isLocked()) &&
                mContext.getContentResolver().delete(
                        message.getUri(),   // the user dictionary content URI
                        null,                    // the column to select on
                        null                      // the value to compare to
                ) == 1;
    }

    public void LockMessage(final IMessageView message)
    {
        final ContentValues values = new ContentValues();
        values.put("locked", 1);
        new Thread(new Runnable() {
            public void run() {
                mContext.getContentResolver().update(message.getUri(),
                        values, null, null);
            }
        }, "lockMessage").start();
    }

    public void UnlockMessage(final IMessageView message)
    {
        final ContentValues values = new ContentValues();
        values.put("locked", 0);
        new Thread(new Runnable() {
            public void run() {
                mContext.getContentResolver().update(message.getUri(),
                        values, null, null);
            }
        }, "unlockMessage").start();
    }

    public void MarkMessageRead(final IMessageView message)
    {
        if (message.isUnread())
        {
            final ContentValues values = new ContentValues();
            values.put("read", 1);
            new Thread(new Runnable() {
                public void run() {
                    mContext.getContentResolver().update(message.getUri(),
                            values, null, null);
                }
            }, "readMessage").start();
        }
    }

    public void MarkMessageUnread(final IMessageView message)
    {
        final ContentValues values = new ContentValues();
        values.put("read", 0);
        new Thread(new Runnable() {
            public void run() {
                mContext.getContentResolver().update(message.getUri(),
                        values, null, null);
            }
        }, "unreadMessage").start();
    }

    public void SaveDraft(final IMessageView message)
    {
        final ContentValues values = new ContentValues();

        values.put("body", message.getBody());
        values.put("address", message.getAddress());
        values.put("date", message.getDate());
        values.put("read", 1);
        values.put("type", MessageType.DRAFT);

        if (message.getAddress() != null) {
            final Long threadId = new Transaction(mContext).getOrCreateThreadId(message.getAddress());
            values.put("thread_id", threadId);
        }
        final ContentResolver resolver = mContext.getContentResolver();
        resolver.insert(SmsUri.DRAFT_URI, values);
    }


}
