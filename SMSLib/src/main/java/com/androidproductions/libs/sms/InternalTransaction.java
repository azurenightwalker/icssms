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
import com.androidproductions.libs.sms.com.androidproductions.libs.sms.readonly.IMessageView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/*
 * InternalTransaction:
 *      Designed to encapsulate a single messaging transaction in its entirety without chaining
 *      function calls to get the desired behaviour.
 */
public class InternalTransaction {
    private final Context mContext;

    public InternalTransaction(Context context)
    {
        mContext = context;
    }

    public boolean DeleteMessage(IMessageView message)
    {

    }

    public void LockMessage(IMessageView message)
    {

    }

    public void UnlockMessage(IMessageView message)
    {

    }

    public void MarkMessageRead(IMessageView message)
    {

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


}
