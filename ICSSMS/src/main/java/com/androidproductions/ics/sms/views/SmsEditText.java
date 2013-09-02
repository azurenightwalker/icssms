package com.androidproductions.ics.sms.views;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.EditText;

import com.androidproductions.ics.sms.preferences.ConfigurationHelper;

public class SmsEditText extends EditText {

	public SmsEditText(final Context context) {
		super(context);
		setAttrs();
	}
	
	public SmsEditText(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		setAttrs();
	}
	
	public SmsEditText(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		setAttrs();
	}
	
	private void setAttrs()
	{
        ConfigurationHelper.getInstance();
		if (ConfigurationHelper.getInstance().getBooleanValue(ConfigurationHelper.SMILEY_KEY_ENABLED))
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
