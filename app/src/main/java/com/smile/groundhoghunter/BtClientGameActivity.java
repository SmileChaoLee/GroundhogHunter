package com.smile.groundhoghunter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.smile.groundhoghunter.Constants.CommonConstants;
import com.smile.groundhoghunter.Threads.BluetoothFunctionThread;
import com.smile.groundhoghunter.Utilities.BluetoothUtil;

public class BtClientGameActivity extends ClientGameActivity {

    private final static String TAG = "BtClientGameActivity";
    private BluetoothFunctionThread selectedBtFunctionThread;

    public BtClientGameActivity() {
        BtClientGameHandler btClientGameHandler = new BtClientGameHandler(Looper.getMainLooper(), this);
        selectedBtFunctionThread = (BluetoothFunctionThread) super.selectedIoFunctionThread;
        selectedBtFunctionThread.setHandler(btClientGameHandler);
        selectedBtFunctionThread.setStartRead(true);    // start reading data
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        BluetoothUtil.stopBluetoothFunctionThread(selectedBtFunctionThread);
        selectedBtFunctionThread = null;
        super.onDestroy();
    }

    private class BtClientGameHandler extends ClientGameHandler {

        public BtClientGameHandler(Looper looper, Context context) {
            super(looper, context);
        }

        @Override
        public void handleMessage(Message msg) {
            // super.handleMessage(msg);
            super.handleMessage(msg);
            Log.d(TAG, "Message received: " + msg.what);
            switch (msg.what) {
                case CommonConstants.TwoPlayerDefaultReading:
                default:
                    // wrong or read error
                    // read the next data
                    selectedBtFunctionThread.setStartRead(true);    // start reading data
                    break;
            }
        }
    }
}
