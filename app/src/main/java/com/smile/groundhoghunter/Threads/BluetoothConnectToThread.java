package com.smile.groundhoghunter.Threads;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.smile.groundhoghunter.Utilities.BluetoothUtil;

import java.io.IOException;

public class BluetoothConnectToThread extends Thread {

    public static final String BluetoothConnectToThreadNoClientSocket = ".Threads.BluetoothConnectToThread.NoClientSocket";
    public static final String BluetoothConnectToThreadStarted = ".Threads.BluetoothConnectToThread.Started";
    public static final String BluetoothConnectToThreadConnected =  ".Threads.BluetoothConnectToThread.Connected";
    public static final String BluetoothConnectToThreadFailedToConnect =  ".Threads.BluetoothConnectToThread.FailedToConnect";

    private static final String TAG = new String(".Threads.BluetoothConnectToThread");
    private final BluetoothDevice mBluetoothDevice;
    private final Context mContext;
    private final java.util.UUID mAppUUID;

    private final BluetoothSocket mBluetoothSocket;

    public BluetoothConnectToThread(Context context, BluetoothDevice bluetoothDevice, java.util.UUID appUUID) {
        mContext = context;
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
        if (mBluetoothSocket == null) {
            // cannot create Server Socket
            broadcastIntent = new Intent();
            broadcastIntent.setAction(BluetoothConnectToThreadNoClientSocket);
            mContext.sendBroadcast(broadcastIntent);
            return;
        }

        if (mBluetoothSocket != null) {

            String deviceName = BluetoothUtil.getBluetoothDeviceName(mBluetoothDevice);
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                Log.e(TAG, "Started to connect to server socket ..........");

                broadcastIntent = new Intent();
                broadcastIntent.setAction(BluetoothConnectToThreadStarted);
                broadcastIntent.putExtra("BluetoothDeviceName", deviceName);
                mContext.sendBroadcast(broadcastIntent);

                mBluetoothSocket.connect();
                Log.e(TAG, "Finished to connect to server socket ..........");

                broadcastIntent = new Intent();
                broadcastIntent.setAction(BluetoothConnectToThreadConnected);
                broadcastIntent.putExtra("BluetoothDeviceName", deviceName);
                mContext.sendBroadcast(broadcastIntent);

                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                // manageMyConnectedSocket(mmSocket); // has not been implemented yet

            } catch (IOException ex) {
                // Unable to connect; close the socket and return.
                Log.e(TAG, "Could not connect to server socket", ex);
                broadcastIntent = new Intent();
                broadcastIntent.setAction(BluetoothConnectToThreadFailedToConnect);
                broadcastIntent.putExtra("BluetoothDeviceName", deviceName);
                mContext.sendBroadcast(broadcastIntent);
                try {
                    mBluetoothSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
            }
        }
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {

        if (mBluetoothSocket != null) {
            try {
                mBluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }
}
