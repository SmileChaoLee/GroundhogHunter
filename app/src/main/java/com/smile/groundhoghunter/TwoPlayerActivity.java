package com.smile.groundhoghunter;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatRadioButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smile.groundhoghunter.Models.SmileImageButton;
import com.smile.groundhoghunter.Utilities.BluetoothUtil;
import com.smile.smilepublicclasseslibrary.utilities.FontAndBitmapUtil;
import com.smile.smilepublicclasseslibrary.utilities.ScreenUtil;

public class TwoPlayerActivity extends AppCompatActivity {

    private static final String TAG = "TwoPlayerActivity";
    // private properties
    private float textFontSize;
    private float fontScale;
    private float toastTextSize;

    private int mediaType;
    private TextView explainProblemTextView;
    private EditText playerNameEditText;
    private String playerName;
    private String bluetoothNotSupportedString;
    private String playerNameCannotBeEmptyString;
    private String explainProblemForBluetoothString;
    private String explainProblemForWifiString;
    private String explainProblemForInternetString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this, GroundhogHunterApp.FontSize_Scale_Type, null);
        textFontSize = ScreenUtil.suitableFontSize(this, defaultTextFontSize, GroundhogHunterApp.FontSize_Scale_Type, 0.0f);
        fontScale = ScreenUtil.suitableFontScale(this, GroundhogHunterApp.FontSize_Scale_Type, 0.0f);
        toastTextSize = textFontSize * 0.8f;

        bluetoothNotSupportedString = getString(R.string.bluetoothNotSupportedString);
        playerNameCannotBeEmptyString = getString(R.string.playerNameCannotBeEmptyString);
        explainProblemForBluetoothString = getString(R.string.explainProblemForBluetoothString);
        explainProblemForWifiString = getString(R.string.explainProblemForWifiString);
        explainProblemForInternetString = getString(R.string.explainProblemForInternetString);

        // int colorDarkOrange = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkOrange);
        // int colorRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.red);
        int colorDarkRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkRed);
        int colorDarkGreen = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkGreen);

        mediaType = GameView.BluetoothMediaType;

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_two_players);

        TextView twoPlayerSettingTitleTextView = findViewById(R.id.twoPlayerSettingTitleTextView);
        ScreenUtil.resizeTextSize(twoPlayerSettingTitleTextView, textFontSize * 1.2f, GroundhogHunterApp.FontSize_Scale_Type);

        explainProblemTextView = findViewById(R.id.explainProblemTextView);
        ScreenUtil.resizeTextSize(explainProblemTextView, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);

        final AppCompatRadioButton bluetoothRadioButton = findViewById(R.id.bluetoothRadioButton);
        ScreenUtil.resizeTextSize(bluetoothRadioButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        bluetoothRadioButton.setChecked(false);
        bluetoothRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaType = GameView.BluetoothMediaType;
            }
        });

        final AppCompatRadioButton wifiRadioButton = findViewById(R.id.lanRadioButton);
        ScreenUtil.resizeTextSize(wifiRadioButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        wifiRadioButton.setChecked(false);
        wifiRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaType = GameView.wifiMediaType;
            }
        });
        final AppCompatRadioButton internetRadioButton = findViewById(R.id.internetRadioButton);
        ScreenUtil.resizeTextSize(internetRadioButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        internetRadioButton.setChecked(false);
        internetRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaType = GameView.InternetMediaType;
            }
        });

        // device detecting
        // Bluetooth
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // this device does not support Bluetooth
            ScreenUtil.showToast(TwoPlayerActivity.this, bluetoothNotSupportedString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
            bluetoothRadioButton.setChecked(false);
            bluetoothRadioButton.setEnabled(false);
            if (wifiRadioButton.isEnabled()) {
                mediaType = GameView.wifiMediaType;
            } else if (internetRadioButton.isEnabled()) {
                mediaType = GameView.InternetMediaType;
            } else {
                mediaType = GameView.NoneMediaType;
            }
        } else {
            mediaType = GameView.BluetoothMediaType;
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
        }

        switch (mediaType) {
            case GameView.BluetoothMediaType:
                explainProblemTextView.setText(explainProblemForBluetoothString);
                bluetoothRadioButton.setChecked(true);
                break;
            case GameView.wifiMediaType:
                explainProblemTextView.setText(explainProblemForWifiString);
                wifiRadioButton.setChecked(true);
                break;
            case GameView.InternetMediaType:
                explainProblemTextView.setText(explainProblemForInternetString);
                internetRadioButton.setChecked(true);
                break;
            default:
                // no media supported
                explainProblemTextView.setText("");
                bluetoothRadioButton.setChecked(false);
                wifiRadioButton.setChecked(false);
                internetRadioButton.setChecked(false);
                returnToPrevious();

                return;
        }

        TextView playerNameStringTextView = findViewById(R.id.playerNameStringTextView);
        ScreenUtil.resizeTextSize(playerNameStringTextView, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);

        String deviceName = BluetoothUtil.getBluetoothDeviceName(mBluetoothAdapter);
        playerName = deviceName;
        playerNameEditText = findViewById(R.id.playerNameEditText);
        playerNameEditText.setEnabled(true);
        playerNameEditText.setText("");
        playerNameEditText.append(playerName);
        ScreenUtil.resizeTextSize(playerNameEditText, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        playerNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                playerName = editable.toString();
            }
        });

        int buttonLeftMargin = ScreenUtil.dpToPixel(this, 100);
        int buttonTopMargin = ScreenUtil.dpToPixel(this, 10);
        int buttonRightMargin = buttonLeftMargin;
        int buttonBottomMargin = buttonTopMargin;
        LinearLayout.LayoutParams buttonLp;

        final SmileImageButton createGameButton = findViewById(R.id.createTwoPlayerGameButton);
        Bitmap createGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, getString(R.string.createString), colorDarkGreen);
        createGameButton.setImageBitmap(createGameBitmap);
        buttonLp = (LinearLayout.LayoutParams) createGameButton.getLayoutParams();
        buttonLp.leftMargin = buttonLeftMargin;
        buttonLp.topMargin = buttonTopMargin;
        buttonLp.rightMargin = buttonRightMargin;
        buttonLp.bottomMargin = buttonBottomMargin;
        createGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Host game. Turn on Bluetooth and make this device visible to others
                if (playerName.isEmpty()) {
                    ScreenUtil.showToast(TwoPlayerActivity.this, playerNameCannotBeEmptyString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    return;
                }
                Intent gameIntent;
                switch (mediaType) {
                    case GameView.BluetoothMediaType:
                        gameIntent = new Intent(TwoPlayerActivity.this, BluetoothCreateGameActivity.class);
                        gameIntent.putExtra("PlayerName", playerName);
                        startActivity(gameIntent);
                        break;
                }
            }
        });

        final SmileImageButton joinGameButton = findViewById(R.id.joinTwoPlayerGameButton);
        Bitmap joinGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, getString(R.string.joinString), colorDarkGreen);
        joinGameButton.setImageBitmap(joinGameBitmap);
        buttonLp = (LinearLayout.LayoutParams) joinGameButton.getLayoutParams();
        buttonLp.leftMargin = buttonLeftMargin;
        buttonLp.topMargin = buttonTopMargin;
        buttonLp.rightMargin = buttonRightMargin;
        buttonLp.bottomMargin = buttonBottomMargin;
        joinGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playerName.isEmpty()) {
                    ScreenUtil.showToast(TwoPlayerActivity.this, playerNameCannotBeEmptyString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    return;
                }
                Intent gameIntent;
                switch (mediaType) {
                    case GameView.BluetoothMediaType:
                        gameIntent = new Intent(TwoPlayerActivity.this, BluetoothJoinGameActivity.class);
                        gameIntent.putExtra("PlayerName", playerName);
                        startActivity(gameIntent);
                        break;
                }
            }
        });

        final SmileImageButton cancelButton = findViewById(R.id.exitTwoPlayerActivityButton);
        Bitmap cancelGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, getString(R.string.exitString), colorDarkRed);
        cancelButton.setImageBitmap(cancelGameBitmap);
        buttonLp = (LinearLayout.LayoutParams) cancelButton.getLayoutParams();
        buttonLp.leftMargin = buttonLeftMargin;
        buttonLp.topMargin = buttonTopMargin;
        buttonLp.rightMargin = buttonRightMargin;
        buttonLp.bottomMargin = buttonBottomMargin;
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToPrevious();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        returnToPrevious();
    }

    private void returnToPrevious() {
        finish();
    }
}
