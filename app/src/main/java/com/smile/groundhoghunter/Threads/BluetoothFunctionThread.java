package com.smile.groundhoghunter.Threads;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.util.Log;

import com.smile.groundhoghunter.AbstractClasses.IoFunctionThread;
import com.smile.groundhoghunter.Constants.CommonConstants;
import com.smile.groundhoghunter.Interfaces.ConnectDevice;
import com.smile.groundhoghunter.Models.BtConnectDevice;

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
        BtConnectDevice btConnectDevice = new BtConnectDevice(mBluetoothSocket.getRemoteDevice());
        data.putParcelable("ConnectDevice", btConnectDevice);

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
                    case CommonConstants.TwoPlayerHostExitCode:
                        readMsg = mHandler.obtainMessage(CommonConstants.TwoPlayerHostExitCode);
                        break;
                    case CommonConstants.TwoPlayerClientExitCode:
                        readMsg = mHandler.obtainMessage(CommonConstants.TwoPlayerClientExitCode);
                        break;
                    case CommonConstants.TwoPlayerHostStartGame:
                        readMsg = mHandler.obtainMessage(CommonConstants.TwoPlayerHostStartGame);
                        break;
                    case CommonConstants.TwoPlayerOppositeLeftGame:
                        readMsg = mHandler.obtainMessage(CommonConstants.TwoPlayerOppositeLeftGame);
                        break;
                    case CommonConstants.TwoPlayerStartGameButton:
                        readMsg = mHandler.obtainMessage(CommonConstants.TwoPlayerStartGameButton);
                        break;
                    case CommonConstants.TwoPlayerPauseGameButton:
                        readMsg = mHandler.obtainMessage(CommonConstants.TwoPlayerPauseGameButton);
                        break;
                    case CommonConstants.TwoPlayerResumeGameButton:
                        readMsg = mHandler.obtainMessage(CommonConstants.TwoPlayerResumeGameButton);
                        break;
                    case CommonConstants.TwoPlayerNewGameButton:
                        readMsg = mHandler.obtainMessage(CommonConstants.TwoPlayerNewGameButton);
                        break;
                    case CommonConstants.TwoPlayerClientGameTimerRead:
                        readMsg = mHandler.obtainMessage(CommonConstants.TwoPlayerClientGameTimerRead);
                        data.putString("TimerRemaining", mBuffer);
                        break;
                    case CommonConstants.TwoPlayerClientGameGroundhogRead:
                        readMsg = mHandler.obtainMessage(CommonConstants.TwoPlayerClientGameGroundhogRead);
                        data.putString("GroundhogData", mBuffer);
                        break;
                    case CommonConstants.TwoPlayerGameGroundhogHit:
                        readMsg = mHandler.obtainMessage(CommonConstants.TwoPlayerGameGroundhogHit);
                        data.putString("GroundhogHitData", mBuffer);
                        break;
                    case CommonConstants.TwoPlayerGameScoreReceived:
                        readMsg = mHandler.obtainMessage(CommonConstants.TwoPlayerGameScoreReceived);
                        data.putString("OppositeCurrentScore", mBuffer);
                        break;
                    default:
                        readMsg = mHandler.obtainMessage(CommonConstants.TwoPlayerDefaultReading);
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
