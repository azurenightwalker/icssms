package com.androidproductions.ics.sms.preferences;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class Defaults {
    static final Map<String,Integer> INT_CONSTANTS;
    static final Map<String,String> STRING_CONSTANTS;
    static final Map<String,Boolean> BOOLEAN_CONSTANTS;
	static {
		Map<String, Integer> int_constants = new HashMap<String, Integer>();
		int_constants.put(ConfigurationHelper.APEX_KEY_COUNT, -1);
		INT_CONSTANTS = Collections.unmodifiableMap(int_constants);
		
		Map<String, String> string_constants = new HashMap<String, String>();
		string_constants.put(ConfigurationHelper.THEME, "0");
        string_constants.put(ConfigurationHelper.NOTIFICATION_SOUND, "");
        string_constants.put(ConfigurationHelper.DIALOG_TYPE, "0");
        STRING_CONSTANTS = Collections.unmodifiableMap(string_constants);
		
		Map<String, Boolean> boolean_constants = new HashMap<String, Boolean>();
		boolean_constants.put(ConfigurationHelper.NOTIFICATIONS_ENABLED, true);
		boolean_constants.put(ConfigurationHelper.DIALOG_ENABLED, true);
		boolean_constants.put(ConfigurationHelper.SMILEY_KEY_ENABLED, true);
		boolean_constants.put(ConfigurationHelper.HIDE_KEYBOARD_ON_SEND, true);
		boolean_constants.put(ConfigurationHelper.ALLOW_APEX, true);
        boolean_constants.put(ConfigurationHelper.LIGHT_THEME, false);
        boolean_constants.put(ConfigurationHelper.DISABLE_OTHER_NOTIFICATIONS, true);
        boolean_constants.put(ConfigurationHelper.SHOW_ADS, true);
        boolean_constants.put(ConfigurationHelper.VIBRATION, true);
        boolean_constants.put(ConfigurationHelper.NOTIFICATION_SHOWING, false);
        boolean_constants.put(ConfigurationHelper.ALTERNATIVE_ICON, false);
        boolean_constants.put(ConfigurationHelper.CUSTOM_SOUND,true);
        boolean_constants.put(ConfigurationHelper.PRIVATE_NOTIFICATIONS,false);
		BOOLEAN_CONSTANTS = Collections.unmodifiableMap(boolean_constants);
		
	}
}
