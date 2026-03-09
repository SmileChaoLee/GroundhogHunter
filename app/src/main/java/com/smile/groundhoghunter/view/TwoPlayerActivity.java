package com.smile.groundhoghunter.view;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.core.location.LocationManagerCompat;

import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smile.groundhoghunter.R;
import com.smile.groundhoghunter.view.bluetooth.BtCreateGameActivity;
import com.smile.groundhoghunter.view.bluetooth.BtJoinGameActivity;
import com.smile.groundhoghunter.utilities.BluetoothUtil;
import com.smile.groundhoghunter.constants.Constants;
import com.smile.smilelibraries.customized_button.SmileImageButton;
import com.smile.smilelibraries.utilities.FontAndBitmapUtil;
import com.smile.smilelibraries.utilities.ScreenUtil;

public class TwoPlayerActivity extends AppCompatActivity {

    private static final String TAG = "TwoPlayerAct";
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 101;
    private boolean isBluetoothPermitted = false;
    private float toastTextSize;
    private int mediaType;
    private EditText playerNameEditText;
    private String playerName;
    private String btDeviceName;
    private String thisDeviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        float textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this);
        toastTextSize = textFontSize * 0.8f;
        String playerNameCannotBeEmptyString = getString(R.string.playerNameCannotBeEmptyString);
        int colorDarkRed = ContextCompat.getColor(this, R.color.darkRed);
        int colorDarkGreen = ContextCompat.getColor(this, R.color.darkGreen);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_two_players);

        TextView twoPlayerSettingTitleTextView = findViewById(R.id.twoPlayerSettingTitleTextView);
        ScreenUtil.resizeTextSize(twoPlayerSettingTitleTextView, textFontSize * 1.2f);

        TextView explainProblemTextView = findViewById(R.id.explainProblemTextView);
        ScreenUtil.resizeTextSize(explainProblemTextView, textFontSize);

        btDeviceName = "";
        mediaType = GameView.BT_MEDIA_TYPE;
        AppCompatRadioButton btRadioButton = findViewById(R.id.bluetoothRadioButton);
        btRadioButton.setVisibility(View.VISIBLE);
        ScreenUtil.resizeTextSize(btRadioButton, textFontSize);
        btRadioButton.setChecked(true);
        btRadioButton.setOnClickListener(view -> {
            explainProblemTextView.setText(getString(R.string.explainProblemForBluetoothString));
            btRadioButton.setChecked(true);
            mediaType = GameView.BT_MEDIA_TYPE;
            thisDeviceName = btDeviceName;
            setPlayerName();
        });
        AppCompatRadioButton wifiRadioButton = findViewById(R.id.wifiRadioButton);
        wifiRadioButton.setVisibility(View.VISIBLE);
        ScreenUtil.resizeTextSize(wifiRadioButton, textFontSize);
        wifiRadioButton.setChecked(false);
        wifiRadioButton.setOnClickListener(view -> {
            explainProblemTextView.setText(getString(R.string.explainProblemForWifiString));
            wifiRadioButton.setChecked(true);
            mediaType = GameView.WIFI_MEDIA_TYPE;
            thisDeviceName = btDeviceName;
            setPlayerName();
        });

        TextView playerNameStringTextView = findViewById(R.id.playerNameStringTextView);
        ScreenUtil.resizeTextSize(playerNameStringTextView, textFontSize);

        playerNameEditText = findViewById(R.id.playerNameEditText);
        playerNameEditText.setEnabled(true);
        setPlayerName();
        ScreenUtil.resizeTextSize(playerNameEditText, textFontSize);
        playerNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                playerName = editable.toString();
            }
        });

        int buttonLeftMargin = (int)ScreenUtil.dpToPixel(100);
        int buttonTopMargin = (int)ScreenUtil.dpToPixel(10);
        LinearLayout.LayoutParams buttonLp;

        final SmileImageButton createGameButton = findViewById(R.id.createTwoPlayerGameButton);
        Bitmap createGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this,
                R.drawable.normal_button_image, getString(R.string.createString), colorDarkGreen);
        createGameButton.setImageBitmap(createGameBitmap);
        buttonLp = (LinearLayout.LayoutParams) createGameButton.getLayoutParams();
        buttonLp.leftMargin = buttonLeftMargin;
        buttonLp.topMargin = buttonTopMargin;
        buttonLp.rightMargin = buttonLeftMargin;
        buttonLp.bottomMargin = buttonTopMargin;
        createGameButton.setOnClickListener(view -> {
            Log.d(TAG, "createGameButton.setOnClickListener");
            if (playerName.isEmpty()) {
                ScreenUtil.showToast(TwoPlayerActivity.this,
                        playerNameCannotBeEmptyString, toastTextSize, Toast.LENGTH_SHORT);
                return;
            }
            Intent gameIntent;
            if (mediaType == GameView.BT_MEDIA_TYPE) {
                Log.d(TAG, "createGameButton.setOnClickListener.BtCreateGameActivity");
                gameIntent = new Intent(TwoPlayerActivity.this,
                        BtCreateGameActivity.class);
                gameIntent.putExtra(Constants.PLAYER_NAME, playerName);
                startActivity(gameIntent);
            }
        });

        final SmileImageButton joinGameButton = findViewById(R.id.joinTwoPlayerGameButton);
        Bitmap joinGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this,
                R.drawable.normal_button_image, getString(R.string.joinString), colorDarkGreen);
        joinGameButton.setImageBitmap(joinGameBitmap);
        buttonLp = (LinearLayout.LayoutParams) joinGameButton.getLayoutParams();
        buttonLp.leftMargin = buttonLeftMargin;
        buttonLp.topMargin = buttonTopMargin;
        buttonLp.rightMargin = buttonLeftMargin;
        buttonLp.bottomMargin = buttonTopMargin;
        joinGameButton.setOnClickListener(view -> {
            Log.d(TAG, "joinGameButton.setOnClickListener");
            if (playerName.isEmpty()) {
                ScreenUtil.showToast(TwoPlayerActivity.this,
                        playerNameCannotBeEmptyString, toastTextSize, Toast.LENGTH_SHORT);
                return;
            }
            Intent gameIntent;
            if (mediaType == GameView.BT_MEDIA_TYPE) {
                Log.d(TAG, "joinGameButton.setOnClickListener.BtJoinGameActivity");
                gameIntent = new Intent(TwoPlayerActivity.this,
                        BtJoinGameActivity.class);
                gameIntent.putExtra(Constants.PLAYER_NAME, playerName);
                startActivity(gameIntent);
            }
        });

        final SmileImageButton cancelButton = findViewById(R.id.exitTwoPlayerActivityButton);
        Bitmap cancelGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this,
                R.drawable.normal_button_image, getString(R.string.exitString), colorDarkRed);
        cancelButton.setImageBitmap(cancelGameBitmap);
        buttonLp = (LinearLayout.LayoutParams) cancelButton.getLayoutParams();
        buttonLp.leftMargin = buttonLeftMargin;
        buttonLp.topMargin = buttonTopMargin;
        buttonLp.rightMargin = buttonLeftMargin;
        buttonLp.bottomMargin = buttonTopMargin;
        cancelButton.setOnClickListener(view -> returnToPrevious());

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        returnToPrevious();
                    }
                });

        ActivityResultLauncher<Intent> locationLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d(TAG, "onCreate.locationLauncher.result");
                    if (isLocationEnabled()) {
                        askBluetoothPermission();
                    } else {
                        returnToPrevious(); // unable to do 2 players
                    }
                }
        );

        // Bluetooth
        if (!isLocationEnabled()) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            locationLauncher.launch(intent);
        } else {
            Log.d(TAG, "onCreate.isLocationEnabled = true");
            askBluetoothPermission();
        }
    }

    private boolean isLocationEnabled() {
        Log.d(TAG, "isLocationEnabled");
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean result = LocationManagerCompat.isLocationEnabled(lm);
        Log.d(TAG, "isLocationEnabled.result = " + result);
        return result;
    }

    private void askBluetoothPermission() {
        // Check for permissions at runtime if Android 12 or higher
        String logStr = "askBluetoothPermission";
        Log.d(TAG, logStr);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.d(TAG, logStr + ".checkSelfPermission");
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, logStr + ".checkSelfPermission.not PERMISSION_GRANTED");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_BLUETOOTH_PERMISSIONS);
                return;
            } else {
                // maybe it is PERMISSION_GRANTED when it is just asking but not needed now
                // ,so it is always PERMISSION_GRANTED until when it needed
                Log.d(TAG, logStr + ".checkSelfPermission.PERMISSION_GRANTED");
            }
        } else {
            Log.d(TAG, logStr + ".< API 31 or PERMISSION_GRANTED");
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, logStr + ".checkSelfPermission.ACCESS_FINE_LOCATION.not PERMISSION_GRANTED");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_BLUETOOTH_PERMISSIONS);
                return;
            }
        }
        isBluetoothPermitted = true;
        initBluetooth();
    }

    private void initBluetooth() {
        String logStr = "initBluetooth";
        Log.d(TAG, logStr);
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter btAdapter = bluetoothManager.getAdapter();
        if (btAdapter == null) {
            Log.d(TAG, logStr + ".btAdapter == null");
            String bluetoothNotSupportedString = getString(R.string.bluetoothNotSupportedString);
            ScreenUtil.showToast(this, bluetoothNotSupportedString, toastTextSize, Toast.LENGTH_SHORT);
            AppCompatRadioButton bluetoothRadioButton = findViewById(R.id.bluetoothRadioButton);
            bluetoothRadioButton.setChecked(false);
            bluetoothRadioButton.setEnabled(false);
            mediaType = GameView.NONE_MEDIA_TYPE;
            returnToPrevious(); // unable to do 2 players
            return;
        }
        // If we have permissions (or are on an older version), run the logic
        Log.d(TAG, logStr + ".btAdapter != null");
        accessBluetoothHardware(btAdapter);
    }

    private void accessBluetoothHardware(BluetoothAdapter adapter) {
        String logStr = "accessBluetoothHardware";
        Log.d(TAG, logStr);
        try {
            btDeviceName = BluetoothUtil.getBluetoothDeviceName(adapter);
            thisDeviceName = btDeviceName;
            setPlayerName(); // Update the EditText with the name
            AppCompatRadioButton bluetoothRadioButton = findViewById(R.id.bluetoothRadioButton);
            bluetoothRadioButton.setEnabled(true);
            if (thisDeviceName == null || thisDeviceName.isEmpty()) {
                mediaType = GameView.NONE_MEDIA_TYPE;
                returnToPrevious(); // unable to do 2 players
                return;
            }
            mediaType = GameView.BT_MEDIA_TYPE;
            if (adapter.isDiscovering()) {
                adapter.cancelDiscovery();
            }
            Log.d(TAG, logStr + ".thisDeviceName = " + thisDeviceName);
        } catch (SecurityException e) {
            Log.e(TAG, logStr + ".Permission denied even after check", e);
            returnToPrevious(); // unable to do 2 players
        }
    }

    // Handle the user's response to the permission popup
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        String logStr = "onRequestPermissionsResult";
        Log.d(TAG, logStr + ".requestCode = " + requestCode);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            isBluetoothPermitted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    isBluetoothPermitted = false;
                    break;
                }
            }
            Log.d(TAG, logStr + ".isBluetoothPermitted = " + isBluetoothPermitted);
            if (isBluetoothPermitted) {
                initBluetooth();
            } else {
                ScreenUtil.showToast(this,
                        "Bluetooth permissions are required for multiplayer.",
                        toastTextSize, Toast.LENGTH_SHORT);
                returnToPrevious(); // unable to do 2 players
            }
        }
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

    private void returnToPrevious() {
        finish();
    }

    private void setPlayerName() {
        Log.d(TAG, "thisDeviceName = " + thisDeviceName);
        playerName = thisDeviceName;
        playerNameEditText.setText(playerName);
    }
}
