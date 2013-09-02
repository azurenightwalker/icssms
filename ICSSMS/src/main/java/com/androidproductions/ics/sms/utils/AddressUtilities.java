package com.androidproductions.ics.sms.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public final class AddressUtilities {
    public static String StandardiseNumber(String address, Context context)
    {
    	try {
    		String code = "US";
    		try
    		{
    		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    		code = tm.getNetworkCountryIso();
    		}
    		catch (Exception e) { e.printStackTrace(); }
    		PhoneNumberUtil inst = PhoneNumberUtil.getInstance();
			PhoneNumber val = inst.parse(address,code);
			if (inst.isValidNumber(val))
				return inst.format(val, PhoneNumberFormat.NATIONAL);
			// Try US
			val = inst.parse(address,"US");
			if (inst.isValidNumber(val))
				return inst.format(val, PhoneNumberFormat.NATIONAL);
			return address;
			//return "0"+String.valueOf(val.getNationalNumber());
		} catch (Exception e) {
			if (address == null) return "";
			return address;
		}
    }
}
