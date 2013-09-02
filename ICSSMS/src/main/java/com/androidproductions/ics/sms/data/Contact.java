package com.androidproductions.ics.sms.data;

import android.database.Cursor;
import android.provider.ContactsContract;

class Contact {
    private final String displayName;
    private final String phoneNumber;

    public Contact(final String name, final String number)
    {
        displayName = name;
        phoneNumber = number;
    }

    public Contact(final Cursor cur)
    {
        displayName = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
        phoneNumber = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
