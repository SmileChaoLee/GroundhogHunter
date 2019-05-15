package com.smile.groundhoghunter;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatRadioButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
    private String wifiDirectNotSupportedString;
    private String playerNameCannotBeEmptyString;
    private String explainProblemForBluetoothString;
    private String explainProblemForWifiString;
    private String explainProblemForInternetString;

    private String btDeviceName;
    private String wifiDeviceName;
    private AppCompatRadioButton wifiRadioButton;
    private AppCompatRadioButton bluetoothRadioButton;
    private String thisDeviceName;
    private WifiDirectReceiver wifiDirectReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this, GroundhogHunterApp.FontSize_Scale_Type, null);
        textFontSize = ScreenUtil.suitableFontSize(this, defaultTextFontSize, GroundhogHunterApp.FontSize_Scale_Type, 0.0f);
        fontScale = ScreenUtil.suitableFontScale(this, GroundhogHunterApp.FontSize_Scale_Type, 0.0f);
        toastTextSize = textFontSize * 0.8f;

        bluetoothNotSupportedString = getString(R.string.bluetoothNotSupportedString);
        wifiDirectNotSupportedString = getString(R.string.wifiDirectNotSupportedString);
        playerNameCannotBeEmptyString = getString(R.string.playerNameCannotBeEmptyString);
        explainProblemForBluetoothString = getString(R.string.explainProblemForBluetoothString);
        explainProblemForWifiString = getString(R.string.explainProblemForWifiString);
        explainProblemForInternetString = getString(R.string.explainProblemForInternetString);

        // int colorDarkOrange = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkOrange);
        // int colorRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.red);
        int colorDarkRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkRed);
        int colorDarkGreen = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkGreen);

        wifiDeviceName = "";
        btDeviceName = "";
        mediaType = GameView.BluetoothMediaType;

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_two_players);

        TextView twoPlayerSettingTitleTextView = findViewById(R.id.twoPlayerSettingTitleTextView);
        ScreenUtil.resizeTextSize(twoPlayerSettingTitleTextView, textFontSize * 1.2f, GroundhogHunterApp.FontSize_Scale_Type);

        explainProblemTextView = findViewById(R.id.explainProblemTextView);
        ScreenUtil.resizeTextSize(explainProblemTextView, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);

        bluetoothRadioButton = findViewById(R.id.bluetoothRadioButton);
        ScreenUtil.resizeTextSize(bluetoothRadioButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        bluetoothRadioButton.setChecked(false);
        bluetoothRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaType = GameView.BluetoothMediaType;
                thisDeviceName = btDeviceName;
                Log.d(TAG, "btDeviceName = " + btDeviceName);
                setPlayerName();
            }
        });

        wifiRadioButton = findViewById(R.id.lanRadioButton);
        ScreenUtil.resizeTextSize(wifiRadioButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        wifiRadioButton.setChecked(false);
        wifiRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaType = GameView.wifiMediaType;
                thisDeviceName = wifiDeviceName;
                Log.d(TAG, "wifiDeviceName = " + wifiDeviceName);
                setPlayerName();
            }
        });

        boolean isWifiDirectSupported = isWifiDirectSupported(this);
        // device detecting
        wifiRadioButton.setEnabled(false);
        if (isWifiDirectSupported) {
            // Wifi-Direct
            WifiP2pManager mWifiP2pManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
            if (mWifiP2pManager != null) {
                WifiP2pManager.Channel mChannel = mWifiP2pManager.initialize(this, getMainLooper(), null);
                if (mChannel != null) {
                    wifiRadioButton.setEnabled(true);
                }
            }
        } else {
            ScreenUtil.showToast(this, wifiDirectNotSupportedString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
        }
        // Bluetooth
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // this device does not support Bluetooth
            ScreenUtil.showToast(this, bluetoothNotSupportedString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
            bluetoothRadioButton.setChecked(false);
            bluetoothRadioButton.setEnabled(false);
            if (wifiRadioButton.isEnabled()) {
                mediaType = GameView.wifiMediaType;
            } else {
                mediaType = GameView.NoneMediaType;
            }
        } else {
            btDeviceName = BluetoothUtil.getBluetoothDeviceName(mBluetoothAdapter);
            mediaType = GameView.BluetoothMediaType;
            bluetoothRadioButton.setEnabled(true);
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
        }

        switch (mediaType) {
            case GameView.BluetoothMediaType:
                explainProblemTextView.setText(explainProblemForBluetoothString);
                bluetoothRadioButton.setChecked(true);
                thisDeviceName = btDeviceName;
                break;
            case GameView.wifiMediaType:
                explainProblemTextView.setText(explainProblemForWifiString);
                wifiRadioButton.setChecked(true);
                // device name from Wifi-Direct
                thisDeviceName = wifiDeviceName;
                break;
            default:
                // no media supported
                explainProblemTextView.setText("");
                bluetoothRadioButton.setChecked(false);
                wifiRadioButton.setChecked(false);
                thisDeviceName = "";
                returnToPrevious();

                return;
        }

        TextView playerNameStringTextView = findViewById(R.id.playerNameStringTextView);
        ScreenUtil.resizeTextSize(playerNameStringTextView, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);

        playerNameEditText = findViewById(R.id.playerNameEditText);
        playerNameEditText.setEnabled(true);
        setPlayerName();
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

        wifiDirectReceiver = new WifiDirectReceiver();
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        registerReceiver(wifiDirectReceiver, mIntentFilter);
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
        if (wifiDirectReceiver != null) {
            unregisterReceiver(wifiDirectReceiver);
        }
    }

    @Override
    public void onBackPressed() {
        returnToPrevious();
    }

    private void returnToPrevious() {
        finish();
    }

    private void setPlayerName() {
        Log.d(TAG, "thisDeviceName = " + thisDeviceName);
        playerName = thisDeviceName;
        // playerNameEditText.setText("");
        // playerNameEditText.append(playerName);
        playerNameEditText.setText(playerName);
    }

    private boolean isWifiDirectSupported(Context ctx) {
        PackageManager pm = ctx.getPackageManager();
        FeatureInfo[] features = pm.getSystemAvailableFeatures();
        for (FeatureInfo info : features) {
            if (info != null && info.name != null && info.name.equalsIgnoreCase("android.hardware.wifi.direct")) {
                return true;
            }
        }
        return false;
    }

    private class WifiDirectReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                WifiP2pDevice wifiDevice =(WifiP2pDevice)intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                wifiDeviceName = wifiDevice.deviceName;
                Log.d(TAG, "Wifi Direct: My Device = " + wifiDeviceName);
                if (wifiRadioButton.isChecked()) {
                    thisDeviceName = wifiDeviceName;
                    setPlayerName();
                }
            }
        }
    }
}
