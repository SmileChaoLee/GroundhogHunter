package com.smile.groundhoghunter;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import com.smile.groundhoghunter.GameView;
import com.smile.groundhoghunter.MainActivity;

public class GameViewDrawThread extends Thread {

    private MainActivity mainActivity = null;
    private GameView gameView = null;
    private boolean keepRunning;
    private int synchronizeTime;

    public GameViewDrawThread(GameView gView) {
        this.gameView = gView;
        this.mainActivity = gView.mainActivity;
        keepRunning = true; // keepRunning = true -> loop in run() still going
        synchronizeTime = 70;   // 70 mini seconds
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
