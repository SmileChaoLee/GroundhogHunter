package com.smile.groundhoghunter;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

public class GroundhogHunterApp extends Application {

    public static Resources AppResources;
    public static Context AppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        AppResources = getResources();
        AppContext = getApplicationContext();
    }
}
