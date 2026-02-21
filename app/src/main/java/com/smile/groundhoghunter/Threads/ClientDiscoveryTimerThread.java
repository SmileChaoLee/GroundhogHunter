package com.smile.groundhoghunter.Threads;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.smile.groundhoghunter.constants.CommonConstants;

public class ClientDiscoveryTimerThread extends Thread {

    private static final String TAG = "CD_TimerThread";
    private final Handler mHandler;
    private final int mTimerPeriod;
    private boolean keepRunning;

    public ClientDiscoveryTimerThread(Handler handler, int timerPeriod) {
        mHandler = handler;
        mTimerPeriod = timerPeriod;
        keepRunning = true;
    }

    public void run() {
        Message msg;
        int elapsedTime= 0;
        while ( (elapsedTime < mTimerPeriod) && keepRunning) {
            try {
                // 300 ms
                int mTimeEachLopp = 300;
                Thread.sleep(mTimeEachLopp);
                elapsedTime += mTimeEachLopp;
            } catch (InterruptedException ex) {
                Log.e(TAG, "run.Exception: ", ex);
            }
        }

        if (keepRunning) {
            // send message to activity to cancel discovery
            msg = mHandler.obtainMessage(CommonConstants.ClientDiscoveryTimerHasReached);
            msg.sendToTarget();
        } else {
            msg = mHandler.obtainMessage(CommonConstants.ClientDiscoveryTimerHasBeenDismissed);
            msg.sendToTarget();
        }
    }

    public void dismissTimerThread() {
        keepRunning = false;
    }
}
