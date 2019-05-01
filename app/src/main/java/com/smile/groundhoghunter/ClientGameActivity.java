package com.smile.groundhoghunter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.smile.groundhoghunter.Constants.CommonConstants;
import com.smile.smilepublicclasseslibrary.utilities.ScreenUtil;

public class ClientGameActivity extends GroundhogActivity {

    private final static String TAG = "ClientGameActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startGameButton.setVisibility(View.INVISIBLE);
        startGameButton.setEnabled(false);
        // pauseGameButton.setEnabled(false);
        // resumeGameButton.setEnabled(false);
        newGameButton.setVisibility(View.INVISIBLE);
        newGameButton.setEnabled(false);
    }

    @Override
    protected void pauseGame() {
        selectedIoFunctionThread.write(CommonConstants.TwoPlayerPauseGameButton, "");
        gameView.pauseGame();
        pauseGameButton.setEnabled(false);
        pauseGameButton.setVisibility(View.INVISIBLE);
        resumeGameButton.setEnabled(true);
        resumeGameButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void resumeGame() {
        selectedIoFunctionThread.write(CommonConstants.TwoPlayerResumeGameButton, "");
        gameView.resumeGame();
        pauseGameButton.setEnabled(true);
        pauseGameButton.setVisibility(View.VISIBLE);
        resumeGameButton.setEnabled(false);
        resumeGameButton.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void quitGame() {
        selectedIoFunctionThread.write(CommonConstants.TwoPlayerOppositeLeftGame, "");
        super.quitGame();
    }

    protected class ClientGameHandler extends Handler {

        protected final Looper mLooper;
        protected final Context mContext;

        public ClientGameHandler(Looper looper, Context context) {
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
                    selectedIoFunctionThread.setStartRead(true);    // start reading data
                    break;
                case CommonConstants.TwoPlayerNewGameButton:
                    // received by client side
                    // ScreenUtil.showToast(mContext, "Host pressed new game button.", toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    gameView.newGame(); // new game on client side
                    pauseGameButton.setEnabled(false);
                    pauseGameButton.setVisibility(View.INVISIBLE);
                    resumeGameButton.setEnabled(false);
                    resumeGameButton.setVisibility(View.INVISIBLE);
                    selectedIoFunctionThread.setStartRead(true);    // start reading data
                    break;
                case CommonConstants.TwoPlayerStartGameButton:
                    // received by client side
                    // ScreenUtil.showToast(mContext, "Host pressed start game button.", toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    gameView.startGame();   // start game on client side
                    pauseGameButton.setEnabled(true);
                    pauseGameButton.setVisibility(View.VISIBLE);
                    resumeGameButton.setEnabled(false);
                    resumeGameButton.setVisibility(View.INVISIBLE);
                    selectedIoFunctionThread.setStartRead(true);    // start reading data
                    break;
                case CommonConstants.TwoPlayerPauseGameButton:
                    // received by host and client sides
                    // ScreenUtil.showToast(mContext, "Opposite player pressed pause game button.", toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    gameView.pauseGame();
                    pauseGameButton.setEnabled(false);
                    pauseGameButton.setVisibility(View.INVISIBLE);
                    resumeGameButton.setEnabled(true);
                    resumeGameButton.setVisibility(View.VISIBLE);
                    selectedIoFunctionThread.setStartRead(true);    // start reading data
                    break;
                case CommonConstants.TwoPlayerResumeGameButton:
                    // received by host and client sides
                    // ScreenUtil.showToast(mContext, "Opposite player pressed resume game button.", toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    gameView.resumeGame();
                    resumeGameButton.setEnabled(false);
                    resumeGameButton.setVisibility(View.INVISIBLE);
                    pauseGameButton.setEnabled(true);
                    pauseGameButton.setVisibility(View.VISIBLE);
                    selectedIoFunctionThread.setStartRead(true);    // start reading data
                    break;
            }
        }
    }
}
