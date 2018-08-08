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
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import com.smile.groundhoghunter.Model.Groundhog;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private final String TAG = new String("com.smile.groundhoghunter.GameView");
    private SurfaceHolder surfaceHolder;
    private int gameViewWidth;
    private int gameViewHeight;

    private GameViewDrawThread gameViewDrawThread;
    private GroundhogRandomThread groundhogRandomThread;

    // default properties (package modifier)
    MainActivity mainActivity;
    Handler gameViewHandler;  // for synchronizing
    boolean gameViewPause;    // for synchronizing
    int rowNum;
    int colNum;
    List<Groundhog> groundhogList;

    // public properties
    public static Bitmap[] groundhogBitmaps = new Bitmap[4];
    public static Bitmap groundhog_hit;

    public GameView(Context context) {
        super(context);

        Log.d(TAG, "GameView created.");

        groundhogBitmaps[0] = BitmapFactory.decodeResource(getResources(), R.drawable.groundhog_0);
        groundhogBitmaps[1] = BitmapFactory.decodeResource(getResources(), R.drawable.groundhog_1);
        groundhogBitmaps[2] = BitmapFactory.decodeResource(getResources(), R.drawable.groundhog_2);
        groundhogBitmaps[3] = BitmapFactory.decodeResource(getResources(), R.drawable.groundhog_3);
        groundhog_hit = BitmapFactory.decodeResource(getResources(), R.drawable.groundhog_hit);

        mainActivity = (MainActivity)context;
        rowNum = mainActivity.getRowNum();
        colNum = mainActivity.getColNum();

        groundhogList = new ArrayList<>();

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
        float widthInc = gameViewWidth / (float)rowNum;
        float heightInc = gameViewHeight / (float)colNum;
        float bottomY;
        Groundhog groundhog;

        groundhogList.clear();
        RectF temp = new RectF();
        for (int i=0; i<rowNum; ++i) {
            x = 0;
            bottomY = y + heightInc;
            for (int j=0; j<colNum; ++j) {
                temp.left = x;
                x += widthInc;
                temp.right = x;
                temp.top = y;
                temp.bottom = bottomY;
                groundhog = new Groundhog(shrinkRectF(temp,20.0f));
                groundhogList.add(groundhog);
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
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.i(TAG, "surfaceDestroyed() is called");
    }

    public void drawGameScreen() {

        System.out.println("drawGameScreen is called.");
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
                Log.i(TAG, "surfaceHolder.unlockCanvasAndPost(canvas) is called.");
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
                    System.out.println("groundhogRandomThread.Join()........\n");
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
                    System.out.println("gameViewDrawThread.Join()........\n");
                    retry = false;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }// continue processing until the thread ends
            }
        }

    }

    // private methods
    private void doDraw(Canvas canvas) {
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        for (Groundhog groundhog : groundhogList) {
            groundhog.draw(canvas);
        }
    }

    private RectF shrinkRectF(RectF rectF, float percentage) {

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
