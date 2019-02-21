package com.smile.groundhoghunter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.smile.groundhoghunter.Models.SmileImageButton;
import com.smile.groundhoghunter.Threads.BluetoothAcceptThread;
import com.smile.groundhoghunter.Threads.BluetoothConnectToThread;
import com.smile.groundhoghunter.Utilities.FontAndBitmapUtil;
import com.smile.smilepublicclasseslibrary.utilities.ScreenUtil;

import java.util.ArrayList;
import java.util.List;

public class TwoPlayerActivity extends AppCompatActivity {

    private static final String TAG = "TwoPlayerActivity";
    private static final int Request_Enable_Bluetooth_For_Being_Discovered = 1; // request to enable bluetooth for being discovered
    private static final int Request_Enable_Bluetooth_For_Discovering = 2; // request to enable bluetooth for discovering
    private static final int Request_Enable_Bluetooth_Discoverability = 3;
    private static final int durationForBluetoothVisible = 60;   // 300 sec.
    // private properties
    private float textFontSize;
    private float fontScale;
    private float toastTextSize;

    private boolean isDefaultBluetoothEnabled;
    private int mediaType;
    private BluetoothAdapter mBluetoothAdapter;

    private TextView playerNameTextView;
    private String playerName;
    private String oppositePlayerName;
    private ListView playerListView;
    private ArrayList<String> playerNameList;
    private String bluetoothAlreadyOnString;
    private String bluetoothNotSupportedString;
    private String playNameCannotBeEmptyString;
    private String bluetoothVisibilityForPeriodString;
    private String bluetoothVisibilityIsDisabledString;
    private String bluetoothRefusedByUserString;
    private String bluetoothCannotBeTurnedOnString;
    private String bluetoothCannotBeVisibleString;
    private String scanBluetoothStartedString;
    private String scanBluetoothFinishedString;
    private String stopScanningBluetoothString;
    private String deviceNameString;
    private String waitingForConnectionString;
    private String waitingIsStoppedOrTimeoutString;
    private String connectToHostSucceededString;
    private String connectToHostFailedString;

