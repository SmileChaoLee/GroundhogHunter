package com.smile.groundhoghunter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Handler;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // the following 2 statements have been moved to AndroidManifest.xml
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // getSupportActionBar().hide();

        setContentView(R.layout.activity_main);

        // game view area
        GridLayout gameGrid = findViewById(R.id.gameAreaGridLayout);
        int rowNum = gameGrid.getRowCount();
        int colNum = gameGrid.getColumnCount();
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
                imageView.setBackgroundResource(R.drawable.ground_hole);
                gameGrid.addView(imageView, index, glP);
            }
        }

        // buttons for start game, new game, quit game
        String startGameStr = getString(R.string.start_game_string);
        String pauseGameStr = getString(R.string.pause_game_string);
        String resumeGameStr = getString(R.string.resume_game_string);

        ImageButton startGameButton = findViewById(R.id.gameControlButton);
        Bitmap startGameBitmap = FontAndBitmapUtility.getBitmapFromResourceWithText(this, R.drawable.start_game_button, startGameStr, Color.BLUE);
        Bitmap pauseGameBitmap = FontAndBitmapUtility.getBitmapFromResourceWithText(this, R.drawable.pause_game_button, pauseGameStr, Color.BLUE);
        Bitmap resumeGameBitmap = FontAndBitmapUtility.getBitmapFromResourceWithText(this, R.drawable.resume_game_button, resumeGameStr, Color.BLUE);
        startGameButton.setImageBitmap(startGameBitmap);

        String newGameStr = getString(R.string.new_game_string);
        ImageButton newGameButton = findViewById(R.id.newGameButton);
        Bitmap newGameBitmap = FontAndBitmapUtility.getBitmapFromResourceWithText(this, R.drawable.new_game_button, newGameStr, Color.BLUE);
        newGameButton.setImageBitmap(newGameBitmap);

        String quitGameStr = getString(R.string.quit_game_string);
        ImageButton quitGameButton = findViewById(R.id.quitGameButton);
        Bitmap quitGameBitmap = FontAndBitmapUtility.getBitmapFromResourceWithText(this, R.drawable.quit_game_button, quitGameStr, Color.BLUE);
        quitGameButton.setImageBitmap(quitGameBitmap);
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
            // gameView.releaseSynchronizings();
            // gameView.newGame();
            return true;
        }

        if (id == R.id.top_10_score) {
            // gameView.getTop10ScoreList();
            return true;
        }

        if (id == R.id.scoreHistory) {
            // gameView.getScoreHistory();
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

    private void finishApplication() {
        // release resources and threads
        // gameView.releaseSynchronizings();
        // gameView.stopThreads();
    }
}
