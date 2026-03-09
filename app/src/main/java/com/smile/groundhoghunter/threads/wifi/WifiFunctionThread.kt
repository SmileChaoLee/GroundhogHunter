package com.smile.groundhoghunter.threads.wifi

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import com.smile.groundhoghunter.abstract_threads.IoFunctionThread
import com.smile.groundhoghunter.constants.Constants
import com.smile.groundhoghunter.models.WifiConnectDevice
import java.net.Socket

class WifiFunctionThread(handler: Handler, private val mSocket: Socket) : IoFunctionThread(handler) {

    private val ioFunctionThread: IoFunctionThread = thisThread

    companion object {
        private const val TAG = "WifiFunctionThread"
    }

    init {
        try {
            inputStream = mSocket.getInputStream()
        } catch (ex: Exception) {
            Log.d(TAG, "Failed to getInputStream().", ex)
        }
        try {
            outputStream = mSocket.getOutputStream()
        } catch (ex: Exception) {
            Log.d(TAG, "Failed to getOutputStream().", ex)
        }
        keepRunning = true
        synchronized(ioFunctionThread) {
            startRead = false
        }
    }

    override fun run() {
        if (inputStream == null || outputStream == null) {
            return
        }
        val data = Bundle()
        val wifiConnectDevice = WifiConnectDevice(mSocket.inetAddress, mSocket.port)
        data.putParcelable("ConnectDevice", wifiConnectDevice)

        while (keepRunning) {
            synchronized(ioFunctionThread) {
                while (!startRead) {
                    try {
                        Log.d(TAG, "run().Waiting for notification to read data.")
                        (ioFunctionThread as Object).wait()
                    } catch (ex: InterruptedException) {
                        ex.printStackTrace()
                    }
                }
            }
            try {
                Log.d(TAG, "run().start reading")
                val byteHead = inputStream.read()
                val dataLength = inputStream.read()
                val sb = StringBuilder()
                var readBuff: Int
                var byteRead = 0
                while (byteRead <= dataLength) {
                    readBuff = inputStream.read()
                    if (readBuff == -1 || readBuff == '\n'.code) break
                    sb.append(readBuff.toChar())
                    byteRead++
                }
                mBuffer = sb.toString()
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
                synchronized(ioFunctionThread) {
                    startRead = false
                }
                readMsg.data = data
                readMsg.sendToTarget()
                Log.d(TAG, "run().byteHead = $byteHead")
                Log.d(TAG, "run().mBuffer = $mBuffer")
            } catch (ex: Exception) {
                Log.d(TAG, "run().Exception.", ex)
                break
            }
        }
    }

    override fun closeIoSocket() {
        try {
            mSocket.close()
        } catch (ex: Exception) {
            Log.d(TAG, "closeIoSocket.Could not close Socket.")
            ex.printStackTrace()
        }
    }
}