    private TwoPlayerListAdapter twoPlayerListAdapter;
    private TwoPlayerBroadcastReceiver twoPlayerReceiver;
    private BluetoothAcceptThread mBluetoothAcceptThread;
    private BluetoothConnectToThread mBluetoothConnectToThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this, GroundhogHunterApp.FontSize_Scale_Type, null);
        textFontSize = ScreenUtil.suitableFontSize(this, defaultTextFontSize, GroundhogHunterApp.FontSize_Scale_Type, 0.0f);
        fontScale = ScreenUtil.suitableFontScale(this, GroundhogHunterApp.FontSize_Scale_Type, 0.0f);
        toastTextSize = textFontSize * 0.8f;

        bluetoothAlreadyOnString = getString(R.string.bluetoothAlreadyOnString);
        bluetoothNotSupportedString = getString(R.string.bluetoothNotSupportedString);
        playNameCannotBeEmptyString = getString(R.string.playNameCannotBeEmptyString);
        bluetoothVisibilityForPeriodString = getString(R.string.bluetoothVisibilityForPeriodString)
                + "(" + durationForBluetoothVisible + " " + getString(R.string.secondString) + ")";
        bluetoothVisibilityIsDisabledString = getString(R.string.bluetoothVisibilityIsDisabledString);
        bluetoothRefusedByUserString = getString(R.string.bluetoothRefusedByUserString);
        bluetoothCannotBeTurnedOnString = getString(R.string.bluetoothCannotBeTurnedOnString);
        bluetoothCannotBeVisibleString = getString(R.string.bluetoothCannotBeVisibleString);
        scanBluetoothStartedString = getString(R.string.scanBluetoothStartedString);
        stopScanningBluetoothString = getString(R.string.stopScanningBluetoothString);
        scanBluetoothFinishedString = getString(R.string.scanBluetoothFinishedString);
        deviceNameString = getString(R.string.deviceNameString);
        waitingForConnectionString = getString(R.string.waitingForConnectionString);
        waitingIsStoppedOrTimeoutString = getString(R.string.waitingIsStoppedOrTimeoutString);
        connectToHostSucceededString = getString(R.string.connectToHostSucceededString);
        connectToHostFailedString = getString(R.string.connectToHostFailedString);

        int colorDarkOrange = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkOrange);
        int colorRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.red);
        int colorDarkRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkRed);
        int colorDarkGreen = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkGreen);

        mediaType = GameView.BluetoothMediaType;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mediaType = extras.getInt("MediaType");
        }

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            // not Oreo
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        // BroadcastReceiver and register it
        twoPlayerReceiver = new TwoPlayerBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothAcceptThread.BluetoothAcceptThreadStarted);
        intentFilter.addAction(BluetoothAcceptThread.BluetoothAcceptThreadConnected);
        intentFilter.addAction(BluetoothAcceptThread.BluetoothAcceptThreadStopped);
        intentFilter.addAction(BluetoothConnectToThread.BluetoothConnectToThreadConnected);
        intentFilter.addAction(BluetoothConnectToThread.BluetoothConnectToThreadFailedToConnect);
        registerReceiver(twoPlayerReceiver, intentFilter);

        setContentView(R.layout.activity_two_players);

        playerListView = findViewById(R.id.playerListView);
        playerNameList = new ArrayList<>();
        twoPlayerListAdapter = new TwoPlayerListAdapter(this, R.layout.player_list_item_layout, R.id.playerNameTextView, playerNameList);
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

        TextView twoPlayerSettingTitle = findViewById(R.id.twoPlayerSettingTitle);
        ScreenUtil.resizeTextSize(twoPlayerSettingTitle, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);

        final AppCompatRadioButton bluetoothRadioButton = findViewById(R.id.bluetoothRadioButton);
        ScreenUtil.resizeTextSize(bluetoothRadioButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        bluetoothRadioButton.setChecked(false);
        bluetoothRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaType = GameView.BluetoothMediaType;
            }
        });

        final AppCompatRadioButton wifiRadioButton = findViewById(R.id.lanRadioButton);
        ScreenUtil.resizeTextSize(wifiRadioButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        wifiRadioButton.setChecked(false);
        wifiRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaType = GameView.wifiMediaType;
            }
        });
        final AppCompatRadioButton internetRadioButton = findViewById(R.id.internetRadioButton);
        ScreenUtil.resizeTextSize(internetRadioButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        internetRadioButton.setChecked(false);
        internetRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaType = GameView.InternetMediaType;
            }
        });

        // device detecting
        // Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // this device does not support Bluetooth
            ScreenUtil.showToast(TwoPlayerActivity.this, bluetoothNotSupportedString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
            bluetoothRadioButton.setChecked(false);
            bluetoothRadioButton.setEnabled(false);
            if (wifiRadioButton.isEnabled()) {
                mediaType = GameView.wifiMediaType;
            } else if (internetRadioButton.isEnabled()) {
                mediaType = GameView.InternetMediaType;
            } else {
                mediaType = GameView.NoneMediaType;
            }
        } else {
            mediaType = GameView.BluetoothMediaType;
            isDefaultBluetoothEnabled = mBluetoothAdapter.isEnabled();
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
        }

        switch (mediaType) {
            case GameView.BluetoothMediaType:
                bluetoothRadioButton.setChecked(true);
                break;
            case GameView.wifiMediaType:
                wifiRadioButton.setChecked(true);
                break;
            case GameView.InternetMediaType:
                internetRadioButton.setChecked(true);
                break;
            default:
                // no media supported
                bluetoothRadioButton.setChecked(false);
                wifiRadioButton.setChecked(false);
                internetRadioButton.setChecked(false);

                returnToPrevious(false);
                return;
        }

        TextView deviceNameStringTextView = findViewById(R.id.deviceNameStringTextView);
        ScreenUtil.resizeTextSize(deviceNameStringTextView, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);

        playerName = "";
        if (mBluetoothAdapter != null) {
            playerName = mBluetoothAdapter.getName();
            if (playerName == null) {
                playerName = mBluetoothAdapter.getAddress();
                if (playerName == null) {
                   playerName = "";
                }
            }
        }
        playerNameTextView = findViewById(R.id.playerNameTextView);
        playerNameTextView.setText(playerName);
        ScreenUtil.resizeTextSize(playerNameTextView, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);

        final SmileImageButton createGameButton = findViewById(R.id.createGameButton);
        Bitmap createGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, getString(R.string.createGameString), colorDarkGreen);
        createGameButton.setImageBitmap(createGameBitmap);
        createGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Host game. Turn on Bluetooth and make this device visible to others
                if (playerName.isEmpty()) {
                    ScreenUtil.showToast(TwoPlayerActivity.this, playNameCannotBeEmptyString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    return;
                }
                playerNameList.clear();
                twoPlayerListAdapter.notifyDataSetChanged();
                if (mediaType == GameView.BluetoothMediaType) {
                    if (!mBluetoothAdapter.isEnabled()) {
                        // has not been enabled yet
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, Request_Enable_Bluetooth_For_Being_Discovered);
                    } else {
                        onActivityResult(Request_Enable_Bluetooth_For_Being_Discovered, Activity.RESULT_OK, null);
                    }
                }
            }
        });

        final SmileImageButton joinGameButton = findViewById(R.id.joinGameButton);
        Bitmap joinGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, getString(R.string.joinGameString), colorDarkGreen);
        joinGameButton.setImageBitmap(joinGameBitmap);
        joinGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playerName.isEmpty()) {
                    ScreenUtil.showToast(TwoPlayerActivity.this, playNameCannotBeEmptyString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                }
                if (mediaType == GameView.BluetoothMediaType) {
                    if (!mBluetoothAdapter.isEnabled()) {
                        // has not been enabled yet
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, Request_Enable_Bluetooth_For_Discovering);
                    } else {
                        onActivityResult(Request_Enable_Bluetooth_For_Discovering, Activity.RESULT_OK, null);
                    }
                }
            }
        });

        final SmileImageButton cancelButton = findViewById(R.id.cancelTwoPlayerButton);
        Bitmap cancelGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, getString(R.string.cancelString), colorDarkRed);
        cancelButton.setImageBitmap(cancelGameBitmap);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToPrevious(false);
            }
        });

        final SmileImageButton confirmButton = findViewById(R.id.confirmTwoPlayerButton);
        Bitmap confirmGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, getString(R.string.okString), colorDarkGreen);
        confirmButton.setImageBitmap(confirmGameBitmap);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToPrevious(true);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Response from bluetooth activity.");

        switch(requestCode) {
            case Request_Enable_Bluetooth_For_Being_Discovered:
                if (resultCode == Activity.RESULT_OK) {
                    // succeeded to enable bluetooth. Start enabling discoverability
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, durationForBluetoothVisible);
                    startActivityForResult(discoverableIntent, Request_Enable_Bluetooth_Discoverability);
                    // ScreenUtil.showToast(TwoPlayerActivity.this, bluetoothAlreadyOnString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                } else {
                    ScreenUtil.showToast(TwoPlayerActivity.this, bluetoothCannotBeTurnedOnString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                }
                break;
            case Request_Enable_Bluetooth_For_Discovering:
                if (resultCode == Activity.RESULT_OK) {
                    // succeeded to enable bluetooth
                    // ScreenUtil.showToast(TwoPlayerActivity.this, bluetoothAlreadyOnString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    playerNameList.clear(); // clear name list
                    twoPlayerListAdapter.notifyDataSetChanged();

                    if (mBluetoothAcceptThread != null) {
                        // stop listening for connection
                        if (mBluetoothAcceptThread.isListening() ) {
                            ScreenUtil.showToast(TwoPlayerActivity.this, "Canceling the listening for connection.", toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                            mBluetoothAcceptThread.cancel();
                        }
                    }
                    if (mBluetoothAdapter.isDiscovering()) {
                        // if discovering then stop discovering
                        mBluetoothAdapter.cancelDiscovery();
                    }
                    mBluetoothAdapter.startDiscovery();
                }else {
                    ScreenUtil.showToast(TwoPlayerActivity.this, bluetoothCannotBeTurnedOnString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                }
                break;
            case Request_Enable_Bluetooth_Discoverability:
                // if (resultCode == durationForBluetoothVisible) {
                if (resultCode != Activity.RESULT_CANCELED) {
                    // succeeded
                    ScreenUtil.showToast(TwoPlayerActivity.this, bluetoothVisibilityForPeriodString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    // create a BluetoothSocket for listening for connection using a thread
                    mBluetoothAcceptThread = new BluetoothAcceptThread(TwoPlayerActivity.this, mBluetoothAdapter, playerName, GroundhogHunterApp.ApplicationUUID);
                    mBluetoothAcceptThread.start();
                    // ScreenUtil.showToast(TwoPlayerActivity.this, "Listening for connection.", toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                } else {
                    ScreenUtil.showToast(TwoPlayerActivity.this, bluetoothCannotBeVisibleString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                }
                break;
            default:
                break;
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
        // recover the status of bluetooth
        if (mediaType == GameView.BluetoothMediaType) {
            if (mBluetoothAdapter.isDiscovering()) {
                ScreenUtil.showToast(TwoPlayerActivity.this, stopScanningBluetoothString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                mBluetoothAdapter.cancelDiscovery();
            }
            if (isDefaultBluetoothEnabled) {
                mBluetoothAdapter.enable();
            } else {
                mBluetoothAdapter.disable();
            }
        }

        if (twoPlayerListAdapter != null) {
            unregisterReceiver(twoPlayerReceiver);
        }
    }

    @Override
    public void onBackPressed() {
        returnToPrevious(false);
    }

    private void returnToPrevious(boolean confirmed) {
        Intent returnIntent = new Intent();
        Bundle extras = new Bundle();
        extras.putInt("MediaType", mediaType);
        returnIntent.putExtras(extras);

        int resultYn = Activity.RESULT_OK;
        if (!confirmed) {
            // cancelled
            resultYn = Activity.RESULT_CANCELED;
        }

        setResult(resultYn, returnIntent);    // can bundle some data to previous activity

        finish();
    }

    private class TwoPlayerListAdapter extends ArrayAdapter {

        private final int mResourceId;
        private final int mTextViewResourceId;

        @SuppressWarnings("unchecked")
        public TwoPlayerListAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List objects) {
            super(context, resource, textViewResourceId, objects);
            mResourceId = resource;
            mTextViewResourceId = textViewResourceId;
        }

        @Nullable
        @Override
        public Object getItem(int position) {
            return super.getItem(position);
        }

        @SuppressWarnings("unchecked")
        @Override
        public int getPosition(@Nullable Object item) {
            return super.getPosition(item);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            // or
            // View view = getLayoutInflater().inflate(mResourceId, parent, false);

            if (getCount() == 0) {
                return view;
            }

            if (view != null) {
                TextView itemTextView = (TextView) view.findViewById(mTextViewResourceId);
                // If using View view = getLayoutInflater().inflate(mResourceId, parent, false);
                // then the following statement must be used
                // itemTextView.setText(getItem(position).toString());
                ScreenUtil.resizeTextSize(itemTextView, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
            }

            return view;
        }
    }

    private class TwoPlayerBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
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
                            if (mBluetoothAcceptThread != null) {
                                // stop listening for connection
                                if (mBluetoothAcceptThread.isListening()) {
                                    mBluetoothAcceptThread.cancel();
                                }
                            }
                            ScreenUtil.showToast(TwoPlayerActivity.this, bluetoothVisibilityIsDisabledString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                        }
                    }
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    BluetoothDevice mBluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = mBluetoothDevice.getName();
                    String deviceHardwareAddress = mBluetoothDevice.getAddress(); // MAC address
                    // ScreenUtil.showToast(TwoPlayerActivity.this, deviceName, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    String tempPlayerName = "";
                    if ( (deviceName != null) && (!deviceName.isEmpty()) ) {
                        if (!playerNameList.contains(deviceName)) {
                            // not contains
                            tempPlayerName = deviceName;
                        }
                    } else {
                        if ( (deviceHardwareAddress != null) && (!deviceHardwareAddress.isEmpty()) ) {
                            tempPlayerName = deviceHardwareAddress;
                        }
                    }
                    if (!tempPlayerName.isEmpty()) {
                        // start to connect to host game
                        mBluetoothConnectToThread = new BluetoothConnectToThread(TwoPlayerActivity.this, mBluetoothAdapter, mBluetoothDevice, GroundhogHunterApp.ApplicationUUID);
                        mBluetoothConnectToThread.start();

                        playerNameList.add(tempPlayerName);
                        twoPlayerListAdapter.notifyDataSetChanged();
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    ScreenUtil.showToast(TwoPlayerActivity.this, scanBluetoothStartedString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    ScreenUtil.showToast(TwoPlayerActivity.this, scanBluetoothFinishedString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    break;
                case BluetoothAcceptThread.BluetoothAcceptThreadStarted:
                    ScreenUtil.showToast(TwoPlayerActivity.this, waitingForConnectionString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    break;
                case BluetoothAcceptThread.BluetoothAcceptThreadConnected:
                    ScreenUtil.showToast(TwoPlayerActivity.this, waitingForConnectionString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    break;
                case BluetoothAcceptThread.BluetoothAcceptThreadStopped:
                    ScreenUtil.showToast(TwoPlayerActivity.this, waitingIsStoppedOrTimeoutString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    break;
                case BluetoothConnectToThread.BluetoothConnectToThreadConnected:
                    ScreenUtil.showToast(TwoPlayerActivity.this, connectToHostSucceededString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    break;
                case BluetoothConnectToThread.BluetoothConnectToThreadFailedToConnect:
                    ScreenUtil.showToast(TwoPlayerActivity.this, connectToHostFailedString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    break;
            }
        }
    }
}
