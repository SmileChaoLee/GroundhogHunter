package com.smile.groundhoghunter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.smile.groundhoghunter.AbstractClasses.IoFunctionThread;
import com.smile.groundhoghunter.Constants.CommonConstants;
import com.smile.smilepublicclasseslibrary.utilities.ScreenUtil;

public class HostGameActivity extends GroundhogActivity {

    private final static String TAG = "HostGameActivity";
    protected IoFunctionThread selectedIoFunctionThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        selectedIoFunctionThread = GroundhogHunterApp.selectedIoFuncThread;
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

            // Context mContext = getApplicationContext();
            Bundle data = msg.getData();

            Log.d(TAG, "Message received: " + msg.what);
            switch (msg.what) {
                case CommonConstants.TwoPlayerOppositeLeftGame:
                    // received by host and client sides
                    msgString = mContext.getString(R.string.oppositePlayerLeftGameString);
                    ScreenUtil.showToast(mContext, msgString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    break;
                case CommonConstants.TwoPlayerPauseGameButton:
                    // received by host and client sides
                    // ScreenUtil.showToast(mContext, "Opposite player pressed pause game button.", toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    HostGameActivity.super.pauseGame();
                    break;
                case CommonConstants.TwoPlayerResumeGameButton:
                    // received by host and client sides
                    // ScreenUtil.showToast(mContext, "Opposite player pressed resume game button.", toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    HostGameActivity.super.resumeGame();
                    break;
                case CommonConstants.TwoPlayerDefaultReading:
                default:
                    // wrong or read error
                    break;

            }

            if (selectedIoFunctionThread != null) {
                // read the next data
                selectedIoFunctionThread.setStartRead(true);    // start reading data
            }
        }
    }
}
