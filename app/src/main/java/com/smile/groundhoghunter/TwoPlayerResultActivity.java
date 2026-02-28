package com.smile.groundhoghunter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.smile.groundhoghunter.constants.Constants;
import com.smile.smilelibraries.utilities.ScreenUtil;

public class TwoPlayerResultActivity extends AppCompatActivity {

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        float textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this);
        Intent callingIntent = getIntent();
        int hostScore = callingIntent.getIntExtra(Constants.HOST_SCORE, 0);
        int hostHitNum = callingIntent.getIntExtra(Constants.HOST_HIT_NUM, 0);
        int clientScore = callingIntent.getIntExtra(Constants.CLIENT_SCORE, 0);
        int clientHitNum = callingIntent.getIntExtra(Constants.CLIENT_HIT_NUM, 0);

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            // not Oreo
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.two_player_result_layout);

        TextView gameCreatorTitle = findViewById(R.id.gameCreatorTitle);
        ScreenUtil.resizeTextSize(gameCreatorTitle, textFontSize);

        TextView hostGameScoreTitle = findViewById(R.id.hostGameScoreTitle);
        ScreenUtil.resizeTextSize(hostGameScoreTitle, textFontSize);
        TextView hostGameScoreText = findViewById(R.id.hostGameScoreText);
        hostGameScoreText.setText(String.valueOf(hostScore));
        ScreenUtil.resizeTextSize(hostGameScoreText, textFontSize);

        TextView hostGameHitNumTitle = findViewById(R.id.hostGameHitNumTitle);
        ScreenUtil.resizeTextSize(hostGameHitNumTitle, textFontSize);
        TextView hostGameHitNumText = findViewById(R.id.hostGameHitNumText);
        hostGameHitNumText.setText(String.valueOf(hostHitNum));
        ScreenUtil.resizeTextSize(hostGameHitNumText, textFontSize);

        TextView gameJoinerTitle = findViewById(R.id.gameJoinerTitle);
        ScreenUtil.resizeTextSize(gameJoinerTitle, textFontSize);

        TextView clientGameScoreTitle = findViewById(R.id.clientGameScoreTitle);
        ScreenUtil.resizeTextSize(clientGameScoreTitle, textFontSize);
        TextView clientGameScoreText = findViewById(R.id.clientGameScoreText);
        clientGameScoreText.setText(String.valueOf(clientScore));
        ScreenUtil.resizeTextSize(clientGameScoreText, textFontSize);

        TextView clientGameHitNumTitle = findViewById(R.id.clientGameHitNumTitle);
        ScreenUtil.resizeTextSize(clientGameHitNumTitle, textFontSize);
        TextView clientGameHitNumText = findViewById(R.id.clientGameHitNumText);
        clientGameHitNumText.setText(String.valueOf(clientHitNum));
        ScreenUtil.resizeTextSize(clientGameHitNumText, textFontSize);

        Button messageArea_OK_button = findViewById(R.id.messageArea_OK_button);
        ScreenUtil.resizeTextSize(messageArea_OK_button, textFontSize);
        messageArea_OK_button.setOnClickListener(view -> returnToPrevious());

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        returnToPrevious();
                    }
                });
    }

    private void returnToPrevious() {
        setResult(Activity.RESULT_OK);
        finish();
    }
}
