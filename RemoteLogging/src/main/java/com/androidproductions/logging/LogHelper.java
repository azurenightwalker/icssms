package com.androidproductions.logging;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public final class LogHelper {

    private static LogHelper mLogHelper;

	public static final int DEBUG_LEVEL_ERROR = 0;
    public static final int DEBUG_LEVEL_WARNING = 1;
    private static final int DEBUG_LEVEL_INFO = 2;
    private static final int DEBUG_LEVEL_DEBUG = 3;
    private static final int DEBUG_LEVEL_VERBOSE = 4;

    private static final int TOAST_ENABLED = 1;
	private static final int TOAST_DISABLED = 0;

    private int DEBUG_LEVEL;
    private int TOAST;
    private final String TAG;

    public static LogHelper getInstance() {
        return mLogHelper;
    }

    public LogHelper(String logTag) {
        TAG = logTag;
    }

    public static void initInstance(final String tag) {
        if (mLogHelper == null)
            mLogHelper = new LogHelper(tag);
    }

    public void setDebug(final int level)
	{
		DEBUG_LEVEL = level;
	}
	
	public void v(final String msg)
	{
		if (DEBUG_LEVEL >= DEBUG_LEVEL_VERBOSE)
			Log.v(TAG, msg);
	}
	
	public void d(final String msg)
	{
		if (DEBUG_LEVEL >= DEBUG_LEVEL_DEBUG)
			Log.d(TAG, msg);
	}
	
	public void i(final String msg)
	{
		if (DEBUG_LEVEL >= DEBUG_LEVEL_INFO)
			Log.d(TAG, msg);
	}
	
	public void w(final String msg)
	{
		if (DEBUG_LEVEL >= DEBUG_LEVEL_WARNING)
			Log.w(TAG, msg);
	}
	
	public void e(final String msg)
	{
		Log.e(TAG, msg);
	}
	
	public void enableToast()
	{
		TOAST = TOAST_ENABLED;
	}
	
	public void disableToast()
	{
		TOAST = TOAST_DISABLED;
	}
	
	public void t(final Context c, final String msg)
	{
		if (TOAST == TOAST_ENABLED)
			Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
	}
}
