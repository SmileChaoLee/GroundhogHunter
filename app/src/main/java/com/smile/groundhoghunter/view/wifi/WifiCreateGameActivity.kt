package com.smile.groundhoghunter.view.wifi

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresPermission
import com.smile.groundhoghunter.R
import com.smile.groundhoghunter.constants.Constants
import com.smile.groundhoghunter.threads.wifi.WifiAcceptThread
import com.smile.groundhoghunter.view.CreateGameActivity

class WifiCreateGameActivity : CreateGameActivity() {

    companion object {
        private const val TAG = "WifiCreateGameAct"
    }

    private lateinit var mWifiP2pManager: WifiP2pManager
    private lateinit var mChannel: WifiP2pManager.Channel
    private lateinit var mWifiDirectReceiver: WifiDirectBroadcastReceiver
    private var isWifiP2pEnabled: Boolean = false
    private lateinit var wifiDirectNotEnabledString: String
    private lateinit var wifiDirectVisibilityActiveString: String
    private lateinit var wifiDirectCannotStartString: String

    override fun onCreate(savedInstanceState: Bundle?) {
        wifiDirectNotEnabledString = getString(R.string.wifiDirectNotEnabledString)
        wifiDirectVisibilityActiveString = getString(R.string.wifiDirectVisibilityActiveString)
        wifiDirectCannotStartString = getString(R.string.wifiDirectCannotStartString)

        mWifiP2pManager = getSystemService(WIFI_P2P_SERVICE) as WifiP2pManager
        mChannel = mWifiP2pManager.initialize(this, mainLooper, null)

        mWifiDirectReceiver = WifiDirectBroadcastReceiver()

        super.onCreate(savedInstanceState)

        createGameTitleTextView?.text = getString(R.string.createWifiDirectGameString)
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
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

    // ─── startDiscoverability ─────────────────────────────────────────────────
    // Sequence: stopPeerDiscovery → removeGroup → createGroup
    //
    // Why stopPeerDiscovery first?
    //   createGroup() returns BUSY if any discovery operation is still running.
    //   Calling stopPeerDiscovery() ensures the framework is idle before we
    //   proceed — even on onFailure, we continue, because it just means nothing
    //   was running.
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    override fun startDiscoverability() {
        super.startDiscoverability()

        mWifiP2pManager.stopPeerDiscovery(mChannel, object : WifiP2pManager.ActionListener {
            @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
            override fun onSuccess() {
                Log.d(TAG, "startDiscoverability.stopPeerDiscovery.onSuccess")
                removeGroupAndCreate()
            }
            @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
            override fun onFailure(reason: Int) {
                // Nothing was running — fine, proceed anyway
                Log.d(TAG, "startDiscoverability.stopPeerDiscovery.onFailure reason=$reason")
                removeGroupAndCreate()
            }
        })
    }

    // ─── removeGroupAndCreate ─────────────────────────────────────────────────
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    private fun removeGroupAndCreate() {
        mWifiP2pManager.removeGroup(mChannel, object : WifiP2pManager.ActionListener {
            @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
            override fun onSuccess() {
                Log.d(TAG, "removeGroupAndCreate.removeGroup.onSuccess")
                createP2pGroup()
            }
            @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
            override fun onFailure(reason: Int) {
                // No existing group to remove — fine, proceed to create
                Log.d(TAG, "removeGroupAndCreate.removeGroup.onFailure reason=$reason")
                createP2pGroup()
            }
        })
    }

    // ─── createP2pGroup ───────────────────────────────────────────────────────
    // On API 29+, force 2.4 GHz so that the P2P social channels (1, 6, 11)
    // used for discovery are always reachable regardless of which band the peer's
    // WiFi radio prefers.  Without this, a device whose chip defaults to 5 GHz
    // creates the group on a non-social channel and becomes invisible to scanners.
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    private fun createP2pGroup() {
        val listener = object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "createP2pGroup.onSuccess — now a permanent P2P Group Owner")
                showMessage.showMessageInTextView(wifiDirectVisibilityActiveString, MSG_DURATION)
                mServerAcceptThread = WifiAcceptThread(createGameHandler, playerName)
                mServerAcceptThread.start()
                // ✅ Also enter extended listen state so scanning devices (e.g. Samsung A10)
                //    can discover this GO on the 2.4 GHz social channels.
                startGroupOwnerDiscovery()
            }
            override fun onFailure(reason: Int) {
                Log.d(TAG, "createP2pGroup.onFailure reason=$reason")
                showMessage.showMessageInTextView(wifiDirectCannotStartString, MSG_DURATION)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Force 2.4 GHz. networkName + passphrase are mandatory for createGroup;
            // omitting them causes IllegalStateException in build().
            val config = WifiP2pConfig.Builder()
                .setNetworkName(Constants.WIFI_P2P_NETWORK_NAME)
                .setPassphrase(Constants.WIFI_P2P_PASSPHRASE)
                .setGroupOperatingBand(WifiP2pConfig.GROUP_OWNER_BAND_2GHZ)
                .build()
            mWifiP2pManager.createGroup(mChannel, config, listener)
        } else {
            mWifiP2pManager.createGroup(mChannel, listener)
        }
    }

    // After becoming a Group Owner, call discoverPeers() to enter "extended listen" state.
    // This makes the GO actively respond to Probe Requests on P2P social channels (1, 6, 11)
    // so that devices running discoverPeers() can find it.
    // Some devices/firmware may not support discovery in GO state — onFailure is silently ignored.
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    private fun startGroupOwnerDiscovery() {
        mWifiP2pManager.discoverPeers(mChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "startGroupOwnerDiscovery.onSuccess — GO is in extended listen state")
            }
            override fun onFailure(reason: Int) {
                Log.d(TAG, "startGroupOwnerDiscovery.onFailure reason=$reason — extended listen not supported, GO still beacons")
            }
        })
    }

    override fun startHostGame() {
        super.startHostGame()
        Log.d(TAG, "startHostGame")
        val gameIntent = Intent(this, WifiHostGameActivity::class.java)
        gameIntent.putExtra(Constants.GAME_TYPE, Constants.TWO_PLAY_GAME_BY_HOST)
        hostGameLauncher.launch(gameIntent)
    }

    private inner class WifiDirectBroadcastReceiver : BroadcastReceiver() {
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
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    // no-op — TCP connection is handled by WifiAcceptThread
                }
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    // no-op
                }
            }
        }
    }
}
