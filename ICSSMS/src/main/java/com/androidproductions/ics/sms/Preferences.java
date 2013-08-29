package com.androidproductions.ics.sms;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.androidproductions.ics.sms.preferences.ConfigurationHelper;
import com.androidproductions.ics.sms.utils.ApexHelper;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;


@EActivity(R.layout.preferences)
public class Preferences extends ThemeableActivity {

	private ConfigurationHelper config;
	private String[] keys;
	private String[] types;
	private Object[] widgets;
	private int current = 0;
	private static final int permCount = 6;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setHomeButtonEnabled(true);
            ab.setTitle(R.string.preferencesTitle);
            ab.setDisplayHomeAsUpEnabled(true);
        }
        keys = new String[permCount];
    	types = new String[permCount];
    	widgets = new Object[permCount];
    	config = ConfigurationHelper.getInstance(getApplicationContext());
	}
	
	@AfterViews
	public void setupPreferences()
	{
		setHeader(R.id.notificationsHeader,R.string.notificationsHeader);
		setHeader(R.id.miscHeader,R.string.miscHeader);
		
		setMasterSwitch(R.id.notifications,ConfigurationHelper.NOTIFICATIONS_ENABLED,
        		R.string.notificationTitle,null,
        		new Intent(Preferences.this,AdditionalPreferences.class));
		setMasterSwitch(R.id.dialog,ConfigurationHelper.DIALOG_ENABLED,
        		R.string.dialogTitle,null,
        		new Intent(Preferences.this,AdditionalPreferences.class));
        
		
		setListPreference(R.id.theme,ConfigurationHelper.THEME,R.array.ThemeDefinitions,
				R.array.ThemeValues,R.string.themeTitle,null,null);
		
		setCheckboxPreference(R.id.smiley,ConfigurationHelper.SMILEY_KEY_ENABLED,
				R.string.smileyTitle,R.string.smileySummary,null);
		
		setCheckboxPreference(R.id.hideKeyboard,ConfigurationHelper.HIDE_KEYBOARD_ON_SEND,
				R.string.keyboardTitle,null,null);

        setCheckboxPreference(R.id.apex,ConfigurationHelper.ALLOW_APEX,
                R.string.apexTitle,null,null);

        setCheckboxPreference(R.id.showAds,ConfigurationHelper.SHOW_ADS,
                R.string.showAds,null,null);
		
		if (!ApexHelper.getInstance(this).isInstalled()){
	        findViewById(R.id.apex).setVisibility(View.GONE);
        }
	}

	private void setHeader(Integer id, Integer title) {
		((TextView)findViewById(id).findViewById(R.id.title)).setText(title);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		int i = 0;
		do {
			if (types[i].equals("boolean"))
				((CompoundButton)widgets[i]).setChecked(config.getBooleanValue(keys[i]));
			i++;
		} while (i < keys.length);
	}
	
	private void setMasterSwitch(Integer id, final String key, Integer title, Integer icon, final Intent intent) {
		LinearLayout switchLayout = setBasePreference(id,key, title, null,
				icon, intent);     
		Switch switchWidget = (Switch)switchLayout.findViewById(R.id.switchWidget);
		switchWidget.setOnCheckedChangeListener(new OnCheckedChangeListener() {	
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				config.setBooleanValue(key, isChecked);
			}
		});
		keys[current] = key;
		types[current] = "boolean";
		widgets[current] = switchWidget;
		current++;
	}
	
	private void setCheckboxPreference(Integer id, final String key, Integer title, Integer summary, final Intent intent) {
		LinearLayout checkboxLayout = setBasePreference(id,key, title, summary,
				null, intent);
        
		final CheckBox checkWidget = (CheckBox)checkboxLayout.findViewById(R.id.checkboxWidget);
		checkboxLayout.setOnClickListener(new OnClickListener() {	
			public void onClick(View v) {
				boolean newVal = !config.getBooleanValue(key);
				checkWidget.setChecked(newVal);
				config.setBooleanValue(key,newVal);
			}
		});
		checkWidget.setOnClickListener(new OnClickListener() {	
			public void onClick(View v) {
				boolean newVal = !config.getBooleanValue(key);
				checkWidget.setChecked(newVal);
				config.setBooleanValue(key,newVal);
			}
		});
		keys[current] = key;
		types[current] = "boolean";
		widgets[current] = checkWidget;
		current++;
	}
	
	private void setListPreference(Integer id, final String key, final int keyArray, final int valArray, final Integer title, Integer summary, Integer icon) {
		LinearLayout listLayout = setBasePreference(id,key, title, summary,
				icon, null);
		
		listLayout.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final CharSequence[] vals = getResources().getTextArray(valArray);
				int i = 0;
				String selected = config.getStringValue(key);
				boolean found = false;
				do {
					if (vals[i].equals(selected))
						found = true;
					i++;
				} while (!found && i <vals.length);
				AlertDialog.Builder builder = new AlertDialog.Builder(Preferences.this);
				builder.setTitle(title);
				builder.setSingleChoiceItems(getResources().getTextArray(keyArray), i-1,new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				    	config.setStringValue(key,(String)vals[item]);
						dialog.cancel();
				    }
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
		});
	}

	private LinearLayout setBasePreference(int id, final String key, Integer title,
			Integer summary, Integer icon, final Intent intent) {
		LinearLayout switchLayout = (LinearLayout)findViewById(id);
		
		((TextView)switchLayout.findViewById(R.id.title)).setText(title);
        if (summary == null)
        	switchLayout.findViewById(R.id.summary).setVisibility(View.GONE);
        else
        	((TextView)switchLayout.findViewById(R.id.summary)).setText(summary);
        
        if (icon != null)
        	((ImageView)switchLayout.findViewById(R.id.icon)).setImageResource(icon);
        if (intent != null)
	        switchLayout.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					intent.putExtra("Preference", key);
					startActivity(intent);
				}
			});
		return switchLayout;
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
