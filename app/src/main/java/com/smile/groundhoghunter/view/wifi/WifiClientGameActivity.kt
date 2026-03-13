package com.smile.groundhoghunter.view.wifi

import android.os.Bundle
import android.util.Log
import com.smile.groundhoghunter.threads.wifi.WifiFunctionThread
import com.smile.groundhoghunter.utilities.WifiDirectUtil
import com.smile.groundhoghunter.view.ClientGameActivity

class WifiClientGameActivity : ClientGameActivity() {

    companion object {
        private const val TAG = "WifiClientGameAct"
    }

    private var selectedWifiFunctionThread: WifiFunctionThread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate() is called.")
        super.onCreate(savedInstanceState)
        if (mIoFuncThread != null) {
            selectedWifiFunctionThread = mIoFuncThread as WifiFunctionThread
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
