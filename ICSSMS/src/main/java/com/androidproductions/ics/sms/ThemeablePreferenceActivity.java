package com.androidproductions.ics.sms;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.androidproductions.ics.sms.preferences.ConfigurationHelper;
import com.twww.excepttrack.ExceptTrackHandler;

abstract class ThemeablePreferenceActivity extends PreferenceActivity {
	
	protected void onCreate(final Bundle savedInstanceState)
	{
		ExceptTrackHandler.setUrl("http://x2-starlit-vim-g.appspot.com/error");
        ExceptTrackHandler.setup(this,"1000");
        final int themeId = Integer.parseInt(ConfigurationHelper.getInstance().getStringValue(ConfigurationHelper.THEME));
        int theme = 0;
        switch (themeId)
		{
			case Constants.THEME_HOLO:
				theme = R.style.Holo;
				break;
			case Constants.THEME_HOLO_BLUE:
				theme = R.style.HoloBlue;
				break;
			case Constants.THEME_HOLO_GREEN:
				theme = R.style.HoloGreen;
				break;
			case Constants.THEME_HOLO_RED:
				theme = R.style.HoloRed;
				break;
			case Constants.THEME_HOLO_PURPLE:
				theme = R.style.HoloPurple;
				break;
			case Constants.THEME_HOLO_ORANGE:
				theme = R.style.HoloOrange;
				break;
            case Constants.THEME_HOLO_LIGHT:
                theme = R.style.HoloLight;
                break;
            case Constants.THEME_SMOOTH:
                theme = R.style.Smooth;
                break;
		}
        setTheme(theme);
        super.onCreate(savedInstanceState);
    }
}
