package com.smile.groundhoghunter.Utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;

public class FontAndBitmapUtil {

    public static Bitmap getBitmapFromBitmapWithText(Bitmap orgBitmap, String caption, int textColor) {

        if (orgBitmap == null) {
            return null;
        }

        Bitmap bm = Bitmap.createBitmap(orgBitmap);
        android.graphics.Bitmap.Config bitmapConfig = bm.getConfig();
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        bm = bm.copy(bitmapConfig, true);   // convert to mutable
        Canvas canvas = new Canvas(bm);

        // draw start button
        TextPaint paint = new TextPaint();
        paint.setColor(textColor);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);

        Rect bounds = new Rect();
        float fontSize = 3.0f;
        paint.setTextSize(fontSize);    // fontSize can be any number except 0
        paint.getTextBounds(caption,0, caption.length(), bounds);
        float fontSize1 = fontSize * canvas.getWidth() / (float)bounds.width();
        float fontSize2 = fontSize * canvas.getHeight() / (float)bounds.height();
        fontSize = Math.min(fontSize1, fontSize2);

        paint.setTextSize(fontSize);

        // for align.CENTER
        Paint.FontMetrics fm = new Paint.FontMetrics();
        paint.getFontMetrics(fm);
        float textWidth = canvas.getWidth();
        float textHeight = canvas.getHeight() - (fm.ascent + fm.descent);
        canvas.drawText(caption, textWidth/2, textHeight/2, paint);

        return bm;

    }

    public static Bitmap getBitmapFromResourceWithText(Context context, int resourceId, String caption, int textColor) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

        return getBitmapFromBitmapWithText(bm, caption, textColor);
    }
}
