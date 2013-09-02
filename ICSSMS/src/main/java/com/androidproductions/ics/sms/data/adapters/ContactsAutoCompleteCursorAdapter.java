package com.androidproductions.ics.sms.data.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.DataUsageFeedback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidproductions.ics.sms.R;
import com.androidproductions.ics.sms.data.ContactHelper;

public class ContactsAutoCompleteCursorAdapter extends CursorAdapter {

	private final Context mContext;

	private static final String[] PROJECTION_PHONE = {
        Phone._ID,                  // 0
        Phone.CONTACT_ID,           // 1
        Phone.TYPE,                 // 2
        Phone.NUMBER,               // 3
        Phone.LABEL,                // 4
        Phone.DISPLAY_NAME,         // 5
    };

    public ContactsAutoCompleteCursorAdapter(final Context context, final Cursor c) {
    	super(context,c,0);
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

    @Override
    public String convertToString(final Cursor cursor) {
        // this method dictates what is shown when the user clicks each entry in your autocomplete list
        // in my case i want the number data to be shown
    	final int nameIdx = cursor.getColumnIndexOrThrow(Phone.DISPLAY_NAME);
        final int id = cursor.getColumnIndex(Phone.NUMBER);

        final String name = cursor.getString(nameIdx);
        final String number = cursor.getString(id);
        return name + " (" + number + ")";
    }

    @Override
    public Cursor runQueryOnBackgroundThread(final CharSequence constraint) {
        final String phone = "";
        String cons = null;

        if (constraint != null) {
            cons = constraint.toString();
        }


        Uri uri = null;
        if (Phone.CONTENT_FILTER_URI != null) {
            uri = Phone.CONTENT_FILTER_URI.buildUpon()
                    .appendPath(cons)
                    .appendQueryParameter(DataUsageFeedback.USAGE_TYPE,
                            DataUsageFeedback.USAGE_TYPE_SHORT_TEXT)
                    .build();
        }
        if (uri != null)
            {
                /*
                 * if we decide to filter based on phone types use a selection
                 * like this.
                String selection = String.format("%s=%s OR %s=%s OR %s=%s",
                        Phone.TYPE,
                        Phone.TYPE_MOBILE,
                        Phone.TYPE,
                        Phone.TYPE_WORK_MOBILE,
                        Phone.TYPE,
                        Phone.TYPE_MMS);
                 */
                final Cursor phoneCursor =
                    mContext.getContentResolver().query(uri,
                            PROJECTION_PHONE,
                            null, //selection,
                            null,
                            null);

                if (!phone.isEmpty()) {
                    final Object[] result = new Object[7];
                    result[0] = -1;                    // ID
                    result[1] = -1L;                       // CONTACT_ID
                    result[2] = Phone.TYPE_CUSTOM;     // TYPE
                    result[3] = phone;                                  // NUMBER

                    /*
                     * The "\u00A0" keeps Phone.getDisplayLabel() from deciding
                     * to display the default label ("Home") next to the transformation
                     * of the letters into numbers.
                     */
                    result[4] = "\u00A0";                               // LABEL
                    result[5] = cons;                                   // NAME
                    result[6] = phone;                                  // NORMALIZED_NUMBER

                    final MatrixCursor translated = new MatrixCursor(PROJECTION_PHONE, 1);
                    translated.addRow(result);
                    return new MergeCursor(new Cursor[] { translated, phoneCursor });
                } else {
                    return phoneCursor;
                }
            }
        return null;
    }
}
