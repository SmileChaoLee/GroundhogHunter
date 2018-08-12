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
            int hiding;
            int status;
            for (Groundhog groundhog : gameView.groundhogList) {
                if (groundhog.getIsHiding()) {
                    // if hiding
                    hiding = groundhogRandom.nextInt(2);   // 0 or 1
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

                    // simulating hitting
                    int isHit = groundhogRandom.nextInt(4);   // 0, 1, 2, 3
                    if (isHit == 0) {
                        // hit
                        groundhog.setIsHit(true);
                    }
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
