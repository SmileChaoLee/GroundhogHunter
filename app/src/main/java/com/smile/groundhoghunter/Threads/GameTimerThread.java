package com.smile.groundhoghunter.Threads;

import com.smile.groundhoghunter.GameView;
import com.smile.groundhoghunter.GroundhogActivity;

public class GameTimerThread extends Thread {

    private GameView gameView;
    private boolean keepRunning;
    private int synchronizeTime = 1000; // one second
    private int timeRemaining;

    public GameTimerThread(GameView gView) {
        this.gameView = gView;
        keepRunning = true;
        timeRemaining = GameView.TimerInterval;
    }

    public void run() {
        while ( (timeRemaining>0) && (keepRunning) ) {
            synchronized (GroundhogActivity.ActivityHandler) {
                // for application's (Main activity) synchronizing
                while (GroundhogActivity.GamePause) {
                    try {
                        GroundhogActivity.ActivityHandler.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            synchronized (GameView.GameViewHandler) {
                // for GameView's synchronizing
                while (GameView.GameViewPause) {
                    try {
                        GameView.GameViewHandler.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            try{Thread.sleep(synchronizeTime);}
            catch(Exception e){e.printStackTrace();}

            --timeRemaining;
        }
    }

    public int getTimeRemaining() {
        return this.timeRemaining;
    }
    public void setKeepRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }
}
