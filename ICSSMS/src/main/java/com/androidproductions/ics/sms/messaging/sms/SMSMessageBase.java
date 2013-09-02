package com.androidproductions.ics.sms.messaging.sms;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsMessage;
import android.util.Log;

import com.androidproductions.ics.sms.R;
import com.androidproductions.ics.sms.data.ContactHelper;
import com.androidproductions.ics.sms.data.ImageCache;
import com.androidproductions.ics.sms.messaging.IMessage;
import com.androidproductions.ics.sms.utils.TextUtilities;
import com.androidproductions.libs.sms.MessageType;
import com.androidproductions.libs.sms.SmsUri;

import java.io.InputStream;
import java.util.Calendar;

public abstract class SMSMessageBase implements IMessage{
	public final Context mContext;
	public String Address;
	public String Body;
	private String Name;
	final Long Date;
	public long ID;
	final int Type;
	public int Read;
	private int Locked;
	public long ThreadId;
	private long ContactID;
	public Uri uri;
	public int SummaryCount;
	Long DateSent;
	int Protocol;
	int Seen;
	String Subject;
	int ReplyPathPresent;
	String ServiceCentre;
	public boolean HasAttachment;

    private final ContactHelper contactHelper;
	
	SMSMessageBase(final Context con, final Cursor c)
	{
		mContext = con;
		final int typeCol = c.getColumnIndex("type");
		final int threadCol = c.getColumnIndex("thread_id");
    	final int addressCol = c.getColumnIndex("address");
    	final int bodyCol = c.getColumnIndex("body");
    	final int dateCol = c.getColumnIndex("date");
    	Address = c.getString(addressCol);
    	Type = c.getInt(typeCol);
    	Body = c.getString(bodyCol);
    	Date = c.getLong(dateCol);
    	ThreadId = c.getLong(threadCol);
    	ContactID = -1;
    	SummaryCount = 0;
    	Read = c.getInt(c.getColumnIndex("read"));
    	ID = c.getLong(0);
    	Locked = c.getInt(c.getColumnIndex("locked"));
    	uri = ContentUris.withAppendedId(SmsUri.BASE_URI, ID);
    	HasAttachment = false;
        contactHelper = new ContactHelper(mContext);
    }
	
	SMSMessageBase(final Context con, final String address, final int type, final String body, final long date)
	{
		ID = -1;
		mContext = con;
    	Address = address;
    	Type = type;
    	Body = body;
    	Date = date;
    	ContactID = -1;
    	ThreadId = -1L;
    	HasAttachment = false;
    	if (Body == null) Body = "";
        contactHelper = new ContactHelper(mContext);
    }
	
	SMSMessageBase(final Context con, final Cursor c, final String address)
	{
		this(con,c);
    	Address = address;
    }
	
	SMSMessageBase(final Context con, final Object[] messages)
	{
		final SmsMessage[] msgs = new SmsMessage[messages.length];
		for (int n = 0; n < messages.length; n++) {
			msgs[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
		}
		final SmsMessage smsa = msgs[0];
		final int pduCount = msgs.length;
        if (pduCount == 1) {
            // There is only one part, so grab the body directly.
            Body = smsa.getDisplayMessageBody();
        } else {
            // Build up the body from the parts.
            final StringBuilder bdy = new StringBuilder();
            for (final SmsMessage smsb : msgs) {
                bdy.append(smsb.getDisplayMessageBody());
            }
            Body = TextUtilities.replaceFormFeeds(bdy.toString());
        }
        Address = smsa.getOriginatingAddress();
        Type = MessageType.INBOX;
        Date = System.currentTimeMillis();
        mContext = con;
        DateSent =  smsa.getTimestampMillis();
        Protocol =  smsa.getProtocolIdentifier();
        Read = 0;
        Seen = 0;
        Subject = "";
        if (!smsa.getPseudoSubject().isEmpty())
        	Subject = smsa.getPseudoSubject();
        ReplyPathPresent = smsa.isReplyPathPresent() ? 1 : 0;
        ServiceCentre = smsa.getServiceCenterAddress();
        ContactID = -1;
        contactHelper = new ContactHelper(mContext);
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

	void findName() {
		Name = Address;
		try
		{
			final Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(Address));
			if (uri != null)
            {
                final Cursor c = mContext.getContentResolver().query(uri, new String[]{PhoneLookup.DISPLAY_NAME, PhoneLookup._ID},null,null,null);
                if (c != null)
                {
                    if (c.moveToFirst())
                    {
                        Name = c.getString(c.getColumnIndex(PhoneLookup.DISPLAY_NAME));
                        ContactID = c.getLong(c.getColumnIndex(PhoneLookup._ID));
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
	
	public Bitmap getContactPhoto()
	{
        if (IsIncoming())
        {
            if (ContactID < 0)
                getContactName();
            contactHelper.setId(ContactID);
        }
        else
        {
            return contactHelper.getProfileContactImage();
        }
        return contactHelper.getContactImage();
	}
	
	public Bitmap getConversationContactImage() {
		try
		{
			if (ContactID < 0)
				getContactName();
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
            Log.e("SMS","DD",ex);
            return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_contact_picture);
		}
	}
	
	public void lockMessage() {
        final Uri lockUri = ContentUris.withAppendedId(SmsUri.BASE_URI, ID);

        final ContentValues values = new ContentValues(1);
        Locked = 1;
        values.put("locked", 1);

        new Thread(new Runnable() {
            public void run() {
                if (lockUri != null)
                    mContext.getContentResolver().update(lockUri,
                            values, null, null);
            }
        }, "lockMessage").start();
    }
	
	public void unlockMessage() {
        final Uri lockUri = ContentUris.withAppendedId(SmsUri.BASE_URI, ID);

        final ContentValues values = new ContentValues(1);
        Locked = 0;
        values.put("locked", 0);

        new Thread(new Runnable() {
            public void run() {
                if (lockUri != null)
                    mContext.getContentResolver().update(lockUri,
                            values, null, null);
            }
        }, "lockMessage").start();
    }

	public boolean deleteMessage() {
		return (Locked == 0) &&
		    mContext.getContentResolver().delete(
                ContentUris.withAppendedId(SmsUri.BASE_URI,ID),   // the user dictionary content URI
                null,                    // the column to select on
                null                      // the value to compare to
            ) == 1;
	}
	
	public void markAsRead()
	{
		if (Read != 1)
		{
			final ContentValues values = new ContentValues();
			values.put("read", 1);
			Read = 1;
			new Thread(new Runnable() {
	            public void run() {
	                mContext.getContentResolver().update(uri,
	                        values, null, null);
	            }
	        }, "readMessage").start();
		}
		
	}
	public void markAsUnread()
	{
		if (Read != 0)
		{
			final ContentValues values = new ContentValues();
			values.put("read", 0);
			Read = 0;
			new Thread(new Runnable() {
	            public void run() {
	                mContext.getContentResolver().update(uri,
	                        values, null, null);
	            }
	        }, "unreadMessage").start();
		}
	}

	public String getText() {
		return Body;
	}

	public boolean hasAttachments() {
		return HasAttachment;
	}

	public String getAddress() {
		return Address;
	}

	public boolean isUnread() {
		return Read != 1;
	}

	public Long getThreadId() {
		return ThreadId;
	}

	public Long getDate() {
		return Date;
	}

	public boolean isLocked() {
		return Locked == 1;
	}

	public long getId() {
		return ID;
	}

	public boolean sendingFailed() {
		return Type == 5;
	}
}