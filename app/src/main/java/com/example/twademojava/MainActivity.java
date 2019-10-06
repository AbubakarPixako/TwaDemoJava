package com.example.twademojava;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsCallback;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsServiceConnection;
import androidx.browser.customtabs.CustomTabsSession;
import androidx.browser.customtabs.TrustedWebUtils;

import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String WEB_URL = "https://google.com";
    @Nullable
    private MainActivity.TwaCustomTabsServiceConnection mServiceConnection;
    private boolean mTwaWasLaunched;
    private static boolean sChromeVersionChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String chromePackage = CustomTabsClient.getPackageName(this, TrustedWebUtils.SUPPORTED_CHROME_PACKAGES, true);
        if (chromePackage == null) {
            TrustedWebUtils.showNoPackageToast(this);
            this.finish();
        } else {
            if (!sChromeVersionChecked) {
                if (chromeNeedsUpdate(this.getPackageManager(), chromePackage)) {
                    showToastIfResourceExists(this, "string/update_chrome_toast");
                    this.finish();
                    return;
                }
                sChromeVersionChecked = true;
            }
            if (savedInstanceState != null && savedInstanceState.getBoolean("android.support.customtabs.trusted.TWA_WAS_LAUNCHED_KEY")) {
                this.finish();
            } else {
                this.mServiceConnection = new MainActivity.TwaCustomTabsServiceConnection();
                CustomTabsClient.bindCustomTabsService(this, chromePackage, this.mServiceConnection);
            }
        }

    }

    protected void onRestart() {
        super.onRestart();
        if (this.mTwaWasLaunched) {
            this.finish();
        }

    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mServiceConnection != null) {
            this.unbindService(this.mServiceConnection);
        }

    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("android.support.customtabs.trusted.TWA_WAS_LAUNCHED_KEY", this.mTwaWasLaunched);
    }


    protected CustomTabsSession getSession(CustomTabsClient client) {
        return client.newSession((CustomTabsCallback) null, 96375);
    }

    protected CustomTabsIntent getCustomTabsIntent(CustomTabsSession session) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(session);
        return builder.build();
    }

    protected Uri getLaunchingUrl() {
        String WebUrl = "https://google.com";
        if (WebUrl == null || WebUrl.isEmpty()) {
            Log.d("Web Url: ", "is not set");
            WebUrl = WEB_URL;
        }
        Uri uri = Uri.parse(WebUrl);
        return uri;
    }



    private class TwaCustomTabsServiceConnection extends CustomTabsServiceConnection {

        public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient client) {

            CustomTabsSession session = MainActivity.this.getSession(client);
            CustomTabsIntent intent = MainActivity.this.getCustomTabsIntent(session);
            Uri url = MainActivity.this.getLaunchingUrl();

            Log.d("TWALink", url.toString());
            // build twa
            TrustedWebUtils.launchAsTrustedWebActivity(MainActivity.this, intent, url);
            MainActivity.this.mTwaWasLaunched = true;
        }

        public void onServiceDisconnected(ComponentName componentName) {
        }
    }

    private static boolean chromeNeedsUpdate(PackageManager pm, String chromePackage) {
        try {
            PackageInfo packageInfo = pm.getPackageInfo(chromePackage, 0);
            int firstDotIndex = packageInfo.versionName.indexOf(".");
            String majorVersion = packageInfo.versionName.substring(0, firstDotIndex);
            return Integer.parseInt(majorVersion) < 72;

        } catch (PackageManager.NameNotFoundException var3) {
        }

        return false;
    }

    private static void showToastIfResourceExists(Context context, String resource) {
        int stringId = context.getResources().getIdentifier(resource, (String) null, context.getPackageName());
        if (stringId != 0) {
            Toast.makeText(context, stringId, Toast.LENGTH_LONG).show();
        }
    }
}
