package com.smile.groundhoghunter.Threads;

import com.smile.groundhoghunter.AbstractClasses.IoFunctionThread;
import com.smile.groundhoghunter.Constants.CommonConstants;
import com.smile.groundhoghunter.GameView;
import com.smile.groundhoghunter.GroundhogActivity;

public class GameTimerThread extends Thread {

    private final static String TAG = "Threads.GameTimerThread";
    private final GameView gameView;
    private final int gameType;
    private final IoFunctionThread selectedIoFunctionThread;
    private final int synchronizeTime = 1000; // one second

    private boolean keepRunning;
    private int timeRemaining;

    public GameTimerThread(GameView gView) {
        gameView = gView;
        gameType = gameView.getGameType();
        selectedIoFunctionThread = gameView.getIoFunctionThread();
        keepRunning = true;
        timeRemaining = GameView.TimerInterval;
    }

    public void run() {
        while ((timeRemaining > 0) && (keepRunning)) {
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

            try {
                Thread.sleep(synchronizeTime);
            } catch (Exception e) {
                e.printStackTrace();
            }

            switch (gameType) {
                case CommonConstants.GameBySinglePlayer:
                    --timeRemaining;
                    break;
                case CommonConstants.TwoPlayerGameByHost:
                    --timeRemaining;
                    selectedIoFunctionThread.write(CommonConstants.TwoPlayerClientGameTimerRead, "" + timeRemaining);
                    break;
                case CommonConstants.TwoPlayerGameByClient:
                    // only read timeRemaining from Host game
                    break;
            }

        }
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(int timeRemaining) {
        this.timeRemaining = timeRemaining;
    }

    public void setKeepRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }
}
