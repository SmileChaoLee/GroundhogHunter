package com.smile.groundhoghunter;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatRadioButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smile.groundhoghunter.Utilities.BluetoothUtil;
import com.smile.groundhoghunter.constants.CommonConstants;
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
    private String playerNameCannotBeEmptyString;
    private String btDeviceName;
    private String thisDeviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // private properties
        float textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this);
        toastTextSize = textFontSize * 0.8f;

        // String bluetoothNotSupportedString = getString(R.string.bluetoothNotSupportedString);
        playerNameCannotBeEmptyString = getString(R.string.playerNameCannotBeEmptyString);
        String explainProblemForBluetoothString = getString(R.string.explainProblemForBluetoothString);
        int colorDarkRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkRed);
        int colorDarkGreen = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkGreen);

        btDeviceName = "";
        mediaType = GameView.BluetoothMediaType;

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_two_players);

        TextView twoPlayerSettingTitleTextView = findViewById(R.id.twoPlayerSettingTitleTextView);
        ScreenUtil.resizeTextSize(twoPlayerSettingTitleTextView, textFontSize * 1.2f);

        TextView explainProblemTextView = findViewById(R.id.explainProblemTextView);
        ScreenUtil.resizeTextSize(explainProblemTextView, textFontSize);

        AppCompatRadioButton bluetoothRadioButton = findViewById(R.id.bluetoothRadioButton);
        ScreenUtil.resizeTextSize(bluetoothRadioButton, textFontSize);
        bluetoothRadioButton.setChecked(false);
        bluetoothRadioButton.setOnClickListener(view -> {
            mediaType = GameView.BluetoothMediaType;
            thisDeviceName = btDeviceName;
            setPlayerName();
        });

        if (mediaType == GameView.BluetoothMediaType) {
            explainProblemTextView.setText(explainProblemForBluetoothString);
            bluetoothRadioButton.setChecked(true);
            thisDeviceName = btDeviceName;
        } else {// no media supported
            explainProblemTextView.setText("");
            bluetoothRadioButton.setChecked(false);
            thisDeviceName = "";
            returnToPrevious();
            return;
        }

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
        Bitmap createGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, getString(R.string.createString), colorDarkGreen);
        createGameButton.setImageBitmap(createGameBitmap);
        buttonLp = (LinearLayout.LayoutParams) createGameButton.getLayoutParams();
        buttonLp.leftMargin = buttonLeftMargin;
        buttonLp.topMargin = buttonTopMargin;
        buttonLp.rightMargin = buttonLeftMargin;
        buttonLp.bottomMargin = buttonTopMargin;
        createGameButton.setOnClickListener(view -> {
            // Host game. Turn on Bluetooth and make this device visible to others
            if (playerName.isEmpty()) {
                ScreenUtil.showToast(TwoPlayerActivity.this,
                        playerNameCannotBeEmptyString, toastTextSize, Toast.LENGTH_SHORT);
                return;
            }
            Intent gameIntent;
            if (mediaType == GameView.BluetoothMediaType) {
                gameIntent = new Intent(TwoPlayerActivity.this,
                        BluetoothCreateGameActivity.class);
                gameIntent.putExtra(CommonConstants.PLAYER_NAME, playerName);
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
            if (playerName.isEmpty()) {
                ScreenUtil.showToast(TwoPlayerActivity.this,
                        playerNameCannotBeEmptyString, toastTextSize, Toast.LENGTH_SHORT);
                return;
            }
            Intent gameIntent;
            if (mediaType == GameView.BluetoothMediaType) {
                gameIntent = new Intent(TwoPlayerActivity.this,
                        BluetoothJoinGameActivity.class);
                gameIntent.putExtra(CommonConstants.PLAYER_NAME, playerName);
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

        // Bluetooth
        askBluetoothPermission();
    }

    private void askBluetoothPermission() {
        // Check for permissions at runtime if Android 12 or higher
        String logStr = "askBluetoothPermission";
        Log.d(TAG, logStr);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.d(TAG, logStr + ".checkSelfPermission");
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, logStr + ".checkSelfPermission.not PERMISSION_GRANTED");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                        REQUEST_BLUETOOTH_PERMISSIONS);
                return;
            } else {
                // maybe it is PERMISSION_GRANTED when it is just asking but not needed now
                // ,so it is always PERMISSION_GRANTED until when it needed
                Log.d(TAG, logStr + ".checkSelfPermission.PERMISSION_GRANTED");
            }
        }
        Log.d(TAG, logStr + ".< API 31 or PERMISSION_GRANTED");
        isBluetoothPermitted = true;
        initBluetooth();
    }

    private void initBluetooth() {
        String logStr = "initBluetooth";
        Log.d(TAG, logStr);
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.d(TAG, logStr + ".mBluetoothAdapter == null");
            String bluetoothNotSupportedString = getString(R.string.bluetoothNotSupportedString);
            ScreenUtil.showToast(this, bluetoothNotSupportedString, toastTextSize, Toast.LENGTH_SHORT);
            AppCompatRadioButton bluetoothRadioButton = findViewById(R.id.bluetoothRadioButton);
            bluetoothRadioButton.setChecked(false);
            bluetoothRadioButton.setEnabled(false);
            mediaType = GameView.NoneMediaType;
            returnToPrevious(); // unable to do 2 players
            return;
        }
        // If we have permissions (or are on an older version), run the logic
        Log.d(TAG, logStr + ".mBluetoothAdapter != null");
        accessBluetoothHardware(mBluetoothAdapter);
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
                mediaType = GameView.NoneMediaType;
                returnToPrevious(); // unable to do 2 players
                return;
            }
            mediaType = GameView.BluetoothMediaType;
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            isBluetoothPermitted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    isBluetoothPermitted = false;
                    break;
                }
            }
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
