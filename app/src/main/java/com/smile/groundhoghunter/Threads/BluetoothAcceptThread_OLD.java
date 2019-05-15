package com.smile.groundhoghunter.Threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.smile.groundhoghunter.Constants.CommonConstants;

import java.util.HashMap;

public class BluetoothAcceptThread_OLD extends Thread {

    private static final String TAG = new String(".Threads.BluetoothAcceptThread");
    private static final int MaxConnectedBluetoothDevices = 5;
    // private final int acceptThreadId;
    private final Handler mHandler;
    private final String mPlayerName;
    private final java.util.UUID mAppUUID;
    private final BluetoothAdapter mBluetoothAdapter;
    private final BluetoothServerSocket mServerSocket;

    private BluetoothSocket mBluetoothSocket;
    // private BluetoothFunctionThread btFunctionThread;
    private HashMap<BluetoothDevice, BluetoothFunctionThread_OLD> btFunctionThreadMap;
    private int numOfConnections;
    private boolean keepRunning;

    // public BluetoothAcceptThread(int threadId, Handler handler, BluetoothAdapter bluetoothAdapter, String playerName, java.util.UUID appUUID) {
    public BluetoothAcceptThread_OLD(Handler handler, BluetoothAdapter bluetoothAdapter, String playerName, java.util.UUID appUUID) {

        // acceptThreadId = threadId;
        mHandler = handler;
        mBluetoothAdapter = bluetoothAdapter;
        mBluetoothSocket = null;
        mPlayerName = playerName;
        mAppUUID = appUUID;

        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        BluetoothServerSocket temp = null;

        if (mBluetoothAdapter != null) {
            try {
                // app_UUID is the app's UUID string, also used by the client code.
                temp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(mPlayerName, mAppUUID);
            } catch (Exception ex) {
                Log.e(TAG, "Socket's listen() method failed", ex);
            }
        }
        mServerSocket = temp;

        btFunctionThreadMap = new HashMap<>();
        numOfConnections = 0;
        keepRunning = true;
    }

    public void run() {

        Message msg;
        Bundle data;

        if (mServerSocket == null) {
            // cannot create Server Socket
            msg = mHandler.obtainMessage(CommonConstants.BluetoothAcceptThreadNoServerSocket);
            msg.sendToTarget();
            return;
        }

        if (mServerSocket != null) {
            while (keepRunning && (numOfConnections < MaxConnectedBluetoothDevices) ) {
                // Keep listening until exception occurs or a socket is returned.
                try {

                    mBluetoothSocket = mServerSocket.accept();
                    Log.e(TAG, "BluetoothSocket's accept() method finished.");

                    boolean isConnected = false;
                    if (mBluetoothSocket != null) {
                        // A connection was accepted.
                        numOfConnections++;
                        isConnected = true;
                        // mServerSocket.close();

                        BluetoothFunctionThread_OLD btFunctionThread = new BluetoothFunctionThread_OLD(mHandler, mBluetoothSocket);
                        btFunctionThread.start();
                        btFunctionThread.write(CommonConstants.OppositePlayerNameHasBeenRead, mPlayerName);

                        BluetoothDevice btDevice = mBluetoothSocket.getRemoteDevice();

                        btFunctionThreadMap.put(btDevice, btFunctionThread);

                        msg = mHandler.obtainMessage(CommonConstants.BluetoothAcceptThreadConnected);
                        data = new Bundle();
                        data.putParcelable("BluetoothDevice", btDevice);
                        msg.setData(data);
                        msg.sendToTarget();
                    }
                    if (!isConnected) {
                        throw new Exception("mBluetoothSocket is null or no device name.");
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "BluetoothSocket's accept() method failed", ex);

                    // listening is stopped (means BluetoothServerSocket closed or exception occurred)
                    msg = mHandler.obtainMessage(CommonConstants.BluetoothAcceptThreadStopped);
                    msg.sendToTarget();
                }
            }
        }
    }

    // Closes the connect socket and causes the thread to finish.
    public void closeServerSocket() {

        if (mServerSocket != null) {
            try {
                Log.e(TAG, "Closing the server socket");
                mServerSocket.close();
                Log.e(TAG, "server socket closed.");
            } catch (Exception ex) {
                Log.e(TAG, "Could not close the connect socket", ex);
            }
        }
    }

    public BluetoothFunctionThread_OLD getBtFunctionThread(BluetoothDevice btDevice) {
        return btFunctionThreadMap.get(btDevice);
    }
    public void setKeepRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }
}
