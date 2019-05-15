package com.smile.groundhoghunter;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.smile.groundhoghunter.AbstractClasses.IoFunctionThread;
import com.smile.groundhoghunter.Constants.CommonConstants;
import com.smile.groundhoghunter.Models.Groundhog;
import com.smile.groundhoghunter.Threads.GameTimerThread;
import com.smile.groundhoghunter.Threads.GameViewDrawThread;
import com.smile.groundhoghunter.Threads.GroundhogRandomThread;
import com.smile.smilepublicclasseslibrary.SoundPoolUtil;
import com.smile.smilepublicclasseslibrary.player_record_rest.PlayerRecordRest;
import com.smile.smilepublicclasseslibrary.utilities.ScreenUtil;

import org.json.JSONObject;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "GameView";
    private static final int singlePlayerHitStatus = 1;
    private static final int hostPlayerHitStatus = 1;
    private static final int clientPlayerHitStatus = 2;
    private final int gameType;

    private float textFontSize;
    private float fontScale;
    private final SurfaceHolder surfaceHolder;
    private final GroundhogActivity groundhogActivity;
    private final int rowNum;
    private final int colNum;
    private final int gameViewWidth;
    private final int gameViewHeight;

    private float rectWidthForOneGroundhog;
    private float rectHeightForOneGroundhog;
    private int highestScore;
    private int currentScore;
    private int numOfHits;
    private int oppositeCurrentScore;
    private int oppositeNumOfHits;
    private boolean isOppositePlayerLeft;
    private boolean isReceivedScoreFromOpposite;
    private int timeRemaining;
    private GameViewDrawThread gameViewDrawThread;
    private GroundhogRandomThread groundhogRandomThread;
    private GameTimerThread gameTimerThread;
    private boolean surfaceViewCreated;
    private int runningStatus;
    private boolean hasSound;

    private IoFunctionThread selectedIoFunctionThread;

    private SoundPoolUtil soundPoolUtil;

    // default properties (package modifier)
    public Groundhog[] groundhogArray;
    // public static properties
    public static boolean GameViewPause = false;    // for synchronizing

    // public static final properties
    public static final Handler GameViewHandler = new Handler(Looper.getMainLooper());  // for synchronizing
    public static final int BluetoothMediaType = 0;
    public static final int WifiMediaType = 1;
    public static final int InternetMediaType = 2;
    public static final int NoneMediaType = -1;
    public static final int TimerInterval = 60; // 60 seconds
    public static final int DrawingInterval;
    public static final int NumberOfGroundhogTypes;
    public static final int TimeIntervalShown;
    public static final int[] NumTimeIntervalShown;
    public static final Bitmap[] GroundhogBitmaps;
    public static final Bitmap[] GroundhogHitBitmaps;
    public static final int[] hitScores;
    public static final Bitmap[] score_board;

    static {
        DrawingInterval = 80;
        NumberOfGroundhogTypes = 4;     // including hiding
        TimeIntervalShown = 300;        // 300 milli seconds
        NumTimeIntervalShown = new int[NumberOfGroundhogTypes];
        NumTimeIntervalShown[0] = 4;    // has to be even (4 frames for animation, total time is 300 * 4 milliseconds)
        NumTimeIntervalShown[1] = 6;    // has to be even (6 frames for animation, total time is 300 * 6 milliseconds)
        NumTimeIntervalShown[2] = 8;    // has to be even (8 frames for animation, total time is 300 * 8 milliseconds)
        NumTimeIntervalShown[3] = 10;   // has to be even (10 frames for animation, total time is 300 * 10 milliseconds)
        GroundhogBitmaps = new Bitmap[NumberOfGroundhogTypes];
        GroundhogBitmaps[0] = BitmapFactory.decodeResource(GroundhogHunterApp.AppResources, R.drawable.groundhog_0);
        GroundhogBitmaps[1] = BitmapFactory.decodeResource(GroundhogHunterApp.AppResources, R.drawable.groundhog_1);
        GroundhogBitmaps[2] = BitmapFactory.decodeResource(GroundhogHunterApp.AppResources, R.drawable.groundhog_2);
        GroundhogBitmaps[3] = BitmapFactory.decodeResource(GroundhogHunterApp.AppResources, R.drawable.groundhog_3);
        GroundhogHitBitmaps = new Bitmap[NumberOfGroundhogTypes];
        GroundhogHitBitmaps[0] = BitmapFactory.decodeResource(GroundhogHunterApp.AppResources, R.drawable.groundhog_0);
        GroundhogHitBitmaps[1] = BitmapFactory.decodeResource(GroundhogHunterApp.AppResources, R.drawable.groundhog_1);
        GroundhogHitBitmaps[2] = BitmapFactory.decodeResource(GroundhogHunterApp.AppResources, R.drawable.groundhog_2);
        GroundhogHitBitmaps[3] = BitmapFactory.decodeResource(GroundhogHunterApp.AppResources, R.drawable.groundhog_3);
        hitScores = new int[NumberOfGroundhogTypes];
        hitScores[0] = 40;
        hitScores[1] = 30;
        hitScores[2] = 20;
        hitScores[3] = 10;
        score_board = new Bitmap[2];
        score_board[0] = BitmapFactory.decodeResource(GroundhogHunterApp.AppResources, R.drawable.red_score_board);
        score_board[1] = BitmapFactory.decodeResource(GroundhogHunterApp.AppResources, R.drawable.yellow_score_board);
    }

    public GameView(Context context, int gameType, int gWidth, int gHeight,IoFunctionThread ioFunctionThread) {
        super(context);

        Log.d(TAG, "GameView.GameView(Context context, int gWidth, int gHeight) is called.");

        selectedIoFunctionThread = ioFunctionThread;

        this.groundhogActivity = (GroundhogActivity)context;
        this.gameType = gameType;

        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(groundhogActivity, GroundhogHunterApp.FontSize_Scale_Type, null);
        textFontSize = ScreenUtil.suitableFontSize(groundhogActivity, defaultTextFontSize, GroundhogHunterApp.FontSize_Scale_Type, 0.0f);
        fontScale = ScreenUtil.suitableFontScale(groundhogActivity, GroundhogHunterApp.FontSize_Scale_Type, 0.0f);

        rowNum = groundhogActivity.getRowNum();
        colNum = groundhogActivity.getColNum();

        gameViewWidth = gWidth;
        gameViewHeight = gHeight;

        GameViewPause = false;   // for synchronizing

        setWillNotDraw(true);   // added on 2017-11-07 for just in case, the default is true

        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this); // register the interface

        setZOrderOnTop(true);
        // surfaceHolder.setFormat(PixelFormat.TRANSPARENT);    // same effect as the following
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);

        highestScore = groundhogActivity.getHighestScore();
        currentScore = 0;
        numOfHits = 0;
        isOppositePlayerLeft = false;
        isReceivedScoreFromOpposite = false;

        surfaceViewCreated = false; // surfaceView has not been created yet
        runningStatus = 0;  // game is not running
        timeRemaining = GameView.TimerInterval;

        hasSound = true;    // default is having sound

        // Creating groundhogs' object
        Log.d(TAG, "Creating groundhogArray....");
        groundhogArray = new Groundhog[rowNum * colNum];

        // start to initialize groundhogArray array
        createGroundhogs();

        // create sound pool
        soundPoolUtil = new SoundPoolUtil(context, R.raw.ouh);
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
        // gameViewWidth = getWidth();
        // gameViewHeight = getHeight();
        Log.i(TAG, "The width of GameView (SurfaceView) = " + getWidth());
        Log.i(TAG, "The height of GameView (SurfaceView) = " + getHeight());

        /*
        if (groundhogArray == null) {
            createGroundhogs();
        }
        */

        surfaceViewCreated = true;  // surfaceView has been created
        startDrawingScreen();       // draw screen
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.i(TAG, "surfaceChanged() is called");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // the if statement is added on 2018-08-31
        if ( (runningStatus == 1) && (!GameViewPause) ) {
            // game is running
            int x = (int) event.getX();
            int y = (int) event.getY();
            int action = event.getAction();
            Groundhog groundhog;

            switch (action & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_BUTTON_PRESS:
                case MotionEvent.ACTION_DOWN:
                    int i = (int) (y / rectHeightForOneGroundhog);   // row
                    int j = (int) (x / rectWidthForOneGroundhog);    // col
                    int index = colNum * i + j;
                    groundhog = groundhogArray[index];
                    if (!groundhog.getIsHiding()) {
                        // showing but not hiding
                        int hitStatus = groundhog.getHitStatus();
                        if (hitStatus == 0) {
                            // not hit
                            int newHitStatus;
                            if (groundhog.getDrawArea().contains(x, y)) {
                                // hit
                                if (hasSound) {
                                    // needs to play sound for hitting
                                    // SoundUtil.playSound(groundhogActivity, R.raw.ouh);
                                    soundPoolUtil.playSound();
                                }

                                if (gameType == CommonConstants.GameBySinglePlayer) {
                                    newHitStatus = singlePlayerHitStatus;
                                } else {
                                    // not single player
                                    if (gameType == CommonConstants.TwoPlayerGameByHost) {
                                        // host
                                        newHitStatus = hostPlayerHitStatus;
                                    } else {
                                        // client
                                        newHitStatus = clientPlayerHitStatus;
                                    }
                                    String writeString = "";
                                    writeString += String.format("%02d", index);
                                    int status = groundhog.getStatus();
                                    writeString += String.format("%01d", status);
                                    boolean isHiding = groundhog.getIsHiding();
                                    if (isHiding) {
                                        writeString += "1";
                                    } else {
                                        writeString += "0";
                                    }
                                    int numOfTimeIntervalShown = groundhog.getNumOfTimeIntervalShown();
                                    writeString += String.format("%02d", numOfTimeIntervalShown);
                                    writeString += newHitStatus; // hit status
                                    selectedIoFunctionThread.write(CommonConstants.TwoPlayerGameGroundhogHit, writeString);
                                }
                                groundhog.setHitStatus(newHitStatus);
                                ++numOfHits;
                                currentScore += hitScores[groundhog.getStatus()];
                                startDrawingScreen();   // added on 2018-10-29 for testing
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.i(TAG, "surfaceDestroyed() is called");
        soundPoolUtil.release();
    }

    public boolean getHasSound() {
        return hasSound;
    }
    public void setHasSound(boolean hasSound)
    {
        this.hasSound = hasSound;
    }

    public void startGame() {
        if ( (surfaceViewCreated) && (runningStatus == 0) ) {

            runningStatus = 1;  // game is set to be running

            gameTimerThread = new GameTimerThread(this);
            groundhogRandomThread = new GroundhogRandomThread(this);
            gameViewDrawThread = new GameViewDrawThread(this);

            groundhogRandomThread.start();
            gameViewDrawThread.start();
            gameTimerThread.start();
        }
    }

    public void pauseGame() {
        if ( (surfaceViewCreated) && (runningStatus == 1) && (!GameViewPause)) {
            // when game is running
            synchronized (GameViewHandler) {
                GameViewPause = true;
            }
        }
    }

    public void resumeGame() {
        if ( (surfaceViewCreated) && (runningStatus == 1) && (GameViewPause) ) {
            // when game is running
            synchronized (GameViewHandler) {
                GameViewPause = false;
                GameViewHandler.notifyAll();
            }
        }
    }

    public void newGame() {

        if (runningStatus != 0) {
            // game is running or game over
            currentScore = 0;
            numOfHits = 0;
            runningStatus = 0;  // game is not running

            releaseSynchronizations();
            stopThreads();

            timeRemaining = GameView.TimerInterval;

            if (groundhogArray != null) {
                for (Groundhog groundhog : groundhogArray) {
                    groundhog.setIsHiding(true);
                }

                startDrawingScreen();
            }
        }
    }

    public void drawGameScreen() {
        Canvas canvas = null;
        timeRemaining = gameTimerThread.getTimeRemaining();
        startDrawingScreen();
        if ( (timeRemaining <=0 ) && (runningStatus == 1) ) {
            // if game is running and timer is finished, then it is game over
            gameOver();
        }
    }

    public void releaseSynchronizations() {
        if (GroundhogActivity.GamePause) {
            // in pause status
            synchronized (GroundhogActivity.ActivityHandler) {
                GroundhogActivity.GamePause = false;
                GroundhogActivity.ActivityHandler.notifyAll();
            }
        }

        if (GameViewPause) {
            // GameView in pause status
            synchronized (GameViewHandler) {
                GameViewPause = false;
                GameViewHandler.notifyAll();
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

        if (gameTimerThread != null) {
            gameTimerThread.setKeepRunning(false);    // stop the gameTimerThread
            retry = true;
            while (retry) {
                try {
                    gameTimerThread.join();
                    Log.d(TAG, "gameTimerThread.Join()........\n");
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

    public int getRunningStatus() {
        return runningStatus;
    }

    public void setRunningStatus(int runningStatus) {
        this.runningStatus = runningStatus;
    }

    public int getGameType() {
        return gameType;
    }

    public IoFunctionThread getIoFunctionThread() {
        return selectedIoFunctionThread;
    }

    public GameTimerThread getGameTimerThread() {
        return gameTimerThread;
    }

    public void setGroundhogByMsgString(String msgString) {
        int numOfTimeIntervalShown;

        int index = Integer.valueOf(msgString.substring(0, 2));
        Groundhog groundhog = groundhogArray[index];

        int status = Integer.valueOf(msgString.substring(2, 3));
        groundhog.setStatus(status);

        int hideByte = Integer.valueOf(msgString.substring(3, 4));
        if (hideByte == 1) {
            groundhog.setIsHiding(true);
        } else {
            groundhog.setIsHiding(false);
        }

        numOfTimeIntervalShown = Integer.valueOf(msgString.substring(4, 6));
        groundhog.setNumOfTimeIntervalShown(numOfTimeIntervalShown);

        int hitByte = Integer.valueOf(msgString.substring(6, 7));
        groundhog.setHitStatus(hitByte);
    }

    public boolean getOppositePlayerLeft() {
        return this.isOppositePlayerLeft;
    }

    public void setOppositePlayerLeft(boolean isOppositePlayerLeft) {
        this.isOppositePlayerLeft = isOppositePlayerLeft;
    }

    public void setOppositeCurrentScore(int oppositeCurrentScore) {
        this.oppositeCurrentScore = oppositeCurrentScore;
    }

    public void setOppositeNumOfHits(int oppositeNumOfHits) {
        this.oppositeNumOfHits = oppositeNumOfHits;
    }

    public void setReceivedScoreFromOpposite(boolean isReceivedScoreFromOpposite) {
        this.isReceivedScoreFromOpposite = isReceivedScoreFromOpposite;
    }

    // private methods
    private void createGroundhogs() {
        float x;
        float y = 0;
        rectWidthForOneGroundhog = gameViewWidth / (float) colNum;
        rectHeightForOneGroundhog = gameViewHeight / (float) rowNum;
        float bottomY;
        int index;
        Groundhog groundhog;

        RectF temp = new RectF();
        for (int i = 0; i < rowNum; ++i) {
            x = 0;
            bottomY = y + rectHeightForOneGroundhog;
            for (int j = 0; j < colNum; ++j) {
                index = colNum * i + j;
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
    }

    private void startDrawingScreen() {
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

    private void doDraw(Canvas canvas) {

        groundhogActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groundhogActivity.setTextForHighScoreTextView(String.valueOf(highestScore));
                groundhogActivity.setTextForScoreTextView(String.valueOf(currentScore));
                groundhogActivity.setTextForTimerTextView(String.valueOf(timeRemaining));
                groundhogActivity.setTextForHitNumTextView(String.valueOf(numOfHits));
            }
        });

        if (canvas != null) {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            // Game View part
            for (Groundhog groundhog : groundhogArray) {
                groundhog.draw(canvas);
            }
        }
    }

    private void gameOver() {
        // game over
        // set threads to stop running loop
        // but do not use Thread.join() to stop stop thread
        gameViewDrawThread.setKeepRunning(false);
        groundhogRandomThread.setKeepRunning(false);
        gameTimerThread.setKeepRunning(false);

        runningStatus = 2;
        if (gameType == CommonConstants.GameBySinglePlayer) {
            // single player then record the score
            boolean isInTop10 = GroundhogHunterApp.ScoreSQLiteDB.isInTop10(currentScore);
            if (isInTop10) {
                // record the current score
                recordScore(currentScore);
            }
        } else {
            // display the competition result

            String scoreString = String.format("%04d", currentScore);
            scoreString += String.format("%04d", numOfHits);
            selectedIoFunctionThread.write(CommonConstants.TwoPlayerGameScoreReceived, scoreString);

            groundhogActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AsyncTask displayResultAsyncTask = new DisplayResultAsyncTask();
                    displayResultAsyncTask.execute();
                }
            });

        }
    }

    private void recordScore(final int score) {
        //    record currentScore as a score in database
        groundhogActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final EditText et = new EditText(groundhogActivity);
                ScreenUtil.resizeTextSize(et, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
                // et.setHeight(200);
                et.setTextColor(Color.BLUE);
                // et.setBackground(new ColorDrawable(Color.TRANSPARENT));
                // et.setBackgroundColor(Color.TRANSPARENT);
                et.setHint(getResources().getString(R.string.nameString));
                et.setGravity(Gravity.CENTER);
                AlertDialog alertD = new AlertDialog.Builder(groundhogActivity).create();
                alertD.setTitle(null);
                alertD.requestWindowFeature(Window.FEATURE_NO_TITLE);
                alertD.setCancelable(false);
                alertD.setView(et);
                alertD.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.cancelString), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertD.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.submitString), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        // use thread to add a record to database (remote database on AWS-EC2)
                        Thread restThread = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    String webUrl = new String(GroundhogHunterApp.REST_Website + "/AddOneRecordREST");   // ASP.NET Cor
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("PlayerName", et.getText().toString());
                                    jsonObject.put("Score", score);
                                    jsonObject.put("GameId", GroundhogHunterApp.GameId);
                                    PlayerRecordRest.addOneRecord(webUrl, jsonObject);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    Log.d(TAG, "Failed to add one record to Playerscore table.");
                                }
                            }
                        };
                        restThread.start();

                        GroundhogHunterApp.ScoreSQLiteDB.addScore(et.getText().toString(), score);
                        GroundhogHunterApp.ScoreSQLiteDB.deleteAllAfterTop10();  // only keep the top 10
                        if (currentScore > highestScore) {
                            highestScore = currentScore;
                            groundhogActivity.setHighestScore(highestScore);
                            groundhogActivity.setTextForHighScoreTextView(String.valueOf(highestScore));
                        }
                    }
                });
                alertD.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        setDialogStyle(dialog);
                    }
                });
                alertD.show();
            }
        });
    }

    private void setDialogStyle(DialogInterface dialog) {
        AlertDialog dlg = (AlertDialog)dialog;

        dlg.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dlg.getWindow().setDimAmount(0.0f); // no dim for background screen

        dlg.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
        dlg.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background_image);

        Button nBtn = dlg.getButton(DialogInterface.BUTTON_NEGATIVE);
        ScreenUtil.resizeTextSize(nBtn, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        nBtn.setTypeface(Typeface.DEFAULT_BOLD);
        nBtn.setTextColor(Color.RED);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)nBtn.getLayoutParams();
        layoutParams.weight = 10;
        nBtn.setLayoutParams(layoutParams);

        Button pBtn = dlg.getButton(DialogInterface.BUTTON_POSITIVE);
        ScreenUtil.resizeTextSize(pBtn, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        pBtn.setTypeface(Typeface.DEFAULT_BOLD);
        pBtn.setTextColor(Color.rgb(0x00,0x64,0x00));
        pBtn.setLayoutParams(layoutParams);
    }

    private class DisplayResultAsyncTask extends AsyncTask {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            groundhogActivity.disableAllButtons();
        }
        @Override
        protected Object doInBackground(Object[] objects) {

            if (isOppositePlayerLeft) {
                // opposite player has left game then show result
                oppositeCurrentScore = 0;
                oppositeNumOfHits = 0;
            } else {
                // waiting until received the scores from opposite player (only wait 6 seconds)
                int maxLoop = 30, i = 0;
                while ( (!isReceivedScoreFromOpposite) && (i<maxLoop) ) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    i++;
                }
                Log.d(TAG, "Number of loop (i) = " + i);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (gameType == CommonConstants.TwoPlayerGameByHost) {
                groundhogActivity.displayTwoPlayerResult(currentScore, numOfHits, oppositeCurrentScore, oppositeNumOfHits);
            } else {
                // TwoPlayerGameByClient
                groundhogActivity.displayTwoPlayerResult(oppositeCurrentScore, oppositeNumOfHits, currentScore, numOfHits);
            }
            groundhogActivity.disableAllButtons();
        }
    }
}
