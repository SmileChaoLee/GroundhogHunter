package com.smile.groundhoghunter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Looper;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smile.groundhoghunter.constants.Constants;
import com.smile.smilelibraries.customized_button.SmileImageButton;
import com.smile.smilelibraries.utilities.FontAndBitmapUtil;
import com.smile.smilelibraries.privacy_policy.PrivacyPolicyUtil;
import com.smile.smilelibraries.utilities.ScreenUtil;

public class MainActivity extends AppCompatActivity {

    private final int PrivacyPolicyActivityRequestCode = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        float textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this);
        String singlePlayerString = getString(R.string.singlePlayerString);
        String twoPlayerString = getString(R.string.twoPlayerString);
        String privacyPolicyString = getString(com.smile.smilelibraries.R.string.privacyPolicyString);
        String exitAppString = getString(R.string.exitAppString);
        int colorDarkRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkRed);

        setContentView(R.layout.activity_main);

        int buttonLeftMargin = (int)ScreenUtil.dpToPixel(60.0f);
        int buttonTopMargin = (int)ScreenUtil.dpToPixel(10.0f);
        LinearLayout.LayoutParams buttonLp;

        final SmileImageButton singlePlayerButton = findViewById(R.id.singlePlayerButton);
        Bitmap singlePlayerBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this,
                R.drawable.normal_button_image, singlePlayerString, Color.BLUE);
        singlePlayerButton.setImageBitmap(singlePlayerBitmap);
        buttonLp = (LinearLayout.LayoutParams) singlePlayerButton.getLayoutParams();
        buttonLp.leftMargin = buttonLeftMargin;
        buttonLp.topMargin = buttonTopMargin;
        buttonLp.rightMargin = buttonLeftMargin;
        buttonLp.bottomMargin = buttonTopMargin;
        singlePlayerButton.setOnClickListener(view -> {
            Intent gameIntent = new Intent(MainActivity.this, GroundhogActivity.class);
            gameIntent.putExtra(Constants.GAME_TYPE, Constants.GAME_BY_SINGLE_PLAY);
            startActivity(gameIntent);
        });

        final SmileImageButton twoPlayerButton = findViewById(R.id.twoPlayerButton);
        Bitmap twoPlayerBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this,
                R.drawable.normal_button_image, twoPlayerString, Color.BLUE);
        twoPlayerButton.setImageBitmap(twoPlayerBitmap);
        buttonLp = (LinearLayout.LayoutParams) twoPlayerButton.getLayoutParams();
        buttonLp.leftMargin = buttonLeftMargin;
        buttonLp.topMargin = buttonTopMargin;
        buttonLp.rightMargin = buttonLeftMargin;
        buttonLp.bottomMargin = buttonTopMargin;
        twoPlayerButton.setOnClickListener(view -> {
            Intent multiPlayerIntent = new Intent(MainActivity.this,
                    TwoPlayerActivity.class);
            startActivity(multiPlayerIntent);
        });

        final SmileImageButton privacyPolicyButton = findViewById(R.id.privacyPolicyButton);
        Bitmap privacyPolicyBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this,
                R.drawable.normal_button_image, privacyPolicyString, Color.BLUE);
        privacyPolicyButton.setImageBitmap(privacyPolicyBitmap);
        buttonLp = (LinearLayout.LayoutParams) privacyPolicyButton.getLayoutParams();
        buttonLp.leftMargin = buttonLeftMargin;
        buttonLp.topMargin = buttonTopMargin;
        buttonLp.rightMargin = buttonLeftMargin;
        buttonLp.bottomMargin = buttonTopMargin;
        privacyPolicyButton.setOnClickListener(view ->
                PrivacyPolicyUtil.startPrivacyPolicyActivity(MainActivity.this,
                        PrivacyPolicyActivityRequestCode));

        final SmileImageButton exitAppButton = findViewById(R.id.exitAppButton);
        Bitmap exitAppBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this,
                R.drawable.normal_button_image, exitAppString, colorDarkRed);
        exitAppButton.setImageBitmap(exitAppBitmap);
        buttonLp = (LinearLayout.LayoutParams) exitAppButton.getLayoutParams();
        buttonLp.leftMargin = buttonLeftMargin;
        buttonLp.topMargin = buttonTopMargin;
        buttonLp.rightMargin = buttonLeftMargin;
        buttonLp.bottomMargin = buttonTopMargin;
        exitAppButton.setOnClickListener(view -> exitApplication());

        TextView companyNameTextView = findViewById(R.id.companyNameTextView);
        ScreenUtil.resizeTextSize(companyNameTextView, textFontSize);
        TextView companyContactEmailTextView = findViewById(R.id.companyContactEmailTextView);
        ScreenUtil.resizeTextSize(companyContactEmailTextView, textFontSize);

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        exitApplication();
                    }
                });
    }

    private void exitApplication() {
        final Handler handlerClose = new Handler(Looper.getMainLooper());
        final int timeDelay = 200;
        handlerClose.postDelayed(() -> {
            // quit game
            finish();
            int pid = android.os.Process.myPid();
            android.os.Process.killProcess(pid);
            System.exit(0);
        },timeDelay);
    }
}
