package com.smile.groundhoghunter.Threads;

import android.util.Log;

import com.smile.groundhoghunter.AbstractClasses.IoFunctionThread;
import com.smile.groundhoghunter.Constants.CommonConstants;
import com.smile.groundhoghunter.GameView;
import com.smile.groundhoghunter.GroundhogActivity;
import com.smile.groundhoghunter.Models.Groundhog;

import java.util.Random;

public class GroundhogRandomThread extends Thread {

    private static final String TAG = new String("Threads.GroundhogRandomThread");
    private final GameView gameView;
    private final int gameType;
    private final IoFunctionThread selectedIoFunctionThread;
    private final int synchronizeTime;
    private final int chanceToShow;
    private final Random groundhogRandom;

    private boolean keepRunning;

    public GroundhogRandomThread(GameView gView) {
        gameView = gView;
        gameType = gameView.getGameType();
        selectedIoFunctionThread = gameView.getIoFunctionThread();
        synchronizeTime = GameView.TimeIntervalShown;       // 500 mini seconds (1 second)
        chanceToShow = 10;   // 2-->1/2, 3-->1/3, 4-->1/4, 5-->1/5, 6-->1/6, .. 10--> 1/10;
        groundhogRandom = new Random(System.currentTimeMillis());

        keepRunning = true; // keepRunning = true -> loop in run() still going
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

            switch (gameType) {
                case CommonConstants.GameBySinglePlayer:
                    setGroundhogArray();
                    break;
                case CommonConstants.TwoPlayerGameByHost:
                    String writeString = setGroundhogArray();
                    selectedIoFunctionThread.write(CommonConstants.TwoPlayerClientGameGroundhogRead, writeString);
                    break;
                case CommonConstants.TwoPlayerGameByClient:
                    // only read data from Host game
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
