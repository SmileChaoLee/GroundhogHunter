package com.smile.groundhoghunter.Models;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import com.smile.groundhoghunter.Interfaces.ConnectDevice;

public class BtConnectDevice implements ConnectDevice {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;

    public BtConnectDevice(BluetoothAdapter btAdapter) {
        bluetoothAdapter = btAdapter;
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(bluetoothAdapter.getAddress());

    }
    public BtConnectDevice(BluetoothDevice btDevice) {
        bluetoothAdapter = null;
        bluetoothDevice = btDevice;
    }

    @Override
    public String getName() {
        String name;
        if (bluetoothAdapter != null) {
            name = bluetoothAdapter.getName();
        } else {
            name = bluetoothDevice.getName();
        }
        return name;
    }

    @Override
    public String getAddress() {
        String address;
        if (bluetoothAdapter != null) {
            address = bluetoothAdapter.getAddress();
        } else {
            address = bluetoothDevice.getAddress();
        }
        return address;
    }

    @Override
    public boolean isDiscovering() {
        boolean yn;
        if (bluetoothAdapter != null) {
            yn = bluetoothAdapter.isDiscovering();
        } else {
            yn = false;
        }
        return yn;
    }

    @Override
    public boolean cancelDiscovery() {
        boolean yn;
        if (bluetoothAdapter != null) {
            yn = bluetoothAdapter.cancelDiscovery();
        } else {
            yn = false;
        }
        return yn;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.bluetoothDevice, flags);
    }

    protected BtConnectDevice(Parcel in) {
        this.bluetoothDevice = in.readParcelable(BluetoothDevice.class.getClassLoader());
    }

    public static final Parcelable.Creator<BtConnectDevice> CREATOR = new Parcelable.Creator<BtConnectDevice>() {
        @Override
        public BtConnectDevice createFromParcel(Parcel source) {
            return new BtConnectDevice(source);
        }

        @Override
        public BtConnectDevice[] newArray(int size) {
            return new BtConnectDevice[size];
        }
    };
}
