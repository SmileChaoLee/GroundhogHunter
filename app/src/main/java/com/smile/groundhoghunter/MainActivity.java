package com.smile.groundhoghunter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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

        String singlePlayerString = getString(R.string.singlePlayerString);
        String twoPlayerString = getString(R.string.twoPlayerString);
        String exitAppString = getString(R.string.exitAppString);

        int colorDarkOrange = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkOrange);
        int colorRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.red);
        int colorDarkRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkRed);
        int colorDarkGreen = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkGreen);

        setContentView(R.layout.activity_main);

        int buttonLeftMargin = ScreenUtil.dpToPixel(this, 50);
        int buttonTopMargin = ScreenUtil.dpToPixel(this, 10);
        int buttonRightMargin = buttonLeftMargin;
        int buttonBottomMargin = buttonTopMargin;

        final SmileImageButton singlePlayerButton = findViewById(R.id.singlePlayerButton);
        Bitmap singlePlayerBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, singlePlayerString, Color.BLUE);
        singlePlayerButton.setImageBitmap(singlePlayerBitmap);
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


        final SmileImageButton twoPlayerButton = findViewById(R.id.twoPlayerButton);
        Bitmap twoPlayerBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, twoPlayerString, Color.BLUE);
        twoPlayerButton.setImageBitmap(twoPlayerBitmap);
        buttonLp = (LinearLayout.LayoutParams) twoPlayerButton.getLayoutParams();
        buttonLp.leftMargin = buttonLeftMargin;
        buttonLp.topMargin = buttonTopMargin;
        buttonLp.rightMargin = buttonRightMargin;
        buttonLp.bottomMargin = buttonBottomMargin;
        twoPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent multiPlayerIntent = new Intent(MainActivity.this, TwoPlayerActivity.class);
                startActivity(multiPlayerIntent);
            }
        });

        final SmileImageButton exitAppButton = findViewById(R.id.exitAppButton);
        Bitmap exitAppBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, exitAppString, colorDarkRed);
        exitAppButton.setImageBitmap(exitAppBitmap);
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
