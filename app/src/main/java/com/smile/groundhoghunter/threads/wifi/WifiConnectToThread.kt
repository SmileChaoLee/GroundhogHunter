package com.smile.groundhoghunter.threads.wifi

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import com.smile.groundhoghunter.abstract_threads.ClientConnectToThread
import com.smile.groundhoghunter.abstract_threads.IoFunctionThread
import com.smile.groundhoghunter.constants.Constants
import com.smile.groundhoghunter.models.wifi.WifiConnectDevice
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class WifiConnectToThread(
    handler: Handler,
    private val hostAddress: InetAddress,
    private val port: Int
) : ClientConnectToThread(handler) {

    private var mSocket: Socket? = null
    private var wifiFunctionThread: WifiFunctionThread? = null
    private val wifiConnectDevice = WifiConnectDevice(hostAddress.hostAddress, hostAddress.hostAddress)

    companion object {
        private const val TAG = "WifiConnectToThread"
        private const val CONNECT_TIMEOUT_MS = 5000
    }

    init {
        try {
            mSocket = Socket()
        } catch (ex: Exception) {
            Log.e(TAG, "Failed to create Socket", ex)
            mSocket = null
        }
    }

    override fun run() {
        var msg: Message
        val data = Bundle()
        data.putParcelable("ConnectDevice", wifiConnectDevice)

        if (mSocket == null) {
            msg = mHandler.obtainMessage(Constants.CL_CONN_TO_TH_NO_CL_SOCKET)
            msg.data = data
            msg.sendToTarget()
            return
        }

        val socket = mSocket!!
        try {
            Log.d(TAG, "run().Started to connect to host: ${hostAddress.hostAddress}:$port")
            socket.connect(InetSocketAddress(hostAddress, port), CONNECT_TIMEOUT_MS)
            Log.d(TAG, "run().Connected to host.")
            wifiFunctionThread = WifiFunctionThread(mHandler, socket)
            wifiFunctionThread!!.start()
            msg = mHandler.obtainMessage(Constants.CL_CONN_TO_TH_CONNECTED)
        } catch (ex: Exception) {
            Log.e(TAG, "run().Failed to connect.", ex)
            msg = mHandler.obtainMessage(Constants.CL_CONN_TO_TH_FAILED_CONNECT)
            try {
                socket.close()
            } catch (closeEx: Exception) {
                Log.e(TAG, "run().close socket exception", closeEx)
            }
        }
        msg.data = data
        msg.sendToTarget()
    }

    override fun closeClientSocket() {
        try {
            mSocket?.close()
        } catch (ex: Exception) {
            Log.e(TAG, "closeClientSocket.Exception: ", ex)
        }
    }

    override fun getIoFunctionThread(): IoFunctionThread? = wifiFunctionThread
}
