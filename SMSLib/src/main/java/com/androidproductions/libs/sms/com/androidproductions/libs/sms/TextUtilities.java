package com.androidproductions.libs.sms.com.androidproductions.libs.sms;

public final class TextUtilities {
    private TextUtilities() {
    }

    public static String replaceFormFeeds(final String input){
		return input.replace('\f', '\n');
	}
}
