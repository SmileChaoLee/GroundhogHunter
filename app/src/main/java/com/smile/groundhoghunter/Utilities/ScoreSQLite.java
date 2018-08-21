package com.smile.groundhoghunter.Utilities;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

import java.util.ArrayList;

/**
 * Created by lee on 10/8/2014.
 */
public class ScoreSQLite extends SQLiteOpenHelper {

    private static final String _id = new String("id");
    private static final String playerName = new String("playerName");
    private static final String playerScore = new String("playerScore");

    private static final String dbName = new String("colorBallDatabase.db");
    private static final String tableName = new String("score");
    private static final String createTable = "create table if not exists " + tableName + " ("
            + _id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + playerName + " TEXT NOT NULL ,  " + playerScore + " INTEGER );";
    private static final String upDateTable = new String("update");

    private static final int dbVersion = 2;

    private Context myContext;
    private SQLiteDatabase scoreDatabase;

    public ScoreSQLite(Context context) {
        super(context, dbName,null,dbVersion);
        myContext = context;
        scoreDatabase = null;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database , int oldVersion , int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + tableName);
        onCreate(database);
    }

    private void openScoreDatabase() {

        try {
            scoreDatabase = getWritableDatabase();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public int readHighestScore() {
        int highestScore = 0;

        openScoreDatabase();
        if (scoreDatabase != null) {
            try {
                String sql = "select playerScore from " + tableName + " order by playerScore desc limit 1";
                Cursor cur = scoreDatabase.rawQuery(sql, new String[]{});
                if (cur.moveToFirst()) {
                    highestScore = cur.getInt(0);
                }
                cur.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            closeScoreDatabase();
        }

        return highestScore;
    }

    public ArrayList<Pair<String, Integer>> readTop10ScoreList() {

        ArrayList<Pair<String, Integer>> result = new ArrayList<>();

        openScoreDatabase();
        if (scoreDatabase != null) {
            String temp = new String("");
            int score = 0;
            // int i = 0;
            Pair<String, Integer> pair;

            Cursor cur = scoreDatabase.query(tableName, new String[] {"playerName","playerScore"}, null, null, null, null, "playerScore desc", "10");
            if (cur.moveToFirst()) {
                do {
                    temp = cur.getString(0);
                    score = cur.getInt(1);
                    pair = new Pair<>(temp, score);
                    result.add(pair);
                    // i++;
                // } while (cur.moveToNext() && (i<10));
                } while (cur.moveToNext());
            }

            closeScoreDatabase();
        }

        return result;
    }

    public ArrayList<Pair<String, Integer>> readAllScores() {

        ArrayList<Pair<String, Integer>> result = new ArrayList<>();

        openScoreDatabase();
        if (scoreDatabase != null) {
            // succeeded to open
            String temp = new String("");
            int score = 0;
            Pair<String, Integer> pair;
            try {
                Cursor cur = scoreDatabase.query(tableName, new String[] {"playerName", "playerScore"},null,null,null,null,null);
                if (cur.moveToFirst()) {
                    do {
                        temp = cur.getString(0);
                        score = cur.getInt(1);
                        pair = new Pair<>(temp, score);
                        result.add(pair);
                    } while (cur.moveToNext());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return result;
    }

    public boolean isInTop10(int score) {

        boolean yn = false;

        openScoreDatabase();
        if (scoreDatabase != null) {
            // succeeded to open database
            try {
                Cursor cur = scoreDatabase.query(tableName, new String[]{"playerScore"}, null, null, null, null, "playerScore desc", "10");
                if (cur.getCount() < 10) {
                    // less than 10 scores
                    yn = true;
                } else {
                    // has 10 scores
                    if (cur.moveToLast()) {
                        int tenThScore = cur.getInt(0);
                        if (score > tenThScore) {
                            // in the top 10 list
                            yn = true;
                        }
                    }
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            closeScoreDatabase();
        }

        return yn;
    }

    public void addScore(final String name , final int score) {
        /*
        String sql = "update " + tableName +" set playerName="+"'"+name+"'"
                + ","+"playerScore="+String.valueOf(score)
                + " where playerName='ChaoLee'";
        */

        openScoreDatabase();
        if (scoreDatabase != null) {
            try {
                String sql = "select count(*) as totalRec from " + tableName + ";";
                Cursor cur = scoreDatabase.rawQuery(sql, new String[]{});
                if (cur.moveToFirst()) {
                    if (cur.getInt(0) >= 100) {
                        //   Over 100 records,   delete one record
                        sql = "delete from " + tableName + " where playerScore in ( select playerScore from " + tableName + " order by playerScore limit 1);";
                        scoreDatabase.execSQL(sql);
                    }
                }
                cur.close();
                //  insert one record into table    SCORE
                System.out.println("addScore(final String name , final int score) --> score " + score);
                sql = "insert into " + tableName + " ( playerName , playerScore) values ("
                        + "'" + name + "'," + String.valueOf(score) + ");";
                scoreDatabase.execSQL(sql);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            closeScoreDatabase();
        }
    }

    public void deleteOneScore(final int id) {

        openScoreDatabase();
        if (scoreDatabase != null) {
            try {
                scoreDatabase.delete(tableName, _id + "=" + id,null);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            closeScoreDatabase();
        }
    }

    public void deleteAllAfterTop10() {
            openScoreDatabase();
            if (scoreDatabase != null) {
                try {
                    String query = "DELETE FROM " + tableName + " WHERE id NOT IN (SELECT ID FROM " + tableName + " ORDER BY " + playerScore + " DESC LIMIT 10)";
                    scoreDatabase.execSQL(query);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                closeScoreDatabase();
            }
    }

    public void deleteAllScore() {
            openScoreDatabase();
            if (scoreDatabase != null) {
                try {
                    scoreDatabase.delete(tableName, null,null);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                closeScoreDatabase();
            }
    }

    private void closeScoreDatabase() {
        if (scoreDatabase != null) {
            try {
                scoreDatabase.close();
                scoreDatabase = null;
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
