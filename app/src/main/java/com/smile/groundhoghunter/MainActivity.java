package com.smile.groundhoghunter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smile.groundhoghunter.Model.SmileImageButton;
import com.smile.groundhoghunter.Utilities.FontAndBitmapUtil;
import com.smile.smilepublicclasseslibrary.utilities.ScreenUtil;

public class MainActivity extends AppCompatActivity {

    private float textFontSize;
    private float fontScale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this, GroundhogHunterApp.FontSize_Scale_Type, null);
        textFontSize = ScreenUtil.suitableFontSize(this, defaultTextFontSize, GroundhogHunterApp.FontSize_Scale_Type, 0.0f);
        fontScale = ScreenUtil.suitableFontScale(this, GroundhogHunterApp.FontSize_Scale_Type, 0.0f);

        setContentView(R.layout.activity_main);

        int darkRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkRed);
        String singlePlayerString = getString(R.string.singlePlayerString);
        String twoPlayerString = getString(R.string.twoPlayerString);
        String exitAppString = getString(R.string.exitAppString);

        int buttonLeftMargin = ScreenUtil.dpToPixel(this, 50);
        int buttonTopMargin = ScreenUtil.dpToPixel(this, 10);
        int buttonRightMargin = buttonLeftMargin;
        int buttonBottomMargin = buttonTopMargin;

        Button singlePlayerButton = findViewById(R.id.singlePlayerButton);
        ScreenUtil.resizeTextSize(singlePlayerButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        LinearLayout.LayoutParams buttonLp = (LinearLayout.LayoutParams) singlePlayerButton.getLayoutParams();
        buttonLp.leftMargin = buttonLeftMargin;
        buttonLp.topMargin = buttonTopMargin;
        buttonLp.rightMargin = buttonRightMargin;
        buttonLp.bottomMargin = buttonBottomMargin;
        singlePlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gameIntent = new Intent(MainActivity.this, GroundhogActivity.class);
                startActivity(gameIntent);
            }
        });


        Button twoPlayerButton = findViewById(R.id.twoPlayerButton);
        ScreenUtil.resizeTextSize(twoPlayerButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        buttonLp = (LinearLayout.LayoutParams) twoPlayerButton.getLayoutParams();
        buttonLp.leftMargin = buttonLeftMargin;
        buttonLp.topMargin = buttonTopMargin;
        buttonLp.rightMargin = buttonRightMargin;
        buttonLp.bottomMargin = buttonBottomMargin;

        Button exitAppButton = findViewById(R.id.exitAppButton);
        ScreenUtil.resizeTextSize(exitAppButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        buttonLp = (LinearLayout.LayoutParams) exitAppButton.getLayoutParams();
        buttonLp.leftMargin = buttonLeftMargin;
        buttonLp.topMargin = buttonTopMargin;
        buttonLp.rightMargin = buttonRightMargin;
        buttonLp.bottomMargin = buttonBottomMargin;
        exitAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitApp();;
            }
        });

        TextView companyNameTextView = findViewById(R.id.companyNameTextView);
        ScreenUtil.resizeTextSize(companyNameTextView, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        TextView companyContactEmailTextView = findViewById(R.id.companyContactEmailTextView);
        ScreenUtil.resizeTextSize(companyContactEmailTextView, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
    }

    @Override
    public void onBackPressed() {
        exitApp();
    }

    private void exitApp() {
        finish();
    }
}
