package com.smile.groundhoghunter;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.multidex.MultiDexApplication;
import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.MobileAds;
import com.smile.groundhoghunter.abstract_threads.IoFunctionThread;
import com.smile.groundhoghunter.constants.Constants;
import com.smile.smilelibraries.facebook_ads_util.*;
import com.smile.smilelibraries.google_ads_util.AdMobInterstitial;
import com.smile.smilelibraries.scoresqlite.*;
import com.smile.smilelibraries.show_interstitial_ads.ShowInterstitial;
import java.util.UUID;

public class GroundhogHunterApp extends MultiDexApplication {
    private static final String TAG = "GroundhogHunterApp";
    public static final String UUID_String = "b5af9bad-42e0-4d0d-8546-ebeb97e1abfa";
    public static final UUID ApplicationUUID = UUID.fromString(UUID_String);
    public static Resources AppResources;
    public static Context AppContext;
    public static ScoreSQLite ScoreSQLiteDB;
    public static IoFunctionThread selectedIoFuncThread;
    public static ShowInterstitial InterstitialAd;
    public static String facebookBannerID = "";
    public static String googleAdMobBannerID = "";
    public static int AdProvider = 0;    // default is AdMob
    public static boolean isFirstStartApp;
    public static FacebookInterstitial facebookAds;
    public static AdMobInterstitial googleInterstitialAd;

    @Override
    public void onCreate() {
        super.onCreate();

        isFirstStartApp = true;

        AppResources = getResources();
        AppContext = getApplicationContext();
        ScoreSQLiteDB = new ScoreSQLite(GroundhogHunterApp.AppContext, Constants.DATABASE_NAME);
        // Facebook ads (Interstitial ads)
        // groundhog hunter contains ads
        AudienceNetworkAds.initialize(this);
        String facebookInterstitialID = "308861513197370_308861586530696";
        String testString = "";
        // for debug mode
        if (BuildConfig.DEBUG) {
            testString = "IMG_16_9_APP_INSTALL#";
        }
        facebookInterstitialID = testString + facebookInterstitialID;
        facebookAds = new FacebookInterstitial(AppContext, facebookInterstitialID);

        // Google AdMob
        String googleAdMobAppID = getString(R.string.google_AdMobAppID);
        String googleAdMobInterstitialID = "ca-app-pub-8354869049759576/6595392508";
        MobileAds.initialize(AppContext, initializationStatus ->
                Log.d(TAG, "Google AdMob was initialized successfully."));

        googleInterstitialAd = new AdMobInterstitial(AppContext, googleAdMobInterstitialID);
        googleInterstitialAd.loadAd(); // load first ad
        googleAdMobBannerID = "ca-app-pub-8354869049759576/7169443235";

        final Handler adHandler = new Handler(Looper.getMainLooper());
        final Runnable adRunnable = () -> {
            adHandler.removeCallbacksAndMessages(null);
            if (googleInterstitialAd != null) {
                googleInterstitialAd.loadAd(); // load first google ad
            }
            if (facebookAds != null) {
                facebookAds.loadAd();   // load first facebook ad
            }
        };
        adHandler.postDelayed(adRunnable, 1000);
    }
}
