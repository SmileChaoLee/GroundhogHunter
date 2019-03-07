package com.smile.groundhoghunter.Threads;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.smile.groundhoghunter.Interfaces.MessageConstants;

import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothFunctionThread extends Thread {
    private final String TAG = new String(".Threads.BluetoothFunctionThread");
    private final Handler mHandler;
    private final BluetoothSocket mBluetoothSocket;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private String mBuffer;
    private boolean keepRunning;

    public BluetoothFunctionThread(Handler handler, BluetoothSocket bluetoothSocket) {
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
        keepRunning = true;
    }

    public void run() {
        if ( (inputStream == null) || (outputStream == null) ) {
            // finish running
            return;
        }

        Message readMsg;
        Bundle data;

        while (keepRunning) {
            try {
                Log.d(TAG, "BluetoothFunctionThread start reading");
                int byteHead = inputStream.read();
                int dataLength = inputStream.read();
                StringBuilder sb = new StringBuilder();
                int readBuff = -1;
                int byteRead = 0;
                while ( (byteRead<=dataLength) && ((readBuff=inputStream.read()) != -1) && (readBuff != '\n')) {
                    sb.append((char)readBuff);
                    byteRead++;
                }
                mBuffer = sb.toString();
                switch (byteHead) {
                    case MessageConstants.PlayerNameHasBeenRead:
                        if (!mBuffer.isEmpty()) {
                            readMsg = mHandler.obtainMessage(MessageConstants.PlayerNameHasBeenRead);
                            data = new Bundle();
                            data.putString("PlayerName", mBuffer);
                            readMsg.setData(data);
                            readMsg.sendToTarget();
                            Log.d(TAG, "Player name is not empty.");
                        } else {
                            Log.d(TAG, "Player name is empty.");
                        }
                        break;
                }
                Log.d(TAG, "BluetoothFunctionThread: " + mBuffer);
            } catch (Exception ex) {
                Log.d(TAG, "Failed to read data.", ex);
                break;
            }
        }
    }

    public String getDataRead() {
        return mBuffer;
    }

    public void write(int headByte, String data) {
        try {
            Log.d(TAG, "Started to write data to the other.");

            int dataLength = data.length();
            byte[] byteWrite = new byte[dataLength + 3];
            byteWrite[0] = (byte)headByte;
            byteWrite[1] = (byte)dataLength;
            System.arraycopy(data.getBytes(), 0, byteWrite, 2, dataLength);
            byteWrite[byteWrite.length - 1] = '\n';
            Log.d(TAG, "byteWrite = " + new String(byteWrite));

            outputStream.write(byteWrite);

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
