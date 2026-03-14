package com.smile.groundhoghunter.threads.wifi

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import com.smile.groundhoghunter.abstract_threads.IoFunctionThread
import com.smile.groundhoghunter.constants.Constants
import com.smile.groundhoghunter.models.WifiConnectDevice
import java.net.Socket
import java.util.concurrent.Executors

class WifiFunctionThread(handler: Handler, private val mSocket: Socket) : IoFunctionThread(handler) {

    companion object {
        private const val TAG = "WifiFunctionThread"
    }

    // ✅ Dedicated single-thread executor for all socket writes.
    //    IoFunctionThread.write() calls outputStream.write() which is a TCP socket
    //    operation.  Android's BlockGuard throws NetworkOnMainThreadException when
    //    this is called from the main thread (e.g. from a Handler callback).
    //    Bluetooth sockets are NOT intercepted by BlockGuard, which is why BT never
    //    hit this problem.  All WiFi writes must go through this background thread.
    private val writeExecutor = Executors.newSingleThreadExecutor()

    init {
        try {
            inputStream = mSocket.getInputStream()
        } catch (ex: Exception) {
            Log.e(TAG, "Failed to getInputStream().", ex)
        }
        try {
            outputStream = mSocket.getOutputStream()
        } catch (ex: Exception) {
            Log.e(TAG, "Failed to getOutputStream().", ex)
        }
        keepRunning = true
        // ✅ Use startReadLock (same lock that setStartRead() uses) — matches BtIoFunctionThread
        synchronized(startReadLock) {
            startRead = false
        }
    }

    // Dispatch every write to the background executor instead of the calling thread.
    override fun write(headByte: Int, data: String) {
        if (!writeExecutor.isShutdown) {
            writeExecutor.execute { super.write(headByte, data) }
        }
    }

