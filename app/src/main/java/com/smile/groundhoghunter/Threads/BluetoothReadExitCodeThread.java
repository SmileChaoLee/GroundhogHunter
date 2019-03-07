package com.smile.groundhoghunter.Threads;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class BluetoothReadExitCodeThread extends Thread {

    public static final String ExitCodeHasBeenRead = ".Threads.BluetoothReadExitCodeThread.ExitCodeHasBeenRead";

    private final String TAG = new String(".Threads.BluetoothReadExitCodeThread");
    private final Context mContext;
    private final BluetoothSocket mBluetoothSocket;
    private final String mExitCode;
    private InputStream inputStream;
    private boolean keepRunning;

    public BluetoothReadExitCodeThread(Context context, BluetoothSocket bluetoothSocket, String exitCode) {
        mContext = context;
        mBluetoothSocket = bluetoothSocket;
        mExitCode = exitCode.trim();
        inputStream = null;

        try {
            inputStream = mBluetoothSocket.getInputStream();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d(TAG, "Failed to getInputStream()");
        }

        keepRunning = true;
    }

    public void run() {
        if (inputStream != null) {
            Log.d(TAG, "Started reading exit code " + "(" + mExitCode + ")");
            byte[] xCode = new byte[100];
            try {
                while (keepRunning) {
                    int numBytes = inputStream.read(xCode);
                    String exitCodeString = new String(xCode);
                    exitCodeString = exitCodeString.substring(0, numBytes).toUpperCase();
                    if (exitCodeString.equals(mExitCode.toUpperCase())) {
                        // exit code has been read
                        Intent broadCastIntent = new Intent();
                        broadCastIntent.setAction(ExitCodeHasBeenRead);
                        mContext.sendBroadcast(broadCastIntent);
                        break;
                    }
                    Log.d(TAG, "Finished reading exit code " + "(" + mExitCode + ")");
                }
            } catch (Exception ex) {
                Log.d(TAG, "Failed to read exit code " + "(" + mExitCode + ")");
                ex.printStackTrace();
            }
        } else {
            Log.d(TAG, "InputStream is null.");
        }
    }

    public void closeInputStream() {
        if (inputStream != null) {
            try {
                inputStream.close();
                inputStream = null;
            } catch (Exception ex) {
                Log.d(TAG, "Failed to close InputStream");
                ex.printStackTrace();
            }
        }
    }

    public void setKeepRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }
}
