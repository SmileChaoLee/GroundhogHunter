package com.smile.groundhoghunter.threads.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.smile.groundhoghunter.abstract_threads.IoFunctionThread;
import com.smile.groundhoghunter.constants.Constants;
import com.smile.groundhoghunter.models.BtConnectDevice;

import java.io.InputStream;
import java.io.OutputStream;

public class BtFunctionThread extends IoFunctionThread {
    private final String TAG = "BtFunctionThread";
    private final BluetoothSocket mBluetoothSocket;
    private final IoFunctionThread ioFunctionThread;

    public BtFunctionThread(Handler handler, BluetoothSocket bluetoothSocket) {
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
                        Log.d(TAG, "run().Waiting for notification to read data.");
                        ioFunctionThread.wait();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            try {
                Log.d(TAG, "run().start reading");
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
                    case Constants.OPPOS_PLAYER_NAME_READ:
                        Log.d(TAG, "run().OPPOS_PLAYER_NAME_READ");
                        readMsg = mHandler.obtainMessage(Constants.OPPOS_PLAYER_NAME_READ);
                        data.putString("OppositePlayerName", mBuffer);
                        break;
                    case Constants.TWO_PLAY_HOST_EX_CODE:
                        Log.d(TAG, "run().TWO_PLAY_HOST_EX_CODE");
                        readMsg = mHandler.obtainMessage(Constants.TWO_PLAY_HOST_EX_CODE);
                        break;
                    case Constants.TWO_PLAY_CLIENT_EX_CODE:
                        Log.d(TAG, "run().TWO_PLAY_CLIENT_EX_CODE");
                        readMsg = mHandler.obtainMessage(Constants.TWO_PLAY_CLIENT_EX_CODE);
                        break;
                    case Constants.TWO_PLAY_HOST_ST_GAME:
                        Log.d(TAG, "run().TWO_PLAY_HOST_ST_GAME");
                        readMsg = mHandler.obtainMessage(Constants.TWO_PLAY_HOST_ST_GAME);
                        break;
                    case Constants.TWO_PLAY_OPPOS_LF_GAME:
                        Log.d(TAG, "run().TWO_PLAY_OPPOS_LF_GAME");
                        readMsg = mHandler.obtainMessage(Constants.TWO_PLAY_OPPOS_LF_GAME);
                        break;
                    case Constants.TWO_PLAY_ST_GAME_BUT:
                        Log.d(TAG, "run().TWO_PLAY_ST_GAME_BUT");
                        readMsg = mHandler.obtainMessage(Constants.TWO_PLAY_ST_GAME_BUT);
                        break;
                    case Constants.TWO_PLAY_PAU_GAME_BUT:
                        Log.d(TAG, "run().TWO_PLAY_PAU_GAME_BUT");
                        readMsg = mHandler.obtainMessage(Constants.TWO_PLAY_PAU_GAME_BUT);
                        break;
                    case Constants.TWO_PLAY_RES_GAME_BUT:
                        Log.d(TAG, "run().TWO_PLAY_RES_GAME_BUT");
                        readMsg = mHandler.obtainMessage(Constants.TWO_PLAY_RES_GAME_BUT);
                        break;
                    case Constants.TWO_PLAY_NEW_GAME_BUT:
                        Log.d(TAG, "run().TWO_PLAY_NEW_GAME_BUT");
                        readMsg = mHandler.obtainMessage(Constants.TWO_PLAY_NEW_GAME_BUT);
                        break;
                    case Constants.TWO_PLAY_CL_GAME_TIMER_READ:
                        Log.d(TAG, "run().TWO_PLAY_CL_GAME_TIMER_READ");
                        readMsg = mHandler.obtainMessage(Constants.TWO_PLAY_CL_GAME_TIMER_READ);
                        data.putString("TimerRemaining", mBuffer);
                        break;
                    case Constants.TWO_PLAY_CL_GAME_G_HOG_READ:
                        Log.d(TAG, "run().TWO_PLAY_CL_GAME_G_HOG_READ");
                        readMsg = mHandler.obtainMessage(Constants.TWO_PLAY_CL_GAME_G_HOG_READ);
                        data.putString("GroundhogData", mBuffer);
                        break;
                    case Constants.TWO_PLAY_GAME_G_HOG_HIT:
                        Log.d(TAG, "run().TWO_PLAY_GAME_G_HOG_HIT");
                        readMsg = mHandler.obtainMessage(Constants.TWO_PLAY_GAME_G_HOG_HIT);
                        data.putString("GroundhogHitData", mBuffer);
                        break;
                    case Constants.TWO_PLAY_GAME_SCORE_RECEIVED:
                        Log.d(TAG, "run().TWO_PLAY_GAME_SCORE_RECEIVED");
                        readMsg = mHandler.obtainMessage(Constants.TWO_PLAY_GAME_SCORE_RECEIVED);
                        data.putString("OppositeCurrentScore", mBuffer);
                        break;
                    default:
                        Log.d(TAG, "run().default");
                        readMsg = mHandler.obtainMessage(Constants.TWO_PLAY_DEF_READ);
                        break;
                }
                synchronized (ioFunctionThread) {
                    startRead = false;
                }
                readMsg.setData(data);
                readMsg.sendToTarget();
                Log.d(TAG, "run().byteHead = " + byteHead);
                Log.d(TAG, "run().mBuffer = " + mBuffer);
            } catch (Exception ex) {
                Log.d(TAG, "run().Exception.", ex);
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
            Log.d(TAG, "closeIoSocket.Could not close BluetoothSocket.");
            ex.printStackTrace();
        }
    }
}
