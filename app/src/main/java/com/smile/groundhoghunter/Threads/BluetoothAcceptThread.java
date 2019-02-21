package com.smile.groundhoghunter.Threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;

public class BluetoothAcceptThread extends Thread {

    public static final String BluetoothAcceptThreadStarted = "com.smile.groundhoghunter.Threads.BluetoothAcceptThread.Started";
    public static final String BluetoothAcceptThreadStopped = "com.smile.groundhoghunter.Threads.BluetoothAcceptThread.Stopped";
    public static final String BluetoothAcceptThreadConnected =  "com.smile.groundhoghunter.Threads.BluetoothAcceptThread.Connected";

    private static final String TAG = new String("com.smile.groundhoghunter.Threads.BluetoothAcceptThread");
    private final Context mContext;
    private final String mDeviceName;
    private final java.util.UUID mAppUUID;
    private final BluetoothAdapter mBluetoothAdapter;
    private final BluetoothServerSocket mServerSocket;
    private boolean isListening;

    public BluetoothAcceptThread(Context context, BluetoothAdapter bluetoothAdapter, String deviceName, java.util.UUID appUUID) {

        mContext = context;
        mBluetoothAdapter = bluetoothAdapter;
        mDeviceName = deviceName;
        mAppUUID = appUUID;
        isListening = false;

        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        BluetoothServerSocket temp = null;

        if (mBluetoothAdapter != null) {
            try {
                // app_UUID is the app's UUID string, also used by the client code.
                temp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(mDeviceName, mAppUUID);
            } catch (IOException ex) {
                Log.e(TAG, "Socket's listen() method failed", ex);
            }
        }
        mServerSocket = temp;
    }

    public void run() {

        Intent broadcastIntent;
        if (mServerSocket != null) {
            BluetoothSocket mBluetoothSocket = null;

            while (true) {
                // Keep listening until exception occurs or a socket is returned.
                try {
                    isListening = true; // listening

                    broadcastIntent = new Intent();
                    broadcastIntent.setAction(BluetoothAcceptThreadStarted);
                    mContext.sendBroadcast(broadcastIntent);

                    mBluetoothSocket = mServerSocket.accept();
                    Log.e(TAG, "BluetoothSocket's accept() method finished.");

                    if (mBluetoothSocket != null) {
                        // A connection was accepted. Perform work associated with
                        // the connection in a separate thread.
                        broadcastIntent = new Intent();
                        broadcastIntent.setAction(BluetoothAcceptThreadConnected);
                        mContext.sendBroadcast(broadcastIntent);

                        // manageMyConnectedSocket(mBluetoothSocket);   // has not been implemented yet.
                        mServerSocket.close();
                        break;
                    }
                } catch (IOException ex) {
                    Log.e(TAG, "BluetoothSocket's accept() method failed", ex);

                    // listening is stopped (means BluetoothServerSocket closed or exception occurred)
                    isListening = false;
                    broadcastIntent = new Intent();
                    broadcastIntent.setAction(BluetoothAcceptThreadStopped);
                    mContext.sendBroadcast(broadcastIntent);
                    break;
                }
            }
        }
    }

    public boolean isListening() {
        return isListening;
    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        Intent broadcastIntent;
        try {
            mServerSocket.close();
            broadcastIntent = new Intent();
            broadcastIntent.setAction(BluetoothAcceptThreadStopped);
            mContext.sendBroadcast(broadcastIntent);
        } catch (IOException ex) {
            Log.e(TAG, "Could not close the connect socket", ex);
        }
    }
}
