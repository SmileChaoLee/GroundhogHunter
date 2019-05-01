package com.smile.groundhoghunter;

import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import com.smile.groundhoghunter.Threads.BluetoothFunctionThread;
import com.smile.groundhoghunter.Utilities.BluetoothUtil;

public class BtClientGameActivity extends ClientGameActivity {

    private final static String TAG = "BtClientGameActivity";
    private BluetoothFunctionThread selectedBtFunctionThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BtClientGameHandler btClientGameHandler = new BtClientGameHandler(Looper.getMainLooper(), this);
        selectedBtFunctionThread = (BluetoothFunctionThread) super.selectedIoFunctionThread;
        selectedBtFunctionThread.setHandler(btClientGameHandler);
        selectedBtFunctionThread.setStartRead(true);    // start reading data
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
    }
}
