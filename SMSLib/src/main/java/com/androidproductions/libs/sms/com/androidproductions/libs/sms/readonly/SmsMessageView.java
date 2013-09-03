package com.androidproductions.libs.sms.com.androidproductions.libs.sms.readonly;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;

import com.androidproductions.libs.sms.SmsMessage;
import com.androidproductions.libs.sms.com.androidproductions.libs.sms.constants.SmsUri;

import java.util.Calendar;

public class SmsMessageView extends SmsMessage implements IMessageView{
    public final Context mContext;
    private String Name;
    final int Type;
    public int Read;
    private int Locked;
    public long ThreadId;
    public Uri uri;
    public int SummaryCount;
    Long DateSent;
    int Seen;

    public SmsMessageView(final Context con, final Cursor c)
    {
        super(c.getString(c.getColumnIndex("body")),
                c.getString(c.getColumnIndex("address")),
                c.getLong(c.getColumnIndex("date")),
                c.getLong(0));
        mContext = con;
        Type = c.getInt(c.getColumnIndex("type"));
        ThreadId = c.getLong(c.getColumnIndex("thread_id"));
        SummaryCount = 0;
        Read = c.getInt(c.getColumnIndex("read"));
        Locked = c.getInt(c.getColumnIndex("locked"));
        uri = ContentUris.withAppendedId(SmsUri.BASE_URI, Id);
    }

    public boolean IsIncoming()
    {
        return Type == 1;
    }

    public String getSummaryHeader()
    {
        return getContactName() + " (" + SummaryCount + ")";
    }

    public String getContactName()
    {
        if (Name == null)
            findName();
        return Name;
    }

    @Override
    public Bitmap getContactPhoto() {
        return null;
    }

    @Override
    public Bitmap getConversationContactImage() {
        return null;
    }

    void findName() {
        Name = Addresses[0];
        try
        {
            final Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(Addresses[0]));
            if (uri != null)
            {
                final Cursor c = mContext.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID},null,null,null);
                if (c != null)
                {
                    if (c.moveToFirst())
                    {
                        Name = c.getString(c.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                    }
                    c.close();
                }
            }
        }
        catch(Exception e){ e.printStackTrace(); }
    }

    public String GetDateString() {
        final java.util.Date date = new java.util.Date(Date);
        final java.text.DateFormat dateFormat =
                android.text.format.DateFormat.getDateFormat(mContext);
        final java.text.DateFormat timeFormat =
                android.text.format.DateFormat.getTimeFormat(mContext);
        return dateFormat.format(date) + " " + timeFormat.format(date);
    }

    public String GetShortDateString() {
        final java.util.Date date = new java.util.Date(Date);
        final Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -1);  // number of days to add
        if (date.before(c.getTime()))
            return android.text.format.DateFormat.getDateFormat(mContext).format(date);
        else
            return android.text.format.DateFormat.getTimeFormat(mContext).format(date);
    }

    public String getAddress() {
        return Addresses[0];
    }

    public boolean isUnread() {
        return Read != 1;
    }

    public boolean isLocked() {
        return Locked == 1;
    }

    @Override
    public IMessageView getPrevious() {
        return null;
    }

    public boolean sendingFailed() {
        return Type == 5;
    }


}
