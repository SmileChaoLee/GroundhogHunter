package com.smile.groundhoghunter.Threads;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

public class BluetoothWritePlayerNameThread extends Thread {

    private final String TAG = new String(".Threads.BluetoothWriteToThread");
    private final BluetoothSocket mBluetoothSocket;
    private OutputStream outputStream;
    private final String mPlayerName;

    public BluetoothWritePlayerNameThread(BluetoothSocket bluetoothSocket, String playerName) {
        mBluetoothSocket = bluetoothSocket;
        mPlayerName = playerName;
        outputStream = null;
        if (mBluetoothSocket != null) {
            try {
                outputStream = mBluetoothSocket.getOutputStream();
            } catch (Exception ex) {
                closeOutputStream();
            }
        }
    }

    public void run() {
        if (outputStream != null) {
            try {
                byte[] temp = mPlayerName.getBytes();
                outputStream.write(temp);
                outputStream.close();
                Log.d(TAG, "OutputStream.write() succeeded.");
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.d(TAG, "OutputStream.write() failed.");
            }
        }
    }

    public void closeOutputStream() {
        if (outputStream != null) {
            try {
                outputStream.close();
                outputStream = null;
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.d(TAG, "Could not close OutputStream");
            }
        }
    }
}
