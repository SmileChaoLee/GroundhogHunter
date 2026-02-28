package com.smile.groundhoghunter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.gridlayout.widget.GridLayout;

import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smile.groundhoghunter.abstract_threads.IoFunctionThread;
import com.smile.groundhoghunter.constants.Constants;
import com.smile.groundhoghunter.services.GlobalTop10IntentService;
import com.smile.groundhoghunter.services.LocalTop10IntentService;
import com.smile.smilelibraries.interfaces.DismissFunction;
import com.smile.smilelibraries.models.ExitAppTimer;
import com.smile.smilelibraries.customized_button.SmileImageButton;
import com.smile.smilelibraries.show_banner_ads.*;
import com.smile.smilelibraries.utilities.FontAndBitmapUtil;
import com.smile.smilelibraries.alertdialogfragment.AlertDialogFragment;
import com.smile.smilelibraries.show_interstitial_ads.*;
import com.smile.smilelibraries.utilities.ScreenUtil;

import java.util.ArrayList;

public class GroundhogActivity extends AppCompatActivity {

    private final static String TAG = "GroundhogAct";
    private final static String LoadingDialogTag = "LoadingDialogTag";
    private String loadingString;
    private int rowNum;
    private int colNum;
    private int highestScore;
    private ImageView soundOnOffImageView;
    private TextView highScoreTextView;
    private TextView scoreTextView;
    private TextView timerTextView;
    private TextView hitNumTextView;
    private SmileImageButton settingButton;
    private SmileImageButton top10Button;
    private SmileImageButton globalTop10Button;
    private boolean isShowingLoadingMessage;
    private AlertDialogFragment loadingDialog;
    private BroadcastReceiver bReceiver;
    private int gameType;
    protected GameView gameView;
    protected float textFontSize;
    protected float fontScale;
    protected float toastTextSize;
    protected SmileImageButton startGameButton;
    protected SmileImageButton pauseGameButton;
    protected SmileImageButton resumeGameButton;
    protected SmileImageButton newGameButton;
    protected SmileImageButton quitGameButton;
    protected IoFunctionThread selectedIoFunctionThread;
    // public static properties
    public static boolean GamePause = false;
    // public static final properties
    public static final Handler ActivityHandler = new Handler(Looper.getMainLooper());
    private ActivityResultLauncher<Intent> settingLauncher;
    private ActivityResultLauncher<Intent> otherLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "onCreate() is called.");

        if ( (GroundhogHunterApp.facebookAds != null) || (GroundhogHunterApp.googleInterstitialAd!= null) ) {
            GroundhogHunterApp.InterstitialAd = new ShowInterstitial(this, GroundhogHunterApp.facebookAds, GroundhogHunterApp.googleInterstitialAd);
        }

        if (GroundhogHunterApp.isFirstStartApp) {
            // first time entering this activity
            GroundhogHunterApp.isFirstStartApp = false;
            Log.d(TAG, "onCreate() --> First time entering.");
        } else {
            Log.d(TAG, "onCreate() --> not First time entering.");
        }

        if (savedInstanceState != null) {
            Log.d(TAG, "onCreate() --> savedInstanceState is not null.");
        } else {
            Log.d(TAG, "onCreate() --> savedInstanceState is null.");
        }

        selectedIoFunctionThread = GroundhogHunterApp.selectedIoFuncThread;
        if (selectedIoFunctionThread == null) {
            Log.d(TAG, "selectedIoFunctionThread is null.");
        }

        highestScore = GroundhogHunterApp.ScoreSQLiteDB.readHighestScore();
        loadingString = getString(R.string.loadingString);
        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this);
        fontScale = ScreenUtil.getPxFontScale(this);
        toastTextSize = textFontSize * 0.8f;
        isShowingLoadingMessage = false;
        Intent callingIntent = getIntent();
        gameType = callingIntent.getIntExtra(Constants.GAME_TYPE, Constants.GameBySinglePlayer);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_groundhog);

        GamePause = false;

        // int darkOrange = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkOrange);
        int darkRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkRed);
        // int darkGreen = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkGreen);

        // upper buttons layout
        // for setting button
        String settingString = getString(R.string.settingString);
        settingButton = findViewById(R.id.settingButton);
        Bitmap settingBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.setting_button, settingString, Color.BLUE);
        settingButton.setImageBitmap(settingBitmap);
        settingButton.setOnClickListener(view -> {
            if (gameView != null) {
                if ((gameView.getRunningStatus() != 1) || (GameView.GameViewPause)) {
                    // client is not playing game or not pause status
                    disableAllButtons();
                    Intent intent = new Intent(GroundhogActivity.this, SettingActivity.class);
                    Bundle extras = new Bundle();
                    extras.putBoolean("HasSound", gameView.getHasSound());
                    intent.putExtras(extras);
                    // startActivityForResult(intent, SettingRequestCode);
                    settingLauncher.launch(intent);
                }
            }
        });

        // for top 10 button
        String localTop10String = getString(R.string.localTop10String);
        top10Button = findViewById(R.id.top10Button);
        Bitmap top10Bitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.top10_button, localTop10String, darkRed);
        top10Button.setImageBitmap(top10Bitmap);
        top10Button.setOnClickListener(view -> {
            if (gameView != null) {
                if ((gameView.getRunningStatus() != 1) || (GameView.GameViewPause)) {
                    // client is not playing game or not pause status
                    disableAllButtons();
                    getLocalTop10ScoreList();    // removed for testing on 2019-05-07
                }
            }
        });

        // for top 10 button
        String globalTop10String = getString(R.string.globalTop10String);
        globalTop10Button = findViewById(R.id.globalTop10Button);
        Bitmap globalTop10Bitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.global_top10_button, globalTop10String, darkRed);
        globalTop10Button.setImageBitmap(globalTop10Bitmap);
        globalTop10Button.setOnClickListener(view -> {
            if (gameView != null) {
                if ((gameView.getRunningStatus() != 1) || (GameView.GameViewPause)) {
                    // client is not playing game or not pause status
                    disableAllButtons();
                    getGlobalTop10ScoreList();
                }
            }
        });

        // score layout
        TextView gameStatusTitleTextView = findViewById(R.id.gameStatusTitle);
        ScreenUtil.resizeTextSize(gameStatusTitleTextView, textFontSize);
        soundOnOffImageView = findViewById(R.id.soundOnOffImageView);

        TextView highScoreTitleTextView = findViewById(R.id.highestScoreTitle);
        ScreenUtil.resizeTextSize(highScoreTitleTextView, textFontSize);
        highScoreTextView = findViewById(R.id.highestScoreText);
        ScreenUtil.resizeTextSize(highScoreTextView, textFontSize);
        highScoreTextView.setText(String.valueOf(highestScore));

        TextView scoreTitleTextView = findViewById(R.id.scoreTitle);
        ScreenUtil.resizeTextSize(scoreTitleTextView, textFontSize);
        scoreTextView = findViewById(R.id.scoreText);
        ScreenUtil.resizeTextSize(scoreTextView, textFontSize);
        scoreTextView.setText("0");

        TextView timerTitleTextView = findViewById(R.id.timerTitle);
        ScreenUtil.resizeTextSize(timerTitleTextView, textFontSize);
        timerTextView = findViewById(R.id.timerText);
        ScreenUtil.resizeTextSize(timerTextView, textFontSize);
        timerTextView.setText(String.valueOf(GameView.TimerInterval));

        TextView hitNumTitleTextView = findViewById(R.id.num_hit_Title);
        ScreenUtil.resizeTextSize(hitNumTitleTextView, textFontSize);
        hitNumTextView = findViewById(R.id.num_hit_Text);
        ScreenUtil.resizeTextSize(hitNumTextView, textFontSize);
        hitNumTextView.setText("0");

        final LinearLayout gameLinearLayout = findViewById(R.id.gameViewAreaLinearLayout);
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

        LinearLayout bannerLinearLayout = findViewById(R.id.linearlayout_for_ads_in_myActivity);
        if (!GroundhogHunterApp.googleAdMobBannerID.isEmpty() || !GroundhogHunterApp.facebookBannerID.isEmpty())  {
            String testString = "";
            // for debug mode
            if (BuildConfig.DEBUG) {
                testString = "IMG_16_9_APP_INSTALL#";
            }
            String facebookBannerID = testString + GroundhogHunterApp.facebookBannerID;
            //
            SetBannerAdView myBannerAdView = new SetBannerAdView(this, null, bannerLinearLayout
                    , GroundhogHunterApp.googleAdMobBannerID, facebookBannerID);
            myBannerAdView.showBannerAdView(GroundhogHunterApp.AdProvider);
        } else {
            ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) bannerLinearLayout.getLayoutParams();
            float tempPercent = lp.matchConstraintPercentHeight;
            lp.matchConstraintPercentHeight = 0.0f;
            // lp = (ConstraintLayout.LayoutParams)FrameLayout.getLayoutParams();
            lp = (ConstraintLayout.LayoutParams)gameLinearLayout.getLayoutParams();
            lp.matchConstraintPercentHeight += tempPercent;
        }

        gameFrameLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // hove to use removeGlobalOnLayoutListener() method after API 16 or is API 16
                gameFrameLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int frameWidth = gameFrameLayout.getWidth();
                int frameHeight = gameFrameLayout.getHeight();

                gameView = new GameView(GroundhogActivity.this, gameType, frameWidth, frameHeight, selectedIoFunctionThread);
                Log.i(TAG, "gameView created.");
                gameFrameLayout.addView(gameView);
                soundOnOffImageView.setImageResource(R.drawable.sound_on_image);
            }
        });

        // buttons for start game, new game, quit game
        String startString = getString(R.string.startString);
        String pauseString = getString(R.string.pauseString);
        String resumeString = getString(R.string.resumeString);

        startGameButton = findViewById(R.id.startGameButton);
        final Bitmap startGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.start_game_button, startString, Color.BLUE);
        startGameButton.setImageBitmap(startGameBitmap);
        startGameButton.setClickable(true);
        startGameButton.setEnabled(true);
        startGameButton.setVisibility(View.VISIBLE);

        pauseGameButton = findViewById(R.id.pauseGameButton);
        Bitmap pauseGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.pause_game_button, pauseString, Color.BLUE);
        pauseGameButton.setImageBitmap(pauseGameBitmap);
        pauseGameButton.setClickable(false);
        pauseGameButton.setEnabled(false);
        pauseGameButton.setVisibility(View.INVISIBLE);

        resumeGameButton = findViewById(R.id.resumeGameButton);
        Bitmap resumeGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.resume_game_button, resumeString, Color.BLUE);
        resumeGameButton.setImageBitmap(resumeGameBitmap);
        resumeGameButton.setClickable(false);
        resumeGameButton.setEnabled(false);
        resumeGameButton.setVisibility(View.INVISIBLE);

        startGameButton.setOnClickListener(view -> startGame());
        pauseGameButton.setOnClickListener(view -> pauseGame());
        resumeGameButton.setOnClickListener(view -> resumeGame());

        String newGameString = getString(R.string.newString);
        newGameButton = findViewById(R.id.newGameButton);
        Bitmap newGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.new_game_button, newGameString, Color.BLUE);
        newGameButton.setImageBitmap(newGameBitmap);
        newGameButton.setOnClickListener(view -> newGame());

        String quitGameString = getString(R.string.quitString);
        quitGameButton = findViewById(R.id.quitGameButton);
        final Bitmap quitGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this,
                R.drawable.quit_game_button, quitGameString, Color.YELLOW);
        quitGameButton.setImageBitmap(quitGameBitmap);
        quitGameButton.setOnClickListener(view -> quitGame());

        bReceiver = new GroundhogHunterBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalTop10IntentService.Action_Name);
        intentFilter.addAction(GlobalTop10IntentService.Action_Name);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(bReceiver, intentFilter);

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        exitApp();
                    }
                });

        settingLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    enableAllButtons();
                    int resultCode = result.getResultCode();
                    if (resultCode == Activity.RESULT_OK) {
                        Log.i(TAG, "SettingActivity returned ok.");
                        Intent data = result.getData();
                        if (data == null) return;
                        Bundle extras = data.getExtras();
                        if (extras != null) {
                            boolean hasSound = extras.getBoolean("HasSound");
                            gameView.setHasSound(hasSound);
                        }
                    } else {
                        Log.i(TAG, "SettingActivity returned cancel.");
                    }
                    // update Main UI for sound
                    if (gameView.getHasSound()) {
                        soundOnOffImageView.setImageResource(R.drawable.sound_on_image);
                    } else {
                        soundOnOffImageView.setImageResource(R.drawable.sound_off_image);
                    }
                });
        otherLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> enableAllButtons());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() is called.");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume() is called.");
        synchronized (ActivityHandler) {
            GamePause = false;
            ActivityHandler.notifyAll();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() is called.");
        synchronized (ActivityHandler) {
            GamePause = true;
        }
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        Log.d(TAG, "onNewIntent() is called.");
        super.onNewIntent(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() is called.");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy() is called.");
        super.onDestroy();
        // release and destroy threads and resources before destroy activity
        if (isFinishing()) {
            if (GroundhogHunterApp.ScoreSQLiteDB != null) {
                GroundhogHunterApp.ScoreSQLiteDB.close();
            }
        }
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.unregisterReceiver(bReceiver);
        finishApplication();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("IsShowingLoadingMessage", isShowingLoadingMessage);
        super.onSaveInstanceState(outState);
    }

    private void exitApp() {
        // capture the event of back button when it is pressed
        // change back button behavior
        ExitAppTimer exitAppTimer = ExitAppTimer.getInstance(1000); // singleton class
        if (exitAppTimer.canExit()) {
            quitGame();
        } else {
            exitAppTimer.start();
            ScreenUtil.showToast(this, getString(R.string.backKeyToExitApp),
                    toastTextSize, Toast.LENGTH_SHORT);
        }
    }

    // private methods
    private void finishApplication() {
        // release resources and threads
        if (gameView == null) {
            return;
        }
        gameView.releaseSynchronizations();
        gameView.stopThreads();
        gameView.releaseResources();
    }

    protected void startGame() {
        if (gameView == null) {
            return;
        }
        gameView.startGame();
        startGameButton.setEnabled(false);
        startGameButton.setVisibility(View.INVISIBLE);
        pauseGameButton.setEnabled(true);
        pauseGameButton.setVisibility(View.VISIBLE);
        resumeGameButton.setEnabled(false);
        resumeGameButton.setVisibility(View.INVISIBLE);
    }

    protected void pauseGame() {
        if (gameView == null) {
            return;
        }
        gameView.pauseGame();
        startGameButton.setEnabled(false);
        startGameButton.setVisibility(View.INVISIBLE);
        pauseGameButton.setEnabled(false);
        pauseGameButton.setVisibility(View.INVISIBLE);
        resumeGameButton.setEnabled(true);
        resumeGameButton.setVisibility(View.VISIBLE);
    }

    protected void resumeGame() {
        if (gameView == null) {
            return;
        }
        gameView.resumeGame();
        startGameButton.setEnabled(false);
        startGameButton.setVisibility(View.INVISIBLE);
        pauseGameButton.setEnabled(true);
        pauseGameButton.setVisibility(View.VISIBLE);
        resumeGameButton.setEnabled(false);
        resumeGameButton.setVisibility(View.INVISIBLE);
    }

    protected void newGame() {
        if (gameView == null) {
            return;
        }
        gameView.newGame();
        startGameButton.setEnabled(true);
        startGameButton.setVisibility(View.VISIBLE);
        pauseGameButton.setEnabled(false);
        pauseGameButton.setVisibility(View.INVISIBLE);
        resumeGameButton.setEnabled(false);
        resumeGameButton.setVisibility(View.INVISIBLE);
    }

    protected void quitGame() {
        if (gameView == null) {
            return;
        }
        // close the socket (BluetoothSocket, Wifi socket, or internet socket)
        gameView.newGame(); // set to new game (refresh the UI and stop threads) before quiting game
        if (GroundhogHunterApp.InterstitialAd != null) {
            // free version
            // int entryPoint = 0; //  no used
            ShowInterstitial.ShowAdThread showInterstitialAdThread =
                    GroundhogHunterApp.InterstitialAd.new ShowAdThread(
                            new DismissFunction() {
                                @Override
                                public void backgroundWork() {

                                }

                                @Override
                                public void executeDismiss() {
                                    returnToPrevious();
                                }

                                @Override
                                public void afterFinished(boolean isAdShown) {
                                    if (!isAdShown) returnToPrevious();
                                }
                            });
            showInterstitialAdThread.startShowAd(GroundhogHunterApp.AdProvider);
        } else {
            returnToPrevious();
        }
    }

    private void returnToPrevious() {
        Intent returnIntent = new Intent(); // used to bundle data
        setResult(Activity.RESULT_OK, returnIntent);
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

        Intent serviceIntent = new Intent(GroundhogHunterApp.AppContext,
                GlobalTop10IntentService.class);
        startService(serviceIntent);
    }

    public void disableAllButtons() {
        startGameButton.setEnabled(false);
        pauseGameButton.setEnabled(false);
        resumeGameButton.setEnabled(false);
        newGameButton.setEnabled(false);
        quitGameButton.setEnabled(false);
        settingButton.setEnabled(false);
        top10Button.setEnabled(false);
        globalTop10Button.setEnabled(false);
    }
    public void enableAllButtons() {
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

        loadingDialog = AlertDialogFragment.newInstance(loadingString,
                ScreenUtil.FontSize_Pixel_Type, textFontSize,
                Color.RED, 0, 0, true);
        loadingDialog.show(getSupportFragmentManager(), LoadingDialogTag);
    }

    public void dismissShowingLoadingMessage() {
        isShowingLoadingMessage = false;

        if (loadingDialog != null) {
            if (loadingDialog.isStateSaved()) {
                loadingDialog.dismissAllowingStateLoss();
            } else {
                loadingDialog.dismiss();
            }
        }
    }

    public void displayTwoPlayerResult(int hostScore, int hostHitNum, int clientScore, int clientHitNum) {
        Intent resultIntent = new Intent(this, TwoPlayerResultActivity.class);
        resultIntent.putExtra(Constants.HOST_SCORE, hostScore);
        resultIntent.putExtra(Constants.HOST_HIT_NUM, hostHitNum);
        resultIntent.putExtra(Constants.CLIENT_SCORE, clientScore);
        resultIntent.putExtra(Constants.CLIENT_HIT_NUM, clientHitNum);
        // startActivityForResult(resultIntent, TwoPlayerResultRequestCode);
        otherLauncher.launch(resultIntent);
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
            if (actionName == null) return;
            switch (actionName) {
                case LocalTop10IntentService.Action_Name:
                    // dismiss showing message
                    dismissShowingLoadingMessage();
                    Intent localTop10Intent = new Intent(getApplicationContext(), Top10ScoreActivity.class);
                    Bundle localTop10Extras = new Bundle();
                    localTop10Extras.putString("Top10TitleName", getString(R.string.localTop10ScoreTitleString));
                    if (extras == null) {
                        localTop10Extras.putStringArrayList("Top10Players", new ArrayList<>());
                        localTop10Extras.putIntegerArrayList("Top10Scores", new ArrayList<>());
                    } else {
                        localTop10Extras.putStringArrayList("Top10Players", extras.getStringArrayList("PlayerNames"));
                        localTop10Extras.putIntegerArrayList("Top10Scores", extras.getIntegerArrayList("PlayerScores"));
                    }
                    localTop10Intent.putExtras(localTop10Extras);
                    // startActivityForResult(localTop10Intent, LocalTop10RequestCode);
                    otherLauncher.launch(localTop10Intent);
                    break;
                case GlobalTop10IntentService.Action_Name:
                    // dismiss showing message
                    dismissShowingLoadingMessage();
                    Intent globalTop10Intent = new Intent(getApplicationContext(), Top10ScoreActivity.class);
                    Bundle globalTop10Extras = new Bundle();
                    globalTop10Extras.putString("Top10TitleName", getString(R.string.globalTop10ScoreTitleString));
                    if (extras == null) {
                        globalTop10Extras.putStringArrayList("Top10Players", new ArrayList<>());
                        globalTop10Extras.putIntegerArrayList("Top10Scores", new ArrayList<>());
                    } else {
                        globalTop10Extras.putStringArrayList("Top10Players", extras.getStringArrayList("PlayerNames"));
                        globalTop10Extras.putIntegerArrayList("Top10Scores", extras.getIntegerArrayList("PlayerScores"));
                    }
                    globalTop10Intent.putExtras(globalTop10Extras);
                    // startActivityForResult(globalTop10Intent, GlobalTop10RequestCode);
                    otherLauncher.launch(globalTop10Intent);
                    break;
            }
        }
    }
}