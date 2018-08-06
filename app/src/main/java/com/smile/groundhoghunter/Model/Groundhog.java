package com.smile.groundhoghunter.Model;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

import com.smile.groundhoghunter.GameView;

public class Groundhog {

    private final Paint eraserPaint = new Paint();
    private RectF drawArea;

    // status = 0 --> no jump
    // status = 1 --> jump to first stage
    // status = 2 --> jump to second stage
    // status = 3 --> jump to third stage
    private int status;

    /*
    constructor
     */
    public Groundhog(RectF rectF) {
        drawArea = new RectF(rectF);
        status = 0;

        eraserPaint.setAlpha(0);
        eraserPaint.setStrokeJoin(Paint.Join.ROUND);
        eraserPaint.setStrokeCap(Paint.Cap.ROUND);
        eraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        eraserPaint.setAntiAlias(true);
    }

    // public methods


    public void setStatus(int status) {
        this.status = status;
    }

    public void draw(Canvas canvas) {
        if (status > 0) {
            // show groundhog
            canvas.drawBitmap(GameView.groundhogBitmaps[status-1], null, drawArea, null);
        } else {
            // do show (hidden)
            canvas.drawBitmap(GameView.groundhogBitmaps[0], null, drawArea, eraserPaint);
        }
    }
}
