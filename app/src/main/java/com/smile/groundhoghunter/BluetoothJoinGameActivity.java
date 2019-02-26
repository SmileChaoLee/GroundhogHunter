package com.smile.groundhoghunter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.smile.groundhoghunter.ArrayAdatpers.TwoPlayerListAdapter;
import com.smile.groundhoghunter.Models.SmileImageButton;
import com.smile.groundhoghunter.Threads.BluetoothConnectToThread;
import com.smile.smilepublicclasseslibrary.utilities.FontAndBitmapUtil;
import com.smile.smilepublicclasseslibrary.utilities.ScreenUtil;

import java.util.ArrayList;
import java.util.HashSet;

public class BluetoothJoinGameActivity extends AppCompatActivity {

    // private properties
    private static final String TAG = new String("com.smile.groundhoghunter.BluetoothJoinGameActivity");
    private static final int Request_Enable_Bluetooth_For_Discovering = 2; // request to enable bluetooth for discovering
    private float textFontSize;
    private float fontScale;
    private float toastTextSize;

    private String deviceName;
    private EditText playerNameEditText;
    private String playerName;
    private String oppositePlayerName;
    private ListView playerListView;
    private ArrayList<String> playerNameList;

    private String bluetoothNotSupportedString;
    private String playerNameCannotBeEmptyString;
    private String bluetoothCannotBeTurnedOnString;
    private String scanBluetoothStartedString;
    private String scanBluetoothFinishedString;
    private String connectToHostSucceededString;
    private String connectToHostFailedString;

    private TwoPlayerListAdapter twoPlayerListAdapter;
    private BluetoothJoinGameBroadcastReceiver btJoinGameReceiver;
    private BluetoothConnectToThread mBluetoothConnectToThread;

    private boolean isDefaultBluetoothEnabled;
    private BluetoothAdapter mBluetoothAdapter;
    private HashSet<BluetoothDevice> bluetoothDeviceHashSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        bluetoothDeviceHashSet = new HashSet<BluetoothDevice>();
        playerNameList = new ArrayList<>();

