package com.smile.groundhoghunter;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.smile.smilepublicclasseslibrary.utilities.ScreenUtil;

public class MultiPlayerActivity extends AppCompatActivity {

    // private properties
    private float textFontSize;
    private float fontScale;
    private int mediaType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this, GroundhogHunterApp.FontSize_Scale_Type, null);
        textFontSize = ScreenUtil.suitableFontSize(this, defaultTextFontSize, GroundhogHunterApp.FontSize_Scale_Type, 0.0f);
        fontScale = ScreenUtil.suitableFontScale(this, GroundhogHunterApp.FontSize_Scale_Type, 0.0f);

        mediaType = GameView.BluetoothMediaType;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mediaType = extras.getInt("MediaType");
        }

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            // not Oreo
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.activity_multi_player);

        TextView twoPlayerSettingTitle = findViewById(R.id.twoPlayerSettingTitle);
        ScreenUtil.resizeTextSize(twoPlayerSettingTitle, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);

        final RadioButton bluetoothRadioButton = findViewById(R.id.bluetoothRadioButton);
        ScreenUtil.resizeTextSize(bluetoothRadioButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        bluetoothRadioButton.setChecked(false);
        bluetoothRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaType = GameView.BluetoothMediaType;
            }
        });
        final RadioButton lanRadioButton = findViewById(R.id.lanRadioButton);
        ScreenUtil.resizeTextSize(lanRadioButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        lanRadioButton.setChecked(false);
        lanRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaType = GameView.LanMediaType;
            }
        });
        final RadioButton internetRadioButton = findViewById(R.id.internetRadioButton);
        ScreenUtil.resizeTextSize(internetRadioButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        internetRadioButton.setChecked(false);
        internetRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaType = GameView.InternetMediaType;
            }
        });

        switch (mediaType) {
            case GameView.BluetoothMediaType:
                bluetoothRadioButton.setChecked(true);
                break;
            case GameView.LanMediaType:
                lanRadioButton.setChecked(true);
                break;
            case GameView.InternetMediaType:
                internetRadioButton.setChecked(true);
                break;
        }

        final Button cancelButton = findViewById(R.id.cancelMultiPlayerButton);
        ScreenUtil.resizeTextSize(cancelButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToPrevious(false);
            }
        });

        final Button confirmButton = findViewById(R.id.confirmMultiPlayerButton);
        ScreenUtil.resizeTextSize(confirmButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToPrevious(true);
            }
        });
    }

    @Override
    public void onBackPressed() {
        returnToPrevious(false);
    }

    private void returnToPrevious(boolean confirmed) {

        Intent returnIntent = new Intent();
        Bundle extras = new Bundle();
        extras.putInt("MediaType", mediaType);
        returnIntent.putExtras(extras);

        int resultYn = Activity.RESULT_OK;
        if (!confirmed) {
            // cancelled
            resultYn = Activity.RESULT_CANCELED;
        }

        setResult(resultYn, returnIntent);    // can bundle some data to previous activity
        finish();
    }
}
