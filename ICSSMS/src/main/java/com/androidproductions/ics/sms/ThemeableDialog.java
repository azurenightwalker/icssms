package com.androidproductions.ics.sms;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;

import com.androidproductions.ics.sms.preferences.ConfigurationHelper;
import com.twww.excepttrack.ExceptTrackHandler;

public abstract class ThemeableDialog extends Activity{
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		ExceptTrackHandler.setUrl("http://x2-starlit-vim-g.appspot.com/error");
        ExceptTrackHandler.setup(this,"1000");
        int dialogType = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("DialogType","0"));
        int themeId = Integer.parseInt(ConfigurationHelper.getInstance().getStringValue(ConfigurationHelper.THEME));
        int theme = 0;
        switch (dialogType)
        {
	        case 0:
	        	switch (themeId)
	    		{
	    			case Constants.THEME_HOLO:
	    				theme = R.style.HoloDialog;
	    				break;
	    			case Constants.THEME_HOLO_BLUE:
	    				theme = R.style.HoloBlueDialog;
	    				break;
	    			case Constants.THEME_HOLO_GREEN:
	    				theme = R.style.HoloGreenDialog;
	    				break;
	    			case Constants.THEME_HOLO_RED:
	    				theme = R.style.HoloRedDialog;
	    				break;
	    			case Constants.THEME_HOLO_PURPLE:
	    				theme = R.style.HoloPurpleDialog;
	    				break;
	    			case Constants.THEME_HOLO_ORANGE:
	    				theme = R.style.HoloOrangeDialog;
	    				break;
                    case Constants.THEME_HOLO_LIGHT:
                        theme = R.style.HoloLightDialog;
                        break;
                    case Constants.THEME_SMOOTH:
                        theme = R.style.SmoothDialog;
                        break;
	    		}
	        	break;
	        case 1:
	        	switch (themeId)
	    		{
	    			case Constants.THEME_HOLO:
	    				theme = R.style.HoloDialogNoPadding;
	    				break;
	    			case Constants.THEME_HOLO_BLUE:
	    				theme = R.style.HoloBlueDialogNoPadding;
	    				break;
	    			case Constants.THEME_HOLO_GREEN:
	    				theme = R.style.HoloGreenDialogNoPadding;
	    				break;
	    			case Constants.THEME_HOLO_RED:
	    				theme = R.style.HoloRedDialogNoPadding;
	    				break;
	    			case Constants.THEME_HOLO_PURPLE:
	    				theme = R.style.HoloPurpleDialogNoPadding;
	    				break;
	    			case Constants.THEME_HOLO_ORANGE:
	    				theme = R.style.HoloOrangeDialogNoPadding;
	    				break;
	    			case Constants.THEME_HOLO_LIGHT:
	    				theme = R.style.HoloLightDialogNoPadding;
	    				break;
                    case Constants.THEME_SMOOTH:
                        theme = R.style.SmoothDialogNoPadding;
                        break;
	    		}
	        	getWindow().setGravity(Gravity.TOP);
		        getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	        	break;
	        case 2:
	        	switch (themeId)
	    		{
	    			case Constants.THEME_HOLO:
	    				theme = R.style.HoloDialogNoPaddingTransparent;
	    				break;
	    			case Constants.THEME_HOLO_BLUE:
	    				theme = R.style.HoloBlueDialogNoPaddingTransparent;
	    				break;
	    			case Constants.THEME_HOLO_GREEN:
	    				theme = R.style.HoloGreenDialogNoPaddingTransparent;
	    				break;
	    			case Constants.THEME_HOLO_RED:
	    				theme = R.style.HoloRedDialogNoPaddingTransparent;
	    				break;
	    			case Constants.THEME_HOLO_PURPLE:
	    				theme = R.style.HoloPurpleDialogNoPaddingTransparent;
	    				break;
	    			case Constants.THEME_HOLO_ORANGE:
	    				theme = R.style.HoloOrangeDialogNoPaddingTransparent;
	    				break;
	    			case Constants.THEME_HOLO_LIGHT:
	    				theme = R.style.HoloLightDialogNoPaddingTransparent;
	    				break;
                    case Constants.THEME_SMOOTH:
                        theme = R.style.SmoothDialogNoPaddingTransparent;
                        break;
	    		}
	        	break;
        }
        setTheme(theme);
	}
}
