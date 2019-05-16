package com.smile.groundhoghunter.Threads;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.smile.groundhoghunter.Constants.CommonConstants;

public class ClientDiscoveryTimerThread extends Thread {

    private final Handler mHandler;
    private final int mTimerPeriod;
    private final int mTimeEachLopp = 300;   // 300 ms
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
                Thread.sleep(mTimeEachLopp);
                elapsedTime += mTimeEachLopp;
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        Intent broadcastIntent = new Intent();
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
