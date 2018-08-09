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
        synchronizeTime = GameView.TimeIntervalShown;       // 5000 mini seconds (1 second)
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
            int hide;
            int status;
            for (Groundhog groundhog : gameView.groundhogList) {
                if (groundhog.getIsHit()) {
                    // if hiding
                    hide = groundhogRandom.nextInt(2);   // 0 or 1
                    if (hide == 0) {
                        // showing
                        // original status is hiding then showing with an image that might be different from previous one
                        status = groundhogRandom.nextInt(GameView.NumberOfGroundhogTypes);    // 0 - 3
                        groundhog.setStatus(status);
                        groundhog.setIsHit(false);
                        groundhog.setNumOfTimeIntervalShown(0);     // status of starting showing
                    }
                } else {
                    // not hiding
                    groundhog.setNumOfTimeIntervalShown(groundhog.getNumOfTimeIntervalShown() + 1);
                }
            }

            try{Thread.sleep(synchronizeTime);}
            catch(Exception e){e.printStackTrace();}
        }
    }

    public void setKeepRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }
}
