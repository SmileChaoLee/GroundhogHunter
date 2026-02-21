package com.smile.groundhoghunter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.smile.groundhoghunter.constants.CommonConstants;

public class HostGameActivity extends GroundhogActivity {

    private final static String TAG = "HostGameAct";
    private HostGameHandler hostGameHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() is called.");
        super.onCreate(savedInstanceState);
        hostGameHandler = new HostGameHandler(Looper.getMainLooper(), this);
        if (selectedIoFunctionThread != null) {
            selectedIoFunctionThread.setHandler(hostGameHandler);
        } else {
            // selectedIoFunctionThread is null then return to previous
            finish();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy() is called.");
        super.onDestroy();
        if (hostGameHandler != null) {
            hostGameHandler.removeCallbacksAndMessages(null);
            hostGameHandler = null;
        }
    }

    @Override
    protected void startGame() {
        super.startGame();
        selectedIoFunctionThread.write(CommonConstants.TwoPlayerStartGameButton, "");
    }

    @Override
    protected void pauseGame() {
        super.pauseGame();
        selectedIoFunctionThread.write(CommonConstants.TwoPlayerPauseGameButton, "");
    }

    @Override
    protected void resumeGame() {
        super.resumeGame();
        selectedIoFunctionThread.write(CommonConstants.TwoPlayerResumeGameButton, "");
    }

    @Override
    protected void newGame() {
        super.newGame();
        selectedIoFunctionThread.write(CommonConstants.TwoPlayerNewGameButton, "");
    }

    @Override
    protected void quitGame() {
        super.quitGame();
        selectedIoFunctionThread.write(CommonConstants.TwoPlayerOppositeLeftGame, "");
    }

    protected class HostGameHandler extends Handler {

        protected final Looper mLooper;
        protected final Context mContext;

        public HostGameHandler(Looper looper, Context context) {
            super(looper);
            mLooper = looper;
            mContext = context;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            // super.handleMessage(msg);
            String msgString;
            Bundle data = msg.getData();
            Log.d(TAG, "Message received: " + msg.what);
            switch (msg.what) {
                case CommonConstants.TwoPlayerOppositeLeftGame:
                    // received by host and client sides
                    msgString = mContext.getString(R.string.oppositePlayerLeftGameString);
                    Toast.makeText(mContext, msgString, Toast.LENGTH_SHORT).show();
                    gameView.setOppositePlayerLeft(true);
                    selectedIoFunctionThread.setStartRead(true);    // start reading data
                    break;
                case CommonConstants.TwoPlayerPauseGameButton:
                    // received by host and client sides
                    HostGameActivity.super.pauseGame();
                    selectedIoFunctionThread.setStartRead(true);    // start reading data
                    break;
                case CommonConstants.TwoPlayerResumeGameButton:
                    // received by host and client sides
                    HostGameActivity.super.resumeGame();
                    selectedIoFunctionThread.setStartRead(true);    // start reading data
                    break;
                case CommonConstants.TwoPlayerGameGroundhogHit:
                    msgString = data.getString("GroundhogHitData");
                    gameView.setGroundhogByMsgString(msgString);
                    selectedIoFunctionThread.setStartRead(true);
                    break;
                case CommonConstants.TwoPlayerGameScoreReceived:
                    msgString = data.getString("OppositeCurrentScore", "0");
                    int oppScore = Integer.parseInt(msgString.substring(0, 4));
                    gameView.setOppositeCurrentScore(oppScore);
                    int oppNumOfHits = Integer.parseInt(msgString.substring(4, 8));
                    gameView.setOppositeNumOfHits(oppNumOfHits);
                    gameView.setReceivedScoreFromOpposite(true);
                    selectedIoFunctionThread.setStartRead(true);    // start reading data
                    break;
                case CommonConstants.TwoPlayerDefaultReading:
                    // wrong or read error
                    selectedIoFunctionThread.setStartRead(true);    // start reading data
                    break;
            }
        }
    }
}
