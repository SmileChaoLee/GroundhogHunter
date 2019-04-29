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
import com.smile.groundhoghunter.Threads.BluetoothFunctionThread;
import com.smile.groundhoghunter.Utilities.BluetoothUtil;
import com.smile.smilepublicclasseslibrary.utilities.ScreenUtil;

public class BtHostGameActivity extends GroundhogActivity {

    private final static String TAG = "BtHostGameActivity";
    private BluetoothFunctionThread selectedBtFunctionThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BtHostGameHandler btHostGameHandler = new BtHostGameHandler(Looper.getMainLooper(), this);
        selectedBtFunctionThread = (BluetoothFunctionThread) GroundhogHunterApp.selectedIoFuncThread;
        selectedBtFunctionThread.setHandler(btHostGameHandler);
        selectedBtFunctionThread.setStartRead(true);    // start reading data
        // setContentView(R.layout.activity_bt_host_game);
    }

    @Override
    public void onDestroy() {
        BluetoothUtil.stopBluetoothFunctionThread(selectedBtFunctionThread);
        selectedBtFunctionThread = null;
        super.onDestroy();
    }

    @Override
    protected void startGame() {
        selectedBtFunctionThread.write(CommonConstants.BluetoothStartGameButton, "");
        super.startGame();
    }

    @Override
    protected void pauseGame() {
        selectedBtFunctionThread.write(CommonConstants.BluetoothPauseGameButton, "");
        super.pauseGame();
    }

    @Override
    protected void resumeGame() {
        selectedBtFunctionThread.write(CommonConstants.BluetoothResumeGameButton, "");
        super.resumeGame();
    }

    @Override
    protected void newGame() {
        selectedBtFunctionThread.write(CommonConstants.BluetoothNewGameButton, "");
        super.newGame();
    }

    @Override
    protected void quitGame() {
        selectedBtFunctionThread.write(CommonConstants.BluetoothLeaveGame, "");
        super.quitGame();
    }

    private class BtHostGameHandler extends Handler {

        private final Looper mLooper;
        private final Context mContext;

        public BtHostGameHandler(Looper looper, Context context) {
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
                case CommonConstants.BluetoothLeaveGame:
                    // received by host and client sides
                    msgString = mContext.getString(R.string.oppositePlayerLeftGameString);
                    ScreenUtil.showToast(mContext, msgString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    break;
                case CommonConstants.BluetoothPauseGameButton:
                    // received by host and client sides
                    // ScreenUtil.showToast(mContext, "Opposite player pressed pause game button.", toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    BtHostGameActivity.super.pauseGame();
                    break;
                case CommonConstants.BluetoothResumeGameButton:
                    // received by host and client sides
                    // ScreenUtil.showToast(mContext, "Opposite player pressed resume game button.", toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    BtHostGameActivity.super.resumeGame();
                    break;
                case CommonConstants.BluetoothDefaultReading:
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
