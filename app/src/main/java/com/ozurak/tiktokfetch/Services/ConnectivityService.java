package com.ozurak.tiktokfetch.Services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
/**
 * Stefan Najdovski
 * 5/21/2020
 */
public class ConnectivityService {

    private static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    public static boolean isConnected(Context context) {
        NetworkInfo info = ConnectivityService.getNetworkInfo(context);
        return (info != null && info.isConnected());
    }
}