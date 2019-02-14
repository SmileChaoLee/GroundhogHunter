package com.smile.groundhoghunter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.smile.smilepublicclasseslibrary.alertdialogfragment.AlertDialogFragment;
import com.smile.smilepublicclasseslibrary.utilities.ScreenUtil;

import java.util.ArrayList;
import java.util.List;

public class TwoPlayerActivity extends AppCompatActivity {

    private static final String TAG = "TwoPlayerActivity";
    private static final int Request_Enable_Bluetooth_For_Being_Discovered = 1; // request to enable bluetooth for being discovered
    private static final int Request_Enable_Bluetooth_For_Discovering = 2; // request to enable bluetooth for discovering
    private static final int Request_Enable_Bluetooth_Discoverability = 3;
    private static final int durationForBluetoothVisible = 300;   // 300 sec.
    // private properties
    private float textFontSize;
    private float fontScale;
    private float toastTextSize;

    private boolean isDefaultBluetoothEnabled;
    private int mediaType;
    private BluetoothAdapter mBluetoothAdapter;

    private EditText playerNameEditText;
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

    private TwoPlayerListAdapter twoPlayerListAdapter;
    private TwoPlayerBroadcastReceiver twoPlayerReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // device detecting
        // Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            isDefaultBluetoothEnabled = mBluetoothAdapter.isEnabled();
        }

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

        final RadioButton bluetoothRadioButton = findViewById(R.id.bluetoothRadioButton);
        ScreenUtil.resizeTextSize(bluetoothRadioButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        bluetoothRadioButton.setChecked(false);
        bluetoothRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaType = GameView.BluetoothMediaType;
            }
        });
        final RadioButton lanRadioButton = findViewById(R.id.lanRadioButton);
        ScreenUtil.resizeTextSize(lanRadioButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        lanRadioButton.setChecked(false);
        lanRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaType = GameView.LanMediaType;
            }
        });
        final RadioButton internetRadioButton = findViewById(R.id.internetRadioButton);
        ScreenUtil.resizeTextSize(internetRadioButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        internetRadioButton.setChecked(false);
        internetRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaType = GameView.InternetMediaType;
            }
        });

        switch (mediaType) {
            case GameView.BluetoothMediaType:
                bluetoothRadioButton.setChecked(true);
                break;
            case GameView.LanMediaType:
                lanRadioButton.setChecked(true);
                break;
            case GameView.InternetMediaType:
                internetRadioButton.setChecked(true);
                break;
        }

        playerName = "";
        playerNameEditText = findViewById(R.id.playerNameEditText);
        playerNameEditText.setText(playerName);
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
                Log.d(TAG, "playerName = " + playerName);
            }
        });

        final Button createGameButton = findViewById(R.id.createGameButton);
        ScreenUtil.resizeTextSize(createGameButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        createGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Host game. Turn on Bluetooth and make this device visible to others
                if (playerName.isEmpty()) {
                    ScreenUtil.showToast(TwoPlayerActivity.this, playNameCannotBeEmptyString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_LONG);
                    return;
                }
                if (mBluetoothAdapter == null) {
                    // this device does not support Bluetooth
                    ScreenUtil.showToast(TwoPlayerActivity.this, bluetoothNotSupportedString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_LONG);
                } else {
                    playerNameList.clear();
                    twoPlayerListAdapter.notifyDataSetChanged();
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

        final Button joinGameButton = findViewById(R.id.joinGameButton);
        ScreenUtil.resizeTextSize(joinGameButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        joinGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playerName.isEmpty()) {
                    ScreenUtil.showToast(TwoPlayerActivity.this, playNameCannotBeEmptyString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_LONG);
                }
                if (mBluetoothAdapter == null) {
                    // this device does not support Bluetooth
                    ScreenUtil.showToast(TwoPlayerActivity.this, bluetoothNotSupportedString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_LONG);
                } else {
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

        final Button confirmButton = findViewById(R.id.confirmTwoPlayerButton);
        ScreenUtil.resizeTextSize(confirmButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToPrevious(true);
            }
        });

        final Button cancelButton = findViewById(R.id.cancelTwoPlayerButton);
        ScreenUtil.resizeTextSize(cancelButton, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToPrevious(false);
            }
        });

        // BroadcastReceiver

        twoPlayerReceiver = new TwoPlayerBroadcastReceiver();
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
                    // ScreenUtil.showToast(TwoPlayerActivity.this, bluetoothAlreadyOnString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_LONG);
                } else {
                    ScreenUtil.showToast(TwoPlayerActivity.this, bluetoothCannotBeTurnedOnString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_LONG);
                }
                break;
            case Request_Enable_Bluetooth_For_Discovering:
                if (resultCode == Activity.RESULT_OK) {
                    // succeeded to enable bluetooth
                    ScreenUtil.showToast(TwoPlayerActivity.this, bluetoothAlreadyOnString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_LONG);
                    mBluetoothAdapter.startDiscovery();
                    ScreenUtil.showToast(TwoPlayerActivity.this, "Start discovering .......", toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_LONG);
                }else {
                    ScreenUtil.showToast(TwoPlayerActivity.this, bluetoothCannotBeTurnedOnString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_LONG);
                }
                break;
            case Request_Enable_Bluetooth_Discoverability:
                // if (resultCode == durationForBluetoothVisible) {
                if (resultCode != Activity.RESULT_CANCELED) {
                    // succeeded
                    ScreenUtil.showToast(TwoPlayerActivity.this, bluetoothVisibilityForPeriodString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_LONG);
                } else {
                    ScreenUtil.showToast(TwoPlayerActivity.this, bluetoothCannotBeVisibleString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_LONG);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(twoPlayerReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(twoPlayerReceiver);
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
            // disable the discoverability 1 second later
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 1);
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

            ScreenUtil.showToast(TwoPlayerActivity.this, "onReceiver() --> Get messages from bluetooth ", toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_LONG);

            switch (action) {
                case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
                    int extraPreviousScanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_SCAN_MODE, BluetoothAdapter.ERROR);
                    int extraScanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                    if (extraPreviousScanMode != extraScanMode) {
                        if ((extraScanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE) || (extraScanMode == BluetoothAdapter.SCAN_MODE_NONE)) {
                            ScreenUtil.showToast(TwoPlayerActivity.this, bluetoothVisibilityIsDisabledString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_LONG);
                        }
                    }
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    mBluetoothAdapter.cancelDiscovery();
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    if ( (deviceName != null) && (!deviceName.isEmpty()) )
                    {
                        ScreenUtil.showToast(TwoPlayerActivity.this, deviceName, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_LONG);
                        if (!playerNameList.contains(deviceName)) {
                            // not contains
                            playerNameList.add(deviceName);
                            twoPlayerListAdapter.notifyDataSetChanged();
                        }
                    }
                    break;
            }
        }
    }
}
