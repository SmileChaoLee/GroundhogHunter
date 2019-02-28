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

    public static final String BluetoothAcceptThreadNoServerSocket = ".Threads.BluetoothAcceptThread.NoServerSocket";
    public static final String BluetoothAcceptThreadStarted = ".Threads.BluetoothAcceptThread.Started";
    public static final String BluetoothAcceptThreadStopped = ".Threads.BluetoothAcceptThread.Stopped";
    public static final String BluetoothAcceptThreadConnected =  ".Threads.BluetoothAcceptThread.Connected";
    public static final String BluetoothAcceptThreadCancelled = ".Threads.BluetoothAcceptThread.Cancelled";

    private static final String TAG = new String("com.smile.groundhoghunter.Threads.BluetoothAcceptThread");
    private final Context mContext;
    private final String mDeviceName;
    private final java.util.UUID mAppUUID;
    private final BluetoothAdapter mBluetoothAdapter;
    private final BluetoothServerSocket mServerSocket;
    private boolean isKeepRunning;
    private boolean isListening;
    private boolean isCancelled;

    public BluetoothAcceptThread(Context context, BluetoothAdapter bluetoothAdapter, String deviceName, java.util.UUID appUUID) {

        mContext = context;
        mBluetoothAdapter = bluetoothAdapter;
        mDeviceName = deviceName;
        mAppUUID = appUUID;
        isKeepRunning = true;
        isListening = false;
        isCancelled = false;

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
        if (mServerSocket == null) {
            // cannot create Server Socket
            broadcastIntent = new Intent();
            broadcastIntent.setAction(BluetoothAcceptThreadNoServerSocket);
            mContext.sendBroadcast(broadcastIntent);
            return;
        }
        if (mServerSocket != null) {
            BluetoothSocket mBluetoothSocket = null;
            isCancelled = false;
            while (isKeepRunning) {
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
                    if (!isCancelled) {
                        broadcastIntent = new Intent();
                        broadcastIntent.setAction(BluetoothAcceptThreadStopped);
                        mContext.sendBroadcast(broadcastIntent);
                    }
                    break;
                }
            }
        }
    }

    public void setKeepRunning(boolean isKeepRunning) {
        this.isKeepRunning = isKeepRunning;
    }
    public boolean isKeepRunning() {
        return this.isKeepRunning;
    }
    public boolean isListening() {
        return isListening;
    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {

        if (isCancelled) {
            return;
        }

        isCancelled = true;
        Intent broadcastIntent;
        if (mServerSocket != null) {
            try {
                Log.e(TAG, "Closing the connect socket");
                mServerSocket.close();
                broadcastIntent = new Intent();
                broadcastIntent.setAction(BluetoothAcceptThreadCancelled);
                mContext.sendBroadcast(broadcastIntent);
                Log.e(TAG, "Connect socket closed.");
            } catch (IOException ex) {
                Log.e(TAG, "Could not close the connect socket", ex);
            }
        }
    }
}
