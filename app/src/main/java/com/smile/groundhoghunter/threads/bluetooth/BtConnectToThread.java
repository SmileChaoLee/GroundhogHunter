package com.smile.groundhoghunter.threads.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import com.smile.groundhoghunter.abstract_threads.ClientConnectToThread;
import com.smile.groundhoghunter.abstract_threads.IoFunctionThread;
import com.smile.groundhoghunter.constants.Constants;
import com.smile.groundhoghunter.models.BtConnectDevice;
import com.smile.groundhoghunter.utilities.BluetoothUtil;

public class BtConnectToThread extends ClientConnectToThread {

    private static final String TAG = "BtConnectToThread";
    private final BluetoothDevice mBluetoothDevice;
    private BluetoothSocket mBluetoothSocket;
    private BtIoFunctionThread btFunctionThread;

    public BtConnectToThread(Handler handler, BluetoothDevice bluetoothDevice, java.util.UUID appUUID) {
        super(handler);
        mBluetoothDevice = bluetoothDevice;
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        // Get a BluetoothSocket to connect with the given BluetoothDevice.
        // MY_UUID is the app's UUID string, also used in the server code.
        Log.d(TAG, "BtConnectToThread.Constructor");
        try {
            // mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(appUUID);
            mBluetoothSocket = mBluetoothDevice.createInsecureRfcommSocketToServiceRecord(appUUID);
            if (mBluetoothSocket.isConnected()) {
                BluetoothUtil.closeBluetoothSocket(mBluetoothSocket);
            }
        } catch (Exception ex) {
            Log.e(TAG, "BtConnectToThread.Constructor.Exception", ex);
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void run() {
        Log.d(TAG, "run()");
        Message msg;
        Bundle data = new Bundle();
        BtConnectDevice btConnectDevice = new BtConnectDevice(mBluetoothDevice);
        data.putParcelable("ConnectDevice", btConnectDevice);
        Log.d(TAG, "run().mBluetoothSocket = " + mBluetoothSocket);
        if (mBluetoothSocket == null) {
            // cannot create Server Socket
            msg = mHandler.obtainMessage(Constants.CL_CONN_TO_TH_NO_CL_SOCKET);
            msg.setData(data);
            msg.sendToTarget();
            return;
        }
        String deviceName = BluetoothUtil.getBluetoothDeviceName(mBluetoothDevice);
        if ( (deviceName == null) || (deviceName.isEmpty()) ) {
            msg = mHandler.obtainMessage(Constants.CL_CONN_TO_TH_FAILED_CONNECT);
            msg.setData(data);
            msg.sendToTarget();
            return;
        }
        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            Log.e(TAG, "run().Started to connect to server socket");
            mBluetoothSocket.connect();
            Log.e(TAG, "run().Connected to server socket.");
            // start reading the opposite player's name
            btFunctionThread = new BtIoFunctionThread(mHandler, mBluetoothSocket);
            btFunctionThread.start();   // default is not reading input stream (startRead = false)
            msg = mHandler.obtainMessage(Constants.CL_CONN_TO_TH_CONNECTED);
        } catch (Exception ex) {
            // Unable to connect; close the socket and return.
            Log.e(TAG, "run().Exception", ex);
            msg = mHandler.obtainMessage(Constants.CL_CONN_TO_TH_FAILED_CONNECT);
            try {
                mBluetoothSocket.close();
            } catch (Exception closeException) {
                Log.e(TAG, "run().mBluetoothSocket.close.Exception: ", closeException);
            }
        }
        msg.setData(data);
        msg.sendToTarget();
    }

    // Closes the client socket and causes the thread to finish.
    public void closeClientSocket() {
        Log.d(TAG, "closeClientSocket.mBluetoothSocket = " + mBluetoothSocket);
        if (mBluetoothSocket != null) {
            try {
                mBluetoothSocket.close();
            } catch (Exception ex) {
                Log.e(TAG, "closeClientSocket.Exception: ", ex);
            }
        }
    }

    public IoFunctionThread getIoFunctionThread() {
        return btFunctionThread;
    }
}
