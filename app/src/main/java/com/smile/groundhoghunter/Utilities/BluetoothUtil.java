package com.smile.groundhoghunter.Utilities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class BluetoothUtil {
    public static String getBluetoothDeviceName(BluetoothDevice mBluetoothDevice) {
        String deviceName = mBluetoothDevice.getName();
        String deviceHardwareAddress = mBluetoothDevice.getAddress(); // MAC address
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
}
