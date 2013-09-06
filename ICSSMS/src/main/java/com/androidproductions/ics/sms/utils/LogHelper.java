package com.androidproductions.ics.sms.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public final class LogHelper {
	private static final String TAG = "ICS SMS";

	private static int DEBUG_LEVEL;
    public static final int DEBUG_LEVEL_ERROR = 0;
    public static final int DEBUG_LEVEL_WARNING = 1;
    private static final int DEBUG_LEVEL_INFO = 2;
    private static final int DEBUG_LEVEL_DEBUG = 3;
    private static final int DEBUG_LEVEL_VERBOSE = 4;
	
	private static int TOAST;
	private static final int TOAST_ENABLED = 1;
	private static final int TOAST_DISABLED = 0;

    private LogHelper() {
    }

    public static void setDebug(final int level)
	{
		DEBUG_LEVEL = level;
	}
	
	public static void v(final String msg)
	{
		if (DEBUG_LEVEL >= DEBUG_LEVEL_VERBOSE)
			Log.v(TAG, msg);
	}
	
	public static void d(final String msg)
	{
		if (DEBUG_LEVEL >= DEBUG_LEVEL_DEBUG)
			Log.d(TAG, msg);
	}
	
	public static void i(final String msg)
	{
		if (DEBUG_LEVEL >= DEBUG_LEVEL_INFO)
			Log.d(TAG, msg);
	}
	
	public static void w(final String msg)
	{
		if (DEBUG_LEVEL >= DEBUG_LEVEL_WARNING)
			Log.w(TAG, msg);
	}
	
	public static void e(final String msg)
	{
		Log.e(TAG, msg);
	}
	
	public static void enableToast()
	{
		TOAST = TOAST_ENABLED;
	}
	
	public static void disableToast()
	{
		TOAST = TOAST_DISABLED;
	}
	
	public static void t(final Context c, final String msg)
	{
		if (TOAST == TOAST_ENABLED)
			Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
	}
}
