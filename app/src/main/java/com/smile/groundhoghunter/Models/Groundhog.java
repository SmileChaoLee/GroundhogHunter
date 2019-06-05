package com.smile.groundhoghunter.Models;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;

import com.smile.groundhoghunter.GameView;
import com.smile.smilelibraries.utilities.FontAndBitmapUtil;
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
    private int hitStatus;
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
        // double the area of showing score by reduce the shrinking ratio
        shrinkRatio = 100.0f * ( 1.0f - (wholeGroundhogArea.top - rectF.top) / rectF.height()) * 0.7f;
        // shrinkRatio = 100.0f * ( 1.0f - (wholeGroundhogArea.top - rectF.top) / rectF.height());
        scoreArea = MathUtil.shrinkRectF(rectF, shrinkRatio);
        shift = scoreArea.top - rectF.top;
        scoreArea.top = rectF.top;
        scoreArea.bottom = scoreArea.bottom - shift;

        status = 0;
        setIsHiding(true);
    }

    // public methods

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
        halfOfAnimationTimes = GameView.NumTimeIntervalShown[status] / 2;
    }

    public int getNumOfTimeIntervalShown() {
        return numOfTimeIntervalShown;
    }

    public void setNumOfTimeIntervalShown(int numTimeInterval) {
        if (numTimeInterval < GameView.NumTimeIntervalShown[status]) {
            if (hitStatus > 0) {
                // when it is hit, then it start hiding
                --numOfAnimationsShown;
            } else {
                numOfTimeIntervalShown = numTimeInterval;
                if (numOfTimeIntervalShown > halfOfAnimationTimes) {
                    --numOfAnimationsShown;
                } else if (numOfTimeIntervalShown < halfOfAnimationTimes) {
                    numOfAnimationsShown = numOfTimeIntervalShown + 1;
                } else {
                    // when numOfTimeIntervalShown = halfOfAnimationTimes
                    // then numOfAnimationsShown no changes
                }
            }
            // calculate the coordinate
            float diff = (wholeGroundhogArea.bottom - wholeGroundhogArea.top) / halfOfAnimationTimes * numOfAnimationsShown;
            drawArea = new RectF(wholeGroundhogArea);
            drawArea.top = wholeGroundhogArea.bottom - diff;
            if (numOfAnimationsShown <= 0 ) {
                setIsHiding(true);
            }
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
            setHitStatus(0);        // disable hit
            numOfTimeIntervalShown = 0;
            numOfAnimationsShown = 0;
        }
    }

    public int getHitStatus() {
        return hitStatus;
    }

    public void setHitStatus(int hitStatus) {
        this.hitStatus = hitStatus;
    }

    public RectF getDrawArea() {
        return drawArea;
    }

    public void draw(Canvas canvas) {

        if (!isHiding) {
            if (hitStatus > 0) {
                // groundhog is hit
                canvas.drawBitmap(GameView.GroundhogHitBitmaps[status], null, drawArea, null);
                // the following is displaying score image
                Bitmap tempBm = FontAndBitmapUtil.getBitmapFromBitmapWithText(GameView.score_board[hitStatus-1], String.valueOf(GameView.hitScores[status]), Color.BLACK);
                canvas.drawBitmap(tempBm, null, scoreArea, null);
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
