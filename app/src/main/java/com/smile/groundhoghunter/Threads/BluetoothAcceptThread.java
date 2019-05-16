package com.smile.groundhoghunter.Threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.smile.groundhoghunter.AbstractClasses.IoFunctionThread;
import com.smile.groundhoghunter.AbstractClasses.ServerAcceptThread;
import com.smile.groundhoghunter.Constants.CommonConstants;
import com.smile.groundhoghunter.Interfaces.ConnectDevice;
import com.smile.groundhoghunter.Models.BtConnectDevice;

import java.util.HashMap;

public class BluetoothAcceptThread extends ServerAcceptThread {

    private static final String TAG = new String(".Threads.BluetoothAcceptThread");
    private static final int MaxConnectedBluetoothDevices = 5;
    private final String mPlayerName;
    private final java.util.UUID mAppUUID;
    private final BluetoothAdapter mBluetoothAdapter;
    private final BluetoothServerSocket mServerSocket;

    private BluetoothSocket mBluetoothSocket;
    private HashMap<BtConnectDevice, BluetoothFunctionThread> btFunctionThreadMap;
    private int numOfConnections;

    public BluetoothAcceptThread(Handler handler, BluetoothAdapter bluetoothAdapter, String playerName, java.util.UUID appUUID) {
        super(handler);
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
            msg = mHandler.obtainMessage(CommonConstants.ServerAcceptThreadNoServerSocket);
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

                        BluetoothFunctionThread btFunctionThread = new BluetoothFunctionThread(mHandler, mBluetoothSocket);
                        btFunctionThread.start();
                        btFunctionThread.write(CommonConstants.OppositePlayerNameHasBeenRead, mPlayerName);

                        BtConnectDevice btConnectDevice = new BtConnectDevice(mBluetoothSocket.getRemoteDevice());
                        btFunctionThreadMap.put(btConnectDevice, btFunctionThread);

                        msg = mHandler.obtainMessage(CommonConstants.ServerAcceptThreadConnected);
                        data = new Bundle();
                        data.putParcelable("ConnectDevice", btConnectDevice);
                        msg.setData(data);
                        msg.sendToTarget();
                    }
                    if (!isConnected) {
                        throw new Exception("mBluetoothSocket is null or no device name.");
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "BluetoothSocket's accept() method failed", ex);

                    // listening is stopped (means BluetoothServerSocket closed or exception occurred)
                    msg = mHandler.obtainMessage(CommonConstants.ServerAcceptThreadStopped);
                    msg.sendToTarget();
                }
            }
        }
    }

    // Closes the connect socket and causes the thread to finish.
    @Override
    public void closeServerSocket() {

        Log.e(TAG, "Closing the server socket");
        if (mServerSocket != null) {
            try {
                mServerSocket.close();
                Log.e(TAG, "server socket closed.");
            } catch (Exception ex) {
                Log.e(TAG, "Could not close the connect socket", ex);
            }
        }
    }

    @Override
    public IoFunctionThread getIoFunctionThread(ConnectDevice btDevice) {
        return btFunctionThreadMap.get(btDevice);
    }
}
