package com.smile.groundhoghunter.view.wifi

// TODO: Register WifiCreateGameActivity and WifiHostGameActivity in AndroidManifest.xml,
//  and add ACCESS_WIFI_STATE and CHANGE_WIFI_STATE permissions for Wi-Fi Direct.

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
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

        mWifiP2pManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        mChannel = mWifiP2pManager.initialize(this, mainLooper, null)

        mWifiDirectReceiver = WifiDirectBroadcastReceiver()

        super.onCreate(savedInstanceState)
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

    override fun startDiscoverability() {
        super.startDiscoverability()
        mWifiP2pManager.discoverPeers(mChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "startDiscoverability.discoverPeers.onSuccess")
                showMessage.showMessageInTextView(wifiDirectVisibilityActiveString, MSG_DURATION)
                mServerAcceptThread = WifiAcceptThread(createGameHandler, playerName)
                mServerAcceptThread.start()
            }

            override fun onFailure(reason: Int) {
                Log.d(TAG, "startDiscoverability.discoverPeers.onFailure: reason=$reason")
                showMessage.showMessageInTextView(wifiDirectCannotStartString, MSG_DURATION)
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
                    // no-op for now
                }
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    // no-op for now
                }
            }
        }
    }
}
