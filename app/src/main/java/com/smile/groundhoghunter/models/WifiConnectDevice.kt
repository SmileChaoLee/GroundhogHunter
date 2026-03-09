package com.smile.groundhoghunter.models

import android.os.Parcel
import android.os.Parcelable
import com.smile.groundhoghunter.interfaces.ConnectDevice
import java.net.InetAddress

class WifiConnectDevice(
    val inetAddress: InetAddress,
    val port: Int
) : ConnectDevice {

    override fun getName(): String = inetAddress.hostName ?: inetAddress.hostAddress ?: ""

    override fun getAddress(): String = inetAddress.hostAddress ?: ""

    override fun isDiscovering(): Boolean = false

    override fun cancelDiscovery(): Boolean = false

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeByteArray(inetAddress.address)
        dest.writeInt(port)
    }

    constructor(parcel: Parcel) : this(
        InetAddress.getByAddress(parcel.createByteArray()),
        parcel.readInt()
    )

    companion object CREATOR : Parcelable.Creator<WifiConnectDevice> {
        override fun createFromParcel(source: Parcel): WifiConnectDevice = WifiConnectDevice(source)
        override fun newArray(size: Int): Array<WifiConnectDevice?> = arrayOfNulls(size)
    }
}
