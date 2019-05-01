package com.smile.groundhoghunter;

import android.content.Context;
import android.os.Looper;
import android.os.Bundle;
import com.smile.groundhoghunter.Threads.BluetoothFunctionThread;
import com.smile.groundhoghunter.Utilities.BluetoothUtil;

public class BtHostGameActivity extends HostGameActivity {

    private final static String TAG = "BtHostGameActivity";
    private BluetoothFunctionThread selectedBtFunctionThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BtHostGameHandler btHostGameHandler = new BtHostGameHandler(Looper.getMainLooper(), this);
        selectedBtFunctionThread = (BluetoothFunctionThread)super.selectedIoFunctionThread;
        selectedBtFunctionThread.setHandler(btHostGameHandler);
        selectedBtFunctionThread.setStartRead(true);    // start reading data
    }

    @Override
    public void onDestroy() {
        BluetoothUtil.stopBluetoothFunctionThread(selectedBtFunctionThread);
        selectedBtFunctionThread = null;
        super.onDestroy();
    }

    private class BtHostGameHandler extends HostGameHandler {

        public BtHostGameHandler(Looper looper, Context context) {
            super(looper, context);

        }
    }
}
