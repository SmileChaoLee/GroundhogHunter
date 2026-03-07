package com.smile.groundhoghunter.view.bluetooth;

import android.os.Bundle;
import android.util.Log;

import com.smile.groundhoghunter.view.HostGameActivity;
import com.smile.groundhoghunter.threads.BluetoothFunctionThread;
import com.smile.groundhoghunter.utilities.BluetoothUtil;

public class BtHostGameActivity extends HostGameActivity {

    private final static String TAG = "BTHostGameAct";
    private BluetoothFunctionThread selectedBtFunctionThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() is called.");
        super.onCreate(savedInstanceState);
        if (selectedIoFuncThread != null) {
            selectedBtFunctionThread = (BluetoothFunctionThread) selectedIoFuncThread;
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
