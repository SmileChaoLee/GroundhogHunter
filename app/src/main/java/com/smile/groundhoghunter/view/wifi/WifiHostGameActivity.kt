package com.smile.groundhoghunter.view.wifi

import android.os.Bundle
import android.util.Log
import com.smile.groundhoghunter.view.HostGameActivity
import com.smile.groundhoghunter.threads.wifi.WifiFunctionThread
import com.smile.groundhoghunter.utilities.WifiDirectUtil

class WifiHostGameActivity : HostGameActivity() {

    companion object {
        private const val TAG = "WifiHostGameAct"
    }
    private var selectedWifiFunctionThread: WifiFunctionThread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate() is called.")
        super.onCreate(savedInstanceState)
        if (selectedIoFuncThread != null) {
            selectedWifiFunctionThread = selectedIoFuncThread as WifiFunctionThread
            selectedWifiFunctionThread!!.setStartRead(true)
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy() is called.")
        super.onDestroy()
        WifiDirectUtil.stopWifiFunctionThread(selectedWifiFunctionThread)
        selectedWifiFunctionThread = null
    }
}
