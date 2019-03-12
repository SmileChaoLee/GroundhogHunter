package com.smile.groundhoghunter.Threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.smile.groundhoghunter.Constants.BluetoothConstants;
import com.smile.groundhoghunter.Utilities.BluetoothUtil;

public class BluetoothAcceptThread extends Thread {

    private static final String TAG = new String(".Threads.BluetoothAcceptThread");
    private final Handler mHandler;
    private final String mPlayerName;
    private final java.util.UUID mAppUUID;
    private final BluetoothAdapter mBluetoothAdapter;
    private final BluetoothServerSocket mServerSocket;

    private BluetoothSocket mBluetoothSocket;
    private BluetoothFunctionThread btFunctionThread;
    private boolean isListening;

    public BluetoothAcceptThread(Handler handler, BluetoothAdapter bluetoothAdapter, String playerName, java.util.UUID appUUID) {

        mHandler = handler;
        mBluetoothAdapter = bluetoothAdapter;
        mBluetoothSocket = null;
        mPlayerName = playerName;
        mAppUUID = appUUID;
        isListening = false;

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
    }

    public void run() {

        Message msg;

        if (mServerSocket == null) {
            // cannot create Server Socket
            msg = mHandler.obtainMessage(BluetoothConstants.BluetoothAcceptThreadNoServerSocket);
            msg.sendToTarget();
            return;
        }
        if (mServerSocket != null) {
            // Keep listening until exception occurs or a socket is returned.
            try {
                isListening = true; // listening

                msg = mHandler.obtainMessage(BluetoothConstants.BluetoothAcceptThreadStarted);
                msg.sendToTarget();

                mBluetoothSocket = mServerSocket.accept();
                Log.e(TAG, "BluetoothSocket's accept() method finished.");

                boolean isConnected = false;
                if (mBluetoothSocket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    String deviceName = BluetoothUtil.getBluetoothDeviceName(mBluetoothSocket.getRemoteDevice());
                    if (deviceName != null) {
                        if (!deviceName.isEmpty()) {
                            mServerSocket.close();
                            btFunctionThread = new BluetoothFunctionThread(mHandler, mBluetoothSocket);
                            btFunctionThread.start();
                            btFunctionThread.write(BluetoothConstants.OppositePlayerNameHasBeenRead, mPlayerName);

                            msg = mHandler.obtainMessage(BluetoothConstants.BluetoothAcceptThreadConnected);
                            msg.sendToTarget();
                            isConnected = true;
                        }
                    }
                }
                if (!isConnected) {
                    throw new Exception("mBluetoothSocket is null or no device name.");
                }
            } catch (Exception ex) {
                Log.e(TAG, "BluetoothSocket's accept() method failed", ex);

                // listening is stopped (means BluetoothServerSocket closed or exception occurred)
                isListening = false;
                msg = mHandler.obtainMessage(BluetoothConstants.BluetoothAcceptThreadStopped);
                msg.sendToTarget();
            }
        }
    }

    public boolean isListening() {
        return isListening;
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

    public BluetoothSocket getBluetoothSocket() {
        return mBluetoothSocket;
    }
    public BluetoothFunctionThread getBtFunctionThread() {
        return btFunctionThread;
    }
}
