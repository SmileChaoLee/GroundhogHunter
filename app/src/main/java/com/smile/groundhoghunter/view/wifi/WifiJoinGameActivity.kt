package com.smile.groundhoghunter.view.wifi

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresPermission
import com.smile.groundhoghunter.R
import com.smile.groundhoghunter.constants.Constants
import com.smile.groundhoghunter.threads.wifi.WifiAcceptThread
import com.smile.groundhoghunter.threads.wifi.WifiConnectToThread
import com.smile.groundhoghunter.view.JoinGameActivity

class WifiJoinGameActivity : JoinGameActivity() {

    companion object {
        private const val TAG = "WifiJoinGameAct"
    }

    private lateinit var mWifiP2pManager: WifiP2pManager
    private lateinit var mChannel: WifiP2pManager.Channel
    private lateinit var mWifiDirectReceiver: WifiDirectBroadcastReceiver
    private var isWifiP2pEnabled: Boolean = false

    // ✅ Separate map to hold WifiP2pDevice by MAC address, for use in onItemClick.
    //    (discoveredDeviceMap in the base class is not suitable here because we don't
    //     have the TCP InetAddress/port until after the P2P group is formed.)
    private val discoveredPeerMap: LinkedHashMap<String, WifiP2pDevice> = LinkedHashMap()

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

    // ─── onItemClick ──────────────────────────────────────────────────────────
    // Mirrors BtJoinGameActivity.onItemClick():
    //   1. Guard checks
    //   2. Look up the selected WifiP2pDevice
    //   3. Stop any previous connect-to-thread
    //   4. Initiate WiFi P2P connection (the TCP socket is created later in the
    //      WIFI_P2P_CONNECTION_CHANGED_ACTION callback once the group IP is known)
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    override fun onItemClick(position: Int, key: String, value: String) {
        Log.d(TAG, "onItemClick.position = $position, key = $key, value = $value")

        if (!isDiscoveryFinished) {
            showMessage.showMessageInTextView(
                getString(R.string.discoverPlayerString), TEMP_MSG_DURATION
            )
            return
        }
        Log.d(TAG, "onItemClick.isConnectingFinished = $isConnectingFinished")
        if (!isConnectingFinished) {
            showMessage.showMessageInTextView(
                getString(R.string.connectingPlayerString), TEMP_MSG_DURATION
            )
            return
        }

        val peer = discoveredPeerMap[key]
        if (peer == null) {
            Log.d(TAG, "onItemClick.peer not found for key=$key")
            return
        }

        // ✅ Close previous connect-to-thread, just like BtJoinGameActivity does
        Log.d(TAG, "onItemClick.mClConnToThread = $mClConnToThread")
        if (mClConnToThread != null) {
            stopClientConnectToThread(mClConnToThread, true)
            mClConnToThread = null
        }

        mConnectedMacAddress = key
        isConnectingFinished = false

        // Initiate WiFi P2P connection. Once the group is formed the broadcast
        // receiver creates the WifiConnectToThread and assigns mClConnToThread.
        connectToPeer(peer)
        showMessage.showMessageInTextView(getString(R.string.connectingPlayerString), CONNECTING_MSG_DURATION)

        twoPlayerListAdapter.myNotifyItemChanged(position)
    }

    // ─── startDiscovery ───────────────────────────────────────────────────────
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    override fun startDiscovery() {
        discoveredPeerMap.clear()   // ✅ Reset our peer map on every new scan
        super.startDiscovery()      // clears discoveredDeviceMap, adapter, starts the 20s timer

        // Path A — standard P2P peer discovery (works on all API levels).
        // Finds the GO via Probe Request/Response on social channels.
        mWifiP2pManager.discoverPeers(mChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "startDiscovery.discoverPeers.onSuccess")
                showMessage.showMessageInTextView(wifiDirectScanStartedString, TEMP_MSG_DURATION)
            }

