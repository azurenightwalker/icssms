package com.androidproductions.ics.sms;

import android.content.ContentUris;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.androidproductions.ics.sms.messaging.IMessage;
import com.androidproductions.ics.sms.messaging.MessageUtilities;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.main)
@OptionsMenu(R.menu.base_menu)
public class ICSSMSActivity extends AdSupportedActivity {

    @ViewById(R.id.smsList)
    public LinearLayout smsList;
	
	// Current action mode (contextual action bar, a.k.a. CAB)
    private ActionMode mCurrentActionMode;
    private List<View> selected;

    /** Called when the activity is first created. */
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selected = new ArrayList<View>();
        if(getIntent().getBooleanExtra(Constants.NOTIFICATION_STATE_UPDATE, false))
        	PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(Constants.NOTIFICATION_SHOWING_KEY, false).apply();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!MessageUtilities.GetUnsentMessages(ICSSMSActivity.this).isEmpty())
            menu.findItem(R.id.unsentSms).setVisible(true);
        else
            menu.findItem(R.id.unsentSms).setVisible(false);
        return true;
    }

	@Override
	public void onResume() {
        super.onResume();
        redrawView();
        InitializeAds();
    }

    @SuppressWarnings("deprecation")
    private void redrawView()
    {
    	List<IMessage> smss = MessageUtilities.GetMessageSummary(ICSSMSActivity.this);
        smsList.removeAllViews();
        for (final IMessage sms : smss)
        {
        	final View child = LayoutInflater.from(getBaseContext()).inflate(R.layout.sms_summary, null);
        	SpannableString name = new SpannableString(sms.getSummaryHeader());
        	SpannableString body = new SpannableString(sms.getText());
        	SpannableString time = new SpannableString(sms.GetShortDateString());
        	if (sms.isUnread() && sms.IsIncoming())
        	{
        		StyleSpan bold = new StyleSpan(android.graphics.Typeface.BOLD);
        		name.setSpan(bold, 0, name.length(), 0);
        		body.setSpan(bold, 0, body.length(), 0);
        		int[] attrs = new int[] { R.attr.read_sms_drawable /* index 0 */};

        		// Obtain the styled attributes. 'themedContext' is a context with a
        		// theme, typically the current Activity (i.e. 'this')
        		TypedArray ta = this.obtainStyledAttributes(attrs);

        		// Now get the value of the 'listItemBackground' attribute that was
        		// set in the theme used in 'themedContext'. The parameter is the index
        		// of the attribute in the 'attrs' array. The returned Drawable
        		// is what you are after
        		Drawable drawableFromTheme = ta.getDrawable(0 /* index */);

        		// Finally free resources used by TypedArray
        		ta.recycle();
        		if (android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.JELLY_BEAN)
        			child.setBackground(drawableFromTheme);
        		else
        			child.setBackgroundDrawable(drawableFromTheme);
        	}
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
					if (mCurrentActionMode != null) {
						if (!v.isSelected())
						{
							v.setSelected(true);
		                    selected.add(v);
						}
						else
						{
							v.setSelected(false);
		                    selected.remove(v);
		                    if (selected.isEmpty())
		                    {
		                    	mCurrentActionMode.finish();
		                    }
						}
                    }
					else
					{
						Intent intent = new Intent(getBaseContext(), SmsViewer_.class);
						intent.setData(ContentUris.withAppendedId(Constants.SMS_CONVERSATIONS_URI,((IMessage)v.getTag()).getThreadId()));  
						intent.putExtra(Constants.SMS_RECEIVE_LOCATION, ((IMessage)v.getTag()).getAddress());
		                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		                startActivity(intent);
					}
				}
			});
    	    child.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View view) {
                    if (mCurrentActionMode != null) {
                        return false;
                    }

                    mCurrentActionMode = startActionMode(
                         mContentSelectionActionModeCallback);
                    view.setSelected(true);
                    selected.add(view);
                    return true;
                }
            });
        	smsList.addView(child);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return true;
            case R.id.newSms:
                Intent intent = new Intent(this, ComposeSms_.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.unsentSms:
                Intent unintent = new Intent(this, UnsentMessages_.class);
                unintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(unintent);
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
    
    private final ActionMode.Callback mContentSelectionActionModeCallback = new ActionMode.Callback() {
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.xml.action_menu, menu);
            return true;
        }

        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
            case R.action.delete:
            	for (View v : selected)
            	{
            		Long thread = ((IMessage)v.getTag()).getThreadId();
            		getContentResolver().delete(
                            ContentUris.withAppendedId(Constants.SMS_CONVERSATIONS_URI, thread),   // the user dictionary content URI
                            "locked = ? ",                    // the column to select on
                            new String[]{"0"}                      // the value to compare to
                    );
            	}
            	
                actionMode.finish();
                redrawView();
                return true;
            }
            return false;
        }

        public void onDestroyActionMode(ActionMode actionMode) {
        	for (View v : selected)
        	{
        		v.setBackgroundColor(getResources().getColor(android.R.color.black));
        		v.setSelected(false);
        	}
        	selected.clear();
            mCurrentActionMode = null;
        }
    };
}