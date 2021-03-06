package com.androidproductions.ics.sms;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import com.androidproductions.ics.sms.preferences.ConfigurationHelper;

public class AdditionalPreferences extends ThemeablePreferenceActivity  {
	
	private static final int Set_Ringtone = 0;
	private ConfigurationHelper config;
	private static final String PREFERENCE_TYPE = "Preference";

	/** Called when the activity is first created. */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final String prefType = getIntent().getExtras().getString(PREFERENCE_TYPE);
		final ActionBar ab = this.getActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(prefType);
        config = ConfigurationHelper.getInstance();
        if (prefType.equals("Dialog"))
        	addPreferencesFromResource(R.xml.dialog_preferences);	
        else
        	addPreferencesFromResource(R.xml.notification_preferences);	
		
		final Switch actionBarSwitch = new Switch(this);
		actionBarSwitch.setChecked(config.getBooleanValue(prefType));
		actionBarSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {	
			public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
				config.setBooleanValue(prefType,isChecked);
			}
		});
		
        if (this.onIsHidingHeaders() || !this.onIsMultiPane()) {
            ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM);
            ab.setCustomView(actionBarSwitch, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_VERTICAL | Gravity.RIGHT));
        }
        
        if (prefType.equals("Notification"))
        	findPreference("NotificationSound").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
    			public boolean onPreferenceClick(final Preference preference) {
    	            final Intent intent = new Intent( RingtoneManager.ACTION_RINGTONE_PICKER);
    	            intent.putExtra( RingtoneManager.EXTRA_RINGTONE_TYPE,
    	            RingtoneManager.TYPE_NOTIFICATION);
    	            intent.putExtra( RingtoneManager.EXTRA_RINGTONE_TITLE, getResources()
                            .getString(R.string.selectTone));
    	            intent.putExtra( RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,(Uri)null);
    	            startActivityForResult(intent, Set_Ringtone);
    				return true;
    			}
    		});
	}
	
	@Override
	public void onActivityResult(final int requestCode, final int resultCode,
            final Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
            final Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
            	config.setStringValue(ConfigurationHelper.NOTIFICATION_SOUND, uri.toString());
            }
		}
	}
	
	@Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                final Intent intent = new Intent(this, Preferences.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
