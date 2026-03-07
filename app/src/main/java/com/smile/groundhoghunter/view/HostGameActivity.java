package com.smile.groundhoghunter.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.smile.groundhoghunter.R;
import com.smile.groundhoghunter.constants.Constants;

public class HostGameActivity extends GroundhogActivity {

    private final static String TAG = "HostGameAct";
    private HostGameHandler hostGameHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() is called.");
        super.onCreate(savedInstanceState);
        hostGameHandler = new HostGameHandler(Looper.getMainLooper(), this);
        if (selectedIoFuncThread != null) {
            selectedIoFuncThread.setHandler(hostGameHandler);
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
        if (gameView == null) return;
        if (selectedIoFuncThread == null) return;
        selectedIoFuncThread.write(Constants.TWO_PLAY_ST_GAME_BUT, "");
    }

    @Override
    protected void pauseGame() {
        super.pauseGame();
        if (gameView == null) return;
        if (selectedIoFuncThread == null) return;
        selectedIoFuncThread.write(Constants.TWO_PLAY_PAU_GAME_BUT, "");
    }

    @Override
    protected void resumeGame() {
        super.resumeGame();
        if (gameView == null) return;
        if (selectedIoFuncThread == null) return;
        selectedIoFuncThread.write(Constants.TWO_PLAY_RES_GAME_BUT, "");
    }

    @Override
    protected void newGame() {
        super.newGame();
        if (gameView == null) return;
        if (selectedIoFuncThread == null) return;
        selectedIoFuncThread.write(Constants.TWO_PLAY_NEW_GAME_BUT, "");
    }

    @Override
    protected void quitGame() {
        super.quitGame();
        if (gameView == null) return;
        if (selectedIoFuncThread == null) return;
        selectedIoFuncThread.write(Constants.TWO_PLAY_OPPOS_LF_GAME, "");
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
            Log.d(TAG, "Message received: " + msg.what);
            if (gameView == null) return;
            if (selectedIoFuncThread == null) return;
            String msgString;
            Bundle data = msg.getData();
            switch (msg.what) {
                case Constants.TWO_PLAY_OPPOS_LF_GAME:
                    // received by host and client sides
                    msgString = mContext.getString(R.string.oppositePlayerLeftGameString);
                    Toast.makeText(mContext, msgString, Toast.LENGTH_SHORT).show();
                    gameView.setOpposPlayerLeft(true);
                    selectedIoFuncThread.setStartRead(true);    // start reading data
                    break;
                case Constants.TWO_PLAY_PAU_GAME_BUT:
                    // received by host and client sides
                    HostGameActivity.super.pauseGame();
                    selectedIoFuncThread.setStartRead(true);    // start reading data
                    break;
                case Constants.TWO_PLAY_RES_GAME_BUT:
                    // received by host and client sides
                    HostGameActivity.super.resumeGame();
                    selectedIoFuncThread.setStartRead(true);    // start reading data
                    break;
                case Constants.TWO_PLAY_GAME_G_HOG_HIT:
                    msgString = data.getString("GroundhogHitData", "");
                    gameView.setGroundhogByMsgString(msgString);
                    selectedIoFuncThread.setStartRead(true);
                    break;
                case Constants.TWO_PLAY_GAME_SCORE_RECEIVED:
                    msgString = data.getString("OppositeCurrentScore", "0");
                    int oppScore = Integer.parseInt(msgString.substring(0, 4));
                    gameView.setOpposCurrentScore(oppScore);
                    int oppNumOfHits = Integer.parseInt(msgString.substring(4, 8));
                    gameView.setOpposNumOfHits(oppNumOfHits);
                    gameView.setReceivedScoreFromOppos(true);
                    selectedIoFuncThread.setStartRead(true);    // start reading data
                    break;
                case Constants.TWO_PLAY_DEF_READ:
                    // wrong or read error
                    selectedIoFuncThread.setStartRead(true);    // start reading data
                    break;
            }
        }
    }
}
