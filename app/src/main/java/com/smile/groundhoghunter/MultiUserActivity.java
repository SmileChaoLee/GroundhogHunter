package com.smile.groundhoghunter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

public class MultiUserActivity extends AppCompatActivity {

    // private properties
    private float textFontSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        textFontSize = 30;
        float scaleX = 1.0f;
        float scaleY = 1.0f;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            textFontSize = extras.getFloat("TextFontSize");
        }

        if (textFontSize == 50) {
            // not a cell phone, it is a tablet
            setTheme(R.style.ThemeTextSize50Transparent);
            scaleX = 2.0f;
            scaleY = 2.0f;
        } else {
            setTheme(R.style.ThemeTextSize30Transparent);
            scaleX = 1.0f;
            scaleY = 1.0f;
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_multi_user);

        final RadioButton bluetoothRadioButton = findViewById(R.id.bluetoothRadioButton);
        final RadioButton lanRadioButton = findViewById(R.id.lanRadioButton);
        final RadioButton internetRadioButton = findViewById(R.id.internetRadioButton);

        final Button cancelButton = findViewById(R.id.cancelMultiUserButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToPrevious(false);
            }
        });

        final Button confirmButton = findViewById(R.id.confirmMultiUserButton);
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
