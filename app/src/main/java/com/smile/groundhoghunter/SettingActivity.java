package com.smile.groundhoghunter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.smile.smilelibraries.utilities.ScreenUtil;

public class SettingActivity extends AppCompatActivity {

    private float textFontSize;
    private float fontScale;
    private ToggleButton soundSwitch;
    private boolean hasSound;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this);
        fontScale = ScreenUtil.getPxFontScale(this);

        hasSound = true;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            hasSound = extras.getBoolean("HasSound");
        }

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            // not Oreo
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.activity_setting);

        TextView settingTitle = findViewById(R.id.settingTitle);
        ScreenUtil.resizeTextSize(settingTitle, textFontSize);
        TextView soundSettingTitle = findViewById(R.id.soundSettingTitle);
        ScreenUtil.resizeTextSize(soundSettingTitle, textFontSize);

        soundSwitch = findViewById(R.id.soundSwitch);
        ScreenUtil.resizeTextSize(soundSwitch, textFontSize);
        soundSwitch.setChecked(hasSound);
        soundSwitch.setOnClickListener(view -> hasSound = ((ToggleButton)view).isChecked());

        Button confirmButton = findViewById(R.id.confirmSettingButton);
        ScreenUtil.resizeTextSize(confirmButton, textFontSize);
        confirmButton.setOnClickListener(view -> returnToPrevious(true));

        Button cancelButton = findViewById(R.id.cancelSettingButton);
        ScreenUtil.resizeTextSize(cancelButton, textFontSize);
        cancelButton.setOnClickListener(view -> returnToPrevious(false));

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        returnToPrevious(false);
                    }
                });
    }

    private void returnToPrevious(boolean confirmed) {

        Intent returnIntent = new Intent();
        Bundle extras = new Bundle();
        extras.putBoolean("HasSound", hasSound);
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
