package com.smile.groundhoghunter;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.smile.groundhoghunter.Utilities.ScreenUtil;

public class SettingActivity extends AppCompatActivity {

    private float textFontSize;
    private float fontScale;
    private ToggleButton soundSwitch;
    private ToggleButton multiPlayerSwitch;
    private boolean hasSound;
    private boolean isSinglePlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        float defaultTextFontSize = com.smile.smilepublicclasseslibrary.utilities.ScreenUtil.getDefaultTextSizeFromTheme(this);
        textFontSize = com.smile.smilepublicclasseslibrary.utilities.ScreenUtil.suitableFontSize(this, defaultTextFontSize, 0.0f);
        fontScale = com.smile.smilepublicclasseslibrary.utilities.ScreenUtil.suitableFontScale(this, 0.0f);

        hasSound = true;
        isSinglePlayer = true;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            hasSound = extras.getBoolean("HasSound");
            isSinglePlayer = extras.getBoolean("IsSinglePlayer");
        }

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            // not Oreo
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.activity_setting);

        TextView settingTitle = findViewById(R.id.settingTitle);
        settingTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textFontSize);
        TextView soundSettingTitle = findViewById(R.id.soundSettingTitle);
        soundSettingTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textFontSize);
        soundSwitch = findViewById(R.id.soundSwitch);
        soundSwitch.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textFontSize);
        soundSwitch.setChecked(hasSound);
        soundSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hasSound = ((ToggleButton)view).isChecked();
            }
        });

        Button confirmButton = findViewById(R.id.confirmSettingButton);
        confirmButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textFontSize);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToPrevious(true);
            }
        });

        Button cancelButton = findViewById(R.id.cancelSettingButton);
        cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textFontSize);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToPrevious(false);
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
        extras.putBoolean("HasSound", hasSound);
        extras.putBoolean("IsSinglePlayer", isSinglePlayer);
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
