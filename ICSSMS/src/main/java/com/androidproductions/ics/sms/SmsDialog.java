package com.androidproductions.ics.sms;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.ScrollView;
import android.widget.TextView;

import com.androidproductions.ics.sms.data.ContactHelper;
import com.androidproductions.ics.sms.messaging.IMessage;
import com.androidproductions.ics.sms.messaging.MessageUtilities;
import com.androidproductions.ics.sms.utils.AddressUtilities;
import com.androidproductions.ics.sms.utils.SmileyParser;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SmsDialog extends ThemeableDialog  {
	private List<IMessage> unread;
	private IMessage message;
	private SmileyParser parser;
	
	private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	updateUnreadMessages();
        	updateArrows();
        }
    };
	private int activeMessage;
	private TextView replyToLink;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = 
                new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.sms_dialog);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        ScrollView sv = (ScrollView)findViewById(R.id.scroller);
        if (PreferenceManager.getDefaultSharedPreferences(this).getString("DialogSize", "Regular").equals("Large"))
        	sv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 300));
        Bundle extras = getIntent().getExtras();
		String number = extras.getString(Constants.SMS_RECEIVE_LOCATION,null);
		String message = extras.getString(Constants.SMS_MESSAGE,null);
		String messageType = extras.getString(Constants.MESSAGE_TYPE,null);
		long time = extras.getLong(Constants.SMS_TIME);
		if (messageType == null)
			updateUnreadMessages();
		else if (messageType.equals("SMS"))
			updateUnreadMessages(MessageUtilities.GenerateMessage(SmsDialog.this, number, message, Constants.MESSAGE_TYPE_INBOX, time));
		activeMessage = unread.size()-1;
		SmileyParser.init(this);
        parser = SmileyParser.getInstance();
        replyToLink = (TextView)findViewById(R.id.replyTo);
		redrawView();
		retrieveDraft();
		replyToLink.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Dialog dialog = new Dialog(SmsDialog.this);
				IMessage previous = SmsDialog.this.message.getPrevious();
				if (previous != null)
				{
		        	dialog.setContentView(R.layout.sms_reply_to);
		        	dialog.setTitle(R.string.replyTo);
		        	((TextView)dialog.findViewById(R.id.message)).setText(previous.getText());
		        	((TextView)dialog.findViewById(R.id.time)).setText(previous.GetShortDateString());
		        	dialog.show();
				}
			}
		});
        ((EditText)findViewById(R.id.text)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                UpdateTextCount(s);
            }
        });
    }

    private void UpdateTextCount(Editable s) {
        TextView textCount = (TextView)findViewById(R.id.textCount);
        String textFormat = getResources().getString(R.string.characterCount);
        int[] params = SmsMessage.calculateLength(s, false);
        if (shouldShowCount(params))
            textCount.setText(String.format(textFormat,params[2],params[0]));
        else
            textCount.setText("");
    }

    private boolean shouldShowCount(int[] params)
    {
        return params[0] > 1 || params[2] < 60;
    }

    
    private void updateUnreadMessages()
    {
    	List<IMessage> _unread = MessageUtilities.GetUnreadMessages(SmsDialog.this);
    	
    	unread = sortMessages(_unread);
    }
    
    private List<IMessage> sortMessages(List<IMessage> unread)
    {
    	Collections.sort(unread, new Comparator<IMessage>() {
		    public int compare(IMessage m1, IMessage m2) {
		        return m1.getDate().compareTo(m2.getDate());
		    }
		});
    	return unread;
    }
    
    private void updateUnreadMessages(IMessage newMessage)
    {
    	updateUnreadMessages();
    	boolean found = false;
		for (IMessage s : unread)
			if (s.getText().equals(newMessage.getText()) &&
					AddressUtilities.StandardiseNumber(s.getAddress(),SmsDialog.this).equals(
							AddressUtilities.StandardiseNumber(newMessage.getAddress(),SmsDialog.this)
							))
				found = true;
		if (!found)
			unread.add(newMessage);
    	unread = sortMessages(unread);
    }

	private void redrawView() {
		if (activeMessage >= unread.size())
			activeMessage = unread.size() - 1;
		message = unread.get(activeMessage);
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("MessageReminder", true))
		{
			updateReplyTo();
		}
        ContactHelper ch = new ContactHelper(this);
		((TextView)findViewById(R.id.sender)).setText(ch.getContactName(message.getAddress()));
		((TextView)findViewById(R.id.sender_number)).setText(AddressUtilities.StandardiseNumber(message.getAddress(),SmsDialog.this));
        ((QuickContactBadge)findViewById(R.id.sender_photo)).assignContactUri(ch.getContactUri());
        ((ImageView)findViewById(R.id.sender_photo)).setImageBitmap(message.getContactPhoto());
        ((TextView)findViewById(R.id.messageContent)).setText(parser.addSmileySpans(message.getText()));
        ((TextView)findViewById(R.id.messageTime)).setText(message.GetShortDateString());
        findViewById(R.id.openConvo).setTag(message.getAddress());
        updateArrows();
        
	}

	private void updateReplyTo() {
		IMessage previous = message.getPrevious();
		if (previous != null)
		{
			java.util.Date date = new java.util.Date(previous.getDate());
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DATE, -1);  // number of days to add
			if (date.after(c.getTime())) // If in past day
			{
				SpannableString content = new SpannableString(getResources().getString(R.string.replyTo));
				content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
				replyToLink.setText(content);
				replyToLink.setVisibility(TextView.VISIBLE);
				return;
			}
		}
		replyToLink.setText("");
		replyToLink.setOnClickListener(null);
		replyToLink.setVisibility(TextView.GONE);
	}
    
    public void sendSms(View v)
    {
        String text = ((EditText)findViewById(R.id.text)).getEditableText().toString();
        if (!text.equals(""))
        {
			markCurrentAsRead();
	        MessageUtilities.SendMessage(SmsDialog.this, text, message.getAddress());
	        removeDraft();
	        ((EditText)findViewById(R.id.text)).getEditableText().clear();
	        updateUnreadMessages();
	        if (unread.isEmpty())
	        	closeDialog(v);
	        else
	        {
		    	activeMessage = activeMessage < unread.size() ? activeMessage : activeMessage - 1;
		    	redrawView();     
	        }
        }
    }

	private void markCurrentAsRead() {
		boolean marked = false;
		int i = 0;
		if (message.getId() > 0)
		{
			message.markAsRead();
			return;
		}
		updateUnreadMessages();
		String addy = AddressUtilities.StandardiseNumber(message.getAddress(),SmsDialog.this);
		if (unread.size() == 1) unread.get(0).markAsRead();
		else if (!unread.isEmpty())
			do
			{
				IMessage sms2 = unread.get(i);
				i++;
				if (AddressUtilities.StandardiseNumber(sms2.getAddress(),SmsDialog.this).equals(addy))
				{
					sms2.markAsRead();
					marked = true;
				}
			}while(!marked && i < unread.size());
	}
	
    @SuppressWarnings("UnusedParameters")
    public void openConversation(View v)
    {
    	Intent conversationIntent = new Intent(SmsDialog.this, SmsViewer_.class);
    	conversationIntent.putExtra(Constants.SMS_MESSAGE, ((EditText)findViewById(R.id.text)).getEditableText().toString());
    	conversationIntent.putExtra(Constants.SMS_RECEIVE_LOCATION, message.getAddress());
    	conversationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(conversationIntent);
		finish();
    }  
    
    @SuppressWarnings("UnusedParameters")
    public void closeDialog(View v)
    {
    	if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("DialogMarkRead", false))
    		markCurrentAsRead();
    	removeDraft();
		finish();
    }

    @SuppressWarnings("UnusedParameters")
    public void moveToPrevious(View v)
    {
    	activeMessage -= 1;
    	redrawView();
    }
    
    @SuppressWarnings("UnusedParameters")
    public void moveToNext(View v)
    {
    	activeMessage += 1;
    	redrawView();
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
    	saveAsDraft();
    	unregisterReceiver(receiver);
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	registerReceiver(receiver, new IntentFilter("com.androidproductions.ics.sms.UPDATE_DIALOG"));
    	updateUnreadMessages();
    	updateArrows();
    }
    
    private void saveAsDraft() {
    	MessageUtilities.SaveDraftMessage(SmsDialog.this, message.getAddress(), ((EditText)findViewById(R.id.text)).getEditableText().toString());
	}
    
    private void retrieveDraft() {
    	((EditText)findViewById(R.id.text)).setText(MessageUtilities.RetrieveDraftMessage(SmsDialog.this, message.getAddress()));
	}
    
    private void removeDraft() {
    	MessageUtilities.SaveDraftMessage(SmsDialog.this, message.getAddress(), "");
	}

	private void updateArrows() {
		if (unread.size()-1 > activeMessage)
		{
			// We have next messages
			findViewById(R.id.next).setClickable(true);
			findViewById(R.id.next).setAlpha(1f);
		}
		else
		{
			findViewById(R.id.next).setClickable(false);
			findViewById(R.id.next).setAlpha(0.25f);
		}
		if (activeMessage > 0)
		{
			// We have previous messages
			findViewById(R.id.previous).setClickable(true);
			findViewById(R.id.previous).setAlpha(1f);
		}
		else
		{
			findViewById(R.id.previous).setClickable(false);
			findViewById(R.id.previous).setAlpha(0.25f);
		}
		String counter = String.format((String)getResources().getText(R.string.messageCounter),
				(activeMessage+1),
				unread.size());
		((TextView)findViewById(R.id.count)).setText(counter);
        
	}
}