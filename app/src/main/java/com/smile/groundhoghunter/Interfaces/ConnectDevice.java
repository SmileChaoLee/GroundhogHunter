package com.smile.groundhoghunter.Interfaces;

import android.os.Parcelable;

public interface ConnectDevice extends Parcelable{
    String getName();
    String getAddress();
    boolean isDiscovering();
    boolean cancelDiscovery();
}
