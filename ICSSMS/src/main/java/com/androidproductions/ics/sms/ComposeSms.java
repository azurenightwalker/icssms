package com.androidproductions.ics.sms;

import android.app.ActionBar;
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
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.androidproductions.ics.sms.data.adapters.ContactsAutoCompleteCursorAdapter;
import com.androidproductions.ics.sms.messaging.MessageUtilities;
import com.androidproductions.libs.sms.com.androidproductions.libs.sms.constants.SmsUri;

public class ComposeSms extends ThemeableActivity {

    private AutoCompleteTextView phoneNumber;
    
    private EditText textView;

    private String textFormat;

    private TextView textCount;
    
	private ContactsAutoCompleteCursorAdapter adapter;
    private AlertDialog dialog;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_compose);
        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setHomeButtonEnabled(true);
            ab.setDisplayHomeAsUpEnabled(true);
        }
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
        initialize();
    }

    void initialize() {
        phoneNumber = (AutoCompleteTextView) findViewById(R.id.phoneNumber);
        textView = (EditText) findViewById(R.id.text);
        textCount = (TextView) findViewById(R.id.textCount);
        textFormat = getResources().getString(R.string.characterCount);
        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {

            }

            @Override
            public void afterTextChanged(final Editable s) {
                UpdateTextCount(s);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        redrawView();
    }

    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.compose_menu, menu);
        return true;
    }

    void redrawView()
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

    /*@AfterTextChange(R.id.text)
    public void afterTextChanged(final Editable s) {
        UpdateTextCount(s);
    }*/

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
                final Intent intent = new Intent(this, ICSSMSActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.discard:
            	finish();
            	return true;
            case R.id.settings:
            	final Intent prefintent = new Intent(this, Preferences.class);
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
            final Intent intent = new Intent(getBaseContext(), SmsViewer.class);
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