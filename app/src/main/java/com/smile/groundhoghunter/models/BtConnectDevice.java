package com.smile.groundhoghunter.models;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresPermission;

import com.smile.groundhoghunter.interfaces.ConnectDevice;

public class BtConnectDevice implements ConnectDevice {

    private BluetoothAdapter mBtAdapter;
    private final BluetoothDevice mBtDevice;

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public BtConnectDevice(BluetoothAdapter btAdapter) {
        mBtAdapter = btAdapter;
        mBtDevice = mBtAdapter.getRemoteDevice(mBtAdapter.getAddress());

    }

    public BtConnectDevice(BluetoothDevice btDevice) {
        mBtAdapter = null;
        mBtDevice = btDevice;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBtAdapter;
    }

    public BluetoothDevice getBluetoothDevice() {
        return mBtDevice;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public String getName() {
        String name;
        if (mBtAdapter != null) {
            name = mBtAdapter.getName();
        } else {
            name = mBtDevice.getName();
        }
        return name;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public String getAddress() {
        String address;
        if (mBtAdapter != null) {
            address = mBtAdapter.getAddress();
        } else {
            address = mBtDevice.getAddress();
        }
        return address;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    @Override
    public boolean isDiscovering() {
        boolean yn;
        if (mBtAdapter != null) {
            yn = mBtAdapter.isDiscovering();
        } else {
            yn = false;
        }
        return yn;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    @Override
    public boolean cancelDiscovery() {
        boolean yn;
        if (mBtAdapter != null) {
            yn = mBtAdapter.cancelDiscovery();
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
        dest.writeParcelable(this.mBtDevice, flags);
    }

    protected BtConnectDevice(Parcel in) {
        this.mBtDevice = in.readParcelable(BluetoothDevice.class.getClassLoader());
    }

    public static final Parcelable.Creator<BtConnectDevice> CREATOR = new Parcelable.Creator<>() {
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
