package com.smile.groundhoghunter;

import com.smile.groundhoghunter.Model.Groundhog;

import java.util.Random;

public class GroundhogRandomThread extends Thread {

    private GameView gameView;
    private MainActivity mainActivity;
    private boolean keepRunning;
    private int synchronizeTime;
    private Random groundhogRandom;

    public GroundhogRandomThread(GameView gView) {
        this.gameView = gView;
        this.mainActivity = gView.mainActivity;
        keepRunning = true; // keepRunning = true -> loop in run() still going
        synchronizeTime = 1000;   // 1000 mini seconds (1 second)
        groundhogRandom = new Random(System.currentTimeMillis());
    }

    public void run() {
        while (keepRunning) {
            synchronized (mainActivity.activityHandler) {
                // for application's (Main activity) synchronizing
                while (mainActivity.gamePause) {
                    try {
                        mainActivity.activityHandler.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            synchronized (gameView.gameViewHandler) {
                // for GameView's synchronizing
                while (gameView.gameViewPause) {
                    try {
                        gameView.gameViewHandler.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            // random the jump of all groundhogs in groundhogList
            int status;
            for (Groundhog groundhog : gameView.groundhogList) {
                status = groundhogRandom.nextInt(GameView.numberOfGroundhogTypes);    // 0 - 4
                groundhog.setStatus(status);
            }

            try{Thread.sleep(synchronizeTime);}
            catch(Exception e){e.printStackTrace();}
        }
    }

    public void setKeepRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }
}
