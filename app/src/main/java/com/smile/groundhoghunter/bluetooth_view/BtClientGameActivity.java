package com.smile.groundhoghunter.bluetooth_view;

import android.os.Bundle;

import com.smile.groundhoghunter.ClientGameActivity;
import com.smile.groundhoghunter.threads.BluetoothFunctionThread;
import com.smile.groundhoghunter.utilities.BluetoothUtil;

public class BtClientGameActivity extends ClientGameActivity {

    private BluetoothFunctionThread selectedBtFunctionThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        super.onDestroy();
        BluetoothUtil.stopBluetoothFunctionThread(selectedBtFunctionThread);
        selectedBtFunctionThread = null;
    }
}
