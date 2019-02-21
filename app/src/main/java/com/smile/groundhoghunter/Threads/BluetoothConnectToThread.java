package com.smile.groundhoghunter.Threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;

public class BluetoothConnectToThread extends Thread {

    public static final String BluetoothConnectToThreadConnected =  "com.smile.groundhoghunter.Threads.BluetoothConnectToThread.Connected";
    public static final String BluetoothConnectToThreadFailedToConnect =  "com.smile.groundhoghunter.Threads.BluetoothConnectToThread.FailedToConnect";

    private static final String TAG = new String("com.smile.groundhoghunter.Threads.BluetoothConnectToThread");
    private final BluetoothAdapter mBluetoothAdapter;
    private final BluetoothDevice mBluetoothDevice;
    private final Context mContext;
    private final java.util.UUID mAppUUID;

    private final BluetoothSocket mBluetoothSocket;

    public BluetoothConnectToThread(Context context, BluetoothAdapter bluetoothAdapter, BluetoothDevice bluetoothDevice, java.util.UUID appUUID) {
        mContext = context;
        mBluetoothAdapter = bluetoothAdapter;
        mBluetoothDevice = bluetoothDevice;
        mAppUUID = appUUID;

        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket temp = null;

        // Get a BluetoothSocket to connect with the given BluetoothDevice.
        // MY_UUID is the app's UUID string, also used in the server code.
        try {
            temp = mBluetoothDevice.createRfcommSocketToServiceRecord(mAppUUID);
        } catch (IOException ex) {
            Log.e(TAG, "BluetoothSocket's create() method failed", ex);
        }

        mBluetoothSocket = temp;
    }

    public void run() {

        Intent broadcastIntent;

        // Cancel discovery because it otherwise slows down the connection.
        mBluetoothAdapter.cancelDiscovery();

        if (mBluetoothSocket != null) {
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                Log.e(TAG, "Started to connect to server socket ..........");
                mBluetoothSocket.connect();
                Log.e(TAG, "Finished to connect to server socket ..........");

                broadcastIntent = new Intent();
                broadcastIntent.setAction(BluetoothConnectToThreadConnected);
                mContext.sendBroadcast(broadcastIntent);

            } catch (IOException ex) {
                // Unable to connect; close the socket and return.
                Log.e(TAG, "Could not connect to server socket", ex);
                broadcastIntent = new Intent();
                broadcastIntent.setAction(BluetoothConnectToThreadFailedToConnect);
                mContext.sendBroadcast(broadcastIntent);
                try {
                    mBluetoothSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            // manageMyConnectedSocket(mmSocket); // has not been implemented yet
        }
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mBluetoothSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }
}
