package com.smile.groundhoghunter;

import android.os.Bundle;
import com.smile.groundhoghunter.threads.BluetoothFunctionThread;
import com.smile.groundhoghunter.utilities.BluetoothUtil;

public class BtClientGameActivity extends ClientGameActivity {

    private BluetoothFunctionThread selectedBtFunctionThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        super.onDestroy();
        BluetoothUtil.stopBluetoothFunctionThread(selectedBtFunctionThread);
        selectedBtFunctionThread = null;
    }
}
