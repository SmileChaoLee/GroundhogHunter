package com.smile.groundhoghunter.Utilities;

import android.content.Context;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;

public class WifiDirectUtil {
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
}
