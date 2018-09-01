package com.smile.groundhoghunter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.smile.groundhoghunter.Utilities.ScreenUtil;

public class SettingActivity extends AppCompatActivity {

    private float textFontSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting);

        textFontSize = 30;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            textFontSize = extras.getFloat("TextFontSize");
        }

        setTheme(R.style.ThemeTextSize30Transparent);
        if (textFontSize == 50) {
            // not a cell phone, it is a tablet
            setTheme(R.style.ThemeTextSize50Transparent);
        }
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
