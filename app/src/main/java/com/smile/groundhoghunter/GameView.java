package com.smile.groundhoghunter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import com.smile.groundhoghunter.Model.Groundhog;
import com.smile.groundhoghunter.Utilities.MathUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = new String("com.smile.groundhoghunter.GameView");
    private final SurfaceHolder surfaceHolder;

    private int gameViewWidth;
    private int gameViewHeight;
    private float rectWidthForOneGroundhog;
    private float rectHeightForOneGroundhog;
    private GameViewDrawThread gameViewDrawThread;
    private GroundhogRandomThread groundhogRandomThread;

    // default properties (package modifier)
    final MainActivity mainActivity;
    final Handler gameViewHandler;  // for synchronizing
    final int rowNum = 5;
    final int colNum = 5;
    boolean gameViewPause;    // for synchronizing
    Groundhog[] groundhogArray;

    // public properties
    public static final int NumberOfGroundhogTypes;
    public static final int TimeIntervalShown;
    public static final int[] NumTimeIntervalShown;
    public static final Bitmap[] GroundhogBitmaps;
    public static final Bitmap[] GroundhogHitBitmaps;
    public static final int[] hitScores;
    public static final Bitmap[] scoreBitmaps;

    static {
        NumberOfGroundhogTypes = 4;     // including hiding
        TimeIntervalShown = 500;        // 500 mini seconds
        NumTimeIntervalShown = new int[NumberOfGroundhogTypes];
        NumTimeIntervalShown[0] = 2;
        NumTimeIntervalShown[1] = 3;
        NumTimeIntervalShown[2] = 4;
        NumTimeIntervalShown[3] = 5;
        GroundhogBitmaps = new Bitmap[NumberOfGroundhogTypes];
        GroundhogBitmaps[0] = BitmapFactory.decodeResource(GroundhogHunterApp.AppResources, R.drawable.groundhog_0);
        GroundhogBitmaps[1] = BitmapFactory.decodeResource(GroundhogHunterApp.AppResources, R.drawable.groundhog_2);
        GroundhogBitmaps[2] = BitmapFactory.decodeResource(GroundhogHunterApp.AppResources, R.drawable.groundhog_1);
        GroundhogBitmaps[3] = BitmapFactory.decodeResource(GroundhogHunterApp.AppResources, R.drawable.groundhog_3);
        GroundhogHitBitmaps = new Bitmap[NumberOfGroundhogTypes];
        GroundhogHitBitmaps[0] = BitmapFactory.decodeResource(GroundhogHunterApp.AppResources, R.drawable.groundhog_0);
        GroundhogHitBitmaps[1] = BitmapFactory.decodeResource(GroundhogHunterApp.AppResources, R.drawable.groundhog_2);
        GroundhogHitBitmaps[2] = BitmapFactory.decodeResource(GroundhogHunterApp.AppResources, R.drawable.groundhog_1);
        GroundhogHitBitmaps[3] = BitmapFactory.decodeResource(GroundhogHunterApp.AppResources, R.drawable.groundhog_3);
        hitScores = new int[NumberOfGroundhogTypes];
        hitScores[0] = 40;
        hitScores[1] = 30;
        hitScores[2] = 20;
        hitScores[3] = 10;
        scoreBitmaps = new Bitmap[NumberOfGroundhogTypes];
        // convert numbers to Bitmaps
        scoreBitmaps[0] = BitmapFactory.decodeResource(GroundhogHunterApp.AppResources, R.drawable.s40);
        scoreBitmaps[1] = BitmapFactory.decodeResource(GroundhogHunterApp.AppResources, R.drawable.s30);
        scoreBitmaps[2] = BitmapFactory.decodeResource(GroundhogHunterApp.AppResources, R.drawable.s20);
        scoreBitmaps[3] = BitmapFactory.decodeResource(GroundhogHunterApp.AppResources, R.drawable.s10);
    }

    public GameView(Context context) {
        super(context);

        Log.d(TAG, "GameView created.");

        mainActivity = (MainActivity)context;

        groundhogArray = new Groundhog[rowNum * colNum];

        gameViewHandler = new Handler(Looper.getMainLooper());  // for synchronizing
        gameViewPause = false;   // for synchronizing

        setWillNotDraw(true);   // added on 2017-11-07 for just in case, the default is true

        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this); // register the interface

        setZOrderOnTop(true);
        // surfaceHolder.setFormat(PixelFormat.TRANSPARENT);    // same effect as the following
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i(TAG, "onDraw() is called");
        // doDraw(canvas);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.i(TAG, "surfaceCreated() is called");
        gameViewWidth = getWidth();
        gameViewHeight = getHeight();

        float x;
        float y = 0;
        rectWidthForOneGroundhog = gameViewWidth / (float)rowNum;
        rectHeightForOneGroundhog = gameViewHeight / (float)colNum;
        float bottomY;
        int index;
        Groundhog groundhog;

        RectF temp = new RectF();
        for (int i=0; i<rowNum; ++i) {
            x = 0;
            bottomY = y + rectHeightForOneGroundhog;
            for (int j=0; j<colNum; ++j) {
                index = rowNum * i + j;
                temp.left = x;
                x += rectWidthForOneGroundhog;
                temp.right = x;
                temp.top = y;
                temp.bottom = bottomY;
                groundhog = new Groundhog(temp);
                groundhogArray[index] = groundhog;
            }
            y = bottomY;
        }

        gameViewDrawThread = new GameViewDrawThread(this);
        gameViewDrawThread.start();

        groundhogRandomThread = new GroundhogRandomThread(this);
        groundhogRandomThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.i(TAG, "surfaceChanged() is called");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        int action = event.getAction();
        Groundhog groundhog;

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_BUTTON_PRESS:
            case MotionEvent.ACTION_DOWN:
                int i = (int)(y / rectHeightForOneGroundhog);   // row
                int j = (int)(x / rectWidthForOneGroundhog);    // col
                int index = rowNum * i + j;
                groundhog = groundhogArray[index];
                if (!groundhog.getIsHiding()) {
                    // showing but not hiding
                    if (groundhog.getDrawArea().contains(x,y)) {
                        // hit
                        groundhog.setIsHit(true);
                    }
                }
                break;
            default:
                break;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.i(TAG, "surfaceDestroyed() is called");
    }

    public void drawGameScreen() {

        Canvas canvas = null;
        try {
            canvas = surfaceHolder.lockCanvas(null);
            synchronized (surfaceHolder) {
                doDraw(canvas);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    public void releaseSynchronizations() {
        if (mainActivity.gamePause) {
            // in pause status
            synchronized (mainActivity.activityHandler) {
                mainActivity.gamePause = false;
                mainActivity.activityHandler.notifyAll();
            }
        }

        if (gameViewPause) {
            // GameView in pause status
            synchronized (gameViewHandler) {
                gameViewPause = false;
                gameViewHandler.notifyAll();
            }
        }
    }

    public void stopThreads() {

        boolean retry = true;
        if (groundhogRandomThread != null) {
            groundhogRandomThread.setKeepRunning(false);
            retry = true;
            while (retry) {
                try {
                    groundhogRandomThread.join();
                    Log.d(TAG, "groundhogRandomThread.Join()........\n");
                    retry = false;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }// continue processing until the thread ends
            }
        }

        if (gameViewDrawThread != null) {
            gameViewDrawThread.setKeepRunning(false);
            retry = true;
            while (retry) {
                try {
                    gameViewDrawThread.join();
                    Log.d(TAG, "gameViewDrawThread.Join()........\n");
                    retry = false;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }// continue processing until the thread ends
            }
        }
    }

    public void releaseResources() {
        Log.d(TAG, "releaseResources() is called.\n");
    }

    // private methods
    private void doDraw(Canvas canvas) {
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        for (Groundhog groundhog : groundhogArray) {
            groundhog.draw(canvas);
        }
    }
}
