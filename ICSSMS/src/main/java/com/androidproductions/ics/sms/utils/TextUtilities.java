package com.androidproductions.ics.sms.utils;

public final class TextUtilities {
	public static String replaceFormFeeds(String input){
		return input.replace('\f', '\n');
	}
}
