package com.smile.groundhoghunter;

import android.os.Bundle;

import com.smile.groundhoghunter.Threads.BluetoothFunctionThread;
import com.smile.groundhoghunter.Utilities.BluetoothUtil;

public class BtHostGameActivity extends HostGameActivity {

    private final static String TAG = ".BtHostGameActivity";
    private BluetoothFunctionThread selectedBtFunctionThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        selectedBtFunctionThread = (BluetoothFunctionThread) selectedIoFunctionThread;
        selectedBtFunctionThread.setStartRead(true);    // start reading data
    }

    @Override
    public void onDestroy() {
        BluetoothUtil.stopBluetoothFunctionThread(selectedBtFunctionThread);
        selectedBtFunctionThread = null;
        super.onDestroy();
    }
}
