package com.androidproductions.ics.sms;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.androidproductions.ics.sms.data.adapters.ContactsAutoCompleteCursorAdapter;
import com.androidproductions.ics.sms.messaging.MessageUtilities;
import com.androidproductions.libs.sms.SmsUri;
import com.googlecode.androidannotations.annotations.AfterTextChange;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.res.StringRes;

@EActivity(R.layout.sms_compose)
@OptionsMenu(R.menu.compose_menu)
public class ComposeSms extends ThemeableActivity {

    @ViewById(R.id.phoneNumber)
    public AutoCompleteTextView phoneNumber;
    
    @ViewById(R.id.text)
    public EditText textView;

    @StringRes(R.string.characterCount)
    public String textFormat;

    @ViewById(R.id.textCount)
    public static TextView textCount;
    
	private ContactsAutoCompleteCursorAdapter adapter;
    private AlertDialog dialog;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        final String[] projection = new String[] {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
             };

    	final Uri contacts =  ContactsContract.Contacts.CONTENT_URI;
        final CursorLoader cursorLoader = new CursorLoader(
                this, 
                contacts, 
                projection, 
                null, 
                null, 
                null);

        final Cursor autoCursor = cursorLoader.loadInBackground();
        adapter = new ContactsAutoCompleteCursorAdapter(this, autoCursor);
    }
    
    @AfterViews
    public void redrawView()
    {
    	final Bundle extras = getIntent().getExtras();
        String text = "";
        if (extras != null)
        {
            text = extras.getString(android.content.Intent.EXTRA_TEXT,"");
            if (text.equals(""))
                text = extras.getString(Constants.SMS_MESSAGE,"");
        }
    	phoneNumber.requestFocus();
        textView.getEditableText().append(text);

        phoneNumber.setAdapter(adapter);
        UpdateTextCount(textView.getEditableText());
    }

    @AfterTextChange(R.id.text)
    public void afterTextChanged(final Editable s) {
        UpdateTextCount(s);
    }

    private void UpdateTextCount(final Editable s) {
        final int[] params = SmsMessage.calculateLength(s, false);
        if (shouldShowCount(params))
            textCount.setText(String.format(textFormat,params[2],params[0]));
        else
            textCount.setText("");
    }

    private boolean shouldShowCount(final int[] params)
    {
        return params[0] > 1 || params[2] < 60;
    }

    
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                final Intent intent = new Intent(this, ICSSMSActivity_.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.discard:
            	finish();
            	return true;
            case R.id.settings:
            	final Intent prefintent = new Intent(this, Preferences_.class);
            	prefintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(prefintent);
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("UnusedParameters")
    public void sendSms(final View v)
    {
        String number = phoneNumber.getEditableText().toString();
        final String text = textView.getEditableText().toString();
        if (!text.trim().equals("") && !number.trim().equals(""))
        {
            number = number.substring(number.lastIndexOf("(")+1).replace(")","");
            final Long thread = MessageUtilities.SendMessage(ComposeSms.this,text,number);
            final Intent intent = new Intent(getBaseContext(), SmsViewer_.class);
            intent.setData(ContentUris.withAppendedId(SmsUri.CONVERSATIONS_URI,thread));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Constants.SMS_RECEIVE_LOCATION, number);
            startActivity(intent);
            finish();
        }
    }

    @SuppressWarnings("UnusedParameters")
    public void showContactList(final View v)
    {
        if (dialog == null)
        {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final LayoutInflater inflater = getLayoutInflater();
            builder.setView(inflater.inflate(R.layout.contact_picker_fragment, null));
            dialog = builder.create();
        }
        dialog.show();
    }

    public void updateCompose(final String text) {
        phoneNumber.setText(text);
        dialog.dismiss();
    }
}