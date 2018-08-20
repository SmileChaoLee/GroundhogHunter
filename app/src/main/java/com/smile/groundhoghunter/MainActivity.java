package com.smile.groundhoghunter;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.smile.groundhoghunter.Utilities.FontAndBitmapUtil;
import com.smile.groundhoghunter.Utilities.ScreenUtil;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    // private properties
    private final String TAG = new String("com.smile.groundhoghunter.MainActivity");
    private GameView gameView;

    // default properties (package modifiers)
    final Handler activityHandler;
    int rowNum;
    int colNum;
    TextView highScoreTextView;
    TextView scoreTextView;
    TextView timerTextView;
    TextView hitNumTextView;
    boolean gamePause = false;

    public MainActivity() {
        activityHandler = new Handler();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // the following 2 statements have been moved to AndroidManifest.xml
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // getSupportActionBar().hide();

        float fontSize = 30;
        int androidType = ScreenUtil.androidDeviceType(this);
        if (androidType != 0) {
            // not a cell phone, it is tablet
            fontSize = 50;
        }

        setContentView(R.layout.activity_main);

        gamePause = false;

        // score layout
        TextView highScoreTitleView = findViewById(R.id.highestScoreTitle);
        highScoreTitleView.setTextSize(fontSize);
        highScoreTextView = findViewById(R.id.highestScoreText);
        highScoreTextView.setTextSize(fontSize);

        TextView scoreTitleView = findViewById(R.id.scoreTitle);
        scoreTitleView.setTextSize(fontSize);
        scoreTextView = findViewById(R.id.scoreText);
        scoreTextView.setTextSize(fontSize);

        TextView timerTitleView = findViewById(R.id.timerTitle);
        timerTitleView.setTextSize(fontSize);
        timerTextView = findViewById(R.id.timerText);
        timerTextView.setTextSize(fontSize);

        TextView hitNumTitleView = findViewById(R.id.num_hit_Title);
        hitNumTitleView.setTextSize(fontSize);
        hitNumTextView = findViewById(R.id.num_hit_Text);
        hitNumTextView.setTextSize(fontSize);

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
        Bitmap quitGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.quit_game_button, quitGameStr, Color.BLUE);
        quitGameButton.setImageBitmap(quitGameBitmap);
        quitGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.newGame) {
            // gameView.releaseSynchronizations();
            // gameView.newGame();
            return true;
        }

        if (id == R.id.top_10_score) {
            // gameView.getTop10ScoreList();
            return true;
        }

        if (id == R.id.quitGame) {
            Handler handlerClose = new Handler();
            handlerClose.postDelayed(new Runnable() {
                public void run() {
                    finish();
                }
            },300);
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    // public methods
}
