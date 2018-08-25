package com.smile.groundhoghunter;

public class TimerThread extends Thread {

    private MainActivity mainActivity;
    private GameView gameView;
    private boolean keepRunning;
    private int synchronizeTime = 1000; // one second
    private int timeRemaining;

    public TimerThread(GameView gView) {
        this.gameView = gView;
        this.mainActivity = gView.mainActivity;
        keepRunning = true;
        timeRemaining = GameView.TimerInterval;
    }

    public void run() {
        while ( (timeRemaining>0) && (keepRunning) ) {
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
