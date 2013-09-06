package com.androidproductions.ics.sms.data;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.content.CursorLoader;

import com.androidproductions.ics.sms.R;
import com.androidproductions.libs.sms.com.androidproductions.libs.sms.readonly.ImageCache;
import com.androidproductions.logging.LogHelper;

import java.io.InputStream;

public class ContactHelper {
	private final Context mContext;
	private Long mId;

    public ContactHelper(final Context context, final Long id)
    {
        mContext = context;
        mId = id;
    }

    public void setId(final Long id)
    {
        mId = id;
    }

    public long getId()
    {
        return mId;
    }

    public ContactHelper(final Context context)
    {
        mContext = context;
        mId = -1L;
    }

    public Bitmap getContactImage()
    {
        Bitmap image = ImageCache.getItem(mId);
        if (image != null)
            return image;
        image = _getContactImage();
        if (image == null)
        {
            image = ImageCache.getItem(0L);
            return image == null ? getDefaultBitmap() : image;
        }
        ImageCache.putItem(mId, image);
        return image;
    }

    public Bitmap getProfileContactImage()
    {
        Bitmap image = ImageCache.getItem(999999L);
        if (image != null)
            return image;
        image = _getProfileContactImage();
        if (image == null)
        {
            image = ImageCache.getItem(0L);
            return image == null ? getDefaultBitmap() : image;
        }
        ImageCache.putItem(999999L, image);
        return image;
    }

    private Bitmap getDefaultBitmap() {
        return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_contact_picture);
    }

    private Bitmap _getContactImage()
    {
        try
        {
            final Uri mContactLookupUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, mId);
            if (mContactLookupUri != null)
            {
                final InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(mContext.getContentResolver(),mContactLookupUri);
                if (input != null)
                {
                    final Bitmap img = BitmapFactory.decodeStream(input);
                    input.close();
                    return img;
                }
            }
        }
        catch(Exception ex)
        {
            LogHelper.getInstance().i(ex.getMessage());
        }
        return null;
    }

    private Bitmap _getProfileContactImage()
    {
        try
        {
            final Uri mContactLookupUri = ContactsContract.Profile.CONTENT_URI;
            if (mContactLookupUri != null)
            {
                final InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(mContext.getContentResolver(),mContactLookupUri);
                if (input != null)
                {
                    final Bitmap img = BitmapFactory.decodeStream(input);
                    input.close();
                    return img;
                }
            }
        }
        catch(Exception ex)
        {
            LogHelper.getInstance().i(ex.getMessage());
        }
        return null;
    }

    public String getContactName(final String phoneNumber)
    {
        String name = null;
        final Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        if (uri != null)
        {
            final Cursor c = mContext.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID},null,null,null);
            if (c != null)
            {
                if (c.moveToFirst())
                {
                    name = c.getString(c.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                    mId = c.getLong(c.getColumnIndex(ContactsContract.PhoneLookup._ID));
                }
                c.close();
            }
        }
        return name;
    }

    public Uri getContactUri()
    {
        if (mId == null) return null;
        return ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,mId);
    }

    public CursorLoader getPhoneCursor()
    {
        final String[] projection = new String[]{
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.LABEL
        };
        return new CursorLoader(mContext, ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
    }
}
