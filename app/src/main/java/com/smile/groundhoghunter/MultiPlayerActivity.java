package com.smile.groundhoghunter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

public class MultiPlayerActivity extends AppCompatActivity {

    // private properties
    private float textFontSize;
    private float fontScale;
    private int mediaType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        float defaultTextFontSize = com.smile.smilepublicclasseslibrary.utilities.ScreenUtil.getDefaultTextSizeFromTheme(this);
        textFontSize = com.smile.smilepublicclasseslibrary.utilities.ScreenUtil.suitableFontSize(this, defaultTextFontSize, 0.0f);
        fontScale = com.smile.smilepublicclasseslibrary.utilities.ScreenUtil.suitableFontScale(this, 0.0f);

        mediaType = GameView.BluetoothMediaType;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mediaType = extras.getInt("MediaType");
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_multi_player);

        TextView multiPlayerSettingTitle = findViewById(R.id.multiPlayerSettingTitle);
        multiPlayerSettingTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textFontSize);

        final RadioButton bluetoothRadioButton = findViewById(R.id.bluetoothRadioButton);
        bluetoothRadioButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textFontSize);
        bluetoothRadioButton.setChecked(false);
        bluetoothRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaType = GameView.BluetoothMediaType;
            }
        });
        final RadioButton lanRadioButton = findViewById(R.id.lanRadioButton);
        lanRadioButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textFontSize);
        lanRadioButton.setChecked(false);
        lanRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaType = GameView.LanMediaType;
            }
        });
        final RadioButton internetRadioButton = findViewById(R.id.internetRadioButton);
        internetRadioButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textFontSize);
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
        cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textFontSize);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToPrevious(false);
            }
        });

        final Button confirmButton = findViewById(R.id.confirmMultiPlayerButton);
        confirmButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textFontSize);
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
