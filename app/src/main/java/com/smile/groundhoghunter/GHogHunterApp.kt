package com.smile.groundhoghunter

import android.content.res.Resources
import android.util.Log
import androidx.multidex.MultiDexApplication
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.smile.groundhoghunter.abstract_threads.IoFunctionThread
import java.util.UUID

class GHogHunterApp : MultiDexApplication() {

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