    override fun run() {
        if (inputStream == null || outputStream == null) {
            Log.d(TAG, "run().inputStream or outputStream is null.")
            return
        }

        val wifiConnectDevice = WifiConnectDevice(mSocket.inetAddress, mSocket.port)

        while (keepRunning) {
            // ✅ Wait on startReadLock — matches the lock used by setStartRead()
            synchronized(startReadLock) {
                while (!startRead) {
                    try {
                        Log.d(TAG, "run().Waiting for notification to read data.")
                        (startReadLock as Object).wait()
                    } catch (ex: InterruptedException) {
                        Log.e(TAG, "run().InterruptedException: ", ex)
                    }
                }
            }
            try {
                val data = Bundle()
                data.putParcelable("ConnectDevice", wifiConnectDevice)

                Log.d(TAG, "run().start reading")
                val byteHead = inputStream!!.read()
                val dataLength = inputStream!!.read()
                val sb = StringBuilder()
                var byteRead = 0
                while (byteRead <= dataLength) {
                    val readBuff = inputStream!!.read()
                    if (readBuff == -1 || readBuff == '\n'.code) break
                    sb.append(readBuff.toChar())
                    byteRead++
                }
                mBuffer = sb.toString()
                Log.d(TAG, "run().byteHead = $byteHead")
                Log.d(TAG, "run().mBuffer = $mBuffer")

                val readMsg: Message = when (byteHead) {
                    Constants.OPPOS_PLAYER_NAME_READ -> {
                        Log.d(TAG, "run().OPPOS_PLAYER_NAME_READ")
                        data.putString("OppositePlayerName", mBuffer)
                        mHandler.obtainMessage(Constants.OPPOS_PLAYER_NAME_READ)
                    }
                    Constants.TWO_PLAY_HOST_EX_CODE -> {
                        Log.d(TAG, "run().TWO_PLAY_HOST_EX_CODE")
                        mHandler.obtainMessage(Constants.TWO_PLAY_HOST_EX_CODE)
                    }
                    Constants.TWO_PLAY_CLIENT_EX_CODE -> {
                        Log.d(TAG, "run().TWO_PLAY_CLIENT_EX_CODE")
                        mHandler.obtainMessage(Constants.TWO_PLAY_CLIENT_EX_CODE)
                    }
                    Constants.TWO_PLAY_HOST_ST_GAME -> {
                        Log.d(TAG, "run().TWO_PLAY_HOST_ST_GAME")
                        mHandler.obtainMessage(Constants.TWO_PLAY_HOST_ST_GAME)
                    }
                    Constants.TWO_PLAY_OPPOS_LF_GAME -> {
                        Log.d(TAG, "run().TWO_PLAY_OPPOS_LF_GAME")
                        mHandler.obtainMessage(Constants.TWO_PLAY_OPPOS_LF_GAME)
                    }
                    Constants.TWO_PLAY_ST_GAME_BUT -> {
                        Log.d(TAG, "run().TWO_PLAY_ST_GAME_BUT")
                        mHandler.obtainMessage(Constants.TWO_PLAY_ST_GAME_BUT)
                    }
                    Constants.TWO_PLAY_PAU_GAME_BUT -> {
                        Log.d(TAG, "run().TWO_PLAY_PAU_GAME_BUT")
                        mHandler.obtainMessage(Constants.TWO_PLAY_PAU_GAME_BUT)
                    }
                    Constants.TWO_PLAY_RES_GAME_BUT -> {
                        Log.d(TAG, "run().TWO_PLAY_RES_GAME_BUT")
                        mHandler.obtainMessage(Constants.TWO_PLAY_RES_GAME_BUT)
                    }
                    Constants.TWO_PLAY_NEW_GAME_BUT -> {
                        Log.d(TAG, "run().TWO_PLAY_NEW_GAME_BUT")
                        mHandler.obtainMessage(Constants.TWO_PLAY_NEW_GAME_BUT)
                    }
                    Constants.TWO_PLAY_CL_GAME_TIMER_READ -> {
                        Log.d(TAG, "run().TWO_PLAY_CL_GAME_TIMER_READ")
                        data.putString("TimerRemaining", mBuffer)
                        mHandler.obtainMessage(Constants.TWO_PLAY_CL_GAME_TIMER_READ)
                    }
                    Constants.TWO_PLAY_CL_GAME_G_HOG_READ -> {
                        Log.d(TAG, "run().TWO_PLAY_CL_GAME_G_HOG_READ")
                        data.putString("GroundhogData", mBuffer)
                        mHandler.obtainMessage(Constants.TWO_PLAY_CL_GAME_G_HOG_READ)
                    }
                    Constants.TWO_PLAY_GAME_G_HOG_HIT -> {
                        Log.d(TAG, "run().TWO_PLAY_GAME_G_HOG_HIT")
                        data.putString("GroundhogHitData", mBuffer)
                        mHandler.obtainMessage(Constants.TWO_PLAY_GAME_G_HOG_HIT)
                    }
                    Constants.TWO_PLAY_GAME_SCORE_RECEIVED -> {
                        Log.d(TAG, "run().TWO_PLAY_GAME_SCORE_RECEIVED")
                        data.putString("OppositeCurrentScore", mBuffer)
                        mHandler.obtainMessage(Constants.TWO_PLAY_GAME_SCORE_RECEIVED)
                    }
                    else -> {
                        Log.d(TAG, "run().default")
                        mHandler.obtainMessage(Constants.TWO_PLAY_DEF_READ)
                    }
                }

                // ✅ Set startRead = false under the correct lock before dispatching
                synchronized(startReadLock) {
                    startRead = false
                }
                readMsg.data = data
                readMsg.sendToTarget()
            } catch (ex: Exception) {
                Log.e(TAG, "run().Exception.", ex)
                break
            }
        }
    }

    override fun closeIoSocket() {
        // Shut down the write executor first so no new writes are queued after close.
        writeExecutor.shutdownNow()
        try {
            mSocket.close()
        } catch (ex: Exception) {
            Log.e(TAG, "closeIoSocket.Exception: ", ex)
        }
    }
}
