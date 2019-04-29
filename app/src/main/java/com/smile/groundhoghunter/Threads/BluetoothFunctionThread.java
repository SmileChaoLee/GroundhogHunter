package com.smile.groundhoghunter.Threads;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.smile.groundhoghunter.AbstractClasses.IoFunctionThread;
import com.smile.groundhoghunter.Constants.CommonConstants;

import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothFunctionThread extends IoFunctionThread {
    private final String TAG = new String(".Threads.BluetoothFunctionThread");
    private final BluetoothSocket mBluetoothSocket;
    private final IoFunctionThread ioFunctionThread;

    public BluetoothFunctionThread(Handler handler, BluetoothSocket bluetoothSocket) {
        super(handler);
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

        ioFunctionThread = getThisThread();

        synchronized (ioFunctionThread) {
            startRead = false;  // default is not reading the input stream
        }
    }

    public void run() {
        if ( (inputStream == null) || (outputStream == null) ) {
            // finish running
            return;
        }

        Message readMsg;
        Bundle data = new Bundle();
        data.putParcelable("BluetoothDevice", mBluetoothSocket.getRemoteDevice());

        while (keepRunning) {

            synchronized (ioFunctionThread) {
                // wait until start reading data
                while (!startRead) {
                    try {
                        Log.d(TAG, "Waiting for notification to read data.");
                        ioFunctionThread.wait();
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
                        readMsg = mHandler.obtainMessage(CommonConstants.OppositePlayerNameHasBeenRead);
                        data.putString("OppositePlayerName", mBuffer);
                        break;
                    case CommonConstants.BluetoothHostExitCode:
                        readMsg = mHandler.obtainMessage(CommonConstants.BluetoothHostExitCode);
                        break;
                    case CommonConstants.BluetoothClientExitCode:
                        readMsg = mHandler.obtainMessage(CommonConstants.BluetoothClientExitCode);
                        break;
                    case CommonConstants.BluetoothStartGame:
                        readMsg = mHandler.obtainMessage(CommonConstants.BluetoothStartGame);
                        break;
                    case CommonConstants.BluetoothLeaveGame:
                        readMsg = mHandler.obtainMessage(CommonConstants.BluetoothLeaveGame);
                        break;
                    case CommonConstants.BluetoothStartGameButton:
                        readMsg = mHandler.obtainMessage(CommonConstants.BluetoothStartGameButton);
                        break;
                    case CommonConstants.BluetoothPauseGameButton:
                        readMsg = mHandler.obtainMessage(CommonConstants.BluetoothPauseGameButton);
                        break;
                    case CommonConstants.BluetoothResumeGameButton:
                        readMsg = mHandler.obtainMessage(CommonConstants.BluetoothResumeGameButton);
                        break;
                    case CommonConstants.BluetoothNewGameButton:
                        readMsg = mHandler.obtainMessage(CommonConstants.BluetoothNewGameButton);
                        break;
                    default:
                        readMsg = mHandler.obtainMessage(CommonConstants.BluetoothDefaultReading);
                        break;
                }

                synchronized (ioFunctionThread) {
                    startRead = false;
                }

                readMsg.setData(data);
                readMsg.sendToTarget();

                Log.d(TAG, "byteHead: " + byteHead);
                Log.d(TAG, "BluetoothFunctionThread: " + mBuffer);
            } catch (Exception ex) {
                Log.d(TAG, "Failed to read data.", ex);
                break;
            }
        }
    }

    public BluetoothSocket getBluetoothSocket() {
        return mBluetoothSocket;
    }

    public void closeIoSocket() {
        try {
            mBluetoothSocket.close();
        } catch (Exception ex) {
            Log.d(TAG, "Could not close BluetoothSocket.");
            ex.printStackTrace();
        }
    }
}
