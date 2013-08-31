package com.androidproductions.ics.sms;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.androidproductions.ics.sms.preferences.ConfigurationHelper;
import com.millennialmedia.android.MMAdView;
import com.millennialmedia.android.MMRequest;
import com.millennialmedia.android.MMSDK;
import com.twww.excepttrack.ExceptTrackHandler;

public abstract class AdSupportedActivity extends ThemeableActivity {
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        MMSDK.initialize(this);
    }

    protected MMRequest getAdRequest()
    {
        return new MMRequest();
    }

    protected void InitializeAds()
    {
        boolean showAds = ConfigurationHelper.getInstance()
                .getBooleanValue(ConfigurationHelper.SHOW_ADS);
        MMAdView adView = (MMAdView) findViewById(R.id.adView);
        if (adView != null)
        {
            if (showAds)
            {
                adView.setVisibility(View.VISIBLE);
                adView.setMMRequest(getAdRequest());
                adView.getAd();
            }
            else
            {
                adView.setVisibility(View.GONE);
            }
        }
    }
}
