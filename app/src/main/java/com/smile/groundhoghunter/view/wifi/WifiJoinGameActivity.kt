package com.smile.groundhoghunter.view.wifi

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresPermission
import com.smile.groundhoghunter.R
import com.smile.groundhoghunter.constants.Constants
import com.smile.groundhoghunter.models.WifiConnectDevice
import com.smile.groundhoghunter.threads.wifi.WifiAcceptThread
import com.smile.groundhoghunter.threads.wifi.WifiConnectToThread
import com.smile.groundhoghunter.view.JoinGameActivity
import java.net.InetAddress

class WifiJoinGameActivity : JoinGameActivity() {

    companion object {
        private const val TAG = "WifiJoinGameAct"
    }

    private lateinit var mWifiP2pManager: WifiP2pManager
    private lateinit var mChannel: WifiP2pManager.Channel
    private lateinit var mWifiDirectReceiver: WifiDirectBroadcastReceiver
    private var isWifiP2pEnabled: Boolean = false
    private val connectingPeers = HashSet<String>()
    private lateinit var wifiDirectNotEnabledString: String
    private lateinit var wifiDirectScanStartedString: String
    private lateinit var foundDeviceString: String

    override fun onCreate(savedInstanceState: Bundle?) {
        wifiDirectNotEnabledString = getString(R.string.wifiDirectNotEnabledString)
        wifiDirectScanStartedString = getString(R.string.wifiDirectScanStartedString)
        foundDeviceString = getString(R.string.foundDeviceString)

        mWifiP2pManager = getSystemService(WIFI_P2P_SERVICE) as WifiP2pManager
        mChannel = mWifiP2pManager.initialize(this, mainLooper, null)

        mWifiDirectReceiver = WifiDirectBroadcastReceiver()

        // Initialize clientConnectDevice with a placeholder.
        // WifiConnectDevice.isDiscovering() returns false, so no-op when the timer ends.
        try {
            clientConnectDevice = WifiConnectDevice(InetAddress.getByName("0.0.0.0"), 0)
        } catch (ex: Exception) {
            Log.e(TAG, "Failed to create placeholder WifiConnectDevice", ex)
        }

        super.onCreate(savedInstanceState)

        joinGameTitleTextView?.text = getString(R.string.joinWifiDirectGameString)
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }
        registerReceiver(mWifiDirectReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(mWifiDirectReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        mWifiP2pManager.removeGroup(mChannel, null)
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    override fun startDiscovery() {
        super.startDiscovery()
        connectingPeers.clear()
        mWifiP2pManager.discoverPeers(mChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "startDiscovery.discoverPeers.onSuccess")
                showMessage.showMessageInTextView(wifiDirectScanStartedString, MSG_DURATION)
            }

            override fun onFailure(reason: Int) {
                Log.d(TAG, "startDiscovery.discoverPeers.onFailure: reason=$reason")
                showMessage.showMessageInTextView(wifiDirectNotEnabledString, MSG_DURATION)
            }
        })
    }

    override fun startClientGame() {
        Log.d(TAG, "startClientGame")
        super.startClientGame()
        val gameIntent = Intent(this, WifiClientGameActivity::class.java)
        gameIntent.putExtra(Constants.GAME_TYPE, Constants.TWO_PLAY_GAME_BY_CLIENT)
        clientGameLauncher.launch(gameIntent)
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    private fun connectToPeer(peer: WifiP2pDevice) {
        val config = WifiP2pConfig().apply {
            deviceAddress = peer.deviceAddress
        }
        mWifiP2pManager.connect(mChannel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "connectToPeer.connect.onSuccess: ${peer.deviceName}")
            }

            override fun onFailure(reason: Int) {
                Log.d(TAG, "connectToPeer.connect.onFailure: reason=$reason, peer=${peer.deviceName}")
                connectingPeers.remove(peer.deviceAddress)
            }
        })
    }

    private inner class WifiDirectBroadcastReceiver : BroadcastReceiver() {
        @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent == null) return
            when (intent.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                        Log.d(TAG, "WifiP2p is enabled")
                        isWifiP2pEnabled = true
                    } else {
                        Log.d(TAG, "WifiP2p is not enabled")
                        isWifiP2pEnabled = false
                        showMessage.showMessageInTextView(wifiDirectNotEnabledString, MSG_DURATION)
                    }
                }
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    Log.d(TAG, "WIFI_P2P_PEERS_CHANGED_ACTION")
                    mWifiP2pManager.requestPeers(mChannel) { peerList ->
                        val peers = peerList.deviceList
                        Log.d(TAG, "requestPeers: ${peers.size} peers found")
                        for (peer in peers) {
                            val peerAddress = peer.deviceAddress
                            if (!connectingPeers.contains(peerAddress)) {
                                connectingPeers.add(peerAddress)
                                val megString = "$foundDeviceString: ${peer.deviceName}"
                                Log.d(TAG, megString)
                                showMessage.showMessageInTextView(megString, MSG_DURATION)
                                connectToPeer(peer)
                            }
                        }
                    }
                }
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    Log.d(TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION")
                    mWifiP2pManager.requestConnectionInfo(mChannel) { wifiP2pInfo ->
                        Log.d(TAG, "requestConnectionInfo: groupFormed=${wifiP2pInfo.groupFormed}, isGroupOwner=${wifiP2pInfo.isGroupOwner}")
                        if (wifiP2pInfo.groupFormed && !wifiP2pInfo.isGroupOwner) {
                            val groupOwnerAddress = wifiP2pInfo.groupOwnerAddress ?: return@requestConnectionInfo
                            val addressKey = groupOwnerAddress.hostAddress ?: return@requestConnectionInfo
                            if (!discoveredDeviceMap.containsKey(addressKey)) {
                                Log.d(TAG, "Creating WifiConnectToThread for $addressKey")
                                val connectToThread = WifiConnectToThread(
                                    joinGameHandler, groupOwnerAddress, WifiAcceptThread.WIFI_PORT
                                )
                                discoveredDeviceMap[addressKey] = connectToThread
                                connectToThread.start()
                            }
                        }
                    }
                }
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    // no-op for now
                }
            }
        }
    }
}
