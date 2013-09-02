package com.androidproductions.ics.sms;

import android.os.Bundle;
import android.view.View;

import com.androidproductions.ics.sms.preferences.ConfigurationHelper;
import com.millennialmedia.android.MMAdView;
import com.millennialmedia.android.MMRequest;
import com.millennialmedia.android.MMSDK;

public abstract class AdSupportedActivity extends ThemeableActivity {
	
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        MMSDK.initialize(this);
    }

    MMRequest getAdRequest()
    {
        return new MMRequest();
    }

    void InitializeAds()
    {
        final boolean showAds = ConfigurationHelper.getInstance()
                .getBooleanValue(ConfigurationHelper.SHOW_ADS);
        final MMAdView adView = (MMAdView) findViewById(R.id.adView);
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
