package com.smile.groundhoghunter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smile.groundhoghunter.Models.SmileImageButton;
import com.smile.smilepublicclasseslibrary.utilities.FontAndBitmapUtil;
import com.smile.smilepublicclasseslibrary.privacy_policy.PrivacyPolicyUtil;
import com.smile.smilepublicclasseslibrary.showing_instertitial_ads_utility.ShowingInterstitialAdsUtil;
import com.smile.smilepublicclasseslibrary.utilities.ScreenUtil;

public class MainActivity extends AppCompatActivity {

    private final int PrivacyPolicyActivityRequestCode = 10;
    private float textFontSize;
    private float fontScale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this, GroundhogHunterApp.FontSize_Scale_Type, null);
        textFontSize = ScreenUtil.suitableFontSize(this, defaultTextFontSize, GroundhogHunterApp.FontSize_Scale_Type, 0.0f);
        fontScale = ScreenUtil.suitableFontScale(this, GroundhogHunterApp.FontSize_Scale_Type, 0.0f);

        String singlePlayerString = getString(R.string.singlePlayerString);
        String twoPlayerString = getString(R.string.twoPlayerString);
        String privacyPolicyString = getString(com.smile.smilepublicclasseslibrary.R.string.privacyPolicyString);
        String exitAppString = getString(R.string.exitAppString);

        // int colorDarkOrange = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkOrange);
        // int colorRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.red);
        int colorDarkRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkRed);
        // int colorDarkGreen = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkGreen);

        setContentView(R.layout.activity_main);

        int buttonLeftMargin = ScreenUtil.dpToPixel(this, 60);
        int buttonTopMargin = ScreenUtil.dpToPixel(this, 10);
        int buttonRightMargin = buttonLeftMargin;
        int buttonBottomMargin = buttonTopMargin;
        LinearLayout.LayoutParams buttonLp;

        final SmileImageButton singlePlayerButton = findViewById(R.id.singlePlayerButton);
        Bitmap singlePlayerBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, singlePlayerString, Color.BLUE);
        singlePlayerButton.setImageBitmap(singlePlayerBitmap);
        buttonLp = (LinearLayout.LayoutParams) singlePlayerButton.getLayoutParams();
        buttonLp.leftMargin = buttonLeftMargin;
        buttonLp.topMargin = buttonTopMargin;
        buttonLp.rightMargin = buttonRightMargin;
        buttonLp.bottomMargin = buttonBottomMargin;
        singlePlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gameIntent = new Intent(MainActivity.this, GroundhogActivity.class);
                gameIntent.putExtra("GameType", GameView.SinglePlayerGame);
                startActivity(gameIntent);
            }
        });


        final SmileImageButton twoPlayerButton = findViewById(R.id.twoPlayerButton);
        Bitmap twoPlayerBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, twoPlayerString, Color.BLUE);

        /*
        if (!BuildConfig.DEBUG) {
            twoPlayerButton.setEnabled(false);
            twoPlayerButton.setVisibility(View.GONE);
        }
        */

        twoPlayerButton.setImageBitmap(twoPlayerBitmap);
        buttonLp = (LinearLayout.LayoutParams) twoPlayerButton.getLayoutParams();
        buttonLp.leftMargin = buttonLeftMargin;
        buttonLp.topMargin = buttonTopMargin;
        buttonLp.rightMargin = buttonRightMargin;
        buttonLp.bottomMargin = buttonBottomMargin;
        twoPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent multiPlayerIntent = new Intent(MainActivity.this, TwoPlayerActivity.class);
                startActivity(multiPlayerIntent);
            }
        });

        final SmileImageButton privacyPolicyButton = findViewById(R.id.privacyPolicyButton);
        Bitmap privacyPolicyBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, privacyPolicyString, Color.BLUE);
        privacyPolicyButton.setImageBitmap(privacyPolicyBitmap);
        buttonLp = (LinearLayout.LayoutParams) privacyPolicyButton.getLayoutParams();
        buttonLp.leftMargin = buttonLeftMargin;
        buttonLp.topMargin = buttonTopMargin;
        buttonLp.rightMargin = buttonRightMargin;
        buttonLp.bottomMargin = buttonBottomMargin;
        privacyPolicyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrivacyPolicyUtil.startPrivacyPolicyActivity(MainActivity.this, GroundhogHunterApp.PrivacyPolicyUrl, PrivacyPolicyActivityRequestCode);
            }
        });

        final SmileImageButton exitAppButton = findViewById(R.id.exitAppButton);
        Bitmap exitAppBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, exitAppString, colorDarkRed);
        exitAppButton.setImageBitmap(exitAppBitmap);
        buttonLp = (LinearLayout.LayoutParams) exitAppButton.getLayoutParams();
        buttonLp.leftMargin = buttonLeftMargin;
        buttonLp.topMargin = buttonTopMargin;
        buttonLp.rightMargin = buttonRightMargin;
        buttonLp.bottomMargin = buttonBottomMargin;
        exitAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitGame();;
            }
        });

        TextView companyNameTextView = findViewById(R.id.companyNameTextView);
        ScreenUtil.resizeTextSize(companyNameTextView, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        TextView companyContactEmailTextView = findViewById(R.id.companyContactEmailTextView);
        ScreenUtil.resizeTextSize(companyContactEmailTextView, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
    }

    @Override
    public void onBackPressed() {
        exitGame();
    }

    private void exitGame() {
        if (GroundhogHunterApp.InterstitialAd != null) {
            // free version
            int entryPoint = 0; //  no used
            ShowingInterstitialAdsUtil.ShowAdAsyncTask showAdAsyncTask =
                    GroundhogHunterApp.InterstitialAd.new ShowAdAsyncTask(MainActivity.this
                            , entryPoint
                            , new ShowingInterstitialAdsUtil.AfterDismissFunctionOfShowAd() {
                        @Override
                        public void executeAfterDismissAds(int endPoint) {
                            exitApplication();
                        }
                    });
            showAdAsyncTask.execute();
        } else {
            exitApplication();
        }
    }
    private void exitApplication() {
        final Handler handlerClose = new Handler();
        final int timeDelay = 200;
        handlerClose.postDelayed(new Runnable() {
            public void run() {
                // quit game
                finish();
                int pid = android.os.Process.myPid();
                android.os.Process.killProcess(pid);
                System.exit(0);
            }
        },timeDelay);
    }
}
