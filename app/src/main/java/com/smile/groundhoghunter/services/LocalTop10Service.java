package com.smile.groundhoghunter.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.smile.groundhoghunter.constants.Constants;
import com.smile.smilelibraries.models.Player;
import com.smile.smilelibraries.player_record_rest.httpUrl.PlayerRecordRest;
import com.smile.smilelibraries.scoresqlite.ScoreSQLite;

import java.util.ArrayList;

public class LocalTop10Service extends IntentService {
    public final static String TAG = "LocalTop10Service";
    public final static String Action_Name = "LocalTop10Service";
    public LocalTop10Service() {
        super(Action_Name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");

        ArrayList<String> playerNames = new ArrayList<>();
        ArrayList<Integer> playerScores = new ArrayList<>();

        ScoreSQLite scoreDB = new ScoreSQLite(this, Constants.DATABASE_NAME);
        ArrayList<Player> players = PlayerRecordRest.GetLocalTop10(scoreDB);
        scoreDB.close();

        if (players.isEmpty()) {
            players = new ArrayList<>();
        }
        for (Player p : players) {
            playerNames.add(p.getPlayerName());
            playerScores.add(p.getScore());
        }
        Intent notificationIntent = new Intent(Action_Name);
        Bundle extras = new Bundle();
        extras.putStringArrayList("PlayerNames", playerNames);
        extras.putIntegerArrayList("PlayerScores", playerScores);
        notificationIntent.putExtras(extras);
        LocalBroadcastManager localBroadcastManager =
                LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastManager.sendBroadcast(notificationIntent);
        Log.d(TAG, "result sent");
    }
}
