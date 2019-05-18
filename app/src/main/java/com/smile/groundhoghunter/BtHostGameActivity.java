package com.smile.groundhoghunter;

import android.os.Bundle;
import android.util.Log;

import com.smile.groundhoghunter.Threads.BluetoothFunctionThread;
import com.smile.groundhoghunter.Utilities.BluetoothUtil;

public class BtHostGameActivity extends HostGameActivity {

    private final static String TAG = ".BtHostGameActivity";
    private BluetoothFunctionThread selectedBtFunctionThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() is called.");
        super.onCreate(savedInstanceState);
        if (selectedIoFunctionThread != null) {
            selectedBtFunctionThread = (BluetoothFunctionThread) selectedIoFunctionThread;
            selectedBtFunctionThread.setStartRead(true);    // start reading data
        } else {
            // selectedIoFunctionThread is null then return to previous
            finish();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy() is called.");
        super.onDestroy();
        BluetoothUtil.stopBluetoothFunctionThread(selectedBtFunctionThread);
        selectedBtFunctionThread = null;
    }
}
