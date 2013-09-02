package com.androidproductions.ics.sms.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class ConfigurationHelper {
	
	/* Preference Keys */
    public static final String NOTIFICATIONS_ENABLED = "Notification";
    public static final String DISABLE_OTHER_NOTIFICATIONS = "DisableOtherNotifications";
	public static final String DIALOG_ENABLED = "Dialog";
	public static final String THEME = "Theme";
	public static final String SMILEY_KEY_ENABLED = "ShowSmileyKey";
	public static final String HIDE_KEYBOARD_ON_SEND = "HideKeyboardOnSend";
	public static final String ALLOW_APEX = "AllowApex";
	public static final String APEX_KEY_COUNT = "pref_count";
    public static final String NOTIFICATION_SOUND = "NotificationSound";
    public static final String CUSTOM_SOUND = "CustomSound";
    public static final String PRIVATE_NOTIFICATIONS = "PrivateNotifications";
    public static final String NOTIFICATION_SHOWING = "NotificationShowing";
    public static final String ALTERNATIVE_ICON = "AlternateIcon";
    public static final String LIGHT_THEME = "LightTheme";
    public static final String DIALOG_TYPE = "DialogType";
    public static final String SHOW_ADS = "ShowAds";
    public static final String VIBRATION = "Vibration";

	private static ConfigurationHelper mConfigurationHelper;
	private final SharedPreferences mPreferences;

	private ConfigurationHelper(final Context applicationContext)
	{
		if (applicationContext == null)
			throw new IllegalArgumentException("Application context cannot be null");
		mPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
	}
	
	public static ConfigurationHelper getInstance()
	{
		return mConfigurationHelper;
	}

    public static void initInstance(final Context applicationContext)
    {
        if (mConfigurationHelper == null)
            mConfigurationHelper = new ConfigurationHelper(applicationContext);
        else
            throw new IllegalStateException("Instance has already been initialized");
    }
	
	public String getStringValue(final String key)
	{
		return mPreferences.getString(key, Defaults.STRING_CONSTANTS.get(key));
	}
	
	public void setStringValue(final String key, final String value)
	{
		final Editor edit = mPreferences.edit();
		edit.putString(key, value);
		edit.commit();
	}
	
	public int getIntValue(final String key)
	{
		try
		{
			return mPreferences.getInt(key, Defaults.INT_CONSTANTS.get(key));
		}
		catch (ClassCastException e)
		{
			return Integer.parseInt(mPreferences.getString(key, Defaults.INT_CONSTANTS.get(key).toString()));
		}
	}
	
	public void setIntValue(final String key, final int value)
	{
		final Editor edit = mPreferences.edit();
		edit.putInt(key, value);
		edit.commit();
	}
	
	public Boolean getBooleanValue(final String key)
	{
		return mPreferences.getBoolean(key, Defaults.BOOLEAN_CONSTANTS.get(key));
	}
	
	public void setBooleanValue(final String key, final Boolean value)
	{
		final Editor edit = mPreferences.edit();
		edit.putBoolean(key, value);
		edit.commit();
	}
	
	
}