        // BroadcastReceiver and register it
        btJoinGameReceiver = new BluetoothJoinGameBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothConnectToThread.BluetoothConnectToThreadConnected);
        intentFilter.addAction(BluetoothConnectToThread.BluetoothConnectToThreadFailedToConnect);
        registerReceiver(btJoinGameReceiver, intentFilter);

        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this, GroundhogHunterApp.FontSize_Scale_Type, null);
        textFontSize = ScreenUtil.suitableFontSize(this, defaultTextFontSize, GroundhogHunterApp.FontSize_Scale_Type, 0.0f);
        fontScale = ScreenUtil.suitableFontScale(this, GroundhogHunterApp.FontSize_Scale_Type, 0.0f);
        toastTextSize = textFontSize * 0.8f;

        bluetoothNotSupportedString = getString(R.string.bluetoothNotSupportedString);
        playerNameCannotBeEmptyString = getString(R.string.playerNameCannotBeEmptyString);
        bluetoothCannotBeTurnedOnString = getString(R.string.bluetoothCannotBeTurnedOnString);
        scanBluetoothStartedString = getString(R.string.scanBluetoothStartedString);
        scanBluetoothFinishedString = getString(R.string.scanBluetoothFinishedString);
        connectToHostSucceededString = getString(R.string.connectToHostSucceededString);
        connectToHostFailedString = getString(R.string.connectToHostFailedString);

        // int colorDarkOrange = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkOrange);
        // int colorRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.red);
        int colorDarkRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkRed);
        int colorDarkGreen = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkGreen);
        int colorBlue = Color.BLUE;

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_join_game);

        // device detecting
        // Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // this device does not support Bluetooth
            ScreenUtil.showToast(BluetoothJoinGameActivity.this, bluetoothNotSupportedString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
            returnToPrevious();
        }

        isDefaultBluetoothEnabled = mBluetoothAdapter.isEnabled();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        deviceName = "";
        if (mBluetoothAdapter != null) {
            deviceName = mBluetoothAdapter.getName();
            if (deviceName == null) {
                deviceName = mBluetoothAdapter.getAddress();
                if (deviceName == null) {
                    deviceName = "";
                }
            }
        }

        TextView joinGameTitleTextView = findViewById(R.id.joinGameTitleTextView);
        joinGameTitleTextView.setText(getString(R.string.joinBluetoothGameString));
        ScreenUtil.resizeTextSize(joinGameTitleTextView, textFontSize * 1.2f, GroundhogHunterApp.FontSize_Scale_Type);

        TextView playerNameStringTextView = findViewById(R.id.playerNameStringTextView);
        ScreenUtil.resizeTextSize(playerNameStringTextView, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);

        playerName = deviceName;
        playerNameEditText = findViewById(R.id.playerNameEditText);
        playerNameEditText.setEnabled(true);
        playerNameEditText.setText("");
        playerNameEditText.append(playerName);
        ScreenUtil.resizeTextSize(playerNameEditText, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);

        playerListView = findViewById(R.id.playerListView);
        twoPlayerListAdapter = new TwoPlayerListAdapter(this, R.layout.player_list_item_layout, R.id.playerNameTextView, playerNameList, textFontSize);
        twoPlayerListAdapter.setNotifyOnChange(false);  // do not call notifyDataSetChanged() method automatically
        playerListView.setAdapter(twoPlayerListAdapter);
        playerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long rowId) {
                if (adapterView != null) {
                    Object item = adapterView.getItemAtPosition(position);
                    if (item != null) {
                        String temp = item.toString();
                        Log.d(TAG, "adapterView.getItemAtPosition(position) = " + temp);
                        oppositePlayerName = temp;
                        view.setSelected(true);
                    }
                }
            }
        });

        SmileImageButton refreshJoinGameButton = findViewById(R.id.refreshJoinGameButton);
        Bitmap refreshJoinGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, getString(R.string.refreshString), colorBlue);
        refreshJoinGameButton.setImageBitmap(refreshJoinGameBitmap);
        refreshJoinGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        SmileImageButton cancelJoinGameButton = findViewById(R.id.cancelJoinGameButton);
        Bitmap cancelJoinGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, getString(R.string.cancelString), colorDarkRed);
        cancelJoinGameButton.setImageBitmap(cancelJoinGameBitmap);
        cancelJoinGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToPrevious();
            }
        });

        playerNameList.clear();
        twoPlayerListAdapter.notifyDataSetChanged();
        if (!mBluetoothAdapter.isEnabled()) {
            // has not been enabled yet
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Request_Enable_Bluetooth_For_Discovering);
        } else {
            onActivityResult(Request_Enable_Bluetooth_For_Discovering, Activity.RESULT_OK, null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Response from bluetooth activity.");

        switch(requestCode) {
            case Request_Enable_Bluetooth_For_Discovering:
                if (resultCode == Activity.RESULT_OK) {
                    // succeeded to enable bluetooth
                    bluetoothDeviceHashSet.clear(); // clear HashSet because of starting discovering
                    playerNameList.clear(); // clear name list
                    twoPlayerListAdapter.notifyDataSetChanged();
                    if (mBluetoothAdapter.isDiscovering()) {
                        // if discovering then stop discovering
                        mBluetoothAdapter.cancelDiscovery();
                    }
                    mBluetoothAdapter.startDiscovery();
                }else {
                    ScreenUtil.showToast(this, bluetoothCannotBeTurnedOnString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
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

        if (twoPlayerListAdapter != null) {
            unregisterReceiver(btJoinGameReceiver);
        }
    }

    private void returnToPrevious() {
        finish();
    }

    private class BluetoothJoinGameBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = "";
            if (intent != null) {
                action = intent.getAction();
            }
            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    BluetoothDevice mBluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // start to connect to host game
                    if (mBluetoothDevice != null) {
                        ScreenUtil.showToast(context, mBluetoothDevice.getName(), toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                        if (!bluetoothDeviceHashSet.contains(mBluetoothDevice)) {
                            bluetoothDeviceHashSet.add(mBluetoothDevice);
                            mBluetoothAdapter.cancelDiscovery();    // stop discovering
                            mBluetoothConnectToThread = new BluetoothConnectToThread(BluetoothJoinGameActivity.this, mBluetoothAdapter, mBluetoothDevice, GroundhogHunterApp.ApplicationUUID);
                            mBluetoothConnectToThread.start();
                        }
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    ScreenUtil.showToast(context, scanBluetoothStartedString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    ScreenUtil.showToast(context, scanBluetoothFinishedString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    break;
                case BluetoothConnectToThread.BluetoothConnectToThreadConnected:
                    ScreenUtil.showToast(context, connectToHostSucceededString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    String deviceName = intent.getStringExtra("BluetoothDeviceName");
                    if ((deviceName != null) && (!deviceName.isEmpty())) {
                        if (!playerNameList.contains(deviceName)) {
                            playerNameList.add(deviceName);
                            twoPlayerListAdapter.notifyDataSetChanged();
                        }
                    }
                    break;
                case BluetoothConnectToThread.BluetoothConnectToThreadFailedToConnect:
                    ScreenUtil.showToast(context, connectToHostFailedString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    mBluetoothAdapter.startDiscovery(); // start discovering again
                    break;
            }
        }
    }
}
