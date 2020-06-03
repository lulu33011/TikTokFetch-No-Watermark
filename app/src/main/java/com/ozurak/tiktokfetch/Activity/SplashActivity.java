package com.ozurak.tiktokfetch.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.ozurak.tiktokfetch.R;
import com.ozurak.tiktokfetch.Services.ConnectivityService;

/**
 * Stefan Najdovski
 * 5/21/2020
 * This is the first Activity of the project that the user sees when they open the app
 */
public class SplashActivity extends AppCompatActivity {
    private boolean isThereConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init for isThere Connection
        isThereConnection = ConnectivityService.isConnected(this);

        //Kitkat translucent statusbar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            setTranslucentStatus();

        //Ignore Error
        // Make screen Portrait to disable Landscape orientation.
        //The Editor Probably will say that this is an error just ignore it, doesn't make any harm.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Binds the Activity to the xml file
        setContentView(R.layout.activity_splash_screen);

        //Show screen function call upon creating activty.
        showScreen();
    }

    // Checks if there is internet connection
    // if there is no internet connection it launches NoConnection Activity else goes to Main Activity.
    // if you want to make the splashscreen faster change the 800 value in ms to smaller number if you want it longer change the value to bigger value.
    // Example 800=0.8sec, 1500=1.5sec etc.
    private void showScreen() {
        new Handler().postDelayed(() -> {
            final Class<? extends Activity> activityClass;

            if (!isThereConnection) {
                // Jumps to No Connection Activity.
                activityClass = NoConnectionActivity.class;
            } else {
                // Jumps to Main Activty.
                activityClass = MainActivity.class;
            }

            startIntent(activityClass);
        }, 1500);
    }

    //This snippet of code is responsible for translucent statusbar if your device runs KitKat.

    private void setTranslucentStatus() {
        final int bits = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS : 0;

        final Window win = getWindow();
        final WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.flags |= bits;
        win.setAttributes(winParams);
    }

    //This snippet of code is responsible for destorying the SplashActivity and launching the new activity with animation in between, animation files are in res/anim folder.
    private void startIntent(final Class<? extends Activity> activityClass) {
        startActivity(new Intent(this, activityClass)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}