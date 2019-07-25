package com.smile.groundhoghunter;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.smile.smilelibraries.utilities.ScreenUtil;

public class TwoPlayerResultActivity extends AppCompatActivity {

    private float textFontSize;
    private float fontScale;

    private TextView gameCreatorTitle;
    private TextView hostGameScoreTitle;
    private TextView hostGameScoreText;
    private TextView hostGameHitNumTitle;
    private TextView hostGameHitNumText;
    private TextView gameJoinerTitle;
    private TextView clientGameScoreTitle;
    private TextView clientGameScoreText;
    private TextView clientGameHitNumTitle;
    private TextView clientGameHitNumText;
    private Button messageArea_OK_button;

    private int hostScore;
    private int hostHitNum;
    private int clientScore;
    private int clientHitNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this, GroundhogHunterApp.FontSize_Scale_Type, null);
        textFontSize = ScreenUtil.suitableFontSize(this, defaultTextFontSize, GroundhogHunterApp.FontSize_Scale_Type, 0.0f);
        fontScale = ScreenUtil.suitableFontScale(this, GroundhogHunterApp.FontSize_Scale_Type, 0.0f);

        Intent callingIntent = getIntent();
        hostScore = callingIntent.getIntExtra("HostScore", 0);
        hostHitNum = callingIntent.getIntExtra("HostHitNum", 0);
        clientScore = callingIntent.getIntExtra("ClientScore", 0);
        clientHitNum = callingIntent.getIntExtra("ClientHitNum", 0);

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            // not Oreo
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.two_player_result_layout);

        gameCreatorTitle = findViewById(R.id.gameCreatorTitle);
        ScreenUtil.resizeTextSize(gameCreatorTitle, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);

        hostGameScoreTitle = findViewById(R.id.hostGameScoreTitle);
        ScreenUtil.resizeTextSize(hostGameScoreTitle, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        hostGameScoreText = findViewById(R.id.hostGameScoreText);
        hostGameScoreText.setText(String.valueOf(hostScore));
        ScreenUtil.resizeTextSize(hostGameScoreText, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);

        hostGameHitNumTitle = findViewById(R.id.hostGameHitNumTitle);
        ScreenUtil.resizeTextSize(hostGameHitNumTitle, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        hostGameHitNumText = findViewById(R.id.hostGameHitNumText);
        hostGameHitNumText.setText(String.valueOf(hostHitNum));
        ScreenUtil.resizeTextSize(hostGameHitNumText, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);

        gameJoinerTitle = findViewById(R.id.gameJoinerTitle);
        ScreenUtil.resizeTextSize(gameJoinerTitle, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);

        clientGameScoreTitle = findViewById(R.id.clientGameScoreTitle);
        ScreenUtil.resizeTextSize(clientGameScoreTitle, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        clientGameScoreText = findViewById(R.id.clientGameScoreText);
        clientGameScoreText.setText(String.valueOf(clientScore));
        ScreenUtil.resizeTextSize(clientGameScoreText, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);

        clientGameHitNumTitle = findViewById(R.id.clientGameHitNumTitle);
        ScreenUtil.resizeTextSize(clientGameHitNumTitle, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        clientGameHitNumText = findViewById(R.id.clientGameHitNumText);
        clientGameHitNumText.setText(String.valueOf(clientHitNum));
        ScreenUtil.resizeTextSize(clientGameHitNumText, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);

        messageArea_OK_button = findViewById(R.id.messageArea_OK_button);
        ScreenUtil.resizeTextSize(messageArea_OK_button, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        messageArea_OK_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToPrevious();
            }
        });

    }

    @Override
    public void onBackPressed() {
        returnToPrevious();
    }

    private void returnToPrevious() {
        setResult(Activity.RESULT_OK);
        finish();
    }
}
