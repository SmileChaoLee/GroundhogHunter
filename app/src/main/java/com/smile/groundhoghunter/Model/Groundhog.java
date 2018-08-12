package com.smile.groundhoghunter.Model;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

import com.smile.groundhoghunter.GameView;
import com.smile.groundhoghunter.Utilities.MathUtil;

public class Groundhog {

    private final Paint eraserPaint = new Paint();
    private RectF drawArea;
    private RectF scoreArea;

    // status = 0 --> jump to first stage
    // status = 1 --> jump to second stage
    // status = 2 --> jump to third stage
    // status = 3 --> jump to fourth stage
    private int status;
    private int numOfTimeIntervalShown;
    private boolean isHiding;
    private boolean isHit;

    /*
    constructor
     */
    public Groundhog(RectF rectF) {
        drawArea = new RectF(rectF);
        scoreArea = MathUtil.shrinkRectF(drawArea, 60);
        status = 0;
        numOfTimeIntervalShown = 0;
        isHiding = true;
        isHit = false;

        // eraserPaint.setAlpha(0);
        // eraserPaint.setStrokeJoin(Paint.Join.ROUND);
        // eraserPaint.setStrokeCap(Paint.Cap.ROUND);
        eraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        // eraserPaint.setAntiAlias(true);
    }

    // public methods

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getNumOfTimeIntervalShown() {
        return numOfTimeIntervalShown;
    }

    public void setNumOfTimeIntervalShown(int numTimeInterval) {
        if (numTimeInterval < GameView.NumTimeIntervalShown[status]) {
            numOfTimeIntervalShown = numTimeInterval;
        } else {
            numOfTimeIntervalShown = 0;
            setIsHiding(true);      // groundhog becomes hiding
            setIsHit(false);        // disable hit
        }
    }

    public boolean getIsHiding() {
        return isHiding;
    }

    public void setIsHiding(boolean isHiding) {
        this.isHiding = isHiding;
    }

    public boolean getIsHit() {
        return isHit;
    }

    public void setIsHit(boolean isHit) {
        this.isHit = isHit;
    }

    public void draw(Canvas canvas) {

        if (!isHiding) {
            // show groundhog
            if (isHit) {
                // groundhog is hit
                canvas.drawBitmap(GameView.GroundhogHitBitmaps[status], null, drawArea, null);
                canvas.drawBitmap(GameView.scoreBitmaps[status], null, scoreArea, null);
            } else {
                // not hit
                canvas.drawBitmap(GameView.GroundhogBitmaps[status], null, drawArea, null);
            }
        } else {
            // hiding
            canvas.drawBitmap(GameView.GroundhogBitmaps[0], null, drawArea, eraserPaint);
        }
    }
}
