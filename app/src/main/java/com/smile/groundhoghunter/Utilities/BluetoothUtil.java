package com.smile.groundhoghunter.Utilities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import com.smile.groundhoghunter.Threads.BluetoothFunctionThread;
import java.util.ArrayList;

public class BluetoothUtil {

    private static final String TAG = new String(".Utilities.BluetoothUtil");

    public static String getBluetoothDeviceName(BluetoothDevice mBluetoothDevice) {
        String deviceName = mBluetoothDevice.getName();
        String deviceHardwareAddress = mBluetoothDevice.getAddress(); // MAC address
        if (deviceName == null) {
            deviceName = "";
        }
        if (deviceName.isEmpty()) {
            if (deviceHardwareAddress != null) {
                if (!deviceHardwareAddress.isEmpty()) {
                    deviceName = deviceHardwareAddress;
                }
            }
        }

        return deviceName;
    }

    public static String getBluetoothDeviceName(BluetoothAdapter mBluetoothAdapter) {
        String deviceName = mBluetoothAdapter.getName();
        String deviceHardwareAddress = mBluetoothAdapter.getAddress(); // MAC address
        if (deviceName == null) {
            deviceName = "";
        }
        if (deviceName.isEmpty()) {
            if ((deviceHardwareAddress != null) && (!deviceHardwareAddress.isEmpty())) {
                deviceName = deviceHardwareAddress;
            }
        }

        return deviceName;
    }

    public static void closeBluetoothSocket(BluetoothSocket mBluetoothSocket ) {

        if (mBluetoothSocket != null) {
            // close connection
            try {
                mBluetoothSocket.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void stopBluetoothFunctionThreads(ArrayList<BluetoothFunctionThread> btFunctionThreadList) {
        for (BluetoothFunctionThread btFunctionThread : btFunctionThreadList) {
            stopBluetoothFunctionThread(btFunctionThread);
        }
    }

    public static void stopBluetoothFunctionThread(BluetoothFunctionThread btFunctionThread) {
        if (btFunctionThread != null) {
            btFunctionThread.setKeepRunning(false);
            btFunctionThread.closeBluetoothSocket();
            boolean retry = true;
            while (retry) {
                try {
                    btFunctionThread.join();
                    Log.d(TAG, "btFunctionThread.Join()........\n");
                    retry = false;
                    btFunctionThread = null;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }// continue processing until the thread ends
            }
        }
    }
}
