package com.smile.groundhoghunter.threads;

import com.smile.groundhoghunter.GameView;
import com.smile.groundhoghunter.GroundhogActivity;

public class GameViewDrawThread extends Thread {

    private GameView gameView;
    private boolean keepRunning;
    private int synchronizeTime = GameView.DRAWING_INTERVAL;

    public GameViewDrawThread(GameView gView) {
        this.gameView = gView;
        keepRunning = true; // keepRunning = true -> loop in run() still going
    }

    public void run() {
        while (keepRunning) {
            synchronized (GroundhogActivity.activityLocker) {
                // for application's (Main activity) synchronizing
                while (GroundhogActivity.GamePause) {
                    try {
                        GroundhogActivity.activityLocker.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            synchronized (GameView.gViewLocker) {
                // for GameView's synchronizing
                while (GameView.gViewPause) {
                    try {
                        GameView.gViewLocker.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            // start drawing
            gameView.drawGameScreen();

            try{Thread.sleep(synchronizeTime);}
            catch(Exception e){e.printStackTrace();}
        }
    }

    public void setKeepRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }
}
