package com.smile.groundhoghunter.Utilities;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.Display;

/**
 * Created by chaolee on 2017-10-24.
 */

public class ScreenUtil {

    public static int getStatusBarHeight(Context context) {
        int height = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            height = context.getResources().getDimensionPixelSize(resourceId);
        }
        return height;
    }

    public static int getNavigationBarHeight(Context context) {
        int height = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            height = context.getResources().getDimensionPixelSize(resourceId);
        }
        return height;
    }

    public static int getActionBarHeight(Context context) {
        int height = 0;
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize,tv, true))
        {
            height = TypedValue.complexToDimensionPixelSize(tv.data,context.getResources().getDisplayMetrics());
        }
        return height;
    }

    public static void getScreenSize(Context context, Point size) {
        // Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Display display =  ((Activity)context).getWindowManager().getDefaultDisplay();
        display.getSize(size);
    }

    public static int androidDeviceType(Context context) {

        int type = 0;   // default is cell phone

        Point size = new Point();
        ScreenUtil.getScreenSize(context, size);
        int screenWidth = size.x;
        int screenHeight = size.y;
        float baseWidth = 1080.0f;      // portrait
        float baseHeight = 1776.0f;
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Landscape
            if (screenWidth >= 2000) {
                // assume Tablet
                type = 1;
            }
        } else {
            // portrait
            if (screenWidth >= 1300) {
                // assume Tablet
                type = 1;
            }
        }

        return type;
    }
}
