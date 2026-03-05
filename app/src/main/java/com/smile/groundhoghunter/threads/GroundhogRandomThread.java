package com.smile.groundhoghunter.threads;

import com.smile.groundhoghunter.abstract_threads.IoFunctionThread;
import com.smile.groundhoghunter.constants.Constants;
import com.smile.groundhoghunter.GameView;
import com.smile.groundhoghunter.GroundhogActivity;
import com.smile.groundhoghunter.models.Groundhog;

import java.util.Random;

public class GroundhogRandomThread extends Thread {

    private final GameView gameView;
    private final int gameType;
    private final IoFunctionThread ioFuncThread;
    private final int synchronizeTime;
    private final int chanceToShow;
    private final Random groundhogRandom;

    private boolean keepRunning;

    public GroundhogRandomThread(GameView gView) {
        gameView = gView;
        gameType = gameView.getGType();
        ioFuncThread = gameView.getSelectedIoFuncTh();
        synchronizeTime = GameView.TIMER_INTERVAL_SHOWN;       // 500 mini seconds (1 second)
        chanceToShow = 18;   // probability is 1/18
        groundhogRandom = new Random(System.currentTimeMillis());
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

            String writeString;
            switch (gameType) {
                case Constants.GAME_BY_SINGLE_PLAY:
                    writeString = setGroundhogArray();
                    break;
                case Constants.TWO_PLAY_GAME_BY_HOST:
                    writeString = setGroundhogArray();
                    if (ioFuncThread != null) {
                        ioFuncThread.write(Constants.TWO_PLAY_CL_GAME_G_HOG_READ, writeString);
                    }
                    break;
                case Constants.TWO_PLAY_GAME_BY_CLIENT:
                    // only read data from Host game
                    boolean isOppositePlayerLeft = gameView.isOpposPlayerLeft();
                    if (isOppositePlayerLeft) {
                        writeString = setGroundhogArray();
                    }
                    break;
            }

            try{Thread.sleep(synchronizeTime);}
            catch(Exception e){e.printStackTrace();}
        }
    }

    public void setKeepRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }

    // random the jump of all groundhogs in groundhogArray
    private String setGroundhogArray() {

        // random the jump of all groundhogs in groundhogList
        String writeString = "";

        int hiding;
        int status;
        for (Groundhog groundhog : gameView.getGHogArray()) {
            if (groundhog.getIsHiding()) {
                // if hiding
                hiding = groundhogRandom.nextInt(chanceToShow);
                if (hiding == 0) {
                    // showing
                    // original status is hiding then showing with an image that might be different from previous one
                    status = groundhogRandom.nextInt(GameView.NUM_G_HOG_TYPES);    // 0 - 3
                    groundhog.setStatus(status);
                    groundhog.setIsHiding(false);
                    groundhog.setNumOfTimeIntervalShown(0);     // status of starting showing
                }
            } else {
                // not hiding
                groundhog.setNumOfTimeIntervalShown(groundhog.getNumOfTimeIntervalShown() + 1);
            }

            // calculate the output string to client
            String temp = String.format("%01d", groundhog.getStatus());
            writeString += temp;
            if (groundhog.getIsHiding()) {
                writeString += "1";
            } else {
                writeString += "0";
            }
            temp = String.format("%02d",groundhog.getNumOfTimeIntervalShown());
            writeString += temp;
        }

        return writeString;
    }
}
