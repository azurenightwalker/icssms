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

import com.androidproductions.ics.sms.messaging.MessageUtilities;
import com.androidproductions.ics.sms.utils.AddressUtilities;
import com.androidproductions.ics.sms.utils.SmileyParser;
import com.androidproductions.libs.sms.InternalTransaction;
import com.androidproductions.libs.sms.com.androidproductions.libs.sms.readonly.IMessageView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SmsNotify extends ThemeableDialog  {
	private List<IMessageView> unread;
	private IMessageView message;
	private SmileyParser parser;
	
	private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
        	updateUnreadMessages();
        }
    };
	private int activeMessage;
	private float startx;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_notify); 
        getWindow().setGravity(Gravity.TOP);
        getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        final Bundle extras = getIntent().getExtras();
		final String number = extras.getString(Constants.SMS_RECEIVE_LOCATION,null);
		final String message = extras.getString(Constants.SMS_MESSAGE,null);
		final long time = extras.getLong(Constants.SMS_TIME);
		if (message != null)
			updateUnreadMessages(MessageUtilities.GenerateMessage(SmsNotify.this, number, message, time));
		else
			updateUnreadMessages();activeMessage = unread.size()-1;
		SmileyParser.init(this);
        parser = SmileyParser.getInstance();
		redrawView();
    }
    
    private void updateUnreadMessages()
    {
    	final List<IMessageView> _unread = MessageUtilities.GetUnreadMessages(SmsNotify.this);
    	unread = sortMessages(_unread);
    }
    
    private List<IMessageView> sortMessages(final List<IMessageView> unread)
    {
    	Collections.sort(unread, new Comparator<IMessageView>() {
		    public int compare(final IMessageView m1, final IMessageView m2) {
		        return m1.getDate().compareTo(m2.getDate());
		    }
		});
    	return unread;
    }
    
    private void updateUnreadMessages(final IMessageView newMessage)
    {
    	updateUnreadMessages();
    	boolean found = false;
		for (final IMessageView s : unread)
			if (s.getBody().equals(newMessage.getBody()) &&
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
        ((TextView)findViewById(R.id.messageContent)).setText(parser.addSmileySpans(message.getBody()));
        ((TextView)findViewById(R.id.messageContent)).setText(parser.addSmileySpans(message.getBody()));
        findViewById(R.id.wrapper).setTag(message.getAddress());
	}

    void openConversation()
    {
    	final Intent conversationIntent = new Intent(SmsNotify.this, SmsViewer_.class);
    	conversationIntent.putExtra(Constants.SMS_RECEIVE_LOCATION, message.getAddress());
    	conversationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(conversationIntent);
		finish();
    }  
    
    void closeDialog(final Animation anim)
    {
    	final View root = findViewById(R.id.wrapper);
    	anim.setAnimationListener(new AnimationListener() {
			public void onAnimationStart(final Animation animation) { }
			public void onAnimationRepeat(final Animation animation) { }
			public void onAnimationEnd(final Animation animation) {
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
			new InternalTransaction(SmsNotify.this).MarkMessageRead(message);
			return;
		}
		updateUnreadMessages();
		final String addy = AddressUtilities.StandardiseNumber(message.getAddress(),SmsNotify.this);
		if (unread.size() == 1) new InternalTransaction(SmsNotify.this).MarkMessageRead(unread.get(0));
		else if (!unread.isEmpty())
			do
			{
				final IMessageView sms2 = unread.get(i);
				i++;
				if (AddressUtilities.StandardiseNumber(sms2.getAddress(),SmsNotify.this).equals(addy))
				{
                    new InternalTransaction(SmsNotify.this).MarkMessageRead(sms2);
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
    public boolean dispatchTouchEvent(final MotionEvent event) {
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