package com.smile.groundhoghunter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.smile.groundhoghunter.Model.SmileImageButton;
import com.smile.groundhoghunter.Service.GlobalTop10IntentService;
import com.smile.groundhoghunter.Service.LocalTop10IntentService;
import com.smile.groundhoghunter.Utilities.FontAndBitmapUtil;
import com.smile.smilepublicclasseslibrary.alertdialogfragment.AlertDialogFragment;
import com.smile.smilepublicclasseslibrary.showing_instertitial_ads_utility.ShowingInterstitialAdsUtil;
import com.smile.smilepublicclasseslibrary.utilities.ScreenUtil;

public class GroundhogActivity extends AppCompatActivity {

    // private properties
    private final static String TAG = "MainActivity";
    private final static String LoadingDialogTag = "LoadingDialogTag";

    private GameView gameView;
    private int rowNum;
    private int colNum;
    private float textFontSize;
    private float fontScale;
    private int highestScore;
    private ImageView soundOnOffImageView;
    private TextView highScoreTextView;
    private TextView scoreTextView;
    private TextView timerTextView;
    private TextView hitNumTextView;
    private ImageView messageImageView;

    private SmileImageButton startGameButton;
    private SmileImageButton pauseGameButton;
    private SmileImageButton resumeGameButton;
    private SmileImageButton newGameButton;
    private SmileImageButton quitGameButton;
    private SmileImageButton settingButton;
    private SmileImageButton top10Button;
    private SmileImageButton globalTop10Button;
    private LinearLayout bannerLinearLayout = null;
    private AdView bannerAdView = null;

    private boolean isShowingLoadingMessage;
    private AlertDialogFragment loadingDialog;

    private BroadcastReceiver bReceiver;

    private final String showingAdsString;
    private final String loadingString;

    private final int SettingRequestCode = 0;
    private final int LocalTop10RequestCode = 1;
    private final int GlobalTop10RequestCode = 2;

    // public static properties
    public static boolean GamePause = false;
    // public static final properties
    public static final Handler ActivityHandler = new Handler();

    public GroundhogActivity() {
        highestScore = GroundhogHunterApp.ScoreSQLiteDB.readHighestScore();
        showingAdsString = GroundhogHunterApp.AppResources.getString(R.string.showingAdsString);
        loadingString = GroundhogHunterApp.AppResources.getString(R.string.loadingString);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this, GroundhogHunterApp.FontSize_Scale_Type, null);
        textFontSize = ScreenUtil.suitableFontSize(this, defaultTextFontSize, GroundhogHunterApp.FontSize_Scale_Type, 0.0f);
        fontScale = ScreenUtil.suitableFontScale(this, GroundhogHunterApp.FontSize_Scale_Type, 0.0f);

        isShowingLoadingMessage = false;

        /*
        if (savedInstanceState != null) {
            isShowingLoadingMessage = savedInstanceState.getBoolean("IsShowingLoadingMessage");
            if (isShowingLoadingMessage) {
                // showing loading message
                showLoadingMessage();
            } else {
                dismissShowingLoadingMessage();
            }
        }
        */

        super.onCreate(savedInstanceState);
        // the following 2 statements have been moved to AndroidManifest.xml
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // getSupportActionBar().hide();

        setContentView(R.layout.activity_groundhog);

        GamePause = false;

