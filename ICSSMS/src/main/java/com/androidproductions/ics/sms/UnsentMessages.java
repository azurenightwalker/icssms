package com.androidproductions.ics.sms;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;

import com.androidproductions.ics.sms.messaging.IMessage;
import com.androidproductions.ics.sms.messaging.MessageUtilities;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;

import java.util.List;

@EActivity(R.layout.main)
@OptionsMenu(R.menu.bare_menu)
public class UnsentMessages extends AdSupportedActivity {

    @ViewById(R.id.smsList)
    public LinearLayout smsList;

    private IMessage PressedMessage;

    /** Called when the activity is first created. */
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setHomeButtonEnabled(true);
            ab.setTitle(R.string.unsentTitle);
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }
	
	@Override
	public void onResume() {
        super.onResume();
        redrawView();
        InitializeAds();
    }

    private void redrawView()
    {
    	List<IMessage> smss = MessageUtilities.GetUnsentMessages(UnsentMessages.this);
        smsList.removeAllViews();
        for (final IMessage sms : smss)
        {
        	final View child = LayoutInflater.from(getBaseContext()).inflate(R.layout.sms_summary, null);
        	SpannableString name = new SpannableString(sms.getSummaryHeader());
        	SpannableString body = new SpannableString(sms.getText());
        	SpannableString time = new SpannableString(sms.GetShortDateString());
        	((TextView)child.findViewById(R.id.contact_name)).setText(name);
        	((TextView)child.findViewById(R.id.messageContent)).setText(body);
        	((TextView)child.findViewById(R.id.messageTime)).setText(time);
            ((QuickContactBadge)child.findViewById(R.id.contact_photo)).assignContactFromPhone(sms.getAddress(),true);
        	new Thread(new Runnable() {
				public void run() {
					((ImageView)child.findViewById(R.id.contact_photo)).setImageBitmap(sms.getConversationContactImage());
				}
			}).run();
			child.setTag(sms);
        	child.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
                    ShowDetails(sms);
                }
			});
            registerForContextMenu(child);
        	smsList.addView(child);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        PressedMessage = (IMessage)v.getTag();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.unsent_action_menu,menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(this, ICSSMSActivity_.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.settings:
            	Intent prefintent = new Intent(this, Preferences_.class);
            	prefintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(prefintent);
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                if (PressedMessage.deleteMessage())
                {
                    View v = smsList.findViewWithTag(PressedMessage);
                    ((ViewManager)v.getParent()).removeView(v);
                }
                else
                {
                    Toast.makeText(this, getString(R.string.deleteLocked), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.details:
                ShowDetails(PressedMessage);
                return true;
            case R.id.resend:
                MessageUtilities.SendMessage(UnsentMessages.this, PressedMessage.getText(), PressedMessage.getAddress());
                PressedMessage.deleteMessage();
                redrawView();
                return true;
            default:
                return false;
        }
    }

    private void ShowDetails(IMessage message) {
        Dialog dialog = new Dialog(UnsentMessages.this);

        dialog.setContentView(R.layout.sms_details);
        dialog.setTitle("Message Details");
        if (!message.IsIncoming())
        {
            ((TextView)dialog.findViewById(R.id.labelLocation)).setText("To:");
            ((TextView)dialog.findViewById(R.id.labelRecieved)).setText("Sent:");
        }
        if (message.hasAttachments())
        {
            ((TextView)dialog.findViewById(R.id.valueType)).setText(R.string.mmsType);
        }
        ((TextView)dialog.findViewById(R.id.valueLocation)).setText(PressedMessage.getAddress());
        ((TextView)dialog.findViewById(R.id.valueRecieved)).setText(PressedMessage.GetDateString());
        dialog.show();
    }
}