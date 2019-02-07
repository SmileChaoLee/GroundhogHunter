package com.smile.groundhoghunter;

import com.smile.groundhoghunter.Model.Groundhog;

import java.util.Random;

public class GroundhogRandomThread extends Thread {

    private GameView gameView;
    private boolean keepRunning;
    private int synchronizeTime;
    private int chanceToShow;
    private Random groundhogRandom;

    public GroundhogRandomThread(GameView gView) {
        this.gameView = gView;
        keepRunning = true; // keepRunning = true -> loop in run() still going
        synchronizeTime = GameView.TimeIntervalShown;       // 500 mini seconds (1 second)
        chanceToShow = 10;   // 2-->1/2, 3-->1/3, 4-->1/4, 5-->1/5, 6-->1/6, .. 10--> 1/10;
        groundhogRandom = new Random(System.currentTimeMillis());
    }

    public void run() {
        while (keepRunning) {
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

            // random the jump of all groundhogs in groundhogList
            int hiding;
            int status;
            for (Groundhog groundhog : gameView.groundhogArray) {
                if (groundhog.getIsHiding()) {
                    // if hiding
                    hiding = groundhogRandom.nextInt(chanceToShow);
                    if (hiding == 0) {
                        // showing
                        // original status is hiding then showing with an image that might be different from previous one
                        status = groundhogRandom.nextInt(GameView.NumberOfGroundhogTypes);    // 0 - 3
                        groundhog.setStatus(status);
                        groundhog.setIsHiding(false);
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
