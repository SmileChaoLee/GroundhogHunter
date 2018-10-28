package com.smile.groundhoghunter;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.smile.smilepublicclasseslibrary.facebookadsutil.*;
import com.smile.smilepublicclasseslibrary.scoresqlite.*;

public class GroundhogHunterApp extends Application {

    // public final String REST_Website = new String("http://192.168.0.11:5000/Playerscore");
    public static final String REST_Website = "http://ec2-13-59-195-3.us-east-2.compute.amazonaws.com/Playerscore";
    public static final int GameId = 2; // this GameId is for backend game_id in playerscore table

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
