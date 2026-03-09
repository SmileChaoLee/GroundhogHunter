package com.smile.groundhoghunter.view.wifi

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import com.smile.groundhoghunter.R
import com.smile.groundhoghunter.constants.Constants
import com.smile.groundhoghunter.view.CreateGameActivity
// import com.smile.groundhoghunter.threads.wifi.WifiAcceptThread // You'll need to create this

class WifiCreateGameActivity : CreateGameActivity() {
    companion object {
        private const val TAG = "WifiCreateGameAct"
    }

    private lateinit var manager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private var receiver: BroadcastReceiver? = null
    private val intentFilter = IntentFilter()

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Initialize Wi-Fi P2P Manager
        manager = getSystemService(WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)

        // 2. Set up IntentFilter for P2P events
        intentFilter.apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }

        super.onCreate(savedInstanceState)
    }

    /**
     * Replaces the Bluetooth visibility logic. In Wi-Fi Direct,
     * we "Create a Group" to become the owner (Host).
     */
    override fun startDiscoverability() {
        super.startDiscoverability()

        manager.createGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Wi-Fi P2P Group Created Successfully")
                showMessage.showMessageInTextView(getString(R.string.wifi_group_created), MSG_DURATION)

                // Start your server thread to listen for socket connections
                // Note: You'll need the Group Owner IP (usually 192.168.49.1)
                mServerAcceptThread = WifiAcceptThread(createGameHandler, playerName)
                mServerAcceptThread?.start()
            }

            override fun onFailure(reason: Int) {
                val errorMsg = when (reason) {
                    WifiP2pManager.P2P_UNSUPPORTED -> "Wi-Fi Direct not supported"
                    WifiP2pManager.BUSY -> "System busy, try again"
                    else -> "Group creation failed: $reason"
                }
                showMessage.showMessageInTextView(errorMsg, MSG_DURATION)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        receiver = WifiDirectBroadcastReceiver(manager, channel, this)
        registerReceiver(receiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up the group when leaving
        manager.removeGroup(channel, null)
    }
}
