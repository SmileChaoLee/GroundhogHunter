package com.smile.groundhoghunter.Models;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import com.smile.groundhoghunter.Interfaces.ConnectDevice;

public class BtConnectDevice implements ConnectDevice {

    private BluetoothDevice bluetoothDevice;

    public BtConnectDevice(BluetoothDevice btDevice) {
        bluetoothDevice = btDevice;
    }

    @Override
    public String getName() {
        return bluetoothDevice.getName();
    }

    @Override
    public String getAddress() {
        return bluetoothDevice.getAddress();
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
