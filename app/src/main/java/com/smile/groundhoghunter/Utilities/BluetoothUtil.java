package com.smile.groundhoghunter.Utilities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import com.smile.groundhoghunter.Threads.BluetoothFunctionThread;
import java.util.ArrayList;

public class BluetoothUtil {

    private static final String TAG = "BluetoothUtil";
    private static final Object lock = new Object();

    @SuppressLint("SupportAnnotationUsage")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
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

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public static String getBluetoothDeviceName(BluetoothAdapter mBluetoothAdapter) {
        String deviceName = mBluetoothAdapter.getName();
        // mBluetoothAdapter.getAddress() is deprecated and restricted.
        // We use a fallback string or just the name.
        if (deviceName == null || deviceName.isEmpty()) {
            // On modern Android, getAddress() returns "02:00:00:00:00:00"
            // and requires LOCAL_MAC_ADDRESS (System only permission).
            // It is best to avoid calling it or wrap it in a try-catch.
            try {
                @SuppressLint("HardwareIds")
                String mac = mBluetoothAdapter.getAddress();
                if (mac != null && !mac.equals("02:00:00:00:00:00")) {
                    deviceName = mac;
                } else {
                    deviceName = "";
                }
            } catch (SecurityException e) {
                deviceName = "";
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
                Log.e(TAG, "closeBluetoothSocket.Exception: ", ex);
            }
        }
    }

    public static void stopBluetoothFunctionThreads(ArrayList<BluetoothFunctionThread> btFunctionThreadList) {
        for (BluetoothFunctionThread btFunctionThread : btFunctionThreadList) {
            stopBluetoothFunctionThread(btFunctionThread);
        }
    }

    public static void stopBluetoothFunctionThread(BluetoothFunctionThread btFunctionThread) {
        if (btFunctionThread == null) {
            Log.d(TAG, "stopBluetoothFunctionThread.btFunctionThread is null");
            return;
        }
        synchronized (lock) {
            btFunctionThread.setKeepRunning(false);
            btFunctionThread.closeIoSocket();
            btFunctionThread.setStartRead(true);
            lock.notify();
        }
        boolean retry = true;
        while (retry) {
            try {
                btFunctionThread.join();
                Log.d(TAG, "stopBluetoothFunctionThread.join()");
                retry = false;
                btFunctionThread = null;
            } catch (InterruptedException ex) {
                Log.e(TAG, "stopBluetoothFunctionThread.Exception: ", ex);
            }
        }
    }
}
