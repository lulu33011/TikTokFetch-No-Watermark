package com.ozurak.tiktokfetch.Activity;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.ozurak.tiktokfetch.R;
import com.ozurak.tiktokfetch.Services.ConnectivityService;
import com.tapadoo.alerter.Alerter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.dmoral.toasty.Toasty;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
/**
 * Stefan Najdovski
 * 5/21/2020
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private static String OPENED_FROM_OUTSIDE = null;
    String playURL = "";
    ArrayList < String > trueLink;
    private AdView mAdView;
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Ignore Error
        // Make screen Portrait to disable Landscape orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //bind xml with main activity
        SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());

        boolean isDark = getPrefs.getBoolean("User_theme_dark", true);

        if (isDark) {
            setContentView(R.layout.activity_main_dark);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(Color.parseColor("#212121"));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            }
        } else {
            setContentView(R.layout.activity_main);
        }

        //Check if the app is launched for the first time
        isFirstTime();
        //Ask for Permissions
        askPermission();
        ImageView mTikTokBtn = findViewById(R.id.TikTokBtn);
        mTikTokBtn.setOnClickListener(this);



        //Admob
        MobileAds.initialize(this, initializationStatus -> {
        });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (getIntent() != null && getIntent().getDataString() != null) {
            OPENED_FROM_OUTSIDE = getIntent().getDataString();
        }
    }
    /**
     * So basilcly here is the onClicklistener when you click the TikTok button
     */

    @Override
    public void onClick(View view) {
        //Checks if wifi or data is present
        //  private InterstitialAd mInterstitialAd;
        boolean isThereConnection = ConnectivityService.isConnected(this);
        if (!isThereConnection) {

            //Jumps to No Connection Activity.
            MainActivity.this.startIntent(new Intent(MainActivity.this, NoConnectionActivity.class));

        }
        else {

            //Checks for tiktok links, are they blank are they valid and displays messages if they are not
            //DO NOT DELETE THE LOGIC HERE The app will crash all the time if you do that
            if (OPENED_FROM_OUTSIDE != null && (OPENED_FROM_OUTSIDE.contains("vm.tiktok.com/"))) {
                //  mFabProgressCircle.show();

                Toasty.custom(MainActivity.this, getString(R.string.please_wait), ContextCompat.getDrawable(this, R.drawable.wait), Toasty.LENGTH_SHORT, true).show();
            } else {
                ClipboardManager mClipboardService = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                if (mClipboardService != null) {
                    if (mClipboardService.hasPrimaryClip() &&
                            mClipboardService.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)) {

                        ClipData link = mClipboardService.getPrimaryClip();


                        ClipData.Item item = link.getItemAt(0);
                        String mClipboardUrl = item.getText().toString();
                        if (mClipboardUrl.contains("tiktok.com/")) {
                            Log.e("tiktok", mClipboardUrl);
                            MainActivity MainActivity = MainActivity.this;
                            MainActivity.trueLink = MainActivity.getURLS(mClipboardUrl);
                            MainActivity MainActivity2 = MainActivity.this;
                            MainActivity2.saveVideo(MainActivity2.trueLink.get(0));

                        }
                        if (mClipboardUrl.isEmpty()) {
                            showAlert(R.string.no_copy_title, R.string.no_copy, R.color.Warning);
                        } else if (mClipboardUrl.contains("tiktok.com/")) {
                            Toasty.custom(MainActivity.this, getString(R.string.please_wait), ContextCompat.getDrawable(this, R.drawable.wait), Toasty.LENGTH_SHORT, true).show();
                        } else {
                            showAlert(R.string.correct_url_title, R.string.correct_url, R.color.Warning);
                        }


                    } else {

                        showAlert(R.string.no_copy_title, R.string.no_copy, R.color.ERROR);
                    }

                } else {
                    showAlert(R.string.clipboard_error_title, R.string.clipboard_error, R.color.ERROR);
                }
            }
        }

    }
    /**
     * This is empty LongClickListener
     */
    @Override
    public boolean onLongClick(View view) {
        return false;
    }

    public final ArrayList < String > getURLS(String str) {
        ArrayList < String > arrayList = new ArrayList < > ();
        Matcher matcher = Pattern.compile("\\(?\\b(https?://|www[.]|ftp://)[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]").matcher(str);
        while (matcher.find()) {
            String group = matcher.group();
            if (group.startsWith("(") && group.endsWith(")")) {
                group = group.substring(1, group.length() - 1);
            }
            arrayList.add(group);
        }
        return arrayList;
    }
    /**
     * This is where the magic happens, first we spoof our device using Jsoup than we process the url and lastly we get the playurl
     */
    public void saveVideo(final String str) {
        new Thread(() -> {
            try {
                for (Element element: Jsoup.connect(str).userAgent("Mozilla/5.0 (Linux; U; Android 4.0.2; en-us; Galaxy Nexus Build/ICL53F) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30").get().select("script")) {
                    String data = element.data();
                    if (data.contains("videoData")) {
                        String substring = data.substring(data.lastIndexOf("urls"));
                        String substring2 = substring.substring(substring.indexOf("[") + 1);
                        String substring3 = substring2.substring(0, substring2.indexOf("]"));
                        MainActivity.this.playURL = substring3.substring(1, substring3.length() - 1);
                    }
                }
                MainActivity.this.downloader(MainActivity.this.playURL);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    /**
     * This snippet tells us when the download has started and where we download it as you can se we are saving all
     * file to /TikFetch folder with tiktok_time.mp4 format.
     */

    public void downloader(String str) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Context applicationContext = MainActivity.this.getApplicationContext();
            Toast.makeText(applicationContext, "" + MainActivity.this.getString(R.string.download_started), Toast.LENGTH_LONG).show();
        });
        String string = getString(R.string.downloading);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(withoutWatermark(str)));
        request.setAllowedNetworkTypes(3);
        request.setAllowedOverRoaming(true);
        request.setTitle(getString(R.string.app_name));
        request.setVisibleInDownloadsUi(true);
        request.setDescription(string);
        request.setVisibleInDownloadsUi(true);
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(1);
        request.setDestinationInExternalFilesDir(this, "/TikFetch", ("tiktok__" + System.currentTimeMillis()) + "." + "mp4");

        ((DownloadManager) getSystemService(DOWNLOAD_SERVICE)).enqueue(request);
    }
    /**
     * We call the TikTok Server to get a watermark free sample of the video
     * DO NOT TOUCH THIS SNIPPET OF CODE IS VERY CRITITCAL AND MAY BREAK THE NO WATERMARK DEAL
     */
    public String withoutWatermark(String url) {
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            httpURLConnection.getResponseCode();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine != null) {
                    stringBuffer.append(readLine);
                    if (stringBuffer.toString().contains("vid:")) {
                        try {
                            if (stringBuffer.substring(stringBuffer.indexOf("vid:")).substring(0, 4).equals("vid:")) {
                                String substring = stringBuffer.substring(stringBuffer.indexOf("vid:"));
                                String trim = substring.substring(4, substring.indexOf("%")).replaceAll("[^A-Za-z0-9]", "").trim();
                                return "http://api2.musical.ly/aweme/v1/playwm/?video_id=" + trim;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            return "";
        }
    }

    /**
     * This fucntion displays an alert using Alerter library.
     */
    public void showAlert(int title, int message, int color) {
        Alerter.create(this)
                .setTitle(getString(title))
                .setText(getString(message))
                .setIcon(R.drawable.ic_error)
                .setBackgroundColorRes(color)
                .show();
    }

    /**
     * Function to check if user is using the app for the first time.
     */
    public void isFirstTime() {
        Runnable r = () -> {
            //  Declare a new thread to do a preference check

            //  Initialize SharedPreferences
            SharedPreferences getPrefs = PreferenceManager
                    .getDefaultSharedPreferences(getBaseContext());

            //  Create a new boolean and preference and set it to true
            boolean isFirstStart = getPrefs.getBoolean("isFirstStartedDialogTheme", true);

            //  If the activity has never started before...
            if (isFirstStart) {
                showalertthemedialog();
                SharedPreferences getPreftheme = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());
                //  Make a new preferences editor
                SharedPreferences.Editor e = getPreftheme.edit();
                //  Edit preference to make it false because we don't want this to run again
                e.putBoolean("isFirstStartedDialogTheme", false);
                e.apply();

            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 0);
    }

    /**
     * This is our Custom ALertDialog Builder for showing the user a dialogbox
     * to choose their Theme
     */
    public void showalertthemedialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.DarkDialog);

        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.alertdialog_custom_darkmode, null);
        Button darkmodebtn = view.findViewById(R.id.btn_dark_theme);
        Button lightmodebtn = view.findViewById(R.id.btn_light_theme);
        builder.setView(view);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
        darkmodebtn.setOnClickListener(view1 -> {
            SharedPreferences getPrefs = PreferenceManager
                    .getDefaultSharedPreferences(getBaseContext());
            //  Make a new preferences editor
            SharedPreferences.Editor e = getPrefs.edit();
            //  Edit preference to make it false because we don't want this to run again
            e.putBoolean("User_theme_dark", true);

            e.apply();

            //  Create a new boolean and preference and set it to true

            dialog.dismiss();
            MainActivity.this.recreate();
        });

        lightmodebtn.setOnClickListener(view12 -> {
            SharedPreferences getPrefs = PreferenceManager
                    .getDefaultSharedPreferences(getBaseContext());
            //  Make a new preferences editor
            SharedPreferences.Editor e = getPrefs.edit();
            //  Edit preference to make it false because we don't want this to run again
            e.putBoolean("User_theme_dark", false);
            e.apply();
            dialog.dismiss();
            MainActivity.this.recreate();
        });

    }

    /**
     * DO NOT DISABLE THIS SNIPPET OF CODE!!!
     * If you disable or ignore this snippet of code the app wont work,it will crash or will not Download
     */
    private void askPermission() {

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_NETWORK_STATE
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            Toasty.info(MainActivity.this, "You have denied some permissions permanently, if the app force close try granting permission from settings.", Toasty.LENGTH_LONG, true).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List < PermissionRequest > permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }
    private void startIntent(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

}