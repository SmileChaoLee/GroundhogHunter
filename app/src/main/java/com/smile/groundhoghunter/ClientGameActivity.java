package com.smile.groundhoghunter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.smile.groundhoghunter.constants.CommonConstants;
import com.smile.groundhoghunter.Models.Groundhog;
import com.smile.groundhoghunter.Threads.GameTimerThread;

public class ClientGameActivity extends GroundhogActivity {

    private final static String TAG = "ClientGameAct";
    private ClientGameHandler clientGameHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        startGameButton.setVisibility(View.INVISIBLE);
        startGameButton.setEnabled(false);
        newGameButton.setVisibility(View.INVISIBLE);
        newGameButton.setEnabled(false);

        clientGameHandler = new ClientGameHandler(Looper.getMainLooper(), this);
        if (selectedIoFunctionThread != null) {
            selectedIoFunctionThread.setHandler(clientGameHandler);
        } else {
            // selectedIoFunctionThread is null then return to previous
            finish();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (clientGameHandler != null) {
            clientGameHandler.removeCallbacksAndMessages(null);
            clientGameHandler = null;
        }
    }

    @Override
    protected void pauseGame() {
        gameView.pauseGame();
        pauseGameButton.setEnabled(false);
        pauseGameButton.setVisibility(View.INVISIBLE);
        resumeGameButton.setEnabled(true);
        resumeGameButton.setVisibility(View.VISIBLE);
        selectedIoFunctionThread.write(CommonConstants.TwoPlayerPauseGameButton, "");
    }

    @Override
    protected void resumeGame() {
        gameView.resumeGame();
        pauseGameButton.setEnabled(true);
        pauseGameButton.setVisibility(View.VISIBLE);
        resumeGameButton.setEnabled(false);
        resumeGameButton.setVisibility(View.INVISIBLE);
        selectedIoFunctionThread.write(CommonConstants.TwoPlayerResumeGameButton, "");
    }

    @Override
    protected void quitGame() {
        super.quitGame();
        selectedIoFunctionThread.write(CommonConstants.TwoPlayerOppositeLeftGame, "");
    }

    protected class ClientGameHandler extends Handler {

        protected final Looper mLooper;
        protected final Context mContext;

        public ClientGameHandler(Looper looper, Context context) {
            super(looper);
            mLooper = looper;
            mContext = context;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            String msgString;
            Bundle data = msg.getData();
            int i;
            int status;
            int hideByte;
            int numOfTimeIntervalShown;

            Log.d(TAG, "Message received: " + msg.what);
            switch (msg.what) {
                case CommonConstants.TwoPlayerOppositeLeftGame:
                    // received by host and client sides
                    msgString = mContext.getString(R.string.oppositePlayerLeftGameString);
                    Toast.makeText(mContext, msgString, Toast.LENGTH_SHORT).show();
                    gameView.setOppositePlayerLeft(true);
                    selectedIoFunctionThread.setStartRead(true);    // start reading data
                    break;
                case CommonConstants.TwoPlayerNewGameButton:
                    // received by client side
                    gameView.newGame(); // new game on client side
                    pauseGameButton.setEnabled(false);
                    pauseGameButton.setVisibility(View.INVISIBLE);
                    resumeGameButton.setEnabled(false);
                    resumeGameButton.setVisibility(View.INVISIBLE);
                    selectedIoFunctionThread.setStartRead(true);    // start reading data
                    break;
                case CommonConstants.TwoPlayerStartGameButton:
                    // received by client side
                    gameView.startGame();   // start game on client side
                    pauseGameButton.setEnabled(true);
                    pauseGameButton.setVisibility(View.VISIBLE);
                    resumeGameButton.setEnabled(false);
                    resumeGameButton.setVisibility(View.INVISIBLE);
                    selectedIoFunctionThread.setStartRead(true);    // start reading data
                    break;
                case CommonConstants.TwoPlayerPauseGameButton:
                    // received by host and client sides
                    gameView.pauseGame();
                    pauseGameButton.setEnabled(false);
                    pauseGameButton.setVisibility(View.INVISIBLE);
                    resumeGameButton.setEnabled(true);
                    resumeGameButton.setVisibility(View.VISIBLE);
                    selectedIoFunctionThread.setStartRead(true);    // start reading data
                    break;
                case CommonConstants.TwoPlayerResumeGameButton:
                    // received by host and client sides
                    gameView.resumeGame();
                    resumeGameButton.setEnabled(false);
                    resumeGameButton.setVisibility(View.INVISIBLE);
                    pauseGameButton.setEnabled(true);
                    pauseGameButton.setVisibility(View.VISIBLE);
                    selectedIoFunctionThread.setStartRead(true);    // start reading data
                    break;
                case CommonConstants.TwoPlayerClientGameTimerRead:
                    msgString = data.getString("TimerRemaining", "");
                    int timeRemaining = Integer.parseInt(msgString);
                    GameTimerThread gameTimerThread = gameView.getGameTimerThread();
                    if (gameTimerThread != null) {
                        gameTimerThread.setTimeRemaining(timeRemaining);
                    }
                    selectedIoFunctionThread.setStartRead(true);
                    break;
                case CommonConstants.TwoPlayerClientGameGroundhogRead:
                    msgString = data.getString("GroundhogData", "");
                    i = 0;
                    for (Groundhog groundhog : gameView.groundhogArray) {
                        status = Integer.parseInt(msgString.substring(i, i+1));
                        groundhog.setStatus(status);

                        hideByte = Integer.parseInt(msgString.substring(i+1, i+2));
                        groundhog.setIsHiding(hideByte == 1);

                        numOfTimeIntervalShown = Integer.parseInt(msgString.substring(i+2, i+4));
                        groundhog.setNumOfTimeIntervalShown(numOfTimeIntervalShown);

                        i += 4;
                    }
                    selectedIoFunctionThread.setStartRead(true);
                    break;
                case CommonConstants.TwoPlayerGameGroundhogHit:
                    msgString = data.getString("GroundhogHitData");
                    gameView.setGroundhogByMsgString(msgString);
                    selectedIoFunctionThread.setStartRead(true);
                    break;
                case CommonConstants.TwoPlayerGameScoreReceived:
                    msgString = data.getString("OppositeCurrentScore", "0");
                    int oppScore = Integer.parseInt(msgString.substring(0, 4));
                    gameView.setOppositeCurrentScore(oppScore);
                    int oppNumOfHits = Integer.parseInt(msgString.substring(4, 8));
                    gameView.setOppositeNumOfHits(oppNumOfHits);
                    gameView.setReceivedScoreFromOpposite(true);
                    selectedIoFunctionThread.setStartRead(true);    // start reading data
                    break;
                case CommonConstants.TwoPlayerDefaultReading:
                    // wrong or read error
                    selectedIoFunctionThread.setStartRead(true);    // start reading data
                    break;
            }
        }
    }
}
