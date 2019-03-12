package com.smile.groundhoghunter.Utilities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;

public class BluetoothUtil {
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
}
