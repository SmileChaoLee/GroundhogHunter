package com.smile.groundhoghunter;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothServiceFunction {

    public static final String PlayerNameHasBeenRead = ".BluetoothServiceFunction.PlayerNameHasBeenRead";

    private static final String TAG = new String(".BluetoothServiceFunction");
    private final Context mContext;
    private final Handler mHandler;
    private final BluetoothSocket mBluetoothSocket;
    private BluetoothServiceThread serviceThread;

    public BluetoothServiceFunction(Context context, Handler handler, BluetoothSocket bluetoothSocket) {
        mContext = context;
        mHandler = handler;
        mBluetoothSocket = bluetoothSocket;
        serviceThread = new BluetoothServiceThread(mBluetoothSocket);
        serviceThread.start();
    }

    public String getDataRead() {
        return serviceThread.getDataRead();
    }

    public void releaseBluetoothServiceFunction() {
        if (serviceThread != null) {
            serviceThread.setKeepRunning(false);
            serviceThread.closeBluetoothSocket();
            boolean retry = true;
            while (retry) {
                try {
                    serviceThread.join();
                    Log.d(TAG, "serviceThread.Join()........\n");
                    retry = false;
                    serviceThread = null;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }// continue processing until the thread ends
            }
        }
    }

    private class BluetoothServiceThread extends Thread {

        private final String TAG = new String(".BluetoothServiceFunction.BluetoothServiceThread");
        private final BluetoothSocket mBluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        private byte[] mBuffer = new byte[1024];
        private int numBytesRead;
        private boolean keepRunning;

        public BluetoothServiceThread(BluetoothSocket bluetoothSocket) {
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
                        break;
                    } else {
                        Log.d(TAG, "Player name is empty.");
                    }
                } catch (IOException ex) {
                    Log.d(TAG, "Failed to read data.", ex);
                }
            }
        }

        public String getDataRead() {
            String data = new String(mBuffer);
            data = data.substring(0, numBytesRead);
            return data;
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
}
