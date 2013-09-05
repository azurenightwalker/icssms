package com.androidproductions.libs.sms.com.androidproductions.libs.sms.readonly;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;

import java.io.InputStream;
import java.util.Calendar;

public class ConversationSummary{
    private String Name;
    private final String[] Addresses;
    private final int Read;
    private final long ThreadId;
    private Uri uri;
    private final int SummaryCount;
    private final long Date;
    private final String Body;
    private final Context mContext;
    private long ContactID;

    public ConversationSummary(final Context context, final Cursor c, final String address, final String snippet, final long date) {
        this(context, c, address, snippet, date,c.getInt(c.getColumnIndex("message_count")));
    }

    public ConversationSummary(final Context context, final Cursor c, final String address, final String snippet, final long date, final int summaryCount) {
        super();
        Addresses = new String[] {address};
        SummaryCount = summaryCount;
        Read = c.getInt(c.getColumnIndex("read"));
        ThreadId = c.getLong(0);
        Date = date;
        Body = snippet;
        mContext = context;
        findName();
    }


    public String getSummaryHeader()
    {
        return Name + " (" + SummaryCount + ")";
    }

    public Long getThreadId()
    {
        return ThreadId;
    }

    final void findName() {
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
                        ContactID = c.getLong(c.getColumnIndex(ContactsContract.PhoneLookup._ID));
                    }
                    c.close();
                }
            }
        }
        catch(Exception e){ e.printStackTrace(); }
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

    public Long getDate()
    {
        return Date;
    }

    public String getBody() {
        return Body;
    }

    public boolean isUnread() {
        return Read == 0;
    }

    public String getAddress() {
        return Addresses[0];
    }

    public Bitmap getConversationContactImage()
    {
        try
        {
            if (ContactID < 0)
                return ImageCache.getDefault();
            Bitmap img = ImageCache.getItem(ContactID);
            if (img != null) return img;
            final Uri mContactLookupUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, ContactID);
            final InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(mContext.getContentResolver(),mContactLookupUri);
            if (input == null)
            {
                return ImageCache.getItem(0L);
            }
            img = BitmapFactory.decodeStream(input);
            ImageCache.putItem(ContactID, img);
            return img;
        }
        catch(Exception ex)
        {
            return ImageCache.getDefault();
        }
    }

}
