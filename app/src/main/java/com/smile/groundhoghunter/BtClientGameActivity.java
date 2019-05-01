package com.smile.groundhoghunter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.smile.groundhoghunter.Constants.CommonConstants;
import com.smile.groundhoghunter.Threads.BluetoothFunctionThread;
import com.smile.groundhoghunter.Utilities.BluetoothUtil;
import com.smile.smilepublicclasseslibrary.utilities.ScreenUtil;

public class BtClientGameActivity extends GroundhogActivity {

    private final static String TAG = "BtClientGameActivity";
    private BluetoothFunctionThread selectedBtFunctionThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BtClientGameHandler btClientGameHandler = new BtClientGameHandler(Looper.getMainLooper(), this);
        selectedBtFunctionThread = (BluetoothFunctionThread) GroundhogHunterApp.selectedIoFuncThread;
        selectedBtFunctionThread.setHandler(btClientGameHandler);
        selectedBtFunctionThread.setStartRead(true);    // start reading data

        // setContentView(R.layout.activity_client_bt_game);

        startGameButton.setVisibility(View.INVISIBLE);
        startGameButton.setEnabled(false);
        // pauseGameButton.setEnabled(false);
        // resumeGameButton.setEnabled(false);
        newGameButton.setVisibility(View.INVISIBLE);
        newGameButton.setEnabled(false);
    }

    @Override
    public void onDestroy() {
        BluetoothUtil.stopBluetoothFunctionThread(selectedBtFunctionThread);
        selectedBtFunctionThread = null;
        super.onDestroy();
    }

    @Override
    protected void pauseGame() {
        selectedBtFunctionThread.write(CommonConstants.TwoPlayerPauseGameButton, "");
        gameView.pauseGame();
        pauseGameButton.setEnabled(false);
        pauseGameButton.setVisibility(View.INVISIBLE);
        resumeGameButton.setEnabled(true);
        resumeGameButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void resumeGame() {
        selectedBtFunctionThread.write(CommonConstants.TwoPlayerResumeGameButton, "");
        gameView.resumeGame();
        pauseGameButton.setEnabled(true);
        pauseGameButton.setVisibility(View.VISIBLE);
        resumeGameButton.setEnabled(false);
        resumeGameButton.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void quitGame() {
        selectedBtFunctionThread.write(CommonConstants.TwoPlayerOppositeLeftGame, "");
        super.quitGame();
    }

    private class BtClientGameHandler extends Handler {

        private final Looper mLooper;
        private final Context mContext;

        public BtClientGameHandler(Looper looper, Context context) {
            super(looper);
            mLooper = looper;
            mContext = context;
        }

        @Override
        public void handleMessage(Message msg) {
            // super.handleMessage(msg);

            String msgString = "";

            Context mContext = getApplicationContext();
            Bundle data = msg.getData();

            Log.d(TAG, "Message received: " + msg.what);
            switch (msg.what) {
                case CommonConstants.TwoPlayerOppositeLeftGame:
                    // received by host and client sides
                    msgString = mContext.getString(R.string.oppositePlayerLeftGameString);
                    ScreenUtil.showToast(mContext, msgString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    break;
                case CommonConstants.TwoPlayerNewGameButton:
                    // received by client side
                    // ScreenUtil.showToast(mContext, "Host pressed new game button.", toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    gameView.newGame(); // new game on client side
                    pauseGameButton.setEnabled(false);
                    pauseGameButton.setVisibility(View.INVISIBLE);
                    resumeGameButton.setEnabled(false);
                    resumeGameButton.setVisibility(View.INVISIBLE);
                    break;
                case CommonConstants.TwoPlayerStartGameButton:
                    // received by client side
                    // ScreenUtil.showToast(mContext, "Host pressed start game button.", toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    gameView.startGame();   // start game on client side
                    pauseGameButton.setEnabled(true);
                    pauseGameButton.setVisibility(View.VISIBLE);
                    resumeGameButton.setEnabled(false);
                    resumeGameButton.setVisibility(View.INVISIBLE);
                    break;
                case CommonConstants.TwoPlayerPauseGameButton:
                    // received by host and client sides
                    // ScreenUtil.showToast(mContext, "Opposite player pressed pause game button.", toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    gameView.pauseGame();
                    pauseGameButton.setEnabled(false);
                    pauseGameButton.setVisibility(View.INVISIBLE);
                    resumeGameButton.setEnabled(true);
                    resumeGameButton.setVisibility(View.VISIBLE);
                    break;
                case CommonConstants.TwoPlayerResumeGameButton:
                    // received by host and client sides
                    // ScreenUtil.showToast(mContext, "Opposite player pressed resume game button.", toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    gameView.resumeGame();
                    resumeGameButton.setEnabled(false);
                    resumeGameButton.setVisibility(View.INVISIBLE);
                    pauseGameButton.setEnabled(true);
                    pauseGameButton.setVisibility(View.VISIBLE);
                    break;
                case CommonConstants.TwoPlayerDefaultReading:
                default:
                    // wrong or read error
                    break;

            }

            if (selectedBtFunctionThread != null) {
                // read the next data
                selectedBtFunctionThread.setStartRead(true);    // start reading data
            }
        }
    }
}
