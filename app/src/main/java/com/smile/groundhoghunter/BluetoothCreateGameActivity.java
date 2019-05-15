package com.smile.groundhoghunter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.smile.groundhoghunter.Constants.CommonConstants;
import com.smile.groundhoghunter.Threads.BluetoothAcceptThread;

import java.util.HashMap;

public class BluetoothCreateGameActivity extends CreateGameActivity {

    private static final String TAG = new String(".BluetoothCreateGameActivity");
    private static final int Request_Enable_Bluetooth_For_Being_Discovered = 1; // request to enable bluetooth for being discovered
    private static final int Request_Enable_Bluetooth_Discoverability = 3;
    private static final int DurationForBluetoothVisible = 120;  // 120 seconds

    private String bluetoothNotSupportedString;
    private String bluetoothVisibilityIsDisabledString;
    private String bluetoothCannotBeTurnedOnString;
    private String bluetoothVisibilityForPeriodString;
    private String bluetoothCannotBeVisibleString;

    private BluetoothAdapter mBluetoothAdapter;
    private boolean isDefaultBluetoothEnabled;
    private BluetoothCreateGameBroadcastReceiver btCreateGameReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // ioFunctionThreadMap = new HashMap<>();
        // mServerAcceptThread = null;
        // selectedIoFunctionThread = null;

        bluetoothNotSupportedString = getString(R.string.bluetoothNotSupportedString);
        bluetoothVisibilityIsDisabledString = getString(R.string.bluetoothVisibilityIsDisabledString);
        bluetoothCannotBeTurnedOnString = getString(R.string.bluetoothCannotBeTurnedOnString);
        bluetoothVisibilityForPeriodString = getString(R.string.bluetoothVisibilityForPeriodString)
                + "(" + DurationForBluetoothVisible + " " + getString(R.string.secondString) + ")";
        bluetoothCannotBeVisibleString = getString(R.string.bluetoothCannotBeVisibleString);

        super.onCreate(savedInstanceState);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // this device does not support Bluetooth
            showMessage.showMessageInTextView(bluetoothNotSupportedString, MessageDuration);
            // ScreenUtil.showToast(BluetoothCreateGameActivity.this, bluetoothNotSupportedString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
            returnToPrevious();
        }

        isDefaultBluetoothEnabled = mBluetoothAdapter.isEnabled();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // BroadcastReceiver and register it
        btCreateGameReceiver = new BluetoothCreateGameBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(btCreateGameReceiver, intentFilter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "BluetoothCreateGameActivity --> Came back from BtHostGameActivity.");

        switch(requestCode) {
            case Request_Enable_Bluetooth_For_Being_Discovered:
                if (resultCode == Activity.RESULT_OK) {
                    // succeeded to enable bluetooth. Start enabling discoverability
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DurationForBluetoothVisible);
                    startActivityForResult(discoverableIntent, Request_Enable_Bluetooth_Discoverability);
                } else {
                    // ScreenUtil.showToast(this, bluetoothCannotBeTurnedOnString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    showMessage.showMessageInTextView(bluetoothCannotBeTurnedOnString, MessageDuration);
                }
                break;
            case Request_Enable_Bluetooth_Discoverability:
                if (resultCode != Activity.RESULT_CANCELED) {
                    // succeeded
                    // ScreenUtil.showToast(this, bluetoothVisibilityForPeriodString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    showMessage.showMessageInTextView(bluetoothVisibilityForPeriodString, MessageDuration);
                    // create a BluetoothSocket for listening for connection using a thread

                    mServerAcceptThread = new BluetoothAcceptThread(createGameHandler, mBluetoothAdapter, playerName, GroundhogHunterApp.ApplicationUUID);
                    mServerAcceptThread.start();
                } else {
                    // ScreenUtil.showToast(this, bluetoothCannotBeVisibleString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_LONG);
                    showMessage.showMessageInTextView(bluetoothCannotBeVisibleString, MessageDuration);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // recover the status of bluetooth
        if (mBluetoothAdapter != null) {
            if (isDefaultBluetoothEnabled) {
                mBluetoothAdapter.enable();
            } else {
                mBluetoothAdapter.disable();
            }
        }
        if (btCreateGameReceiver != null) {
            unregisterReceiver(btCreateGameReceiver);
        }
    }

    @Override
    protected void startDiscoverability() {
        super.startDiscoverability();
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, Request_Enable_Bluetooth_For_Being_Discovered);
    }

    @Override
    protected void startHostGame() {
        super.startHostGame();
        Intent gameIntent = new Intent(this, BtHostGameActivity.class);
        gameIntent.putExtra("GameType", CommonConstants.TwoPlayerGameByHost);
        startActivityForResult(gameIntent, CommonConstants.TwoPlayerGameByHost);
    }

    private class BluetoothCreateGameBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String pName;
            String action = "";
            if (intent != null) {
                action = intent.getAction();
            }
            switch (action) {
                case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
                    int extraPreviousScanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_SCAN_MODE, BluetoothAdapter.ERROR);
                    int extraScanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                    if (extraPreviousScanMode != extraScanMode) {
                        if ((extraScanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE) || (extraScanMode == BluetoothAdapter.SCAN_MODE_NONE)) {
                            showMessage.showMessageInTextView(bluetoothVisibilityIsDisabledString, MessageDuration);
                        }
                    }
                    break;
            }
        }
    }
}
