package com.smile.groundhoghunter;

import android.content.Context;
import android.content.res.Resources;
import android.support.multidex.MultiDexApplication;

import com.google.android.gms.ads.MobileAds;
import com.smile.smilepublicclasseslibrary.facebook_ads_util.*;
import com.smile.smilepublicclasseslibrary.google_admob_ads_util.GoogleAdMobInterstitial;
import com.smile.smilepublicclasseslibrary.scoresqlite.*;
import com.smile.smilepublicclasseslibrary.showing_instertitial_ads_utility.ShowingInterstitialAdsUtil;
import com.smile.smilepublicclasseslibrary.utilities.ScreenUtil;

import java.util.UUID;

public class GroundhogHunterApp extends MultiDexApplication {

    // public final String REST_Website = new String("http://192.168.0.11:5000/Playerscore");
    // public static final String REST_Website = "http://ec2-13-59-195-3.us-east-2.compute.amazonaws.com/Playerscore";
    public static final String REST_Website = "http://ec2-13-59-195-3.us-east-2.compute.amazonaws.com/Playerscore";
    public static final int GameId = 2; // this GameId is for backend game_id in playerscore table
    public static final int FontSize_Scale_Type = ScreenUtil.FontSize_Pixel_Type;
    public static final String PrivacyPolicyUrl = "http://ec2-13-59-195-3.us-east-2.compute.amazonaws.com/PrivacyPolicy";
    public static final String UUID_String = "b5af9bad-42e0-4d0d-8546-ebeb97e1abfa";
    public static final UUID ApplicationUUID = UUID.fromString(UUID_String);
    public static final int durationForBluetoothVisible = 60;   // 60 seconds

    public static Resources AppResources;
    public static Context AppContext;
    public static ScoreSQLite ScoreSQLiteDB;

    public static ShowingInterstitialAdsUtil InterstitialAd;
    public static String googleAdMobBannerID = "";

    private static FacebookInterstitialAds facebookAds;
    private static GoogleAdMobInterstitial googleInterstitialAd;

    @Override
    public void onCreate() {
        super.onCreate();
        AppResources = getResources();
        AppContext = getApplicationContext();
        ScoreSQLiteDB = new ScoreSQLite(GroundhogHunterApp.AppContext);
        // Facebook ads (Interstitial ads)
        if (BuildConfig.APPLICATION_ID == "com.smile.groundhoghunter") {
            // groundhog hunter for free
            String facebookPlacementID = new String("308861513197370_308861586530696");
            facebookAds = new FacebookInterstitialAds(AppContext, facebookPlacementID);
            facebookAds.loadAd();

            // Google AdMob
            String googleAdMobAppID = getString(R.string.google_AdMobAppID);
            String googleAdMobInterstitialID = "ca-app-pub-8354869049759576/6595392508";
            MobileAds.initialize(AppContext, googleAdMobAppID);
            googleInterstitialAd = new GoogleAdMobInterstitial(AppContext, googleAdMobInterstitialID);
            googleInterstitialAd.loadAd(); // load first ad
            googleAdMobBannerID = "ca-app-pub-8354869049759576/7169443235";

            InterstitialAd = new ShowingInterstitialAdsUtil(facebookAds, googleInterstitialAd);
        } else {
            // null stands for this is professional version (needs to be paid for it)
            facebookAds = null;
            googleInterstitialAd = null;
            InterstitialAd = null;
            googleAdMobBannerID = "";
        }
    }
}
