package com.androidproductions.ics.sms.data;

import android.content.Context;

public class PhoneNumber {

	public final String number;
	public final String mLabel;
	public final boolean primary;

	public PhoneNumber(Long contactId, Long phoneId, String num, String label,
			Context context, boolean isPrimary) {
		number = num;
		mLabel = label;
		primary = isPrimary;
	}

}
