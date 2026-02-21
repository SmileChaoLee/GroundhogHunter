package com.smile.groundhoghunter;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresPermission;
import android.util.Log;
import com.smile.groundhoghunter.constants.CommonConstants;
import com.smile.groundhoghunter.Models.BtConnectDevice;
import com.smile.groundhoghunter.Threads.BluetoothConnectToThread;
import com.smile.groundhoghunter.Utilities.BluetoothUtil;

public class BluetoothJoinGameActivity extends JoinGameActivity {

    private static final String TAG = "BTJoinGameAct";
    private String bluetoothCannotBeTurnedOnString;
    private String scanBluetoothStartedString;
    private String scanBluetoothFinishedString;
    private String foundDeviceString;
    private BluetoothJoinGameBroadcastReceiver btJoinGameReceiver;
    private boolean isDefaultBluetoothEnabled;
    private BluetoothAdapter mBluetoothAdapter;
    private ActivityResultLauncher<Intent> enableBtLauncher;

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // BroadcastReceiver and register it
        btJoinGameReceiver = new BluetoothJoinGameBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(btJoinGameReceiver, intentFilter);

        // device detecting
        // Bluetooth
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        isDefaultBluetoothEnabled = mBluetoothAdapter.isEnabled();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        clientConnectDevice = new BtConnectDevice(mBluetoothAdapter);

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
                        if (mBluetoothAdapter.isDiscovering()) {
                            mBluetoothAdapter.cancelDiscovery();
                        }
                        Log.d(TAG, "enableBtLauncher.startDiscovery.");
                        boolean isOK = mBluetoothAdapter.startDiscovery();
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
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            if (isDefaultBluetoothEnabled) {
                mBluetoothAdapter.enable();
            } else {
                mBluetoothAdapter.disable();
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
        gameIntent.putExtra(CommonConstants.GAME_TYPE, CommonConstants.TwoPlayerGameByClient);
        // startActivityForResult(gameIntent, CommonConstants.TwoPlayerGameByClient);
        startActivity(gameIntent);
    }

    private class BluetoothJoinGameBroadcastReceiver extends BroadcastReceiver {
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
            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    Log.d(TAG, "onReceive.ACTION_FOUND");
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    megString = foundDeviceString + ": " + BluetoothUtil.getBluetoothDeviceName(btDevice);
                    Log.d(TAG, megString);
                    // start to connect to host game
                    remoteMacAddress = btDevice.getAddress();
                    if (!discoveredDeviceMap.containsKey(remoteMacAddress)) {
                        showMessage.showMessageInTextView(megString, MessageDuration);
                        BluetoothConnectToThread connectToThread = new BluetoothConnectToThread(joinGameHandler, btDevice, GroundhogHunterApp.ApplicationUUID);
                        discoveredDeviceMap.put(remoteMacAddress, connectToThread);
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    Log.d(TAG, "onReceive.ACTION_DISCOVERY_STARTED");
                    megString = scanBluetoothStartedString;
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView(scanBluetoothStartedString, MessageDuration);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Log.d(TAG, "onReceive.ACTION_DISCOVERY_FINISHED");
                    megString = scanBluetoothFinishedString;
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView(scanBluetoothFinishedString, MessageDuration);
                    break;
            }
        }
    }
}
