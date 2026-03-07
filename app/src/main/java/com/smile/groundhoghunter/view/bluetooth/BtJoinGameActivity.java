package com.smile.groundhoghunter.view.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresPermission;
import android.util.Log;

import com.smile.groundhoghunter.view.JoinGameActivity;
import com.smile.groundhoghunter.R;
import com.smile.groundhoghunter.constants.Constants;
import com.smile.groundhoghunter.models.BtConnectDevice;
import com.smile.groundhoghunter.threads.BtConnectToThread;
import com.smile.groundhoghunter.utilities.BluetoothUtil;

public class BtJoinGameActivity extends JoinGameActivity {

    private static final String TAG = "BtJoinGameAct";
    private String bluetoothCannotBeTurnedOnString;
    private String scanBluetoothStartedString;
    private String scanBluetoothFinishedString;
    private String foundDeviceString;
    private BtJoinGameBroadcastReceiver btJoinGameReceiver;
    private boolean isDefaultBluetoothEnabled;
    private BluetoothAdapter mBtAdapter;
    private ActivityResultLauncher<Intent> enableBtLauncher;

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // BroadcastReceiver and register it
        btJoinGameReceiver = new BtJoinGameBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(btJoinGameReceiver, intentFilter);

        // device detecting
        // Bluetooth
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = bluetoothManager.getAdapter();
        isDefaultBluetoothEnabled = mBtAdapter.isEnabled();
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        clientConnectDevice = new BtConnectDevice(mBtAdapter);

        bluetoothCannotBeTurnedOnString = getString(R.string.bluetoothCannotBeTurnedOnString);
        scanBluetoothStartedString = getString(R.string.scanBluetoothStartedString);
        scanBluetoothFinishedString = getString(R.string.scanBluetoothFinishedString);
        foundDeviceString = getString(R.string.foundDeviceString);

        enableBtLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d(TAG, "enableBtLauncher.Came back from BtJoinGameActivity.");
                    String megString;
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // succeeded to enable bluetooth
                        megString = "enableBtLauncher.Bluetooth has been turn on.";
                        Log.d(TAG, megString);
                        // Note: Ensure you have the BLUETOOTH_SCAN permission here
                        if (mBtAdapter.isDiscovering()) {
                            Log.d(TAG, "enableBtLauncher.cancelDiscovery()");
                            mBtAdapter.cancelDiscovery();
                        }
                        boolean isBtEnabled = mBtAdapter.isEnabled();
                        Log.d(TAG, "enableBtLauncher.isBtEnabled = " + isBtEnabled);
                        Log.d(TAG, "enableBtLauncher.startDiscovery.");
                        boolean isOK = mBtAdapter.startDiscovery();
                        Log.d(TAG, "enableBtLauncher.startDiscovery.isOK = " + isOK);
                    } else {
                        megString = "enableBtLauncher." + bluetoothCannotBeTurnedOnString;
                        Log.d(TAG, megString);
                        showMessage.showMessageInTextView(bluetoothCannotBeTurnedOnString, MessageDuration);
                    }
                }
        );
        super.onCreate(savedInstanceState);
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // recover the status of bluetooth
        if (mBtAdapter != null) {
            if (mBtAdapter.isDiscovering()) {
                mBtAdapter.cancelDiscovery();
            }
            if (isDefaultBluetoothEnabled) {
                mBtAdapter.enable();
            } else {
                mBtAdapter.disable();
            }
        }
        if (btJoinGameReceiver != null) {
            unregisterReceiver(btJoinGameReceiver);
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    protected void startDiscovery() {
        super.startDiscovery();
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        // startActivityForResult(enableBtIntent, Request_Enable_Bluetooth_For_Discovering);
        enableBtLauncher.launch(enableBtIntent);
    }

    @Override
    protected void startClientGame() {
        super.startClientGame();
        Intent gameIntent = new Intent(this, BtClientGameActivity.class);
        gameIntent.putExtra(Constants.GAME_TYPE, Constants.TWO_PLAY_GAME_BY_CLIENT);
        // startActivityForResult(gameIntent, CommonConstants.TwoPlayerGameByClient);
        startActivity(gameIntent);
    }

    private class BtJoinGameBroadcastReceiver extends BroadcastReceiver {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive");
            String megString;
            BluetoothDevice btDevice;
            String remoteMacAddress;
            if (intent == null) return;
            String action = intent.getAction();
            if (action == null) return;
            String logStr;
            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    logStr = "onReceive.ACTION_FOUND";
                    Log.d(TAG, logStr);
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice.class);
                    } else {
                        btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    }
                    if (btDevice == null) break;
                    megString = foundDeviceString + ": " + BluetoothUtil.getBluetoothDeviceName(btDevice);
                    Log.d(TAG, logStr + "." + megString);
                    // start to connect to host game
                    remoteMacAddress = btDevice.getAddress();
                    if (!discoveredDeviceMap.containsKey(remoteMacAddress)) {
                        Log.d(TAG, logStr + ".discoveredDeviceMap.not contains " + remoteMacAddress);
                        showMessage.showMessageInTextView(megString, MessageDuration);
                        BtConnectToThread connectToThread = new BtConnectToThread(joinGameHandler, btDevice,
                                Constants.APP_UUID);
                        discoveredDeviceMap.put(remoteMacAddress, connectToThread);
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    logStr = "onReceive.ACTION_DISCOVERY_STARTED";
                    Log.d(TAG, logStr);
                    megString = scanBluetoothStartedString;
                    Log.d(TAG, logStr + "." + megString);
                    showMessage.showMessageInTextView(scanBluetoothStartedString, MessageDuration);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    logStr = "onReceive.ACTION_DISCOVERY_FINISHED";
                    Log.d(TAG, logStr);
                    megString = scanBluetoothFinishedString;
                    Log.d(TAG, logStr + "." + megString);
                    showMessage.showMessageInTextView(scanBluetoothFinishedString, MessageDuration);
                    break;
            }
        }
    }
}
