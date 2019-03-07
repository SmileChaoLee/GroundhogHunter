package com.smile.groundhoghunter.Threads;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class BluetoothReadPlayerNameThread extends Thread {

    public static final String PlayerNameHasBeenRead = ".Threads.BluetoothReadFromThread.PlayerNameHasBeenRead";

    private final String TAG = new String(".Threads.BluetoothReadFromThread");
    private Context mContext;
    private BluetoothSocket mBluetoothSocket;
    private InputStream inputStream;
    private boolean keepRunning;

    public BluetoothReadPlayerNameThread(Context context, BluetoothSocket bluetoothSocket) {
        mContext = context;
        mBluetoothSocket = bluetoothSocket;
        keepRunning = true;
        try {
            inputStream = mBluetoothSocket.getInputStream();
        } catch (Exception ex) {
            ex.printStackTrace();
            closeInputStream();
        }
    }

    public void run() {

        Intent broadcastIntent;
        String playerName;

        if (inputStream != null) {
            Log.d(TAG, "BluetoothReadFromThread is running");
            while (keepRunning) {
                try {
                    Log.d(TAG, "BluetoothReadFromThread start reading");
                    byte[] stringRead = new byte[100];
                    int byteNum = inputStream.read(stringRead);
                    playerName = new String(stringRead);
                    playerName = playerName.substring(0, byteNum);
                    Log.d(TAG, "BluetoothReadFromThread: " + playerName);

                    if (!playerName.isEmpty()) {
                        broadcastIntent = new Intent();
                        broadcastIntent.setAction(PlayerNameHasBeenRead);
                        broadcastIntent.putExtra("PlayerName", playerName);
                        mContext.sendBroadcast(broadcastIntent);
                        Log.d(TAG, "Player name is not empty.");
                        break;
                    } else {
                        Log.d(TAG, "Player name is empty.");
                    }
                } catch (Exception ex) {
                    Log.d(TAG, "Exception on reading Player Name.");
                    ex.printStackTrace();
                    break;
                }

                SystemClock.sleep(200); // delay 200ms
            }
            closeInputStream();
        }
    }

    public void closeInputStream() {
        if (inputStream != null) {
            try {
                inputStream.close();
                inputStream = null;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void setKeepRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }
}
