package com.smile.groundhoghunter.Utilities;

import android.graphics.RectF;

public class MathUtil {
    public static RectF shrinkRectF(RectF rectF, float percentage) {

        RectF rect = new RectF();

        float halfPercent = percentage / 100.0f / 2.0f;
        float halfPercentWidth = rectF.width() * halfPercent;
        float halfPercentHeight = rectF.height() * halfPercent;

        rect.left = rectF.left + halfPercentWidth;
        rect.top = rectF.top + halfPercentHeight;
        rect.right = rectF.right - halfPercentWidth;
        rect.bottom = rectF.bottom - halfPercentHeight;

        return rect;
    }
}
