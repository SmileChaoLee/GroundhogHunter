package com.smile.groundhoghunter;

public class GameViewDrawThread extends Thread {

    private GameView gameView;
    private boolean keepRunning;
    private int synchronizeTime = GameView.DrawingInterval;

    public GameViewDrawThread(GameView gView) {
        this.gameView = gView;
        keepRunning = true; // keepRunning = true -> loop in run() still going
    }

    public void run() {
        while (keepRunning) {
            synchronized (MainActivity.ActivityHandler) {
                // for application's (Main activity) synchronizing
                while (MainActivity.GamePause) {
                    try {
                        MainActivity.ActivityHandler.wait();
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
