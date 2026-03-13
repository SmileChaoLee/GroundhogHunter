package com.smile.groundhoghunter.threads.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import com.smile.groundhoghunter.abstract_threads.IoFunctionThread;
import com.smile.groundhoghunter.abstract_threads.ServerAcceptThread;
import com.smile.groundhoghunter.constants.Constants;
import com.smile.groundhoghunter.interfaces.ConnectDevice;
import com.smile.groundhoghunter.models.BtConnectDevice;

import java.util.HashMap;
import java.util.UUID;

public class BtAcceptThread extends ServerAcceptThread {

    private static final String TAG = "BtAcceptThread";
    private static final int MAX_CONNECTIONS = 5;
    private final String mPlayerName;
    private final BluetoothServerSocket mServerSocket;
    private BluetoothSocket mBluetoothSocket;
    private final HashMap<BtConnectDevice, BtIoFunctionThread> btFunctionThreadMap;
    private int numOfConnections;

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public BtAcceptThread(Handler handler, BluetoothAdapter bluetoothAdapter, String playerName, UUID appUUID) {
        super(handler);
        mBluetoothSocket = null;
        mPlayerName = playerName;
        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        BluetoothServerSocket temp = null;
        if (bluetoothAdapter != null) {
            try {
                // app_UUID is the app's UUID string, also used by the client code.
                temp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(mPlayerName, appUUID);
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
        Log.d(TAG, "run()");
        Message msg;
        Bundle data;
        Log.d(TAG, "run().mServerSocket = " + mServerSocket);
        if (mServerSocket == null) {
            // cannot create Server Socket
            msg = mHandler.obtainMessage(Constants.SER_ACCEPT_TH_NO_SER_SOCKET);
            msg.sendToTarget();
            return;
        }
        while (keepRunning && (numOfConnections < MAX_CONNECTIONS) ) {
            // Keep listening until exception occurs or a socket is returned.
            try {
                mBluetoothSocket = mServerSocket.accept();
                Log.d(TAG, "run().BluetoothSocket's accept() method finished.");
                boolean isConnected = false;
                if (mBluetoothSocket != null) {
                    // A connection was accepted.
                    numOfConnections++;
                    isConnected = true;
                    BtIoFunctionThread btFunctionThread = new BtIoFunctionThread(mHandler, mBluetoothSocket);
                    btFunctionThread.start();
                    btFunctionThread.write(Constants.OPPOS_PLAYER_NAME_READ, mPlayerName);
                    BtConnectDevice btConnectDevice = new BtConnectDevice(mBluetoothSocket.getRemoteDevice());
                    btFunctionThreadMap.put(btConnectDevice, btFunctionThread);
                    msg = mHandler.obtainMessage(Constants.SER_ACCEPT_TH_CONNECTED);
                    data = new Bundle();
                    data.putParcelable("ConnectDevice", btConnectDevice);
                    msg.setData(data);
                    msg.sendToTarget();
                }
                if (!isConnected) {
                    throw new Exception("run()..mBluetoothSocket is null or no device name.");
                }
            } catch (Exception ex) {
                Log.e(TAG, "run().Exception: ", ex);
                // listening is stopped (means BluetoothServerSocket closed or exception occurred)
                msg = mHandler.obtainMessage(Constants.SER_ACCEPT_TH_STOPPED);
                msg.sendToTarget();
                break;
            }
        }
    }

    // Closes the connect socket and causes the thread to finish.
    @Override
    public void closeServerSocket() {
        Log.d(TAG, "closeServerSocket");
        if (mServerSocket != null) {
            try {
                Log.d(TAG, "closeServerSocket.close()");
                mServerSocket.close();
            } catch (Exception ex) {
                Log.e(TAG, "closeServerSocket.Exception: ", ex);
            }
        }
    }

    @Override
    public IoFunctionThread getIoFunctionThread(ConnectDevice btDevice) {
        return btFunctionThreadMap.get(btDevice);
    }

    @Override
    public void decrementConnections() {
        if (numOfConnections > 0) {
            numOfConnections--;
            Log.d(TAG, "decrementConnections.numOfConnections = " + numOfConnections);
        }
    }
}
