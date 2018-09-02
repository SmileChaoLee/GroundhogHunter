package com.smile.groundhoghunter;

import android.app.Activity;
import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        textFontSize = 30;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            textFontSize = extras.getFloat("TextFontSize");
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

        Button confirmButton = findViewById(R.id.confirmSettingButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToPrevious();
            }
        });

        Button cancelButton = findViewById(R.id.cancelSettingButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
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
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);    // can bundle some data to previous activity
        finish();
    }
}
