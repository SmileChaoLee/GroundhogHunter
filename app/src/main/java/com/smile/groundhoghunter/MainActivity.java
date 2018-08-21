package com.smile.groundhoghunter;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smile.groundhoghunter.Utilities.FontAndBitmapUtil;
import com.smile.groundhoghunter.Utilities.ScoreSQLite;
import com.smile.groundhoghunter.Utilities.ScreenUtil;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // private properties
    private final String TAG = new String("com.smile.groundhoghunter.MainActivity");
    private final ScoreSQLite scoreSQLite;
    private GameView gameView;
    private int rowNum;
    private int colNum;
    private float textFontSize;
    private int highestScore;
    private TextView highScoreTextView;
    private TextView scoreTextView;
    private TextView timerTextView;
    private TextView hitNumTextView;

    // default properties (package modifiers)
    final Handler activityHandler;
    boolean gamePause = false;

    public MainActivity() {
        activityHandler = new Handler();
        // cannot use this as a parameter of context for SQLite because context has not been created yet
        // until onCreate() in activity, so use the context in Application class
        scoreSQLite = new ScoreSQLite(GroundhogHunterApp.AppContext);
        highestScore = scoreSQLite.readHighestScore();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // the following 2 statements have been moved to AndroidManifest.xml
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // getSupportActionBar().hide();

        textFontSize = 30;
        boolean isTable = ScreenUtil.isTablet(this);
        if (isTable) {
            // not a cell phone, it is a tablet
            textFontSize = 50;
        }

        setContentView(R.layout.activity_main);

        gamePause = false;

        int darkOrange = getResources().getColor(R.color.darkOrange);
        int darkRed = getResources().getColor(R.color.darkRed);

        // upper buttons layout
        String top10Str = getString(R.string.top10Str);
        ImageButton top10Button = findViewById(R.id.top10Button);
        Bitmap top10Bitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.top10_button, top10Str, Color.RED);
        top10Button.setImageBitmap(top10Bitmap);
        top10Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTop10ScoreList();
            }
        });

        // score layout
        TextView highScoreTitleView = findViewById(R.id.highestScoreTitle);
        highScoreTitleView.setTextSize(textFontSize);
        highScoreTextView = findViewById(R.id.highestScoreText);
        highScoreTextView.setTextSize(textFontSize);
        highScoreTextView.setText(String.valueOf(highestScore));

        TextView scoreTitleView = findViewById(R.id.scoreTitle);
        scoreTitleView.setTextSize(textFontSize);
        scoreTextView = findViewById(R.id.scoreText);
        scoreTextView.setTextSize(textFontSize);

        TextView timerTitleView = findViewById(R.id.timerTitle);
        timerTitleView.setTextSize(textFontSize);
        timerTextView = findViewById(R.id.timerText);
        timerTextView.setTextSize(textFontSize);

        TextView hitNumTitleView = findViewById(R.id.num_hit_Title);
        hitNumTitleView.setTextSize(textFontSize);
        hitNumTextView = findViewById(R.id.num_hit_Text);
        hitNumTextView.setTextSize(textFontSize);

        FrameLayout gameFrameLayout = findViewById(R.id.gameViewAreaFrameLayout);
        // game view area
        GridLayout gameGrid = findViewById(R.id.gameAreaGridLayout);
        rowNum = gameGrid.getRowCount();
        colNum = gameGrid.getColumnCount();
        for (int i=0; i<rowNum; i++) {
            GridLayout.Spec rowSpec = GridLayout.spec(i, 1, 1);
            for (int j=0; j<colNum; j++) {
                GridLayout.Spec colSpec = GridLayout.spec(j, 1, 1);
                GridLayout.LayoutParams glP = new GridLayout.LayoutParams();
                glP.width = 0;
                glP.height = 0;
                glP.rowSpec = rowSpec;
                glP.columnSpec = colSpec;

                int index = rowNum * i + j;
                ImageView imageView = new ImageView(this);
                imageView.setId(index);
                imageView.setClickable(true);
                imageView.setBackgroundResource(R.drawable.groundhog_hole);
                gameGrid.addView(imageView, index, glP);
            }
        }

        gameView = new GameView(this);
        Log.i(TAG, "gameView created.");
        gameFrameLayout.addView(gameView);

        // buttons for start game, new game, quit game
        String startGameStr = getString(R.string.start_game_string);
        String pauseGameStr = getString(R.string.pause_game_string);
        String resumeGameStr = getString(R.string.resume_game_string);

        ImageButton startGameButton = findViewById(R.id.gameControlButton);
        Bitmap startGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.start_game_button, startGameStr, Color.BLUE);
        Bitmap pauseGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.pause_game_button, pauseGameStr, Color.BLUE);
        Bitmap resumeGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.resume_game_button, resumeGameStr, Color.BLUE);
        startGameButton.setImageBitmap(startGameBitmap);
        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameView.startGame();
            }
        });

        String newGameStr = getString(R.string.new_game_string);
        ImageButton newGameButton = findViewById(R.id.newGameButton);
        Bitmap newGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.new_game_button, newGameStr, Color.BLUE);
        newGameButton.setImageBitmap(newGameBitmap);
        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameView.newGame();
            }
        });

        String quitGameStr = getString(R.string.quit_game_string);
        ImageButton quitGameButton = findViewById(R.id.quitGameButton);
        Bitmap quitGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.quit_game_button, quitGameStr, Color.YELLOW);
        quitGameButton.setImageBitmap(quitGameBitmap);
        quitGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("onResume() is called.");

        synchronized (activityHandler) {
            gamePause = false;
            activityHandler.notifyAll();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("onPause() is called.");

        synchronized (activityHandler) {
            gamePause = true;
        }
        // super.onPause();
    }

    @Override
    public void onDestroy() {
        // release and destroy threads and resources before destroy activity

        System.out.println("onDestroy --> Setting Screen orientation to User");

        /*
        if (facebookBannerAdView != null) {
            facebookBannerAdView.close();
        }
        */

        finishApplication();

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // capture the event of back button when it is pressed
        // change back button behavior
        finish();
    }

    // private methods
    private void finishApplication() {
        // release resources and threads
        gameView.releaseSynchronizations();
        gameView.stopThreads();
        gameView.releaseResources();
    }

    private void getTop10ScoreList() {
        ArrayList<Pair<String, Integer>> top10 = scoreSQLite.readTop10ScoreList();
        ArrayList<String> playerNames = new ArrayList<String>();
        ArrayList<Integer> playerScores = new ArrayList<Integer>();
        for (Pair pair : top10) {
            playerNames.add((String)pair.first);
            playerScores.add((Integer)pair.second);
        }

        Log.d(TAG, "top10.size() = " + top10.size());

        Intent intent = new Intent(this, Top10ScoreActivity.class);
        Bundle extras = new Bundle();
        extras.putStringArrayList("Top10Players", playerNames);
        extras.putIntegerArrayList("Top10Scores", playerScores);
        extras.putFloat("FontSizeForText", textFontSize);
        intent.putExtras(extras);

        Log.d(TAG, "Starting Top10ScoreActivity ......");

        startActivity(intent);
    }

    // public methods
    public ScoreSQLite getScoreSQLite() {
        return scoreSQLite;
    }
    public int getRowNum() {
        return rowNum;
    }
    public int getColNum() {
        return colNum;
    }
    public float getTextFontSize() {
        return textFontSize;
    }
    public int getHighestScore() {
        return highestScore;
    }
    public void setHighestScore(int highestScore) {
        this.highestScore = highestScore;
    }
    public void setTextForHighScoreTextView(String text) {
        highScoreTextView.setText(text);
    }
    public void setTextForScoreTextView(String text) {
        scoreTextView.setText(text);
    }
    public void setTextForTimerTextView(String text) {
        timerTextView.setText(text);
    }
    public void setTextForHitNumTextView(String text) {
        hitNumTextView.setText(text);
    }
}
