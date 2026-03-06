package com.smile.groundhoghunter.threads;

import com.smile.groundhoghunter.abstract_threads.IoFunctionThread;
import com.smile.groundhoghunter.constants.Constants;
import com.smile.groundhoghunter.GameView;
import com.smile.groundhoghunter.GroundhogActivity;

public class GameTimerThread extends Thread {

    private final static String TAG = "Threads.GameTimerThread";
    private final GameView gameView;
    private final int gameType;
    private final IoFunctionThread ioFuncThread;

    private boolean keepRunning;
    private int timeRemaining;

    public GameTimerThread(GameView gView) {
        gameView = gView;
        gameType = gameView.getGType();
        ioFuncThread = gameView.getIoFuncThread();
        keepRunning = true;
        timeRemaining = GameView.TIMER_INTERVAL;
    }

    public void run() {
        while ((timeRemaining > 0) && (keepRunning)) {
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

            try {
                // one second
                int synchronizeTime = 1000;
                Thread.sleep(synchronizeTime);
            } catch (Exception e) {
                e.printStackTrace();
            }

            switch (gameType) {
                case Constants.GAME_BY_SINGLE_PLAY:
                    --timeRemaining;
                    break;
                case Constants.TWO_PLAY_GAME_BY_HOST:
                    --timeRemaining;
                    if (ioFuncThread != null) {
                        ioFuncThread.write(Constants.TWO_PLAY_CL_GAME_TIMER_READ, "" + timeRemaining);
                    }
                    break;
                case Constants.TWO_PLAY_GAME_BY_CLIENT:
                    // only read timeRemaining from Host game
                    boolean isOppositePlayerLeft = gameView.isOpposPlayerLeft();
                    if (isOppositePlayerLeft) {
                        --timeRemaining;
                    }
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
