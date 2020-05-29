package com.ozurak.tiktokfetch.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.preference.PreferenceManager;
import android.view.View;

import com.ozurak.tiktokfetch.R;


/**
  * Stefan Najdovski
  * 5/22/2020
  * This Activity is used to show when no internet is found, the user has 2 options to open settings or to exit the app
 */
public class NoConnectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Ignore Error
        //The Editor Probably will say that this is an error just ignore it, doesn't make any harm.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //binds the xml file with the activty
        SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());

        boolean isDark = getPrefs.getBoolean("User_theme_dark", true);

        if (isDark) {
            setContentView(R.layout.activity_no_connection_dark);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(Color.parseColor("#212121"));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            }
        } else {
            setContentView(R.layout.activity_no_connection);
        }
    }
    //Listens to a Click on one of the Buttons
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.close:
                finish();
                break;
            case R.id.settings:
                Intent Settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
                startActivity(Settings);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
                break;
        }
    }
}