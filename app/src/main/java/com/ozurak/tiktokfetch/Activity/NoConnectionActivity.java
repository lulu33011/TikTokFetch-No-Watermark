package com.ozurak.tiktokfetch.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ozurak.tiktokfetch.R;

/**
 * Stefan Najdovski
 * 5/22/2020
 * This Activity is used to show when no internet is found, the user has 2 options to open settings or to exit the app
 */
@SuppressWarnings("deprecation")
public class NoConnectionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final boolean isDark = getPrefs.getBoolean("User_theme_dark", true);

        if (!isDark) {
            setContentView(R.layout.activity_no_connection);
        } else {
            setContentView(R.layout.activity_no_connection_dark);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final Window window = getWindow();
                window.setStatusBarColor(0xFF_212121);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    final View decorView = window.getDecorView();
                    decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            }
        }
    }

    // Listens to a click on one of the Buttons
    public void onClick(@NonNull final View view) {
        final int id = view.getId();

        if (id == R.id.close) {
            finish();

        } else if (id == R.id.settings) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }
    }
}