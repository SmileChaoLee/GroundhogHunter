package com.smile.groundhoghunter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.smile.groundhoghunter.constants.Constants;

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
        selectedIoFunctionThread.write(Constants.TWO_PLAY_ST_GAME_BUT, "");
    }

    @Override
    protected void pauseGame() {
        super.pauseGame();
        selectedIoFunctionThread.write(Constants.TWO_PLAY_PAU_GAME_BUT, "");
    }

    @Override
    protected void resumeGame() {
        super.resumeGame();
        selectedIoFunctionThread.write(Constants.TWO_PLAY_RES_GAME_BUT, "");
    }

    @Override
    protected void newGame() {
        super.newGame();
        selectedIoFunctionThread.write(Constants.TWO_PLAY_NEW_GAME_BUT, "");
    }

    @Override
    protected void quitGame() {
        super.quitGame();
        selectedIoFunctionThread.write(Constants.TWO_PLAY_OPPOS_LF_GAME, "");
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
                case Constants.TWO_PLAY_OPPOS_LF_GAME:
                    // received by host and client sides
                    msgString = mContext.getString(R.string.oppositePlayerLeftGameString);
                    Toast.makeText(mContext, msgString, Toast.LENGTH_SHORT).show();
                    gameView.setOpposPlayerLeft(true);
                    selectedIoFunctionThread.setStartRead(true);    // start reading data
                    break;
                case Constants.TWO_PLAY_PAU_GAME_BUT:
                    // received by host and client sides
                    HostGameActivity.super.pauseGame();
                    selectedIoFunctionThread.setStartRead(true);    // start reading data
                    break;
                case Constants.TWO_PLAY_RES_GAME_BUT:
                    // received by host and client sides
                    HostGameActivity.super.resumeGame();
                    selectedIoFunctionThread.setStartRead(true);    // start reading data
                    break;
                case Constants.TWO_PLAY_GAME_G_HOG_HIT:
                    msgString = data.getString("GroundhogHitData", "");
                    gameView.setGroundhogByMsgString(msgString);
                    selectedIoFunctionThread.setStartRead(true);
                    break;
                case Constants.TWO_PLAY_GAME_SCORE_RECEIVED:
                    msgString = data.getString("OppositeCurrentScore", "0");
                    int oppScore = Integer.parseInt(msgString.substring(0, 4));
                    gameView.setOpposCurrentScore(oppScore);
                    int oppNumOfHits = Integer.parseInt(msgString.substring(4, 8));
                    gameView.setOpposNumOfHits(oppNumOfHits);
                    gameView.setReceivedScoreFromOppos(true);
                    selectedIoFunctionThread.setStartRead(true);    // start reading data
                    break;
                case Constants.TWO_PLAY_DEF_READ:
                    // wrong or read error
                    selectedIoFunctionThread.setStartRead(true);    // start reading data
                    break;
            }
        }
    }
}
