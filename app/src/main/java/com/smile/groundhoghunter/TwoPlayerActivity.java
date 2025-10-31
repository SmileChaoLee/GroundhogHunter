package com.smile.groundhoghunter;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatRadioButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smile.groundhoghunter.Utilities.BluetoothUtil;
import com.smile.smilelibraries.customized_button.SmileImageButton;
import com.smile.smilelibraries.utilities.FontAndBitmapUtil;
import com.smile.smilelibraries.utilities.ScreenUtil;

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
    // private String wifiDirectNotSupportedString;
    private String playerNameCannotBeEmptyString;
    private String explainProblemForBluetoothString;
    private String explainProblemForWifiString;

    private String btDeviceName;
    private String wifiDeviceName;
    private AppCompatRadioButton wifiRadioButton;
    private AppCompatRadioButton bluetoothRadioButton;
    private String thisDeviceName;
    // private WifiDirectReceiver wifiDirectReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this);
        fontScale = ScreenUtil.getPxFontScale(this);
        toastTextSize = textFontSize * 0.8f;

        bluetoothNotSupportedString = getString(R.string.bluetoothNotSupportedString);
        // wifiDirectNotSupportedString = getString(R.string.wifiDirectNotSupportedString);
        playerNameCannotBeEmptyString = getString(R.string.playerNameCannotBeEmptyString);
        explainProblemForBluetoothString = getString(R.string.explainProblemForBluetoothString);
        explainProblemForWifiString = getString(R.string.explainProblemForWifiString);

        // int colorDarkOrange = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkOrange);
        // int colorRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.red);
        int colorDarkRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkRed);
        int colorDarkGreen = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkGreen);

        // wifiDeviceName = "";
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
                setPlayerName();
            }
        });

        /*
        wifiRadioButton = findViewById(R.id.lanRadioButton);
        ScreenUtil.resizeTextSize(wifiRadioButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        wifiRadioButton.setChecked(false);
        wifiRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaType = GameView.WifiMediaType;
                thisDeviceName = wifiDeviceName;
                setPlayerName();
            }
        });

        boolean isWifiDirectSupported = WifiDirectUtil.isWifiDirectSupported(this);
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
        */

        // Bluetooth
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // this device does not support Bluetooth
            ScreenUtil.showToast(this, bluetoothNotSupportedString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
            bluetoothRadioButton.setChecked(false);
            bluetoothRadioButton.setEnabled(false);
            mediaType = GameView.NoneMediaType; // added because removed the followings
            /*
            if (wifiRadioButton.isEnabled()) {
                mediaType = GameView.WifiMediaType;
            } else {
                mediaType = GameView.NoneMediaType;
            }
            */
        } else {
            btDeviceName = BluetoothUtil.getBluetoothDeviceName(mBluetoothAdapter);
            mediaType = GameView.BluetoothMediaType;
            bluetoothRadioButton.setEnabled(true);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
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
                /*
            case GameView.WifiMediaType:
                explainProblemTextView.setText(explainProblemForWifiString);
                wifiRadioButton.setChecked(true);
                // device name from Wifi-Direct
                thisDeviceName = wifiDeviceName;
                break;
                */
            default:
                // no media supported
                explainProblemTextView.setText("");
                bluetoothRadioButton.setChecked(false);
                // wifiRadioButton.setChecked(false);
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

        int buttonLeftMargin = (int)ScreenUtil.dpToPixel(100);
        int buttonTopMargin = (int)ScreenUtil.dpToPixel(10);
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
                    // case GameView.WifiMediaType:
                    //     break;
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
                    // case GameView.WifiMediaType:
                    //     break;
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

        /*
        wifiDirectReceiver = new WifiDirectReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        registerReceiver(wifiDirectReceiver, intentFilter);
        */
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
        /*
        if (wifiDirectReceiver != null) {
            unregisterReceiver(wifiDirectReceiver);
        }
        */
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

    /*
    private class WifiDirectReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                WifiP2pDevice wifiDevice =(WifiP2pDevice)intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                wifiDeviceName = wifiDevice.deviceName;
                if (wifiRadioButton.isChecked()) {
                    thisDeviceName = wifiDeviceName;
                    setPlayerName();
                }
            }
        }
    }
    */
}
