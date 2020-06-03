package com.ozurak.tiktokfetch.Services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Stefan Najdovski
 * 5/21/2020
 */
public class ConnectivityService {
    @Nullable
    private static NetworkInfo getNetworkInfo(@NonNull final Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null ? cm.getActiveNetworkInfo() : null;
    }

    public static boolean isConnected(final Context context) {
        final NetworkInfo info = ConnectivityService.getNetworkInfo(context);
        return info != null && info.isConnected();
    }
}