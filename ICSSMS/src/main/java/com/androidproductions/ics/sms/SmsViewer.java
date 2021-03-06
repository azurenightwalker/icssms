package com.androidproductions.ics.sms;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;

import com.androidproductions.ics.sms.data.ContactHelper;
import com.androidproductions.ics.sms.messaging.MessageUtilities;
import com.androidproductions.ics.sms.preferences.ConfigurationHelper;
import com.androidproductions.ics.sms.utils.AddressUtilities;
import com.androidproductions.ics.sms.utils.SmileyParser;
import com.androidproductions.ics.sms.views.KeyboardDetectorScrollView;
import com.androidproductions.ics.sms.views.KeyboardDetectorScrollView.IKeyboardChanged;
import com.androidproductions.libs.sms.InternalTransaction;
import com.androidproductions.libs.sms.com.androidproductions.libs.sms.constants.SmsUri;
import com.androidproductions.libs.sms.Transaction;
import com.androidproductions.libs.sms.com.androidproductions.libs.sms.readonly.IMessageView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SmsViewer extends ThemeableActivity {
	private LinearLayout smsList;

	private EditText textBox;

	private TextView textCount;

	private KeyboardDetectorScrollView scrollView;
	
	private SmileyParser parser;
	private List<IMessageView> messages;
	private IMessageView PressedMessage;
    private long threadId;
    private Uri contactUri;
	
	private String address;

    private String draftMessage;

    private String textFormat;

    private String shareString;
	
	private String name;
    private long ContactID;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_viewer);
        final ActionBar ab = getActionBar();

        if (ab != null) {
            ab.setCustomView(R.layout.action_bar);
            ab.setIcon(R.drawable.ic_launcher);
            ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            ab.setHomeButtonEnabled(true);
            ab.setDisplayHomeAsUpEnabled(true);
            final View v = ab.getCustomView();
            final OnClickListener goHome = new OnClickListener() {
                public void onClick(final View v) {
                    final Intent intent = new Intent(SmsViewer.this, ICSSMSActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            };
            v.findViewById(R.id.icon).setOnClickListener(goHome);
            v.findViewById(R.id.up).setOnClickListener(goHome);
        }
        threadId = 0L;
        if (getIntent().getData() != null)
        {
            final Uri data = getIntent().getData();
            if (data.getScheme().equals("smsto") ||data.getScheme().equals("sms"))
                address = data.getSchemeSpecificPart();
            else
                threadId = Long.parseLong(data.getLastPathSegment());
        }
        SmileyParser.init(this);
        parser = SmileyParser.getInstance();
        lastDate = 0L;
        firstDate = Long.MAX_VALUE;
        final Bundle extras = getIntent().getExtras();
        if (extras != null)
            address = extras.getString(Constants.SMS_RECEIVE_LOCATION,address);
        initialize();
        setupView();
    }

    void initialize() {
        smsList = (LinearLayout) findViewById(R.id.smsList);
        textBox = (EditText) findViewById(R.id.text);
        textCount = (TextView) findViewById(R.id.textCount);
        scrollView = (KeyboardDetectorScrollView) findViewById(R.id.scroller);
        textFormat = getResources().getString(R.string.characterCount);
        shareString = getResources().getString(R.string.shareString);
        textBox.addTextChangedListener(new TextWatcher() {
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

    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.conversation_menu, menu);
        return true;
    }

	private void UpdateTextCount(final Editable s) {
		final int[] params = SmsMessage.calculateLength(s,false);
		if (shouldShowCount(params))
			textCount.setText(String.format(textFormat,params[2],params[0]));
		else
			textCount.setText("");
	}

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
    	if (AddressUtilities.StandardiseNumber(name,SmsViewer.this).equals(address))
        	menu.findItem(R.id.add).setVisible(true);
    	else
    		menu.findItem(R.id.add).setVisible(false);
		return true;
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
            case R.id.add:
            	if (AddressUtilities.StandardiseNumber(name,SmsViewer.this).equals(address))
            	{
	            	final Intent addintent = new Intent(Intent.ACTION_INSERT);
	            	addintent.setType(ContactsContract.Contacts.CONTENT_TYPE);
	            	addintent.putExtra(ContactsContract.Intents.Insert.PHONE, address);
	            	startActivity(addintent);
            	}
            	else
            		Toast.makeText(this, getText(R.string.alreadyAdded), Toast.LENGTH_SHORT).show();
            	return true;
            case R.id.call:
            	callNumberConfirm();
            	return true;
            case R.id.settings:
            	final Intent prefintent = new Intent(this, Preferences.class);
            	prefintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(prefintent);
            	return true;
            case R.id.delete:
            	getContentResolver().delete(
            		    ContentUris.withAppendedId(SmsUri.CONVERSATIONS_URI,threadId),   // the user dictionary content URI
            		    null,                    // the column to select on
            		    null                      // the value to compare to
            		);
            	final Intent hintent = new Intent(this, ICSSMSActivity.class);
                hintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(hintent);
                return true;
            /*case R.conv.online:
            	switchState();*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
        	redrawView();
        }
    };
	private long lastDate;
	private long firstDate;
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	redrawView();
    	final IntentFilter filter = new IntentFilter();
        filter.addAction("com.androidproductions.ics.sms.UPDATE_DIALOG");
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
    	registerReceiver(receiver, filter);
        draftMessage = MessageUtilities.RetrieveDraftMessage(SmsViewer.this,address);
        final Editable et = textBox.getEditableText();
        et.clear();
        et.append(draftMessage);
        UpdateTextCount(textBox.getEditableText());
    }
    
    private boolean shouldShowCount(final int[] params)
    {
    	return params[0] > 1 || params[2] < 60;
    }

	@Override
    public void onPause()
    {
    	super.onPause();
    	MessageUtilities.SaveDraftMessage(SmsViewer.this,address,textBox.getEditableText().toString());
    	unregisterReceiver(receiver);
    }

	void setupContact() {
		if (address == null)
			address = messages.get(0).getAddress();
		address = AddressUtilities.StandardiseNumber(address,SmsViewer.this);
        final ContactHelper ch = new ContactHelper(this);
		name = ch.getContactName(address);
		contactUri = ch.getContactUri();
        ContactID = ch.getId();
        final ActionBar ab = getActionBar();
		((TextView)ab.getCustomView().findViewById(R.id.action_bar_title)).setText(name);
		((TextView)ab.getCustomView().findViewById(R.id.action_bar_subtitle)).setText(address);
	}

	void callNumberConfirm()
	{
		new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.sym_action_call)
        .setTitle(getString(R.string.call))
        .setMessage("Call " + name + " (" + address + ") ?")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            public void onClick(final DialogInterface dialog, final int which) {
            	final Uri mNumberUri = Uri.parse("tel:"+address);
            	startActivity(new Intent(Intent.ACTION_CALL,mNumberUri));
            }

        })
        .setNegativeButton("No", null)
        .show();
	}

	@SuppressWarnings("UnusedParameters")
    public void sendSms(final View v)
    {
    	final Editable et = textBox.getEditableText();
		final String text = et.toString();
		if (!text.trim().equals(""))
		{
			et.clear();
			MessageUtilities.SendMessage(SmsViewer.this, text, address);
			redrawView();
		}
		if (ConfigurationHelper.getInstance().getBooleanValue(ConfigurationHelper.HIDE_KEYBOARD_ON_SEND))
		{
			final InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			mgr.hideSoftInputFromWindow(textBox.getWindowToken(), 0);
		}
    }

    void setupView()
    {
		textBox.getEditableText().append(draftMessage == null ? "" : draftMessage);
        scrollView.addKeyboardStateChangedListener(new IKeyboardChanged() {
			public void onKeyboardShown() {
                scrollToBottom();
			}
			public void onKeyboardHidden() {
                scrollToBottom();
			}
		});
    }
	
	void redraw(final boolean topDown)
	{
		for(int i = 0; i<=messages.size()-1;i++)
        {
        	final IMessageView msg = messages.get(i);
        	if (msg.getDate() < firstDate || msg.getDate() > lastDate)
        	{
        		lastDate = Math.max(lastDate,msg.getDate());
        		firstDate = Math.min(firstDate,msg.getDate());
                new InternalTransaction(SmsViewer.this).MarkMessageRead(msg);
	        	final View child = generateMessageView(msg);
	        	registerForContextMenu(child);
                if(msg.IsIncoming())
                {
                    final QuickContactBadge badge = ((QuickContactBadge)child.findViewById(R.id.photo));
                    if (contactUri == null)
                        badge.assignContactFromPhone(address, true);
                    else
                        badge.assignContactUri(contactUri);
                    badge.setMode(ContactsContract.QuickContact.MODE_LARGE);
                }
	        	if (topDown) // Ensure opposite order
                    smsList.addView(child,1);
	        	else
	        		smsList.addView(child);
	        	new Thread(new Runnable() {
	                public void run() {
	                	try {
                            ((ImageView)smsList.findViewWithTag(msg).findViewById(R.id.photo)).setImageBitmap(msg.getContactPhoto());
	                	}
	                	catch(Exception e){ e.printStackTrace(); }
	                }
	            }, "gettingPhoto").start();
        	}
        }
        if (messages.size() < 25)
            smsList.findViewById(R.id.showPrevious).setVisibility(View.GONE);
    }
	
	void redrawView()
    {    
    	if (messages != null && !messages.isEmpty())
    	{
    		messages = MessageUtilities.GetMessages(SmsViewer.this, threadId,ContactID,Constants.MAX_MESSAGE_COUNT);
    	}
    	else
    	{
            setupContact();
    		threadId = new Transaction(SmsViewer.this).getOrCreateThreadId(address);
    		messages = MessageUtilities.GetMessages(SmsViewer.this, threadId,ContactID,Constants.MAX_MESSAGE_COUNT);

        }
    	
    	redraw(false);
        scrollToBottom();
    }
	
	public void showPrevious(final View v)
    {
        firstDate = Math.min(firstDate,Math.min(messages.get(messages.size()-1).getDate(),messages.get(0).getDate()));
    	messages = MessageUtilities.GetMessages(SmsViewer.this, threadId, ContactID, 25, firstDate);
        Collections.sort(messages, new Comparator<IMessageView>() {
            public int compare(final IMessageView m1, final IMessageView m2) {
                return m2.getDate().compareTo(m1.getDate());
            }
        });
        final View topSeen = smsList.getChildAt(1);
        redraw(true);
        scrollTo(topSeen);
    }

	private View generateMessageView(final IMessageView msg) {
		final View child;
		if (msg.IsIncoming())
			child = LayoutInflater.from(getBaseContext()).inflate(R.layout.sms_in, null);
		else
			child = LayoutInflater.from(getBaseContext()).inflate(R.layout.sms_out, null);
		((TextView)child.findViewById(R.id.messageContent)).setText(parser.addSmileySpans(msg.getBody()));
		((TextView)child.findViewById(R.id.messageTime)).setText(msg.GetShortDateString());
		if (msg.isLocked())
			child.findViewById(R.id.messageStatus).setVisibility(View.VISIBLE);
		if (msg.sendingFailed())
			child.findViewById(R.id.messageNotSent).setVisibility(View.VISIBLE);
		child.setTag(msg);

		return child;
	}
    
    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
                                    final ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        PressedMessage = (IMessageView)v.getTag();
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(PressedMessage.IsIncoming() ? PressedMessage.isLocked() ?
        		R.xml.sms_long_menu_in_locked : R.xml.sms_long_menu_in :
        			PressedMessage.isLocked() ? R.xml.sms_long_menu_out_locked : R.xml.sms_long_menu_out,menu);
    }
    
    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
	        case R.smslong.copy:
	        	final ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
	        	final ClipData clip = ClipData.newPlainText("SMS",PressedMessage.getBody());
	        	clipboard.setPrimaryClip(clip);
	        	return true;
	        case R.smslong.delete:
	        	if (new InternalTransaction(SmsViewer.this).DeleteMessage(PressedMessage))
	        	{
		        	final View v = smsList.findViewWithTag(PressedMessage);
		        	((ViewManager)v.getParent()).removeView(v);
	        	}
	        	else
	        	{
	        		Toast.makeText(this, getString(R.string.deleteLocked), Toast.LENGTH_SHORT).show();
	        	}
	            return true;
	        case R.smslong.details:
	        	final Dialog dialog = new Dialog(SmsViewer.this);

	        	dialog.setContentView(R.layout.sms_details);
	        	dialog.setTitle(getString(R.string.messageDetails));
	        	if (!PressedMessage.IsIncoming())
	        	{
	        		((TextView)dialog.findViewById(R.id.labelLocation)).setText("To:");
	        		((TextView)dialog.findViewById(R.id.labelRecieved)).setText("Sent:");
	        	}
	        	((TextView)dialog.findViewById(R.id.valueLocation)).setText(PressedMessage.getAddress());
	        	((TextView)dialog.findViewById(R.id.valueRecieved)).setText(PressedMessage.GetDateString());
	        	dialog.show();
	        	return true;
	        case R.smslong.forward:
	        	final Intent forwardIntent = new Intent(getApplicationContext(), ComposeSms.class);
	        	forwardIntent.putExtra(Constants.SMS_MESSAGE, PressedMessage.getBody());
	        	forwardIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    		startActivity(forwardIntent);
	            return true;
            case R.smslong.share:
                final Intent i = new Intent(android.content.Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(android.content.Intent.EXTRA_TEXT, PressedMessage.getBody());
                startActivity(Intent.createChooser(i, shareString));
                return true;
	        case R.smslong.lock:
	        	new InternalTransaction(SmsViewer.this).LockMessage(PressedMessage);
	        	smsList.findViewWithTag(PressedMessage).findViewById(R.id.messageStatus).setVisibility(View.VISIBLE);
	        	return true;
	        case R.smslong.unlock:
                new InternalTransaction(SmsViewer.this).UnlockMessage(PressedMessage);
	        	smsList.findViewWithTag(PressedMessage).findViewById(R.id.messageStatus).setVisibility(View.GONE);
	        	return true;
	        case R.smslong.unread:
                new InternalTransaction(SmsViewer.this).MarkMessageUnread(PressedMessage);
	        	Toast.makeText(this, getResources().getText(R.string.markedUnread), Toast.LENGTH_SHORT).show();
	            return true;
	        case R.smslong.resend:
                if (PressedMessage.sendingFailed())
                {
                    if (new InternalTransaction(SmsViewer.this).DeleteMessage(PressedMessage))
                    {
                        final View v = smsList.findViewWithTag(PressedMessage);
                        ((ViewManager)v.getParent()).removeView(v);
                    }
                }
	        	MessageUtilities.SendMessage(SmsViewer.this, PressedMessage.getBody(), address);
	        	redrawView();
	            return true;
	        default:
	        	return false;
	    }
    }

    private void scrollTo(final View scrollTo)
    {
        scrollView.post(new Runnable() {
            public void run() {
                scrollView.scrollTo(0, scrollTo.getTop()-scrollView.getHeight());
            }
        });
    }

    private void scrollToBottom()
    {
    	scrollView.post(new Runnable() {
			public void run() {
				scrollView.fullScroll(View.FOCUS_DOWN);
			}
		});
    }
}