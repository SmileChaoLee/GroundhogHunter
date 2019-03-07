package com.smile.groundhoghunter.Threads;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothFunctionThread extends Thread {
    public static final String PlayerNameHasBeenRead = ".Threads.BluetoothFunctionThread.PlayerNameHasBeenRead";

    private final String TAG = new String(".Threads.BluetoothFunctionThread");

    private final Context mContext;
    private final Handler mHandler;
    private final BluetoothSocket mBluetoothSocket;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private byte[] mBuffer = new byte[1024];
    private int numBytesRead;
    private boolean keepRunning;

    public BluetoothFunctionThread(Context context, Handler handler, BluetoothSocket bluetoothSocket) {
        mContext = context;
        mHandler = handler;
        mBluetoothSocket = bluetoothSocket;
        InputStream inpStream = null;
        OutputStream outStream = null;
        try {
            inpStream = mBluetoothSocket.getInputStream();
            outStream = mBluetoothSocket.getOutputStream();
        } catch (Exception ex) {
            Log.d(TAG, "Failed to getInputStream().", ex);
            ex.printStackTrace();
        }
        try {
            outStream = mBluetoothSocket.getOutputStream();
        } catch (Exception ex) {
            Log.d(TAG, "Failed to getOutputStream().", ex);
        }

        inputStream = inpStream;
        outputStream = outStream;
        numBytesRead = 0;
        keepRunning = true;
    }

    public void run() {
        if ( (inputStream == null) || (outputStream == null) ) {
            // finish running
            return;
        }

        while (keepRunning) {
            try {
                // numBytesRead = inputStream.read(mBuffer);
                // int head = inputStream.read();
                Log.d(TAG, "BluetoothReadFromThread start reading");
                byte[] stringRead = new byte[100];
                int byteNum = inputStream.read(stringRead);
                String playerName = new String(stringRead);
                playerName = playerName.substring(0, byteNum);
                Log.d(TAG, "BluetoothReadFromThread: " + playerName);

                if (!playerName.isEmpty()) {
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(PlayerNameHasBeenRead);
                    broadcastIntent.putExtra("PlayerName", playerName);
                    mContext.sendBroadcast(broadcastIntent);
                    Log.d(TAG, "Player name is not empty.");
                } else {
                    Log.d(TAG, "Player name is empty.");
                }
            } catch (Exception ex) {
                Log.d(TAG, "Failed to read data.", ex);
                break;
            }
        }
    }

    public String getDataRead() {
        String data = new String(mBuffer);
        data = data.substring(0, numBytesRead);
        return data;
    }

    public void write(String data) {
        try {
            Log.d(TAG, "Write data to the other.");
            outputStream.write(data.getBytes());
            Log.d(TAG, "Succeeded to write data to the other.");
        } catch (Exception ex) {
            Log.d(TAG, "Failed to write data.", ex);
        }
    }

    public void setKeepRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }

    public void closeBluetoothSocket() {
        try {
            mBluetoothSocket.close();
        } catch (Exception ex) {
            Log.d(TAG, "Could not close BluetoothSocket.");
            ex.printStackTrace();
        }
    }
}
