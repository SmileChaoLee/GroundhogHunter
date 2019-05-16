package com.smile.groundhoghunter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.smile.groundhoghunter.Constants.CommonConstants;
import com.smile.smilepublicclasseslibrary.utilities.ScreenUtil;

public class HostGameActivity extends GroundhogActivity {

    private final static String TAG = "HostGameActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void startGame() {
        selectedIoFunctionThread.write(CommonConstants.TwoPlayerStartGameButton, "");
        super.startGame();
    }

    @Override
    protected void pauseGame() {
        selectedIoFunctionThread.write(CommonConstants.TwoPlayerPauseGameButton, "");
        super.pauseGame();
    }

    @Override
    protected void resumeGame() {
        selectedIoFunctionThread.write(CommonConstants.TwoPlayerResumeGameButton, "");
        super.resumeGame();
    }

    @Override
    protected void newGame() {
        selectedIoFunctionThread.write(CommonConstants.TwoPlayerNewGameButton, "");
        super.newGame();
    }

    @Override
    protected void quitGame() {
        selectedIoFunctionThread.write(CommonConstants.TwoPlayerOppositeLeftGame, "");
        super.quitGame();
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
        public void handleMessage(Message msg) {
            // super.handleMessage(msg);

            String msgString = "";
            Bundle data = msg.getData();

            Log.d(TAG, "Message received: " + msg.what);
            switch (msg.what) {
                case CommonConstants.TwoPlayerOppositeLeftGame:
                    // received by host and client sides
                    msgString = mContext.getString(R.string.oppositePlayerLeftGameString);
                    ScreenUtil.showToast(mContext, msgString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    gameView.setOppositePlayerLeft(true);
                    selectedIoFunctionThread.setStartRead(true);    // start reading data
                    break;
                case CommonConstants.TwoPlayerPauseGameButton:
                    // received by host and client sides
                    // ScreenUtil.showToast(mContext, "Opposite player pressed pause game button.", toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    HostGameActivity.super.pauseGame();
                    selectedIoFunctionThread.setStartRead(true);    // start reading data
                    break;
                case CommonConstants.TwoPlayerResumeGameButton:
                    // received by host and client sides
                    // ScreenUtil.showToast(mContext, "Opposite player pressed resume game button.", toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
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
                    int oppScore = Integer.valueOf(msgString.substring(0, 4));
                    gameView.setOppositeCurrentScore(oppScore);
                    int oppNumOfHits = Integer.valueOf(msgString.substring(4, 8));
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
