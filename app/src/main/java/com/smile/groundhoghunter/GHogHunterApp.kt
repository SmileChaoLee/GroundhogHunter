package com.smile.groundhoghunter

import android.app.Application
import android.content.res.Resources
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.smile.groundhoghunter.abstract_threads.IoFunctionThread

class GHogHunterApp : Application() {

    companion object {
        private const val TAG = "GHogHunterApp"
        @JvmField
        var selectedIoFuncThread: IoFunctionThread? = null
        lateinit var appResources: Resources
    }

    override fun onCreate() {
        super.onCreate()
        appResources = resources
        // Google AdMob
        // val googleAdMobAppID = getString(R.string.google_AdMobAppID)
        MobileAds.initialize(
            applicationContext
        ) { initializationStatus: InitializationStatus? ->
            Log.d(TAG, "Google AdMob was initialized successfully.")
        }
    }
}
