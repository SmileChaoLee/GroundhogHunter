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

import com.smile.groundhoghunter.constants.Constants;
import com.smile.groundhoghunter.models.Groundhog;
import com.smile.groundhoghunter.threads.GameTimerThread;

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
        if (selectedIoFuncThread != null) {
            selectedIoFuncThread.setHandler(clientGameHandler);
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
        if (gameView == null) return;
        if (selectedIoFuncThread == null) return;
        gameView.pauseGame();
        pauseGameButton.setEnabled(false);
        pauseGameButton.setVisibility(View.INVISIBLE);
        resumeGameButton.setEnabled(true);
        resumeGameButton.setVisibility(View.VISIBLE);
        selectedIoFuncThread.write(Constants.TWO_PLAY_PAU_GAME_BUT, "");
    }

    @Override
    protected void resumeGame() {
        if (gameView == null) return;
        if (selectedIoFuncThread == null) return;
        gameView.resumeGame();
        pauseGameButton.setEnabled(true);
        pauseGameButton.setVisibility(View.VISIBLE);
        resumeGameButton.setEnabled(false);
        resumeGameButton.setVisibility(View.INVISIBLE);
        selectedIoFuncThread.write(Constants.TWO_PLAY_RES_GAME_BUT, "");
    }

    @Override
    protected void quitGame() {
        super.quitGame();
        if (gameView == null) return;
        if (selectedIoFuncThread == null) return;
        selectedIoFuncThread.write(Constants.TWO_PLAY_OPPOS_LF_GAME, "");
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
            Log.d(TAG, "Message received: " + msg.what);
            if (gameView == null) return;
            if (selectedIoFuncThread == null) return;
            String msgString;
            Bundle data = msg.getData();
            int i;
            int status;
            int hideByte;
            int numOfTimeIntervalShown;
            switch (msg.what) {
                case Constants.TWO_PLAY_OPPOS_LF_GAME:
                    // received by host and client sides
                    msgString = mContext.getString(R.string.oppositePlayerLeftGameString);
                    Toast.makeText(mContext, msgString, Toast.LENGTH_SHORT).show();
                    gameView.setOpposPlayerLeft(true);
                    selectedIoFuncThread.setStartRead(true);    // start reading data
                    break;
                case Constants.TWO_PLAY_NEW_GAME_BUT:
                    // received by client side
                    gameView.newGame(); // new game on client side
                    pauseGameButton.setEnabled(false);
                    pauseGameButton.setVisibility(View.INVISIBLE);
                    resumeGameButton.setEnabled(false);
                    resumeGameButton.setVisibility(View.INVISIBLE);
                    selectedIoFuncThread.setStartRead(true);    // start reading data
                    break;
                case Constants.TWO_PLAY_ST_GAME_BUT:
                    // received by client side
                    gameView.startGame();   // start game on client side
                    pauseGameButton.setEnabled(true);
                    pauseGameButton.setVisibility(View.VISIBLE);
                    resumeGameButton.setEnabled(false);
                    resumeGameButton.setVisibility(View.INVISIBLE);
                    selectedIoFuncThread.setStartRead(true);    // start reading data
                    break;
                case Constants.TWO_PLAY_PAU_GAME_BUT:
                    // received by host and client sides
                    gameView.pauseGame();
                    pauseGameButton.setEnabled(false);
                    pauseGameButton.setVisibility(View.INVISIBLE);
                    resumeGameButton.setEnabled(true);
                    resumeGameButton.setVisibility(View.VISIBLE);
                    selectedIoFuncThread.setStartRead(true);    // start reading data
                    break;
                case Constants.TWO_PLAY_RES_GAME_BUT:
                    // received by host and client sides
                    gameView.resumeGame();
                    resumeGameButton.setEnabled(false);
                    resumeGameButton.setVisibility(View.INVISIBLE);
                    pauseGameButton.setEnabled(true);
                    pauseGameButton.setVisibility(View.VISIBLE);
                    selectedIoFuncThread.setStartRead(true);    // start reading data
                    break;
                case Constants.TWO_PLAY_CL_GAME_TIMER_READ:
                    msgString = data.getString("TimerRemaining", "");
                    int timeRemaining = Integer.parseInt(msgString);
                    GameTimerThread gameTimerThread = gameView.getGTimerTh();
                    if (gameTimerThread != null) {
                        gameTimerThread.setTimeRemaining(timeRemaining);
                    }
                    selectedIoFuncThread.setStartRead(true);
                    break;
                case Constants.TWO_PLAY_CL_GAME_G_HOG_READ:
                    msgString = data.getString("GroundhogData", "");
                    i = 0;
                    for (Groundhog groundhog : gameView.getGHogArray()) {
                        status = Integer.parseInt(msgString.substring(i, i+1));
                        groundhog.setStatus(status);

                        hideByte = Integer.parseInt(msgString.substring(i+1, i+2));
                        groundhog.setIsHiding(hideByte == 1);

                        numOfTimeIntervalShown = Integer.parseInt(msgString.substring(i+2, i+4));
                        groundhog.setNumOfTimeIntervalShown(numOfTimeIntervalShown);

                        i += 4;
                    }
                    selectedIoFuncThread.setStartRead(true);
                    break;
                case Constants.TWO_PLAY_GAME_G_HOG_HIT:
                    msgString = data.getString("GroundhogHitData", "");
                    gameView.setGroundhogByMsgString(msgString);
                    selectedIoFuncThread.setStartRead(true);
                    break;
                case Constants.TWO_PLAY_GAME_SCORE_RECEIVED:
                    msgString = data.getString("OppositeCurrentScore", "0");
                    int oppScore = Integer.parseInt(msgString.substring(0, 4));
                    gameView.setOpposCurrentScore(oppScore);
                    int oppNumOfHits = Integer.parseInt(msgString.substring(4, 8));
                    gameView.setOpposNumOfHits(oppNumOfHits);
                    gameView.setReceivedScoreFromOppos(true);
                    selectedIoFuncThread.setStartRead(true);    // start reading data
                    break;
                case Constants.TWO_PLAY_DEF_READ:
                    // wrong or read error
                    selectedIoFuncThread.setStartRead(true);    // start reading data
                    break;
            }
        }
    }
}
