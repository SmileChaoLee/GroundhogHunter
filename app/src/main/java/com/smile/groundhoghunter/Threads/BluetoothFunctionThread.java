package com.smile.groundhoghunter.Threads;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.smile.groundhoghunter.Constants.CommonConstants;

import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothFunctionThread extends Thread {
    private final String TAG = new String(".Threads.BluetoothFunctionThread");
    private final BluetoothSocket mBluetoothSocket;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    private Handler mHandler;
    private String mBuffer;
    private boolean keepRunning;
    private boolean startRead;

    private BluetoothFunctionThread thisThread;

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
        startRead = false;  // default is not reading the input stream

        thisThread = this;
    }

    public void run() {
        if ( (inputStream == null) || (outputStream == null) ) {
            // finish running
            return;
        }

        Message readMsg;
        Bundle data;

        while (keepRunning) {

            synchronized (thisThread) {
                // wait until start reading data
                while (!startRead) {
                    try {
                        thisThread.wait();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            try {
                Log.d(TAG, "BluetoothFunctionThread start reading");

                int byteHead = inputStream.read();
                int dataLength = inputStream.read();
                StringBuilder sb = new StringBuilder();
                int readBuff = -1;
                int byteRead = 0;
                while ((byteRead <= dataLength) && ((readBuff = inputStream.read()) != -1) && (readBuff != '\n')) {
                    sb.append((char) readBuff);
                    byteRead++;
                }
                mBuffer = sb.toString();

                switch (byteHead) {
                    case CommonConstants.OppositePlayerNameHasBeenRead:
                        if (!mBuffer.isEmpty()) {
                            readMsg = mHandler.obtainMessage(CommonConstants.OppositePlayerNameHasBeenRead);
                            data = new Bundle();
                            data.putParcelable("BluetoothDevice", mBluetoothSocket.getRemoteDevice());
                            data.putString("OppositePlayerName", mBuffer);
                            readMsg.setData(data);
                            readMsg.sendToTarget();
                        } else {
                            Log.d(TAG, "Opposite player name is empty.");
                        }
                        break;
                    case CommonConstants.BluetoothHostExitCode:
                        readMsg = mHandler.obtainMessage(CommonConstants.BluetoothHostExitCode);
                        data = new Bundle();
                        data.putString("BluetoothMacAddress", mBuffer);
                        readMsg.setData(data);
                        readMsg.sendToTarget();
                        break;
                    case CommonConstants.BluetoothClientExitCode:
                        readMsg = mHandler.obtainMessage(CommonConstants.BluetoothClientExitCode);
                        data = new Bundle();
                        data.putString("BluetoothMacAddress", mBuffer);
                        readMsg.setData(data);
                        readMsg.sendToTarget();
                        break;
                    case CommonConstants.BluetoothStartGame:
                        readMsg = mHandler.obtainMessage(CommonConstants.BluetoothStartGame);
                        readMsg.sendToTarget();
                        break;
                    case CommonConstants.BluetoothLeaveGame:
                        readMsg = mHandler.obtainMessage(CommonConstants.BluetoothLeaveGame);
                        readMsg.sendToTarget();
                        break;
                }
                Log.d(TAG, "byteHead: " + byteHead);
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
    public void setStartRead(boolean startRead) {
        this.startRead = startRead;
    }
    public BluetoothSocket getBluetoothSocket() {
        return mBluetoothSocket;
    }
    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
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
