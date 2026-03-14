package com.smile.groundhoghunter.utilities

import android.content.Context
import android.util.Log
import com.smile.groundhoghunter.threads.wifi.WifiFunctionThread

// ✅ Kotlin object replaces the Java class-with-only-static-methods pattern.
//    Call sites are identical: WifiDirectUtil.stopWifiFunctionThread(...)
object WifiDirectUtil {

    private const val TAG = "WifiDirectUtil"

    fun isWifiDirectSupported(ctx: Context): Boolean {
        return try {
            ctx.packageManager
                .systemAvailableFeatures
                .any { it.name?.equals("android.hardware.wifi.direct", ignoreCase = true) == true }
        } catch (ex: Exception) {
            Log.e(TAG, "isWifiDirectSupported.Exception: ", ex)
            false
        }
    }

    fun stopWifiFunctionThread(wifiFunctionThread: WifiFunctionThread?) {
        if (wifiFunctionThread == null) {
            Log.d(TAG, "stopWifiFunctionThread.wifiFunctionThread is null")
            return
        }

        // ✅ Sync on startReadLock — the same lock WifiFunctionThread.run() waits on.
        //    setStartRead() is reentrant on startReadLock and calls startReadLock.notify()
        //    internally, so no extra notify() is needed here.
        synchronized(wifiFunctionThread.startReadLock) {
            wifiFunctionThread.setKeepRunning(false)
            wifiFunctionThread.setStartRead(true)   // wakes the waiting run() loop
        }

        // ✅ closeIoSocket() is OUTSIDE the synchronized block — I/O must not hold the lock
        wifiFunctionThread.closeIoSocket()

        var retry = true
        while (retry) {
            try {
                wifiFunctionThread.join()
                Log.d(TAG, "stopWifiFunctionThread.join()")
                retry = false
            } catch (ex: InterruptedException) {
                Log.e(TAG, "stopWifiFunctionThread.InterruptedException: ", ex)
                Thread.currentThread().interrupt()  // restore interrupt flag
                break
            }
        }
    }
}
