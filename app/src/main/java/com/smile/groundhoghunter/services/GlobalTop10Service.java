package com.smile.groundhoghunter.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.smile.groundhoghunter.constants.Constants;
import com.smile.smilelibraries.models.Player;
import com.smile.smilelibraries.player_record_rest.httpUrl.PlayerRecordRest;

import java.util.ArrayList;

public class GlobalTop10Service extends IntentService {

    public final static String TAG = "GlobalTop10Service";
    public final static String Action_Name = "GlobalTop10Service";

    public GlobalTop10Service() {
        super(Action_Name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");

        ArrayList<String> playerNames = new ArrayList<>();
        ArrayList<Integer> playerScores = new ArrayList<>();

        ArrayList<Player> players = PlayerRecordRest.GetGlobalTop10(Constants.GROUNDHOG_GAME_ID);
        if (players.isEmpty()) {
            players = new ArrayList<>();
        }
        for (Player p : players) {
            playerNames.add(p.getPlayerName());
            playerScores.add(p.getScore());
        }

        Intent notificationIntent = new Intent(Action_Name);
        Bundle notificationExtras = new Bundle();
        notificationExtras.putStringArrayList("PlayerNames", playerNames);
        notificationExtras.putIntegerArrayList("PlayerScores", playerScores);
        notificationIntent.putExtras(notificationExtras);
        LocalBroadcastManager localBroadcastManager =
                LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastManager.sendBroadcast(notificationIntent);
        Log.d(TAG, "Result sent");
    }
}
