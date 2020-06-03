package com.ozurak.tiktokfetch;

import android.app.Activity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public final class AdsUtils {
    public static void loadAds(final Activity activity) {
        MobileAds.initialize(activity, initializationStatus -> {
        });
        final AdView mAdView = activity.findViewById(R.id.adView);
        final AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }
}