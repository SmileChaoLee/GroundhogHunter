package com.smile.groundhoghunter.models.wifi

import android.os.Parcelable
import com.smile.groundhoghunter.interfaces.ConnectDevice
import kotlinx.parcelize.Parcelize

@Parcelize
data class WifiConnectDevice(
    private val name: String?,
    private val address: String?
) : ConnectDevice, Parcelable {

    override fun getName(): String? = name

    override fun getAddress(): String? = address

    override fun isDiscovering(): Boolean = false

    override fun cancelDiscovery(): Boolean = false
}
