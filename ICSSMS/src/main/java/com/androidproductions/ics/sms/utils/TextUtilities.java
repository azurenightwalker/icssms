package com.androidproductions.ics.sms.utils;

public class TextUtilities {
	public static String replaceFormFeeds(String input){
		return input.replace('\f', '\n');
	}
}
