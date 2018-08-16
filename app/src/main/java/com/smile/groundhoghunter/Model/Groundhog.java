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

    // private final Paint eraserPaint = new Paint();
    private RectF drawArea;
    private RectF scoreArea;
    private RectF wholeGroundhogArea;

    // status = 0 --> jump to first stage
    // status = 1 --> jump to second stage
    // status = 2 --> jump to third stage
    // status = 3 --> jump to fourth stage
    private int status;
    private int numOfTimeIntervalShown;
    private boolean isHiding;
    private boolean isHit;
    private int numOfAnimationsShown;
    private int halfOfAnimationTimes;

    /*
    constructor
     */
    public Groundhog(RectF rectF) {
        float shrinkRatio = 36.0f;
        wholeGroundhogArea = MathUtil.shrinkRectF(rectF, shrinkRatio);
        float shift = rectF.bottom - wholeGroundhogArea.bottom - rectF.height() * 0.05f;    // up 5% of original
        wholeGroundhogArea.top = wholeGroundhogArea.top + shift;
        wholeGroundhogArea.bottom = wholeGroundhogArea.bottom + shift;
        drawArea = new RectF(wholeGroundhogArea);

        // score position
        shrinkRatio = 100.0f * ( 1.0f - (wholeGroundhogArea.top - rectF.top) / rectF.height());
        scoreArea = MathUtil.shrinkRectF(rectF, shrinkRatio);
        shift = scoreArea.top - rectF.top;
        scoreArea.top = rectF.top;
        scoreArea.bottom = scoreArea.bottom - shift;

        status = 0;
        numOfTimeIntervalShown = 0;      // hiding status
        isHiding = true;
        isHit = false;
        numOfAnimationsShown = 1;

        // eraserPaint.setAlpha(0);
        // eraserPaint.setStrokeJoin(Paint.Join.ROUND);
        // eraserPaint.setStrokeCap(Paint.Cap.ROUND);
        // eraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        // eraserPaint.setAntiAlias(true);
    }

    // public methods

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
        // halfOfAnimationTimes = ((float)GameView.TimeIntervalShown / (float)GameView.DrawingInterval)
        //                         * (float)GameView.NumTimeIntervalShown[status] / 2.0f;

        halfOfAnimationTimes = GameView.NumTimeIntervalShown[status] / 2;
    }

    public int getNumOfTimeIntervalShown() {
        return numOfTimeIntervalShown;
    }

    public void setNumOfTimeIntervalShown(int numTimeInterval) {
        if (numTimeInterval < GameView.NumTimeIntervalShown[status]) {
            numOfTimeIntervalShown = numTimeInterval;
            // if ( (numOfTimeIntervalShown >= halfOfAnimationTimes) && (addOne > 0) ){
            if (numOfTimeIntervalShown >= halfOfAnimationTimes){
                --numOfAnimationsShown;
            } else {
                numOfAnimationsShown = numOfTimeIntervalShown + 1;
            }
            // calculate the coordinate
            float diff = (wholeGroundhogArea.bottom - wholeGroundhogArea.top) / halfOfAnimationTimes * numOfAnimationsShown;
            drawArea = new RectF(wholeGroundhogArea);
            drawArea.top = wholeGroundhogArea.bottom - diff;
        } else {
            setIsHiding(true);      // groundhog becomes hiding
        }
    }

    public boolean getIsHiding() {
        return isHiding;
    }

    public void setIsHiding(boolean isHiding) {
        this.isHiding = isHiding;
        if (this.isHiding) {
            setIsHit(false);        // disable hit
            numOfTimeIntervalShown = 0;
            numOfAnimationsShown = 0;
        }
    }

    public boolean getIsHit() {
        return isHit;
    }

    public void setIsHit(boolean isHit) {
        this.isHit = isHit;
    }

    public RectF getDrawArea() {
        return drawArea;
    }

    public void draw(Canvas canvas) {

        if (!isHiding) {
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
            // canvas.drawBitmap(GameView.GroundhogBitmaps[0], null, scoreArea, eraserPaint);
        }
    }
}
