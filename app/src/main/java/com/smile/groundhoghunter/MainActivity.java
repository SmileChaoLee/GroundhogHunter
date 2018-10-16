package com.smile.groundhoghunter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.smile.alertdialogfragment.AlertDialogFragment;
import com.smile.groundhoghunter.Model.SmileImageButton;
import com.smile.groundhoghunter.Utilities.FontAndBitmapUtil;
import com.smile.groundhoghunter.Utilities.ScreenUtil;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // private properties
    private final static String TAG = "MainActivity";

    private GameView gameView;
    private int rowNum;
    private int colNum;
    private float textFontSize;
    private int highestScore;
    private ImageView numPlayersImageView;
    private ImageView soundOnOffImageView;
    private TextView highScoreTextView;
    private TextView scoreTextView;
    private TextView timerTextView;
    private TextView hitNumTextView;

    private SmileImageButton startGameButton;
    private SmileImageButton pauseGameButton;
    private SmileImageButton resumeGameButton;
    private SmileImageButton newGameButton;
    private SmileImageButton quitGameButton;
    private SmileImageButton settingButton;
    private SmileImageButton multiPlayerButton;
    private SmileImageButton top10Button;

    private final String showingAdsString;
    private final String loadingString;

    private final int Top10RequestCode = 0;
    private final int SettingRequestCode = 1;
    private final int MultiPlayerRequestCode = 2;

    // public static properties
    public static boolean GamePause = false;
    // public static final properties
    public static final Handler ActivityHandler = new Handler();

    public MainActivity() {
        highestScore = GroundhogHunterApp.ScoreSQLiteDB.readHighestScore();
        showingAdsString = GroundhogHunterApp.AppResources.getString(R.string.showingAdsString);
        loadingString = GroundhogHunterApp.AppResources.getString(R.string.loadingString);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        boolean isTable = ScreenUtil.isTablet(this);
        if (isTable) {
            // not a cell phone, it is a tablet
            textFontSize = 50;
            setTheme(R.style.ThemeTextSize50);
        } else {
            textFontSize = 30;
            setTheme(R.style.ThemeTextSize30);
        }

        super.onCreate(savedInstanceState);
        // the following 2 statements have been moved to AndroidManifest.xml
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // getSupportActionBar().hide();

        setContentView(R.layout.activity_main);

        GamePause = false;

        int darkOrange = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkOrange);
        int darkRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkRed);
        int darkGreen = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkGreen);

        // upper buttons layout
        // for setting button
        String settingStr = getString(R.string.settingStr);
        settingButton = findViewById(R.id.settingButton);
        Bitmap settingBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.setting_button, settingStr, Color.BLUE);
        settingButton.setImageBitmap(settingBitmap);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( (gameView.getRunningStatus() != 1) || (GameView.GameViewPause) ) {
                    // client is not playing game or not pause status
                    disableAllButtons();
                    Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                    Bundle extras = new Bundle();
                    extras.putFloat("TextFontSize", textFontSize);
                    extras.putBoolean("HasSound", gameView.getHasSound());
                    extras.putBoolean("IsSinglePlayer", gameView.getIsSinglePlayer());
                    intent.putExtras(extras);
                    startActivityForResult(intent, SettingRequestCode);
                }
            }
        });

        String multiPlayerStr = getString(R.string.multiPlayerStr);
        multiPlayerButton = findViewById(R.id.multiPlayerButton);
        Bitmap multiPlayerBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.multi_player_button, multiPlayerStr, darkOrange);
        multiPlayerButton.setImageBitmap(multiPlayerBitmap);
        multiPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!gameView.getIsSinglePlayer()) {
                    if ((gameView.getRunningStatus() != 1) || (GameView.GameViewPause)) {
                        // client is not playing game or not pause status
                        disableAllButtons();
                        Intent intent = new Intent(MainActivity.this, MultiPlayerActivity.class);
                        Bundle extras = new Bundle();
                        extras.putFloat("TextFontSize", textFontSize);
                        extras.putInt("MediaType", gameView.getMediaType());
                        intent.putExtras(extras);
                        startActivityForResult(intent, MultiPlayerRequestCode);
                    }
                }
            }
        });

        /*
        ApplicationInfo appInfo = getApplicationContext().getApplicationInfo();
        boolean isDebuggable = (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        if (!isDebuggable) {
            // release mode
            multiPlayerButton.setEnabled(false);
            multiPlayerButton.setVisibility(View.GONE);
        }
        */

        if (!BuildConfig.DEBUG) {
            multiPlayerButton.setEnabled(false);
            multiPlayerButton.setVisibility(View.GONE);
        }

        // for top 10 button
        String top10Str = getString(R.string.top10Str);
        top10Button = findViewById(R.id.top10Button);
        Bitmap top10Bitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.top10_button, top10Str, darkRed);
        top10Button.setImageBitmap(top10Bitmap);
        top10Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( (gameView.getRunningStatus() != 1) || (GameView.GameViewPause) ) {
                    // client is not playing game or not pause status
                    disableAllButtons();
                    getTop10ScoreList();
                }
            }
        });

        // score layout
        soundOnOffImageView = findViewById(R.id.soundOnOffImageView);
        numPlayersImageView = findViewById(R.id.numPlayerImageView);

        TextView highScoreTitleView = findViewById(R.id.highestScoreTitle);
        // highScoreTitleView.setTextSize(textFontSize);
        highScoreTextView = findViewById(R.id.highestScoreText);
        // highScoreTextView.setTextSize(textFontSize);
        highScoreTextView.setText(String.valueOf(highestScore));

        TextView scoreTitleView = findViewById(R.id.scoreTitle);
        // scoreTitleView.setTextSize(textFontSize);
        scoreTextView = findViewById(R.id.scoreText);
        // scoreTextView.setTextSize(textFontSize);
        scoreTextView.setText("0");

        TextView timerTitleView = findViewById(R.id.timerTitle);
        // timerTitleView.setTextSize(textFontSize);
        timerTextView = findViewById(R.id.timerText);
        // timerTextView.setTextSize(textFontSize);
        timerTextView.setText(String.valueOf(GameView.TimerInterval));

        TextView hitNumTitleView = findViewById(R.id.num_hit_Title);
        // hitNumTitleView.setTextSize(textFontSize);
        hitNumTextView = findViewById(R.id.num_hit_Text);
        // hitNumTextView.setTextSize(textFontSize);
        hitNumTextView.setText("0");

        final FrameLayout gameFrameLayout = findViewById(R.id.gameViewAreaFrameLayout);
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

        gameFrameLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    // ICE_CREAM_SANDWICH_MR1 is API 15
                    // removeGlobalOnLayoutListener() deprecated after API 16
                    gameFrameLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    // hove to use removeGlobalOnLayoutListener() method after API 16 or is API 16
                    gameFrameLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                int frameWidth = gameFrameLayout.getWidth();
                int frameHeight = gameFrameLayout.getHeight();
                Log.i(TAG, "The width of gameFrameLayout = " + frameWidth);
                Log.i(TAG, "The height of gameFrameLayout = " + frameHeight);

                gameView = new GameView(MainActivity.this, frameWidth, frameHeight);
                Log.i(TAG, "gameView created.");
                gameFrameLayout.addView(gameView);
                Log.i(TAG, "Added gameView to gameFrameLayout.");
                numPlayersImageView.setImageResource(R.drawable.single_player_image);
                soundOnOffImageView.setImageResource(R.drawable.sound_on_image);
            }
        });

        // buttons for start game, new game, quit game
        String startGameStr = getString(R.string.start_game_string);
        String pauseGameStr = getString(R.string.pause_game_string);
        String resumeGameStr = getString(R.string.resume_game_string);

        startGameButton = findViewById(R.id.startGameButton);
        final Bitmap startGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.start_game_button, startGameStr, Color.BLUE);
        startGameButton.setImageBitmap(startGameBitmap);
        startGameButton.setClickable(true);
        startGameButton.setEnabled(true);
        startGameButton.setVisibility(View.VISIBLE);

        pauseGameButton = findViewById(R.id.pauseGameButton);
        Bitmap pauseGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.pause_game_button, pauseGameStr, Color.BLUE);
        pauseGameButton.setImageBitmap(pauseGameBitmap);
        pauseGameButton.setClickable(false);
        pauseGameButton.setEnabled(false);
        pauseGameButton.setVisibility(View.GONE);

        resumeGameButton = findViewById(R.id.resumeGameButton);
        Bitmap resumeGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.resume_game_button, resumeGameStr, Color.BLUE);
        resumeGameButton.setImageBitmap(resumeGameBitmap);
        resumeGameButton.setClickable(false);
        resumeGameButton.setEnabled(false);
        resumeGameButton.setVisibility(View.GONE);

        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameView.startGame();

                startGameButton.setEnabled(false);
                startGameButton.setVisibility(View.GONE);

                pauseGameButton.setEnabled(true);
                pauseGameButton.setVisibility(View.VISIBLE);

                resumeGameButton.setEnabled(false);
                resumeGameButton.setVisibility(View.GONE);
            }
        });
        pauseGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameView.pauseGame();

                startGameButton.setEnabled(false);
                startGameButton.setVisibility(View.GONE);

                pauseGameButton.setEnabled(false);
                pauseGameButton.setVisibility(View.GONE);

                resumeGameButton.setEnabled(true);
                resumeGameButton.setVisibility(View.VISIBLE);
            }
        });
        resumeGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameView.resumeGame();

                startGameButton.setEnabled(false);
                startGameButton.setVisibility(View.GONE);

                pauseGameButton.setEnabled(true);
                pauseGameButton.setVisibility(View.VISIBLE);

                resumeGameButton.setEnabled(false);
                resumeGameButton.setVisibility(View.GONE);
            }
        });

        String newGameStr = getString(R.string.new_game_string);
        newGameButton = findViewById(R.id.newGameButton);
        Bitmap newGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.new_game_button, newGameStr, Color.BLUE);
        newGameButton.setImageBitmap(newGameBitmap);
        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameView.newGame();

                startGameButton.setEnabled(true);
                startGameButton.setVisibility(View.VISIBLE);

                pauseGameButton.setEnabled(false);
                pauseGameButton.setVisibility(View.GONE);

                resumeGameButton.setEnabled(false);
                resumeGameButton.setVisibility(View.GONE);
            }
        });

        String quitGameStr = getString(R.string.quit_game_string);
        quitGameButton = findViewById(R.id.quitGameButton);
        final Bitmap quitGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.quit_game_button, quitGameStr, Color.YELLOW);
        quitGameButton.setImageBitmap(quitGameBitmap);
        quitGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GroundhogHunterApp.FacebookAds != null) {
                    // free version
                    disableAllButtons();
                    int entryPoint = 0; //  no used
                    ShowFacebookAdsAsyncTask showAdsAsyncTask = new ShowFacebookAdsAsyncTask(entryPoint, new AfterDismissFunctionOfShowFacebookAds() {
                        @Override
                        public void executeAfterDismissAds(int endPoint) {
                            enableAllButtons();
                            quitApplication();
                        }
                    });
                    showAdsAsyncTask.execute();
                } else {
                    // professional version
                    quitApplication();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Top10RequestCode:
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "Top10ScoreActivity returned successfully.");
                } else {
                    Log.i(TAG, "Top10ScoreActivity did not return successfully.");
                }
                Log.i(TAG, "Facebook showing ads");
                if (GroundhogHunterApp.FacebookAds != null) {
                    int entryPoint = 0; //  no used
                    ShowFacebookAdsAsyncTask showAdsAsyncTask = new ShowFacebookAdsAsyncTask(entryPoint, new AfterDismissFunctionOfShowFacebookAds() {
                        @Override
                        public void executeAfterDismissAds(int endPoint) {
                            enableAllButtons();
                        }
                    });
                    showAdsAsyncTask.execute();
                } else {
                    enableAllButtons();
                }

                break;
            case SettingRequestCode:
                enableAllButtons();
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "SettingActivity returned ok.");
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        boolean hasSound = extras.getBoolean("HasSound");
                        boolean isSinglePlayer = extras.getBoolean("IsSinglePlayer");
                        gameView.setHasSound(hasSound);
                        gameView.setIsSinglePlayer(isSinglePlayer);
                    }
                } else {
                    Log.i(TAG, "SettingActivity returned cancel.");
                }
                break;
            case MultiPlayerRequestCode:
                enableAllButtons();
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "MultiPlayerActivity returned ok.");
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        int mediaType = extras.getInt("MediaType");
                        gameView.setMediaType(mediaType);
                    }
                } else {
                    Log.i(TAG, "MultiPlayerActivity returned cancel.");
                }
                Log.i(TAG, "Facebook showing ads");
                break;
        }

        // update Main UI
        if (gameView.getHasSound()) {
            soundOnOffImageView.setImageResource(R.drawable.sound_on_image);
        } else {
            soundOnOffImageView.setImageResource(R.drawable.sound_off_image);
        }
        if (gameView.getIsSinglePlayer()) {
            numPlayersImageView.setImageResource(R.drawable.single_player_image);
        } else {
            numPlayersImageView.setImageResource(R.drawable.multi_players_image);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("MainActivity.onResume() is called.");

        synchronized (ActivityHandler) {
            GamePause = false;
            ActivityHandler.notifyAll();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("MainActivity.onPause() is called.");

        synchronized (ActivityHandler) {
            GamePause = true;
        }
        // super.onPause();
    }

    @Override
    public void onDestroy() {

        // release and destroy threads and resources before destroy activity
        if (isFinishing()) {
            if (GroundhogHunterApp.FacebookAds != null) {
                GroundhogHunterApp.FacebookAds.close();
            }
            if (GroundhogHunterApp.ScoreSQLiteDB != null) {
                GroundhogHunterApp.ScoreSQLiteDB.close();
            }
        }

        finishApplication();

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        System.out.println("MainActivity.onSaveInstanceState() is called.");
    }

    @Override
    public void onBackPressed() {
        // capture the event of back button when it is pressed
        // change back button behavior
        quitApplication();
    }

    // private methods
    private void finishApplication() {
        // release resources and threads
        gameView.releaseSynchronizations();
        gameView.stopThreads();
        gameView.releaseResources();
    }

    private void quitApplication() {
        finish();
    }

    private void getTop10ScoreList() {

        ArrayList<Pair<String, Integer>> top10 = GroundhogHunterApp.ScoreSQLiteDB.readTop10ScoreList();
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
        extras.putFloat("TextFontSize", textFontSize);
        intent.putExtras(extras);

        startActivityForResult(intent, Top10RequestCode);
    }

    private void disableAllButtons() {
        startGameButton.setEnabled(false);
        pauseGameButton.setEnabled(false);
        resumeGameButton.setEnabled(false);
        newGameButton.setEnabled(false);
        quitGameButton.setEnabled(false);
        settingButton.setEnabled(false);
        multiPlayerButton.setEnabled(false);
        top10Button.setEnabled(false);
    }
    private void enableAllButtons() {
        startGameButton.setEnabled(true);
        pauseGameButton.setEnabled(true);
        resumeGameButton.setEnabled(true);
        newGameButton.setEnabled(true);
        quitGameButton.setEnabled(true);
        settingButton.setEnabled(true);
        multiPlayerButton.setEnabled(true);
        top10Button.setEnabled(true);
    }

    // public methods
    public int getRowNum() {
        return rowNum;
    }
    public int getColNum() {
        return colNum;
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

    // interface for showing Facebook ads
    public interface AfterDismissFunctionOfShowFacebookAds {
        void executeAfterDismissAds(int endPoint);
    }
    public class ShowFacebookAdsAsyncTask_OLD extends AsyncTask<Void, Void, Void> {

        private final int endPoint;
        private final AfterDismissFunctionOfShowFacebookAds afterDismissFunction;

        public ShowFacebookAdsAsyncTask_OLD(final int endPoint, final AfterDismissFunctionOfShowFacebookAds afterDismissFunction) {
            this.endPoint = endPoint;
            this.afterDismissFunction = afterDismissFunction;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // showingAdsMessage();
            GroundhogHunterApp.FacebookAds.showAd(TAG);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            final int timeDelay = 300;
            while (!GroundhogHunterApp.FacebookAds.adsShowDismissedOrStopped()) {
                publishProgress();
                SystemClock.sleep(timeDelay);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            // showingAdsMessage();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // dismissShowingAdsMessage();

            afterDismissFunction.executeAfterDismissAds(endPoint);
        }
        // end of showing facebook ads
    }


    private class ShowFacebookAdsAsyncTask extends AsyncTask<Void, Integer, Void> {

        private final String LoadingDialogTag = "LoadingDialogTag";
        private TextView loadingTextView = null;
        private Animation animationText = null;
        private AlertDialogFragment loadingDialog = null;

        private final int endPoint;
        private final AfterDismissFunctionOfShowFacebookAds afterDismissFunction;

        public ShowFacebookAdsAsyncTask(final int endPoint, final AfterDismissFunctionOfShowFacebookAds afterDismissFunction) {
            loadingDialog = new AlertDialogFragment();
            Bundle args = new Bundle();
            args.putString("textContent", showingAdsString);
            args.putFloat("textSize", textFontSize);
            args.putInt("color", Color.RED);
            args.putInt("width", 0);    // wrap_content
            args.putInt("height", 0);   // wrap_content
            args.putInt("numButtons", 0);
            loadingDialog.setArguments(args);

            this.endPoint = endPoint;
            this.afterDismissFunction = afterDismissFunction;
        }

        @Override
        protected void onPreExecute() {

            animationText = new AlphaAnimation(0.0f,1.0f);
            animationText.setDuration(300);
            animationText.setStartOffset(0);
            animationText.setRepeatMode(Animation.REVERSE);
            animationText.setRepeatCount(Animation.INFINITE);
            loadingDialog.show(getSupportFragmentManager(), LoadingDialogTag);

            GroundhogHunterApp.FacebookAds.showAd(TAG);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            final int timeDelay = 300;
            int i = 0;

            publishProgress(i);
            // wait for one second
            try { Thread.sleep(timeDelay); } catch (InterruptedException ex) { ex.printStackTrace(); }

            i = 1;
            while (loadingTextView == null) {
                loadingTextView = loadingDialog.getText_shown();
                SystemClock.sleep(timeDelay);
            }
            publishProgress(i);

            i = 2;
            while (!GroundhogHunterApp.FacebookAds.adsShowDismissedOrStopped()) {
                SystemClock.sleep(timeDelay);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            if (!isCancelled()) {
                try {
                    if (progress[0] == 1) {
                        if (animationText != null) {
                            loadingTextView.startAnimation(animationText);
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("MyActivity.ShowFacebookAdsAsyncTask.onProgressUpdate() failed --> textLoad animation");
                    ex.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            if (!isCancelled()) {
                if (animationText != null) {
                    if (loadingTextView != null) {
                        loadingTextView.clearAnimation();
                        loadingTextView.setText("");
                    }
                    animationText = null;
                }
                loadingDialog.dismissAllowingStateLoss();
            }

            afterDismissFunction.executeAfterDismissAds(endPoint);
        }
    }
}