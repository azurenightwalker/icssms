package com.androidproductions.ics.sms.utils;

public final class TextUtilities {
    private TextUtilities() {
    }

    public static String replaceFormFeeds(final String input){
		return input.replace('\f', '\n');
	}
}
