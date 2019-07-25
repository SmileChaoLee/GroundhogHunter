package com.smile.groundhoghunter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.util.Log;

import com.smile.groundhoghunter.Constants.CommonConstants;
import com.smile.groundhoghunter.Models.BtConnectDevice;
import com.smile.groundhoghunter.Threads.BluetoothConnectToThread;
import com.smile.groundhoghunter.Utilities.BluetoothUtil;

public class BluetoothJoinGameActivity extends JoinGameActivity {

    // private properties
    private static final String TAG = new String(".BluetoothJoinGameActivity");
    private static final int Request_Enable_Bluetooth_For_Discovering = 2; // request to enable bluetooth for discovering

    private String bluetoothCannotBeTurnedOnString;
    private String scanBluetoothStartedString;
    private String scanBluetoothFinishedString;
    private String foundDeviceString;

    private BluetoothJoinGameBroadcastReceiver btJoinGameReceiver;

    private boolean isDefaultBluetoothEnabled;
    private BluetoothAdapter mBluetoothAdapter;

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
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        isDefaultBluetoothEnabled = mBluetoothAdapter.isEnabled();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        clientConnectDevice = new BtConnectDevice(mBluetoothAdapter);

        bluetoothCannotBeTurnedOnString = getString(R.string.bluetoothCannotBeTurnedOnString);
        scanBluetoothStartedString = getString(R.string.scanBluetoothStartedString);
        scanBluetoothFinishedString = getString(R.string.scanBluetoothFinishedString);
        foundDeviceString = getString(R.string.foundDeviceString);

        // int colorDarkOrange = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkOrange);
        // int colorRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.red);
        int colorDarkRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkRed);
        int colorDarkGreen = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkGreen);
        int colorBlue = Color.BLUE;

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "BluetoothJoinGameActivity --> Came back from BtJoinGameActivity.");
        String megString;
        switch(requestCode) {
            case Request_Enable_Bluetooth_For_Discovering:
                if (resultCode == Activity.RESULT_OK) {
                    // succeeded to enable bluetooth
                    megString = "Bluetooth has been turn on.";
                    Log.d(TAG, megString);
                    if (mBluetoothAdapter.isDiscovering()) {
                        // if discovering then stop discovering
                        mBluetoothAdapter.cancelDiscovery();
                    }
                    // stop the previous discovery thread that created last time
                    // stopBluetoothDiscoveryTimerThread();     // removed on 2019-05-15
                    // start discovery
                    mBluetoothAdapter.startDiscovery();
                    // startBluetoothDiscoveryTimerThread();    // removed on 2019-05-15
                }else {
                    megString = bluetoothCannotBeTurnedOnString;
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView(bluetoothCannotBeTurnedOnString, MessageDuration);
                }
                break;
            default:
                break;
        }
    }

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

    @Override
    protected void startDiscovery() {
        super.startDiscovery();
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, Request_Enable_Bluetooth_For_Discovering);
    }

    @Override
    protected void startClientGame() {
        super.startClientGame();
        Intent gameIntent = new Intent(this, BtClientGameActivity.class);
        gameIntent.putExtra("GameType", CommonConstants.TwoPlayerGameByClient);
        startActivityForResult(gameIntent, CommonConstants.TwoPlayerGameByClient);
    }

    private class BluetoothJoinGameBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String megString;
            BluetoothDevice btDevice;
            String remoteMacAddress;
            String action = "";
            if (intent != null) {
                action = intent.getAction();
            }
            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    megString = foundDeviceString + ": " + BluetoothUtil.getBluetoothDeviceName(btDevice);
                    Log.d(TAG, megString);
                    // start to connect to host game
                    if (btDevice != null) {
                        remoteMacAddress = btDevice.getAddress();
                        if (!discoveredDeviceMap.containsKey(remoteMacAddress)) {
                            showMessage.showMessageInTextView(megString, MessageDuration);
                            BluetoothConnectToThread connectToThread = new BluetoothConnectToThread(joinGameHandler, btDevice, GroundhogHunterApp.ApplicationUUID);
                            discoveredDeviceMap.put(remoteMacAddress, connectToThread);
                        }
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    megString = scanBluetoothStartedString;
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView(scanBluetoothStartedString, MessageDuration);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    megString = scanBluetoothFinishedString;
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView(scanBluetoothFinishedString, MessageDuration);
                    break;
            }
        }
    }
}
