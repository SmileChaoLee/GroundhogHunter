package com.smile.groundhoghunter;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // the following 2 statements have been moved to AndroidManifest.xml
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // getSupportActionBar().hide();

        setContentView(R.layout.activity_main);

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
