package com.androidproductions.ics.sms.views;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.EditText;

import com.androidproductions.ics.sms.Constants;
import com.androidproductions.ics.sms.preferences.ConfigurationHelper;

public class SmsEditText extends EditText {

	public SmsEditText(Context context) {
		super(context);
		setAttrs();
	}
	
	public SmsEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		setAttrs();
	}
	
	public SmsEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setAttrs();
	}
	
	private void setAttrs()
	{
        Context context = getContext();
        ConfigurationHelper.getInstance(context);
		if (ConfigurationHelper.getInstance(context).getBooleanValue(ConfigurationHelper.SMILEY_KEY_ENABLED))
			setInputType(InputType.TYPE_CLASS_TEXT |
						 InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE |
					     InputType.TYPE_TEXT_FLAG_AUTO_CORRECT |
					     InputType.TYPE_TEXT_FLAG_CAP_SENTENCES |
					     InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		else
			setInputType(InputType.TYPE_CLASS_TEXT |
					 InputType.TYPE_TEXT_FLAG_AUTO_CORRECT |
				     InputType.TYPE_TEXT_FLAG_CAP_SENTENCES |
				     InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		setMinLines(1);
		setMaxLines(3);
		setGravity(Gravity.TOP);
	}

}
