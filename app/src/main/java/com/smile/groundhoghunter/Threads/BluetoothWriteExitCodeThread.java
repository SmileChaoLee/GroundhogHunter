package com.smile.groundhoghunter.Threads;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.OutputStream;

public class BluetoothWriteExitCodeThread extends Thread {

    private final String TAG = new String(".Threads.BluetoothWriteExitCodeThread");
    private final BluetoothSocket mBluetoothSocket;
    private final String mExitCode;
    private OutputStream outputStream;

    public BluetoothWriteExitCodeThread(BluetoothSocket bluetoothSocket, String exitCode) {
        mBluetoothSocket = bluetoothSocket;
        mExitCode = exitCode;
        outputStream = null;
        try {
            outputStream = mBluetoothSocket.getOutputStream();
        } catch (Exception ex) {
            Log.d(TAG, "Failed to getOutputStream().");
            ex.printStackTrace();
        }
    }

    public void run() {
        if (outputStream != null) {
            try {
                outputStream.write(mExitCode.getBytes());
                outputStream.flush();
                outputStream.close();
            } catch (Exception ex) {
                Log.d(TAG, "Failed to send exit code " + "(" + mExitCode + ")");
                ex.printStackTrace();
            }
        }
    }

    public void closeOutputStream() {
        if (outputStream != null) {
            try {
                outputStream.close();
                outputStream = null;
            } catch (Exception ex) {
                Log.d(TAG, "Failed to close OutputStream");
                ex.printStackTrace();
            }
        }
    }
}
