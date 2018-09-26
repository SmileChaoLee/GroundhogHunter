package com.smile.groundhoghunter;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.ToggleButton;

import com.smile.groundhoghunter.Utilities.ScreenUtil;

public class SettingActivity extends AppCompatActivity {

    private float textFontSize;
    private ToggleButton soundSwitch;
    private ToggleButton mutiPlayerSwitch;
    private boolean hasSound;
    private boolean isSinglePlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        textFontSize = 30;
        hasSound = true;
        isSinglePlayer = true;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            textFontSize = extras.getFloat("TextFontSize");
            hasSound = extras.getBoolean("HasSound");
            isSinglePlayer = extras.getBoolean("IsSinglePlayer");
        }

        if (textFontSize == 50) {
            // not a cell phone, it is a tablet
            setTheme(R.style.ThemeTextSize50Transparent);
        } else {
            setTheme(R.style.ThemeTextSize30Transparent);
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting);

        soundSwitch = findViewById(R.id.soundSwitch);
        soundSwitch.setTextSize(textFontSize);
        soundSwitch.setChecked(hasSound);
        soundSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hasSound = ((ToggleButton)view).isChecked();
            }
        });

        mutiPlayerSwitch = findViewById(R.id.multiPlayerSwitch);
        mutiPlayerSwitch.setTextSize(textFontSize);
        mutiPlayerSwitch.setChecked(isSinglePlayer);
        mutiPlayerSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSinglePlayer = ((ToggleButton)view).isChecked();
            }
        });

        /*
        ApplicationInfo appInfo = getApplicationContext().getApplicationInfo();
        boolean isDebuggable = (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        if (!isDebuggable) {
            // release mode
            mutiPlayerSwitch.setEnabled(false);
            mutiPlayerSwitch.setVisibility(View.GONE);
        }
        */

        Button confirmButton = findViewById(R.id.confirmSettingButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToPrevious(true);
            }
        });

        Button cancelButton = findViewById(R.id.cancelSettingButton);
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
