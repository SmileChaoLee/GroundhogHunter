package com.smile.groundhoghunter.view.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresPermission;

import com.smile.groundhoghunter.view.CreateGameActivity;
import com.smile.groundhoghunter.GHogHunterApp;
import com.smile.groundhoghunter.R;
import com.smile.groundhoghunter.constants.Constants;
import com.smile.groundhoghunter.threads.BluetoothAcceptThread;

public class BtCreateGameActivity extends CreateGameActivity {

    private static final String TAG = "BtCreateGameAct";
    private static final int DurationForBluetoothVisible = 120;  // 120 seconds
    private String bluetoothVisibilityIsDisabledString;
    private String bluetoothCannotBeTurnedOnString;
    private String bluetoothVisibilityForPeriodString;
    private String bluetoothCannotBeVisibleString;
    private BluetoothAdapter mBtAdapter;
    private boolean isDefaultBluetoothEnabled;
    private BtCreateGameBroadcastReceiver btCreateGameReceiver;
    private ActivityResultLauncher<Intent> discoverableLauncher;
    private ActivityResultLauncher<Intent> enableBtLauncher;

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // BroadcastReceiver and register it
        btCreateGameReceiver = new BtCreateGameBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(btCreateGameReceiver, intentFilter);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = bluetoothManager.getAdapter();
        isDefaultBluetoothEnabled = mBtAdapter.isEnabled();
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        bluetoothVisibilityIsDisabledString = getString(R.string.bluetoothVisibilityIsDisabledString);
        bluetoothCannotBeTurnedOnString = getString(R.string.bluetoothCannotBeTurnedOnString);
        bluetoothVisibilityForPeriodString = getString(R.string.bluetoothVisibilityForPeriodString)
                + "(" + DurationForBluetoothVisible + " " + getString(R.string.secondString) + ")";
        bluetoothCannotBeVisibleString = getString(R.string.bluetoothCannotBeVisibleString);
        discoverableLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    if (resultCode != Activity.RESULT_CANCELED) {
                        // succeeded
                        showMessage.showMessageInTextView(bluetoothVisibilityForPeriodString, MessageDuration);
                        // create a BluetoothSocket for listening for connection using a thread
                        mServerAcceptThread = new BluetoothAcceptThread(createGameHandler, mBtAdapter, playerName,
                                Constants.APP_UUID);
                        mServerAcceptThread.start();
                    } else {
                        showMessage.showMessageInTextView(bluetoothCannotBeVisibleString, MessageDuration);
                    }
                });
        enableBtLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Log.d(TAG, "enableBtLauncher.resultCode = " + resultCode);
                    if (resultCode == Activity.RESULT_OK) {
                        // succeeded to enable bluetooth. Start enabling discoverability
                        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DurationForBluetoothVisible);
                        // startActivityForResult(discoverableIntent, Request_Enable_Bluetooth_Discoverability);
                        Log.d(TAG, "enableBtLauncher.discoverableLauncher");
                        discoverableLauncher.launch(discoverableIntent);
                    } else {
                        Log.d(TAG, "enableBtLauncher.Not Activity.RESULT_OK");
                        showMessage.showMessageInTextView(bluetoothCannotBeTurnedOnString, MessageDuration);
                    }
                });

        super.onCreate(savedInstanceState);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // recover the status of bluetooth
        if (mBtAdapter != null) {
            if (isDefaultBluetoothEnabled) {
                mBtAdapter.enable();
            } else {
                mBtAdapter.disable();
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
        // startActivityForResult(enableBtIntent, Request_Enable_Bluetooth_For_Being_Discovered);
        enableBtLauncher.launch(enableBtIntent);
    }

    @Override
    protected void startHostGame() {
        super.startHostGame();
        Intent gameIntent = new Intent(this, BtHostGameActivity.class);
        gameIntent.putExtra(Constants.GAME_TYPE, Constants.TWO_PLAY_GAME_BY_HOST);
        // startActivityForResult(gameIntent, CommonConstants.TwoPlayerGameByHost);
        startActivity(gameIntent);
    }

    private class BtCreateGameBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // String pName;
            if (intent == null) return;
            String action = intent.getAction();
            if (action == null) return;
            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                int extraPreviousScanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_SCAN_MODE, BluetoothAdapter.ERROR);
                int extraScanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                if (extraPreviousScanMode != extraScanMode) {
                    if ((extraScanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE) || (extraScanMode == BluetoothAdapter.SCAN_MODE_NONE)) {
                        showMessage.showMessageInTextView(bluetoothVisibilityIsDisabledString, MessageDuration);
                    }
                }
            }
        }
    }
}
