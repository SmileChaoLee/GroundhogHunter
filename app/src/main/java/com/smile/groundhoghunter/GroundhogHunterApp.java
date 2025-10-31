package com.smile.groundhoghunter;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.multidex.MultiDexApplication;

import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.smile.groundhoghunter.AbstractClasses.IoFunctionThread;
import com.smile.smilelibraries.facebook_ads_util.*;
import com.smile.smilelibraries.google_admob_ads_util.GoogleAdMobInterstitial;
import com.smile.smilelibraries.scoresqlite.*;
import com.smile.smilelibraries.showing_instertitial_ads_utility.ShowingInterstitialAdsUtil;
import com.smile.smilelibraries.utilities.ScreenUtil;

import java.util.UUID;

public class GroundhogHunterApp extends MultiDexApplication {

    // public final String REST_Website = new String("http://192.168.0.11:5000/Playerscore");
    // public static final String REST_Website = "http://ec2-13-59-195-3.us-east-2.compute.amazonaws.com/Playerscore";
    public static final String REST_Website = "http://ec2-13-59-195-3.us-east-2.compute.amazonaws.com/Playerscore";
    public static final int GameId = 2; // this GameId is for backend game_id in playerscore table
    public static final int FontSize_Scale_Type = ScreenUtil.FontSize_Pixel_Type;
    public static final String UUID_String = "b5af9bad-42e0-4d0d-8546-ebeb97e1abfa";
    public static final UUID ApplicationUUID = UUID.fromString(UUID_String);

    public static Resources AppResources;
    public static Context AppContext;
    public static ScoreSQLite ScoreSQLiteDB;
    // public static BluetoothFunctionThread selectedBtFuncThread;
    public static IoFunctionThread selectedIoFuncThread;

    public static ShowingInterstitialAdsUtil InterstitialAd;
    public static String facebookBannerID = "";
    public static String googleAdMobBannerID = "";
    public static int AdProvider = ShowingInterstitialAdsUtil.FacebookAdProvider;    // default is Facebook Ad

    public static boolean isFirstStartApp;

    public static FacebookInterstitialAds facebookAds;
    public static GoogleAdMobInterstitial googleInterstitialAd;

    private static final String TAG = "GroundhogHunterApp";

    @Override
    public void onCreate() {
        super.onCreate();

        isFirstStartApp = true;

        AppResources = getResources();
        AppContext = getApplicationContext();
        ScoreSQLiteDB = new ScoreSQLite(GroundhogHunterApp.AppContext);
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
        facebookAds = new FacebookInterstitialAds(AppContext, facebookInterstitialID);

        // Google AdMob
        String googleAdMobAppID = getString(R.string.google_AdMobAppID);
        String googleAdMobInterstitialID = "ca-app-pub-8354869049759576/6595392508";
        MobileAds.initialize(AppContext, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.d(TAG, "Google AdMob was initialized successfully.");
            }
        });

        googleInterstitialAd = new GoogleAdMobInterstitial(AppContext, googleAdMobInterstitialID);
        googleInterstitialAd.loadAd(); // load first ad
        googleAdMobBannerID = "ca-app-pub-8354869049759576/7169443235";

        final Handler adHandler = new Handler(Looper.getMainLooper());
        final Runnable adRunnable = new Runnable() {
            @Override
            public void run() {
                adHandler.removeCallbacksAndMessages(null);
                if (googleInterstitialAd != null) {
                    googleInterstitialAd.loadAd(); // load first google ad
                }
                if (facebookAds != null) {
                    facebookAds.loadAd();   // load first facebook ad
                }
            }
        };
        adHandler.postDelayed(adRunnable, 1000);
    }
}