        int darkOrange = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkOrange);
        int darkRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkRed);
        int darkGreen = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkGreen);

        // upper buttons layout
        // for setting button
        String settingString = getString(R.string.settingString);
        settingButton = findViewById(R.id.settingButton);
        Bitmap settingBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.setting_button, settingString, Color.BLUE);
        settingButton.setImageBitmap(settingBitmap);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( (gameView.getRunningStatus() != 1) || (GameView.GameViewPause) ) {
                    // client is not playing game or not pause status
                    disableAllButtons();
                    Intent intent = new Intent(GroundhogActivity.this, SettingActivity.class);
                    Bundle extras = new Bundle();
                    extras.putBoolean("HasSound", gameView.getHasSound());
                    intent.putExtras(extras);
                    startActivityForResult(intent, SettingRequestCode);
                }
            }
        });

        // for top 10 button
        String localTop10String = getString(R.string.localTop10String);
        top10Button = findViewById(R.id.top10Button);
        Bitmap top10Bitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.top10_button, localTop10String, darkRed);
        top10Button.setImageBitmap(top10Bitmap);
        top10Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( (gameView.getRunningStatus() != 1) || (GameView.GameViewPause) ) {
                    // client is not playing game or not pause status
                    disableAllButtons();
                    getLocalTop10ScoreList();
                }
            }
        });

        // for top 10 button
        String globalTop10String = getString(R.string.globalTop10String);
        globalTop10Button = findViewById(R.id.globalTop10Button);
        Bitmap globalTop10Bitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.global_top10_button, globalTop10String, darkRed);
        globalTop10Button.setImageBitmap(globalTop10Bitmap);
        globalTop10Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( (gameView.getRunningStatus() != 1) || (GameView.GameViewPause) ) {
                    // client is not playing game or not pause status
                    disableAllButtons();
                    getGlobalTop10ScoreList();
                }
            }
        });

        // score layout

        TextView gameStatusTitleTextView = findViewById(R.id.gameStatusTitle);
        ScreenUtil.resizeTextSize(gameStatusTitleTextView, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        soundOnOffImageView = findViewById(R.id.soundOnOffImageView);

        TextView highScoreTitleTextView = findViewById(R.id.highestScoreTitle);
        ScreenUtil.resizeTextSize(highScoreTitleTextView, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        highScoreTextView = findViewById(R.id.highestScoreText);
        ScreenUtil.resizeTextSize(highScoreTextView, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        highScoreTextView.setText(String.valueOf(highestScore));

        TextView scoreTitleTextView = findViewById(R.id.scoreTitle);
        ScreenUtil.resizeTextSize(scoreTitleTextView, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        scoreTextView = findViewById(R.id.scoreText);
        ScreenUtil.resizeTextSize(scoreTextView, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        scoreTextView.setText("0");

        TextView timerTitleTextView = findViewById(R.id.timerTitle);
        ScreenUtil.resizeTextSize(timerTitleTextView, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        timerTextView = findViewById(R.id.timerText);
        ScreenUtil.resizeTextSize(timerTextView, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        timerTextView.setText(String.valueOf(GameView.TimerInterval));

        TextView hitNumTitleTextView = findViewById(R.id.num_hit_Title);
        ScreenUtil.resizeTextSize(hitNumTitleTextView, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        hitNumTextView = findViewById(R.id.num_hit_Text);
        ScreenUtil.resizeTextSize(hitNumTextView, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
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

                gameView = new GameView(GroundhogActivity.this, frameWidth, frameHeight);
                Log.i(TAG, "gameView created.");
                gameFrameLayout.addView(gameView);
                Log.i(TAG, "Added gameView to gameFrameLayout.");
                soundOnOffImageView.setImageResource(R.drawable.sound_on_image);
            }
        });

        messageImageView = findViewById(R.id.messageImageView);
        messageImageView.setVisibility(View.GONE);

        // buttons for start game, new game, quit game
        String startGameString = getString(R.string.startGameString);
        String pauseGameString = getString(R.string.pauseGameString);
        String resumeGameString = getString(R.string.resumeGameString);

        startGameButton = findViewById(R.id.startGameButton);
        final Bitmap startGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.start_game_button, startGameString, Color.BLUE);
        startGameButton.setImageBitmap(startGameBitmap);
        startGameButton.setClickable(true);
        startGameButton.setEnabled(true);
        startGameButton.setVisibility(View.VISIBLE);

        pauseGameButton = findViewById(R.id.pauseGameButton);
        Bitmap pauseGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.pause_game_button, pauseGameString, Color.BLUE);
        pauseGameButton.setImageBitmap(pauseGameBitmap);
        pauseGameButton.setClickable(false);
        pauseGameButton.setEnabled(false);
        pauseGameButton.setVisibility(View.GONE);

        resumeGameButton = findViewById(R.id.resumeGameButton);
        Bitmap resumeGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.resume_game_button, resumeGameString, Color.BLUE);
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

        String newGameString = getString(R.string.newGameString);
        newGameButton = findViewById(R.id.newGameButton);
        Bitmap newGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.new_game_button, newGameString, Color.BLUE);
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

        String quitGameString = getString(R.string.quitGameString);
        quitGameButton = findViewById(R.id.quitGameButton);
        final Bitmap quitGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.quit_game_button, quitGameString, Color.YELLOW);
        quitGameButton.setImageBitmap(quitGameBitmap);
        quitGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quitGame();
            }
        });

        if (!GroundhogHunterApp.googleAdMobBannerID.isEmpty()) {
            bannerLinearLayout = findViewById(R.id.linearlayout_for_ads_in_myActivity);
            bannerAdView = new AdView(this);

            // LinearLayout.LayoutParams bannerLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            // bannerLp.gravity = Gravity.CENTER;
            // bannerAdView.setLayoutParams(bannerLp);
            // bannerAdView.setAdSize(AdSize.BANNER);
            AdSize adSize = new AdSize(AdSize.FULL_WIDTH, AdSize.AUTO_HEIGHT);
            bannerAdView.setAdSize(adSize);
            bannerAdView.setAdUnitId(GroundhogHunterApp.googleAdMobBannerID);
            bannerLinearLayout.addView(bannerAdView);
            AdRequest adRequest = new AdRequest.Builder().build();
            bannerAdView.loadAd(adRequest);
        }

        bReceiver = new GroundhogHunterBroadcastReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalTop10IntentService.Action_Name);
        intentFilter.addAction(GlobalTop10IntentService.Action_Name);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(bReceiver, intentFilter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SettingRequestCode:
                enableAllButtons();
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "SettingActivity returned ok.");
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        boolean hasSound = extras.getBoolean("HasSound");
                        gameView.setHasSound(hasSound);
                    }
                } else {
                    Log.i(TAG, "SettingActivity returned cancel.");
                }

                // update Main UI
                if (gameView.getHasSound()) {
                    soundOnOffImageView.setImageResource(R.drawable.sound_on_image);
                } else {
                    soundOnOffImageView.setImageResource(R.drawable.sound_off_image);
                }
                break;
            case LocalTop10RequestCode:
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "Top10ScoreActivity (Local top 10) returned successfully.");
                } else {
                    Log.i(TAG, "Top10ScoreActivity (Local top 10) did not return successfully.");
                }
                Log.i(TAG, "Showing ads");
                if (GroundhogHunterApp.InterstitialAd != null) {
                    int entryPoint = 0; //  no used
                    ShowingInterstitialAdsUtil.ShowAdAsyncTask showAdAsyncTask =
                            GroundhogHunterApp.InterstitialAd.new ShowAdAsyncTask(GroundhogActivity.this
                                    , entryPoint
                                    , new ShowingInterstitialAdsUtil.AfterDismissFunctionOfShowAd() {
                        @Override
                        public void executeAfterDismissAds(int endPoint) {
                            enableAllButtons();
                        }
                    });
                    showAdAsyncTask.execute();
                } else {
                    enableAllButtons();
                }
                break;
            case GlobalTop10RequestCode:
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "Top10ScoreActivity (Global top 10) returned successfully.");
                } else {
                    Log.i(TAG, "Top10ScoreActivity (Global top 10) did not return successfully.");
                }
                Log.i(TAG, "Showing ads");
                if (GroundhogHunterApp.InterstitialAd != null) {
                    int entryPoint = 0; //  no used
                    ShowingInterstitialAdsUtil.ShowAdAsyncTask showAdsAsyncTask =
                            GroundhogHunterApp.InterstitialAd.new ShowAdAsyncTask(GroundhogActivity.this
                                    , entryPoint
                                    , new ShowingInterstitialAdsUtil.AfterDismissFunctionOfShowAd() {
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
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("MainActivity.onStart() is called.");
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
    protected void onStop() {
        super.onStop();
        System.out.println("MainActivity.onStop() is called.");
    }

    @Override
    public void onDestroy() {

        // release and destroy threads and resources before destroy activity
        if (isFinishing()) {
            if (GroundhogHunterApp.ScoreSQLiteDB != null) {
                GroundhogHunterApp.ScoreSQLiteDB.close();
            }
        }

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.unregisterReceiver(bReceiver);

        finishApplication();

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("IsShowingLoadingMessage", isShowingLoadingMessage);
        super.onSaveInstanceState(outState);
        System.out.println("MainActivity.onSaveInstanceState() is called.");
    }

    @Override
    public void onBackPressed() {
        // capture the event of back button when it is pressed
        // change back button behavior
        quitGame();
    }

    // private methods
    private void finishApplication() {
        // release resources and threads
        gameView.releaseSynchronizations();
        gameView.stopThreads();
        gameView.releaseResources();
    }
    private void quitGame() {
        if (GroundhogHunterApp.InterstitialAd != null) {
            // free version
            disableAllButtons();
            int entryPoint = 0; //  no used
            ShowingInterstitialAdsUtil.ShowAdAsyncTask showAdAsyncTask =
                    GroundhogHunterApp.InterstitialAd.new ShowAdAsyncTask(GroundhogActivity.this
                            , entryPoint
                            , new ShowingInterstitialAdsUtil.AfterDismissFunctionOfShowAd() {
                        @Override
                        public void executeAfterDismissAds(int endPoint) {
                            enableAllButtons();
                            quitApplication();
                        }
                    });
            showAdAsyncTask.execute();
        } else {
            // professional version
            quitApplication();
        }
    }
    private void quitApplication() {
        finish();
    }

    private void getLocalTop10ScoreList() {
        // showing loading message
        showLoadingMessage();

        Intent serviceIntent = new Intent(GroundhogHunterApp.AppContext, LocalTop10IntentService.class);
        startService(serviceIntent);
    }

    private void getGlobalTop10ScoreList() {
        // showing loading message
        showLoadingMessage();

        Intent serviceIntent = new Intent(GroundhogHunterApp.AppContext, GlobalTop10IntentService.class);
        String webUrl = GroundhogHunterApp.REST_Website + "/GetTop10PlayerscoresREST";  // ASP.NET Core
        webUrl += "?gameId=" + GroundhogHunterApp.GameId;   // parameters
        serviceIntent.putExtra("WebUrl", webUrl);
        startService(serviceIntent);
    }

    private void disableAllButtons() {
        startGameButton.setEnabled(false);
        pauseGameButton.setEnabled(false);
        resumeGameButton.setEnabled(false);
        newGameButton.setEnabled(false);
        quitGameButton.setEnabled(false);
        settingButton.setEnabled(false);
        top10Button.setEnabled(false);
        globalTop10Button.setEnabled(false);
    }
    private void enableAllButtons() {
        startGameButton.setEnabled(true);
        pauseGameButton.setEnabled(true);
        resumeGameButton.setEnabled(true);
        newGameButton.setEnabled(true);
        quitGameButton.setEnabled(true);
        settingButton.setEnabled(true);
        top10Button.setEnabled(true);
        globalTop10Button.setEnabled(true);
    }

    public void showLoadingMessage() {
        isShowingLoadingMessage = true;

        /*
        Bitmap dialog_board_image = BitmapFactory.decodeResource(GroundhogHunterApp.AppResources, R.drawable.dialog_background_image);
        Bitmap loadingBitmap = FontAndBitmapUtil.getBitmapFromBitmapWithText(dialog_board_image, loadingString, Color.RED);
        messageImageView.setVisibility(View.VISIBLE);
        messageImageView.setImageBitmap(loadingBitmap);
        */

        loadingDialog = AlertDialogFragment.newInstance(loadingString, GroundhogHunterApp.FontSize_Scale_Type, textFontSize, Color.RED, 0, 0, true);
        loadingDialog.show(getSupportFragmentManager(), LoadingDialogTag);
    }

    public void dismissShowingLoadingMessage() {
        isShowingLoadingMessage = false;

        /*
        messageImageView.setImageBitmap(null);
        messageImageView.setVisibility(View.GONE);
        */

        if (loadingDialog != null) {
            if (loadingDialog.isStateSaved()) {
                loadingDialog.dismissAllowingStateLoss();
            } else {
                loadingDialog.dismiss();
            }
        }
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

    // private class (Nested class)
    private class GroundhogHunterBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent == null) {
                return;
            }

            Bundle extras = intent.getExtras();

            String actionName = intent.getAction();
            switch (actionName) {
                case LocalTop10IntentService.Action_Name:
                    // dismiss showing message
                    dismissShowingLoadingMessage();
                    Intent localTop10Intent = new Intent(getApplicationContext(), Top10ScoreActivity.class);
                    Bundle localTop10Extras = new Bundle();
                    localTop10Extras.putString("Top10TitleName", getString(R.string.localTop10ScoreTitleString));
                    localTop10Extras.putStringArrayList("Top10Players", extras.getStringArrayList("PlayerNames"));
                    localTop10Extras.putIntegerArrayList("Top10Scores", extras.getIntegerArrayList("PlayerScores"));
                    localTop10Intent.putExtras(localTop10Extras);
                    startActivityForResult(localTop10Intent, LocalTop10RequestCode);
                    break;
                case GlobalTop10IntentService.Action_Name:
                    // dismiss showing message
                    dismissShowingLoadingMessage();
                    Intent globalTop10Intent = new Intent(getApplicationContext(), Top10ScoreActivity.class);
                    Bundle globalTop10Extras = new Bundle();
                    globalTop10Extras.putString("Top10TitleName", getString(R.string.globalTop10ScoreTitleString));
                    globalTop10Extras.putStringArrayList("Top10Players", extras.getStringArrayList("PlayerNames"));
                    globalTop10Extras.putIntegerArrayList("Top10Scores", extras.getIntegerArrayList("PlayerScores"));
                    globalTop10Intent.putExtras(globalTop10Extras);
                    startActivityForResult(globalTop10Intent, GlobalTop10RequestCode);
                    break;
            }

        }
    }

}