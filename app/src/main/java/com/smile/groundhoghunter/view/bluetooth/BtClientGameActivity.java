package com.smile.groundhoghunter.view.bluetooth;

import android.os.Bundle;

import com.smile.groundhoghunter.view.ClientGameActivity;
import com.smile.groundhoghunter.threads.bluetooth.BtIoFunctionThread;
import com.smile.groundhoghunter.utilities.BluetoothUtil;

public class BtClientGameActivity extends ClientGameActivity {

    private BtIoFunctionThread selectedBtFunctionThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mIoFuncThread != null) {
            selectedBtFunctionThread = (BtIoFunctionThread) mIoFuncThread;
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
