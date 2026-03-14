package com.smile.groundhoghunter.threads.wifi

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import com.smile.groundhoghunter.abstract_threads.IoFunctionThread
import com.smile.groundhoghunter.abstract_threads.ServerAcceptThread
import com.smile.groundhoghunter.constants.Constants
import com.smile.groundhoghunter.interfaces.ConnectDevice
import com.smile.groundhoghunter.models.WifiConnectDevice
import java.net.ServerSocket

class WifiAcceptThread(handler: Handler, playerName: String) : ServerAcceptThread(handler) {

    private val mPlayerName: String = playerName
    private var mServerSocket: ServerSocket? = null
    private val wifiFunctionThreadMap: HashMap<WifiConnectDevice, WifiFunctionThread> = HashMap()
    private var numOfConnections: Int = 0

    companion object {
        private const val TAG = "WifiAcceptThread"
        const val MAX_CONNECTIONS = 5
        const val WIFI_PORT = 8988
    }

    init {
        try {
            mServerSocket = ServerSocket(WIFI_PORT)
        } catch (ex: Exception) {
            Log.e(TAG, "Failed to create ServerSocket on port $WIFI_PORT", ex)
            mServerSocket = null
        }
        numOfConnections = 0
        keepRunning = true
    }

    override fun run() {
        Log.d(TAG, "run()")
        var msg: Message
        if (mServerSocket == null) {
            msg = mHandler.obtainMessage(Constants.SER_ACCEPT_TH_NO_SER_SOCKET)
            msg.sendToTarget()
            return
        }
        while (keepRunning && numOfConnections < MAX_CONNECTIONS) {
            try {
                val socket = mServerSocket!!.accept()
                Log.d(TAG, "run().ServerSocket's accept() method finished.")
                numOfConnections++
                val wifiFunctionThread = WifiFunctionThread(mHandler, socket)
                wifiFunctionThread.start()
                wifiFunctionThread.write(Constants.OPPOS_PLAYER_NAME_READ, mPlayerName)
                val wifiConnectDevice = WifiConnectDevice(socket.inetAddress, socket.port)
                wifiFunctionThreadMap[wifiConnectDevice] = wifiFunctionThread
                msg = mHandler.obtainMessage(Constants.SER_ACCEPT_TH_CONNECTED)
                val bundle = Bundle()
                bundle.putParcelable("ConnectDevice", wifiConnectDevice)
                msg.data = bundle
                msg.sendToTarget()
            } catch (ex: Exception) {
                Log.e(TAG, "run().Exception: ", ex)
                msg = mHandler.obtainMessage(Constants.SER_ACCEPT_TH_STOPPED)
                msg.sendToTarget()
                break
            }
        }
    }

    override fun closeServerSocket() {
        Log.d(TAG, "closeServerSocket")
        try {
            mServerSocket?.close()
            Log.d(TAG, "closeServerSocket.server socket closed.")
        } catch (ex: Exception) {
            Log.e(TAG, "closeServerSocket.Exception: ", ex)
        }
    }

    override fun getIoFunctionThread(mDevice: ConnectDevice): IoFunctionThread? {
        return wifiFunctionThreadMap[mDevice]
    }

    // ✅ Matches BtAcceptThread.decrementConnections() — called by CreateGameActivity when a
    //    client sends TWO_PLAY_CLIENT_EX_CODE, so the server can accept a new connection slot.
    override fun decrementConnections() {
        if (numOfConnections > 0) {
            numOfConnections--
            Log.d(TAG, "decrementConnections.numOfConnections = $numOfConnections")
        }
    }
}
