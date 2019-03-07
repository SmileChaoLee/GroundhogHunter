package com.smile.groundhoghunter.Threads;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.smile.groundhoghunter.Interfaces.MessageConstants;
import com.smile.groundhoghunter.Utilities.BluetoothUtil;

public class BluetoothConnectToThread extends Thread {

    private static final String TAG = new String(".Threads.BluetoothConnectToThread");
    private final BluetoothDevice mBluetoothDevice;
    private final Handler mHandler;
    private final java.util.UUID mAppUUID;

    private final BluetoothSocket mBluetoothSocket;

    public BluetoothConnectToThread(Handler handler, BluetoothDevice bluetoothDevice, java.util.UUID appUUID) {
        mHandler = handler;
        mBluetoothDevice = bluetoothDevice;
        mAppUUID = appUUID;

        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket temp = null;

        // Get a BluetoothSocket to connect with the given BluetoothDevice.
        // MY_UUID is the app's UUID string, also used in the server code.
        try {
            temp = mBluetoothDevice.createRfcommSocketToServiceRecord(mAppUUID);
        } catch (Exception ex) {
            Log.e(TAG, "BluetoothSocket's create() method failed", ex);
        }

        mBluetoothSocket = temp;
        if (mBluetoothSocket.isConnected()) {
            BluetoothUtil.closeBluetoothSocket(mBluetoothSocket);
        }
    }

    public void run() {

        Message msg;
        Bundle data;

        if (mBluetoothSocket == null) {
            // cannot create Server Socket
            msg = mHandler.obtainMessage(MessageConstants.BluetoothConnectToThreadNoClientSocket);
            msg.sendToTarget();
            return;
        }

        if (mBluetoothSocket != null) {

            String deviceName = BluetoothUtil.getBluetoothDeviceName(mBluetoothDevice);
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                Log.e(TAG, "Started to connect to server socket ..........");

                msg = mHandler.obtainMessage(MessageConstants.BluetoothConnectToThreadStarted);
                data = new Bundle();
                data.putString("BluetoothDeviceName", deviceName);
                msg.setData(data);
                msg.sendToTarget();

                mBluetoothSocket.connect();
                Log.e(TAG, "Finished to connect to server socket ..........");

                msg = mHandler.obtainMessage(MessageConstants.BluetoothConnectToThreadConnected);
                data = new Bundle();
                data.putString("BluetoothDeviceName", deviceName);
                msg.setData(data);
                msg.sendToTarget();

                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                // manageMyConnectedSocket(mmSocket); // has not been implemented yet

            } catch (Exception ex) {
                // Unable to connect; close the socket and return.
                Log.e(TAG, "Could not connect to server socket", ex);

                msg = mHandler.obtainMessage(MessageConstants.BluetoothConnectToThreadFailedToConnect);
                data = new Bundle();
                data.putString("BluetoothDeviceName", deviceName);
                msg.setData(data);
                msg.sendToTarget();

                try {
                    mBluetoothSocket.close();
                } catch (Exception closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
            }
        }
    }

    // Closes the client socket and causes the thread to finish.
    public void closeBluetoothSocket() {

        if (mBluetoothSocket != null) {
            try {
                mBluetoothSocket.close();
            } catch (Exception e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    public BluetoothSocket getBluetoothSocket() {
        return mBluetoothSocket;
    }
}
