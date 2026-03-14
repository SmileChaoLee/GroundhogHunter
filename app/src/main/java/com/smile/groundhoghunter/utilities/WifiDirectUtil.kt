package com.smile.groundhoghunter.utilities;

import android.content.Context;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.smile.groundhoghunter.threads.wifi.WifiFunctionThread;

public class WifiDirectUtil {

    private static final String TAG = "WifiDirectUtil";
    private static final Object lock = new Object();

    public static boolean isWifiDirectSupported(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            FeatureInfo[] features = pm.getSystemAvailableFeatures();
            for (FeatureInfo info : features) {
                if (info != null && info.name != null && info.name.equalsIgnoreCase("android.hardware.wifi.direct")) {
                    return true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static void stopWifiFunctionThread(WifiFunctionThread wifiFunctionThread) {
        if (wifiFunctionThread == null) {
            Log.d(TAG, "stopWifiFunctionThread.wifiFunctionThread is null");
            return;
        }
        synchronized (lock) {
            wifiFunctionThread.setKeepRunning(false);
            wifiFunctionThread.closeIoSocket();
            wifiFunctionThread.setStartRead(true);
            lock.notify();
        }
        boolean retry = true;
        while (retry) {
            try {
                wifiFunctionThread.join();
                Log.d(TAG, "stopWifiFunctionThread.join()");
                retry = false;
            } catch (InterruptedException ex) {
                Log.e(TAG, "stopWifiFunctionThread.Exception: ", ex);
            }
        }
    }
}