            override fun onFailure(reason: Int) {
                Log.d(TAG, "startDiscovery.discoverPeers.onFailure: reason=$reason")
                showMessage.showMessageInTextView(wifiDirectNotEnabledString, TEMP_MSG_DURATION)
                // Mark discovery as finished so the user is not stuck waiting for the timer
                isDiscoveryFinished = true
            }
        })

        // Path B — direct connect using the known SSID + passphrase (API 29+ only).
        // Bypasses P2P discovery entirely: if the Create side already has an active
        // group (createGroup() succeeded), this connects immediately without needing
        // the GO to be found via discoverPeers().  Falls back silently to Path A if
        // the group is not yet up (onFailure is ignored).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tryDirectConnect()
        }
    }

    // ─── tryDirectConnect ─────────────────────────────────────────────────────
    // API 29+: connect directly to the known P2P group using SSID + passphrase.
    // No peer discovery needed — if the Create side's group is already up, the
    // WIFI_P2P_CONNECTION_CHANGED_ACTION fires and the TCP socket is opened there.
    // If the group is not yet up, onFailure() resets state and discoverPeers() (Path A)
    // continues as the fallback.
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    private fun tryDirectConnect() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        Log.d(TAG, "tryDirectConnect — attempting direct connection to known group")
        isConnectingFinished = false
        val config = WifiP2pConfig.Builder()
            .setNetworkName(Constants.WIFI_P2P_NETWORK_NAME)
            .setPassphrase(Constants.WIFI_P2P_PASSPHRASE)
            .build()
        mWifiP2pManager.connect(mChannel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "tryDirectConnect.onSuccess — group found, forming connection…")
                showMessage.showMessageInTextView(
                    getString(R.string.connectingPlayerString), CONNECTING_MSG_DURATION
                )
            }
            override fun onFailure(reason: Int) {
                // Group not yet up — silently fall back to Path A (discoverPeers)
                Log.d(TAG, "tryDirectConnect.onFailure reason=$reason — group not found yet")
                isConnectingFinished = true
            }
        })
    }

    // ─── onOppositePlayerNameRead ─────────────────────────────────────────────
    // Called by JoinGameActivity when the host's player name arrives.
    // For WiFi with direct-connect there is no P2P discovery, so PEERS_CHANGED
    // never fires and the RecyclerView stays empty.  We add the host here instead,
    // using mConnectedMacAddress (the GO's IP) as the stable key.
    override fun onOppositePlayerNameRead(deviceAddress: String, playerName: String?) {
        val name = if (!playerName.isNullOrEmpty()) playerName else deviceAddress
        val key  = if (mConnectedMacAddress.isNotEmpty()) mConnectedMacAddress else deviceAddress
        if (key.isNotEmpty() && !oppositePlayerNameMap.containsKey(key)) {
            Log.d(TAG, "onOppositePlayerNameRead.adding host to list: key=$key, name=$name")
            oppositePlayerNameMap[key] = name
            twoPlayerListAdapter.addItem(key, name)
        }
    }

    // ─── startClientGame ──────────────────────────────────────────────────────
    override fun startClientGame() {
        Log.d(TAG, "startClientGame")
        super.startClientGame()
        val gameIntent = Intent(this, WifiClientGameActivity::class.java)
        gameIntent.putExtra(Constants.GAME_TYPE, Constants.TWO_PLAY_GAME_BY_CLIENT)
        clientGameLauncher.launch(gameIntent)
    }

    // ─── connectToPeer (WiFi P2P layer) ───────────────────────────────────────
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    private fun connectToPeer(peer: WifiP2pDevice) {
        val config = WifiP2pConfig().apply {
            deviceAddress = peer.deviceAddress
            // groupOwnerIntent = 0  // Optionally force peer to be the group owner (= TCP server)
        }
        mWifiP2pManager.connect(mChannel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "connectToPeer.connect.onSuccess: ${peer.deviceName}")
                // Actual TCP connection is initiated in WIFI_P2P_CONNECTION_CHANGED_ACTION
            }

            override fun onFailure(reason: Int) {
                Log.d(TAG, "connectToPeer.connect.onFailure: reason=$reason, peer=${peer.deviceName}")
                isConnectingFinished = true
                mConnectedMacAddress = ""
                showMessage.showMessageInTextView(
                    getString(R.string.connectToHostFailedString), TEMP_MSG_DURATION
                )
            }
        })
    }

    // ─── BroadcastReceiver ────────────────────────────────────────────────────
    private inner class WifiDirectBroadcastReceiver : BroadcastReceiver() {

        @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent == null) return
            when (intent.action) {

                // ── WiFi P2P enabled/disabled ──────────────────────────────────
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    isWifiP2pEnabled = (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
                    Log.d(TAG, "WIFI_P2P_STATE_CHANGED_ACTION.isWifiP2pEnabled=$isWifiP2pEnabled")
                    if (!isWifiP2pEnabled) {
                        showMessage.showMessageInTextView(wifiDirectNotEnabledString, TEMP_MSG_DURATION)
                    }
                }

                // ── Peer list updated (equivalent to ACTION_FOUND in Bluetooth) ──
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    Log.d(TAG, "WIFI_P2P_PEERS_CHANGED_ACTION")
                    mWifiP2pManager.requestPeers(mChannel) { peerList ->
                        val peers = peerList.deviceList
                        Log.d(TAG, "requestPeers: ${peers.size} peers found")
                        for (peer in peers) {
                            val peerAddress = peer.deviceAddress   // MAC — use as the map key
                            if (!discoveredPeerMap.containsKey(peerAddress)) {
                                // ✅ Populate the peer map (replaces BT's discoveredDeviceMap entry)
                                discoveredPeerMap[peerAddress] = peer
                                val peerName = if (!peer.deviceName.isNullOrEmpty())
                                    peer.deviceName else peerAddress
                                val megString = "$foundDeviceString: $peerName"
                                Log.d(TAG, megString)
                                showMessage.showMessageInTextView(megString, TEMP_MSG_DURATION)
                                // ✅ Update the RecyclerView list — mirrors BtJoinGameActivity
                                oppositePlayerNameMap[peerAddress] = peerName
                                twoPlayerListAdapter.addItem(peerAddress, peerName)
                            }
                        }
                    }
                }

                // ── P2P group formed — now we can open the TCP socket ──────────
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    Log.d(TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION")
                    mWifiP2pManager.requestConnectionInfo(mChannel) { wifiP2pInfo ->
                        Log.d(TAG, "requestConnectionInfo: groupFormed=${wifiP2pInfo.groupFormed}" +
                                ", isGroupOwner=${wifiP2pInfo.isGroupOwner}")

                        if (wifiP2pInfo.groupFormed && !wifiP2pInfo.isGroupOwner) {
                            // This device is the TCP client (Join side).
                            // The group owner (Create side) runs WifiAcceptThread.
                            val groupOwnerAddress = wifiP2pInfo.groupOwnerAddress
                                ?: return@requestConnectionInfo

                            // ✅ Store the GO's IP as mConnectedMacAddress so that
                            //    onOppositePlayerNameRead() can add the host to the
                            //    RecyclerView with a stable key (direct-connect bypasses
                            //    PEERS_CHANGED so the list is otherwise never populated).
                            val addressKey = groupOwnerAddress.hostAddress ?: ""
                            if (mConnectedMacAddress.isEmpty()) {
                                mConnectedMacAddress = addressKey
                            }

                            // ✅ Guard: don't create a second thread if one is already running
                            val existingThread = mClConnToThread
                            if (existingThread != null &&
                                existingThread.state != Thread.State.TERMINATED) {
                                Log.d(TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION." +
                                        "mClConnToThread already running, skipping")
                                return@requestConnectionInfo
                            }

                            Log.d(TAG, "Creating WifiConnectToThread for " +
                                    "${groupOwnerAddress.hostAddress}:${WifiAcceptThread.WIFI_PORT}")

                            // ✅ Assign mClConnToThread BEFORE start() so the handler can call
                            //    mClConnToThread.getIoFunctionThread() on CL_CONN_TO_TH_CONNECTED
                            val connectToThread = WifiConnectToThread(
                                joinGameHandler, groupOwnerAddress, WifiAcceptThread.WIFI_PORT
                            )
                            mClConnToThread = connectToThread
                            connectToThread.start()

                        } else if (!wifiP2pInfo.groupFormed) {
                            // Group was torn down (e.g. peer disconnected before TCP connect)
                            Log.d(TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION.group not formed")
                            if (!isConnectingFinished) {
                                isConnectingFinished = true
                                mConnectedMacAddress = ""
                                showMessage.showMessageInTextView(
                                    getString(R.string.connectToHostFailedString), TEMP_MSG_DURATION
                                )
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
