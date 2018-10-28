package com.smile.groundhoghunter.Service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Pair;
import com.smile.groundhoghunter.GroundhogHunterApp;

import java.util.ArrayList;

public class LocalTop10IntentService extends IntentService {
    public final static String Action_Name = "LocalTop10IntentService";

    public LocalTop10IntentService() {
        super(Action_Name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        System.out.println("LocalTop10IntentService --> onHandleIntent() is called.");

        ArrayList<Pair<String, Integer>> resultList = GroundhogHunterApp.ScoreSQLiteDB.readTop10ScoreList();
        ArrayList<String> playerNames = new ArrayList<>();
        ArrayList<Integer> playerScores = new ArrayList<>();

        for (Pair pair : resultList) {
            playerNames.add((String)pair.first);
            playerScores.add((Integer)pair.second);
        }
        // wait for 3 seconds
        try { Thread.sleep(3000); } catch (InterruptedException ex) { ex.printStackTrace(); }

        Intent notificationIntent = new Intent(Action_Name);
        Bundle extras = new Bundle();
        extras.putStringArrayList("PlayerNames", playerNames);
        extras.putIntegerArrayList("PlayerScores", playerScores);
        notificationIntent.putExtras(extras);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastManager.sendBroadcast(notificationIntent);
        System.out.println("LocalTop10IntentService sent result");
    }
}
