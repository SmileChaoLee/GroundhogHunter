package com.smile.groundhoghunter;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.smile.smilepublicclasseslibrary.facebookadsutil.*;
import com.smile.smilepublicclasseslibrary.scoresqlite.*;

public class GroundhogHunterApp extends Application {

    public static Resources AppResources;
    public static Context AppContext;
    public static ScoreSQLite ScoreSQLiteDB;
    public static FacebookInterstitialAds FacebookAds;

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
            FacebookAds = new FacebookInterstitialAds(AppContext, facebookPlacementID);
        } else {
            // null stands for this is professional version (needs to be paid for it)
            FacebookAds = null;
        }
    }
}
