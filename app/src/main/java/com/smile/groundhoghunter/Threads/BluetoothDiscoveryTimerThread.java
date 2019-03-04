package com.smile.groundhoghunter.Threads;

import android.content.Context;
import android.content.Intent;

public class BluetoothDiscoveryTimerThread extends Thread {

    public static final String TimerHasReached = ".Threads.BluetoothDiscoveryTimerThread.TimerHasReached";
    public static final String TimerHasBeenDismissed = ".Threads.BluetoothDiscoveryTimerThread.TimerHasBeenDismissed";
    private final Context mContext;
    private final int mTimerPeriod;
    private final int mTimeEachLopp = 300;   // 300 ms
    private boolean keepRunning;

    public BluetoothDiscoveryTimerThread(Context context, int timerPeriod) {
        mContext = context;
        mTimerPeriod = timerPeriod;
        keepRunning = true;
    }

    public void run() {

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
            broadcastIntent.setAction(TimerHasReached);
            mContext.sendBroadcast(broadcastIntent);
        } else {
            broadcastIntent.setAction(TimerHasBeenDismissed);
            mContext.sendBroadcast(broadcastIntent);
        }
    }

    public void dismissTimerThread() {
        keepRunning = false;
    }
}
