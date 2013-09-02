package com.androidproductions.ics.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidproductions.ics.sms.messaging.IMessage;
import com.androidproductions.ics.sms.messaging.MessageUtilities;
import com.androidproductions.ics.sms.utils.AddressUtilities;
import com.androidproductions.ics.sms.utils.SmileyParser;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SmsNotify extends ThemeableDialog  {
	private List<IMessage> unread;
	private IMessage message;
	private SmileyParser parser;
	
	private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	updateUnreadMessages();
        }
    };
	private int activeMessage;
	private float startx;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_notify); 
        getWindow().setGravity(Gravity.TOP);
        getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        Bundle extras = getIntent().getExtras();
		String number = extras.getString(Constants.SMS_RECEIVE_LOCATION,null);
		String message = extras.getString(Constants.SMS_MESSAGE,null);
		long time = extras.getLong(Constants.SMS_TIME);
		if (message != null)
			updateUnreadMessages(MessageUtilities.GenerateMessage(SmsNotify.this, number, message, Constants.MESSAGE_TYPE_INBOX, time));
		else
			updateUnreadMessages();activeMessage = unread.size()-1;
		SmileyParser.init(this);
        parser = SmileyParser.getInstance();
		redrawView();
    }
    
    private void updateUnreadMessages()
    {
    	List<IMessage> _unread = MessageUtilities.GetUnreadMessages(SmsNotify.this);
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
					s.getAddress().equals(
							AddressUtilities.StandardiseNumber(newMessage.getAddress(),SmsNotify.this)
							))
				found = true;
		if (!found)
			unread.add(newMessage);
    	unread = sortMessages(unread);
    }

	private void redrawView() {
		message = unread.get(activeMessage);
		((TextView)findViewById(R.id.sender)).setText(message.getContactName());
		((ImageView)findViewById(R.id.sender_photo)).setImageBitmap(message.getContactPhoto());
        ((TextView)findViewById(R.id.messageContent)).setText(parser.addSmileySpans(message.getText()));
        ((TextView)findViewById(R.id.messageContent)).setText(parser.addSmileySpans(message.getText()));
        findViewById(R.id.wrapper).setTag(message.getAddress());
	}

    void openConversation()
    {
    	Intent conversationIntent = new Intent(SmsNotify.this, SmsViewer_.class);
    	conversationIntent.putExtra(Constants.SMS_RECEIVE_LOCATION, message.getAddress());
    	conversationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(conversationIntent);
		finish();
    }  
    
    void closeDialog(Animation anim)
    {
    	View root = findViewById(R.id.wrapper);
    	anim.setAnimationListener(new AnimationListener() {
			public void onAnimationStart(Animation animation) { }			
			public void onAnimationRepeat(Animation animation) { }
			public void onAnimationEnd(Animation animation) {
				if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("DialogMarkRead", false))
		    		markCurrentAsRead();
				finish();
			}
		});
    	root.startAnimation(anim);
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
		String addy = AddressUtilities.StandardiseNumber(message.getAddress(),SmsNotify.this);
		if (unread.size() == 1) unread.get(0).markAsRead();
		else if (!unread.isEmpty())
			do
			{
				IMessage sms2 = unread.get(i);
				i++;
				if (AddressUtilities.StandardiseNumber(sms2.getAddress(),SmsNotify.this).equals(addy))
				{
					sms2.markAsRead();
					marked = true;
				}
			}while(!marked && i < unread.size());
	}
    
    @Override
    public void onPause()
    {
    	super.onPause();
    	unregisterReceiver(receiver);
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	registerReceiver(receiver, new IntentFilter("com.androidproductions.ics.sms.UPDATE_DIALOG"));
    	updateUnreadMessages();
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
    	if (event.getAction() == MotionEvent.ACTION_DOWN) {
        	startx = event.getX();
        }
        else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (Math.abs(event.getX() - startx) > 40)
            	if (event.getX() > startx)
            		closeDialog(AnimationUtils.loadAnimation(this, R.animator.outright));
            	else
            		closeDialog(AnimationUtils.loadAnimation(this, R.animator.outleft));
            else 
                openConversation();
        }
        return super.dispatchTouchEvent(event);
    }

}