package com.ozurak.tiktokfetch.Activity;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.BasePermissionListener;
import com.ozurak.tiktokfetch.AdsUtils;
import com.ozurak.tiktokfetch.R;
import com.ozurak.tiktokfetch.Services.ConnectivityService;
import com.tapadoo.alerter.Alerter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.dmoral.toasty.Toasty;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

/**
 * Stefan Najdovski
 * 5/21/2020
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static String OPENED_FROM_OUTSIDE = null;
    ArrayList<String> trueLink;
    ///////////////
    private AlertDialog dialog;
    private LayoutInflater layoutInflater;
    private DownloadManager downloadManager;
    private ClipboardManager clipboardManager;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        layoutInflater = getLayoutInflater();
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        final boolean isDark = sharedPreferences.getBoolean("User_theme_dark", true);

        if (!isDark) {
            setContentView(R.layout.activity_main);
        } else {
            final Window window = getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setStatusBarColor(0xFF_212121);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    final View decorView = window.getDecorView();
                    decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            }
            setContentView(R.layout.activity_main_dark);
        }

        // Check if the app is launched for the first time
        isFirstTime();

        //Ask for Permissions
        askPermission();

        final ImageView btnTikTok = findViewById(R.id.TikTokBtn);
        final ImageView btnModeChange = findViewById(R.id.btnModeChange);

        btnTikTok.setOnClickListener(this);
        btnModeChange.setOnClickListener(this);

        //Admob
        AdsUtils.loadAds(this);
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        final Intent intent1 = getIntent();
        final String dataString;
        if (intent1 != null && (dataString = intent1.getDataString()) != null) {
            OPENED_FROM_OUTSIDE = dataString;
        }
    }

    /**
     * So basically here is the onClicklistener when you click the TikTok button
     */
    @Override
    public void onClick(@NonNull final View view) {
        final int id = view.getId();
        if (id == R.id.btnModeChange) {
            showThemeAlertDialog(false);

        } else if (id == R.id.TikTokBtn) {
            // Checks if wifi or data is present
            final boolean isThereConnection = ConnectivityService.isConnected(this);

            if (!isThereConnection) {
                // Jumps to No Connection Activity.
                startActivity(new Intent(this, NoConnectionActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
                return;
            }

            // Checks for tiktok links, are they blank are they valid and displays messages if they are not
            // DO NOT DELETE THE LOGIC HERE The app will crash all the time if you do that
            if (OPENED_FROM_OUTSIDE != null && OPENED_FROM_OUTSIDE.contains("vm.tiktok.com/")) {
                Toasty.custom(this, getString(R.string.please_wait), ContextCompat.getDrawable(this, R.drawable.wait),
                        Toasty.LENGTH_SHORT, true).show();
                return;

            } else if (clipboardManager != null) {
                final ClipDescription clipDescription = clipboardManager.getPrimaryClipDescription();

                if (clipboardManager.hasPrimaryClip() && clipDescription != null && clipDescription.hasMimeType(MIMETYPE_TEXT_PLAIN)) {
                    final ClipData link = clipboardManager.getPrimaryClip();

                    if (link != null) {
                        final ClipData.Item item = link.getItemAt(0);

                        final String clipboardUrl = item.getText().toString();

                        if (clipboardUrl.contains("tiktok.com/")) {
                            Log.e("tiktok", clipboardUrl);
                            trueLink = getURLS(clipboardUrl);
                            saveVideo(trueLink.get(0));
                        }

                        if (clipboardUrl.isEmpty()) {
                            showAlert(R.string.no_copy_title, R.string.no_copy, R.color.colorWarning);
                        } else if (clipboardUrl.contains("tiktok.com/")) {
                            Toasty.custom(MainActivity.this, getString(R.string.please_wait), ContextCompat.getDrawable(this, R.drawable.wait), Toasty.LENGTH_SHORT, true).show();
                        } else {
                            showAlert(R.string.correct_url_title, R.string.correct_url, R.color.colorWarning);
                        }
                        return;
                    }

                } else {
                    showAlert(R.string.no_copy_title, R.string.no_copy, R.color.colorError);
                    return;
                }
            }

            showAlert(R.string.clipboard_error_title, R.string.clipboard_error, R.color.colorError);
        }
    }

    @NonNull
    public final ArrayList<String> getURLS(final String str) {
        final ArrayList<String> arrayList = new ArrayList<>();
        final Matcher matcher = Pattern.compile("\\(?\\b(https?://|www[.]|ftp://)[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]").matcher(str);
        while (matcher.find()) {
            String group = matcher.group();
            if (group.startsWith("(") && group.endsWith(")"))
                group = group.substring(1, group.length() - 1);
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
                String playURL = "";

                final Document document = Jsoup.connect(str)
                        .userAgent("Mozilla/5.0 (Linux; U; Android 4.0.2; en-us; Galaxy Nexus Build/ICL53F) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30")
                        .get();

                for (Element element : document.select("script")) {
                    final String data = element.data();
                    if (data.contains("videoData")) {
                        final String substring = data.substring(data.lastIndexOf("urls"));
                        final String substring2 = substring.substring(substring.indexOf("[") + 1);
                        final String substring3 = substring2.substring(0, substring2.indexOf("]"));
                        playURL = substring3.substring(1, substring3.length() - 1);
                    }
                }

                downloader(playURL);
            } catch (final Exception e) {
                Log.e("tiktok", "", e);
            }
        }).start();
    }

    /**
     * This snippet tells us when the download has started and where we download it as you can se we are saving all
     * file to /TikFetch folder with tiktok_time.mp4 format.
     */
    public void downloader(final String str) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(getApplicationContext(), R.string.download_started, Toast.LENGTH_SHORT).show());

        final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(withoutWatermark(str)));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setAllowedOverRoaming(true);
        request.setTitle(getString(R.string.app_name));
        request.setDescription(getString(R.string.downloading));
        request.setVisibleInDownloadsUi(true);
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(this, "/TikFetch", ("tiktok__" + System.currentTimeMillis()) + "." + "mp4");

        downloadManager.enqueue(request);
    }

    /**
     * We call the TikTok Server to get a watermark free sample of the video
     * DO NOT TOUCH THIS SNIPPET OF CODE IS VERY CRITITCAL AND MAY BREAK THE NO WATERMARK DEAL
     */
    public String withoutWatermark(final String url) {
        try {
            final HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                final StringBuilder stringBuffer = new StringBuilder();

                String readLine;
                while ((readLine = bufferedReader.readLine()) != null) {
                    stringBuffer.append(readLine);

                    if (stringBuffer.toString().contains("vid:")) {
                        try {
                            if (stringBuffer.substring(stringBuffer.indexOf("vid:")).substring(0, 4).equals("vid:")) {
                                final String substring = stringBuffer.substring(stringBuffer.indexOf("vid:"));
                                final String trim = substring.substring(4, substring.indexOf("%"))
                                        .replaceAll("[^A-Za-z0-9]", "").trim();
                                return "'https://api2-16-h2.musical.ly/aweme/v1/play/?video_id=" + trim;
                            }
                        } catch (final Exception e) {
                            Log.e("tiktok", "", e);
                        }
                    }
                }
            }

        } catch (final Exception e) {
            Log.e("tiktok", "", e);
        }

        return "";
    }

    /**
     * This fucntion displays an alert using Alerter library.
     */
    public void showAlert(final int title, final int message, final int color) {
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
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                //  Create a new boolean and preference and set it to true
                final boolean isFirstStart = sharedPreferences.getBoolean("isFirstStartedDialogTheme", true);

                //  If the activity has never started before...
                if (isFirstStart) {
                    showThemeAlertDialog(true);
                    //  Edit preference to make it false because we don't want this to run again
                    sharedPreferences.edit().putBoolean("isFirstStartedDialogTheme", false).apply();
                }

                // Remove all callbacks
                handler.removeCallbacks(this);
            }
        });
    }

    /**
     * This is our Custom ALertDialog Builder for showing the user a dialogbox
     * to choose their Theme
     */
    public void showThemeAlertDialog(final boolean isFirst) {
        final View view = layoutInflater.inflate(R.layout.alertdialog_custom_darkmode, null);
        final CardView btnDarkMode = view.findViewById(R.id.btn_dark_theme);
        final CardView btnLightMode = view.findViewById(R.id.btn_light_theme);

        final View.OnClickListener onClickListener = v -> {
            final SharedPreferences.Editor edit = sharedPreferences.edit();
            if (v == btnDarkMode) {
                edit.putBoolean("User_theme_dark", true);
            } else if (v == btnLightMode) {
                edit.putBoolean("User_theme_dark", false);
            }
            edit.apply();

            if (dialog != null && dialog.isShowing()) dialog.dismiss();
            recreate();
        };

        btnDarkMode.setOnClickListener(onClickListener);
        btnLightMode.setOnClickListener(onClickListener);

        dialog = new AlertDialog.Builder(this, R.style.DarkDialog).setView(view)
                .setCancelable(!isFirst).show();
    }

    /**
     * DO NOT DISABLE THIS SNIPPET OF CODE!!!
     * If you disable or ignore this snippet of code the app wont work,it will crash or will not Download
     */
    private void askPermission() {
        Dexter.withContext(this).withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(new BasePermissionListener() {
            @Override
            public void onPermissionDenied(final PermissionDeniedResponse permissionDeniedResponse) {
                if (permissionDeniedResponse.isPermanentlyDenied()) {
                    Toasty.info(MainActivity.this, "You have denied stroage permissions permanently," +
                                    " if the app force close try granting permission from Settings > Apps.",
                            Toasty.LENGTH_LONG, true).show();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(final PermissionRequest request, final PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }
}
