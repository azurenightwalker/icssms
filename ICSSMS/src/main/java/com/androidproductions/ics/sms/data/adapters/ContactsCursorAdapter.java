package com.androidproductions.ics.sms.data.adapters;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidproductions.ics.sms.R;
import com.androidproductions.ics.sms.data.ContactHelper;

public class ContactsCursorAdapter extends CursorAdapter {

	private final Context mContext;

    public ContactsCursorAdapter(final Context context) {
    	super(context,null,0);
        mContext = context;
    }

    @Override
    public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
    	final LayoutInflater inflater = LayoutInflater.from(context);
        final RelativeLayout ret = (RelativeLayout) inflater.inflate(R.layout.autocomplete_contact, parent, false);
        if (ret != null)
        {
            final TextView mName = (TextView) ret.findViewById(R.id.contact_name);
            final TextView mNumber = (TextView) ret.findViewById(R.id.contact_number);
            final TextView mLabel = (TextView) ret.findViewById(R.id.type);

            final int nameIdx = cursor.getColumnIndexOrThrow(Phone.DISPLAY_NAME);
            final int id = cursor.getColumnIndex(Phone.CONTACT_ID);
            final int num = cursor.getColumnIndex(Phone.NUMBER);
            final ContactHelper ch = new ContactHelper(context,cursor.getLong(id));
            final String name = cursor.getString(nameIdx);
            final String number = cursor.getString(num);
            final CharSequence displayLabel = Phone.getTypeLabel(mContext.getResources(), cursor.getInt(cursor.getColumnIndex(Phone.TYPE)), cursor.getString(cursor.getColumnIndex(Phone.LABEL)));

            mName.setText(name);
            mNumber.setText(number);
            mLabel.setText(displayLabel);

            /**
             * Always add an icon, even if it is null. Keep the layout children
             * indices consistent.
             */
            ((ImageView) ret.findViewById(R.id.contact_photo)).setImageBitmap(ch.getContactImage());
            return ret;
        }
        return null;
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        final int nameIdx = cursor.getColumnIndexOrThrow(Phone.DISPLAY_NAME);
        final int id = cursor.getColumnIndex(Phone.CONTACT_ID);
        final int num = cursor.getColumnIndex(Phone.NUMBER);
        final ContactHelper ch = new ContactHelper(context,cursor.getLong(id));
        final String name = cursor.getString(nameIdx);
        final String number = cursor.getString(num);
        final CharSequence displayLabel = Phone.getTypeLabel(mContext.getResources(), cursor.getInt(cursor.getColumnIndex(Phone.TYPE)), cursor.getString(cursor.getColumnIndex(Phone.LABEL)));


        /**
         * Always add an icon, even if it is null. Keep the layout children
         * indices consistent.
         */
        // notice views have already been inflated and layout has already been set so all you need to do is set the data
        ((TextView) view.findViewById(R.id.contact_name)).setText(name);
        ((TextView) view.findViewById(R.id.type)).setText(displayLabel);
        ((TextView) view.findViewById(R.id.contact_number)).setText(number);
        ((ImageView) view.findViewById(R.id.contact_photo)).setImageBitmap(ch.getContactImage());
    }
}
