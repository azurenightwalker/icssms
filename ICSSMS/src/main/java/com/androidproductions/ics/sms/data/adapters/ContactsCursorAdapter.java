package com.androidproductions.ics.sms.data.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.DataUsageFeedback;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidproductions.ics.sms.R;
import com.androidproductions.ics.sms.data.ContactHelper;

public class ContactsCursorAdapter extends CursorAdapter {

	private final Context mContext;

    private final LruCache<Long,Bitmap> ImageCache;

    public ContactsCursorAdapter(Context context, Cursor c) {
    	super(context,c,0);
        mContext = context;
        ImageCache = new LruCache<Long, Bitmap>(20);
        ImageCache.put(0L, BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
    	final LayoutInflater inflater = LayoutInflater.from(context);
        final RelativeLayout ret = (RelativeLayout) inflater.inflate(R.layout.autocomplete_contact, parent, false);
        if (ret != null)
        {
            TextView mName = (TextView) ret.findViewById(R.id.contact_name);
            TextView mNumber = (TextView) ret.findViewById(R.id.contact_number);
            TextView mLabel = (TextView) ret.findViewById(R.id.type);

            int nameIdx = cursor.getColumnIndexOrThrow(Phone.DISPLAY_NAME);
            int id = cursor.getColumnIndex(Phone.CONTACT_ID);
            int num = cursor.getColumnIndex(Phone.NUMBER);
            ContactHelper ch = new ContactHelper(context,cursor.getLong(id));
            String name = cursor.getString(nameIdx);
            String number = cursor.getString(num);
            CharSequence displayLabel = Phone.getTypeLabel(mContext.getResources(), cursor.getInt(cursor.getColumnIndex(Phone.TYPE)), cursor.getString(cursor.getColumnIndex(Phone.LABEL)));

            mName.setText(name);
            mNumber.setText(number);
            mLabel.setText(displayLabel);

            /**
             * Always add an icon, even if it is null. Keep the layout children
             * indices consistent.
             */
            ((ImageView) ret.findViewById(R.id.contact_photo)).setImageBitmap(ch.getContactImage(ImageCache));
            return ret;
        }
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int nameIdx = cursor.getColumnIndexOrThrow(Phone.DISPLAY_NAME);
        int id = cursor.getColumnIndex(Phone.CONTACT_ID);
        int num = cursor.getColumnIndex(Phone.NUMBER);
        ContactHelper ch = new ContactHelper(context,cursor.getLong(id));
        String name = cursor.getString(nameIdx);
        String number = cursor.getString(num);
        CharSequence displayLabel = Phone.getTypeLabel(mContext.getResources(), cursor.getInt(cursor.getColumnIndex(Phone.TYPE)), cursor.getString(cursor.getColumnIndex(Phone.LABEL)));


        /**
         * Always add an icon, even if it is null. Keep the layout children
         * indices consistent.
         */
        // notice views have already been inflated and layout has already been set so all you need to do is set the data
        ((TextView) view.findViewById(R.id.contact_name)).setText(name);
        ((TextView) view.findViewById(R.id.type)).setText(displayLabel);
        ((TextView) view.findViewById(R.id.contact_number)).setText(number);
        ((ImageView) view.findViewById(R.id.contact_photo)).setImageBitmap(ch.getContactImage(ImageCache));
    }
}
