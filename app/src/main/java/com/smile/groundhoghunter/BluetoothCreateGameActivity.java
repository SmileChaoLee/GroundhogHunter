package com.smile.groundhoghunter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.smile.groundhoghunter.ArrayAdatpers.TwoPlayerListAdapter;
import com.smile.groundhoghunter.Interfaces.MessageConstants;
import com.smile.groundhoghunter.Models.SmileImageButton;
import com.smile.groundhoghunter.Threads.BluetoothAcceptThread;
import com.smile.groundhoghunter.Threads.BluetoothFunctionThread;
import com.smile.groundhoghunter.Utilities.BluetoothUtil;
import com.smile.smilepublicclasseslibrary.utilities.FontAndBitmapUtil;
import com.smile.smilepublicclasseslibrary.utilities.ScreenUtil;
import java.util.ArrayList;

public class BluetoothCreateGameActivity extends AppCompatActivity {

    // private properties
    private static final String TAG = new String(".BluetoothCreateGameActivity");
    private static final int Request_Enable_Bluetooth_For_Being_Discovered = 1; // request to enable bluetooth for being discovered
    private static final int Request_Enable_Bluetooth_Discoverability = 3;
    private static final int durationForBluetoothVisible = GroundhogHunterApp.durationForBluetoothVisible;

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
    private String bluetoothVisibilityForPeriodString;
    private String bluetoothVisibilityIsDisabledString;
    private String bluetoothCannotBeTurnedOnString;
    private String bluetoothCannotBeVisibleString;
    private String cannotCreateServerSocketString;
    private String waitingForConnectionString;
    private String waitingStoppedCancelledString;
    private String serverAcceptedConnectionString;
    private String clientLeftGameString;

    private TwoPlayerListAdapter twoPlayerListAdapter;
    private BluetoothCreateGameBroadcastReceiver btCreateGameReceiver;
    private BluetoothAcceptThread mBluetoothAcceptThread;
    private ArrayList<BluetoothFunctionThread> bluetoothFunctionThreadList;

    private boolean isDefaultBluetoothEnabled;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket;

    private Handler createGameHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        createGameHandler = new CreateGameHandler(Looper.getMainLooper());

        playerNameList = new ArrayList<>();
        bluetoothFunctionThreadList = new ArrayList<>();

        // BroadcastReceiver and register it
        btCreateGameReceiver = new BluetoothCreateGameBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(btCreateGameReceiver, intentFilter);

        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this, GroundhogHunterApp.FontSize_Scale_Type, null);
        textFontSize = ScreenUtil.suitableFontSize(this, defaultTextFontSize, GroundhogHunterApp.FontSize_Scale_Type, 0.0f);
        fontScale = ScreenUtil.suitableFontScale(this, GroundhogHunterApp.FontSize_Scale_Type, 0.0f);
        toastTextSize = textFontSize * 0.8f;

        // bluetoothAlreadyOnString = getString(R.string.bluetoothAlreadyOnString);
        bluetoothNotSupportedString = getString(R.string.bluetoothNotSupportedString);
        playerNameCannotBeEmptyString = getString(R.string.playerNameCannotBeEmptyString);
        bluetoothVisibilityForPeriodString = getString(R.string.bluetoothVisibilityForPeriodString)
                + "(" + durationForBluetoothVisible + " " + getString(R.string.secondString) + ")";
        bluetoothVisibilityIsDisabledString = getString(R.string.bluetoothVisibilityIsDisabledString);
        bluetoothCannotBeTurnedOnString = getString(R.string.bluetoothCannotBeTurnedOnString);
        bluetoothCannotBeVisibleString = getString(R.string.bluetoothCannotBeVisibleString);
        waitingForConnectionString = getString(R.string.waitingForConnectionString);
        waitingStoppedCancelledString = getString(R.string.waitingStoppedCancelledString);
        serverAcceptedConnectionString = getString(R.string.serverAcceptedConnectionString);
        cannotCreateServerSocketString = getString(R.string.cannotCreateServerSocketString);
        clientLeftGameString = getString(R.string.clientLeftGameString);

        // int colorDarkOrange = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkOrange);
        // int colorRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.red);
        int colorDarkRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkRed);
        int colorDarkGreen = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkGreen);
        int colorBlue = Color.BLUE;

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_game);

        // device detecting
        // Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // this device does not support Bluetooth
            ScreenUtil.showToast(BluetoothCreateGameActivity.this, bluetoothNotSupportedString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
            returnToPrevious();
        }

        isDefaultBluetoothEnabled = mBluetoothAdapter.isEnabled();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        deviceName = BluetoothUtil.getBluetoothDeviceName(mBluetoothAdapter);

        TextView createGameTitleTextView = findViewById(R.id.createGameTitleTextView);
        createGameTitleTextView.setText(getString(R.string.createBluetoothGameString));
        ScreenUtil.resizeTextSize(createGameTitleTextView, textFontSize * 1.2f, GroundhogHunterApp.FontSize_Scale_Type);

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
                        // send oppositePlayerName to the host
                    }
                }
            }
        });

        SmileImageButton refreshCreateGameButton = findViewById(R.id.refreshCreateGameButton);
        Bitmap refreshCreateGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, getString(R.string.refreshString), colorBlue);
        refreshCreateGameButton.setImageBitmap(refreshCreateGameBitmap);
        refreshCreateGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // stopBluetoothAcceptThread();
                // Log.d(TAG, "Refresh --> stopBluetoothAcceptThread()");
                startBluetoothDiscoverability();
                Log.d(TAG, "Refresh --> startBluetoothDiscoverability()");
            }
        });

        SmileImageButton startCreateGameButton = findViewById(R.id.startCreateGameButton);
        Bitmap startCreateGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, getString(R.string.startString), colorDarkGreen);
        startCreateGameButton.setImageBitmap(startCreateGameBitmap);
        startCreateGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playerName.isEmpty()) {
                    ScreenUtil.showToast(BluetoothCreateGameActivity.this, playerNameCannotBeEmptyString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    return;
                }
            }
        });

        SmileImageButton cancelCreateGameButton = findViewById(R.id.cancelCreateGameButton);
        Bitmap cancelCreateGameGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, getString(R.string.cancelString), colorDarkRed);
        cancelCreateGameButton.setImageBitmap(cancelCreateGameGameBitmap);
        cancelCreateGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToPrevious();
            }
        });

        startBluetoothDiscoverability();
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
                } else {
                    ScreenUtil.showToast(this, bluetoothCannotBeTurnedOnString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                }
                break;
            case Request_Enable_Bluetooth_Discoverability:
                // if (resultCode == durationForBluetoothVisible) {
                if (resultCode != Activity.RESULT_CANCELED) {
                    // succeeded
                    ScreenUtil.showToast(this, bluetoothVisibilityForPeriodString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    // create a BluetoothSocket for listening for connection using a thread
                    mBluetoothAcceptThread = new BluetoothAcceptThread(createGameHandler, mBluetoothAdapter, playerName, GroundhogHunterApp.ApplicationUUID);
                    mBluetoothAcceptThread.start();
                    // ScreenUtil.showToast(this, "Listening for connection.", toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                } else {
                    ScreenUtil.showToast(this, bluetoothCannotBeVisibleString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
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

        stopBluetoothFunctionThreads();
        stopBluetoothAcceptThread();
        BluetoothUtil.closeBluetoothSocket(mBluetoothSocket);

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

        if (createGameHandler != null) {
            createGameHandler.removeCallbacksAndMessages(null);
            createGameHandler = null;
        }
    }

    private void returnToPrevious() {

        // if it Still is connecting to other device
        // then notify the other device leaving

        finish();
    }

    private void startBluetoothDiscoverability() {
        playerNameList.clear();
        twoPlayerListAdapter.notifyDataSetChanged();

        stopBluetoothFunctionThreads();
        stopBluetoothAcceptThread();
        BluetoothUtil.closeBluetoothSocket(mBluetoothSocket);

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, Request_Enable_Bluetooth_For_Being_Discovered);
    }

    private void stopBluetoothAcceptThread() {
        if (mBluetoothAcceptThread != null) {
            mBluetoothAcceptThread.closeServerSocket();
            boolean retry = true;
            while (retry) {
                try {
                    mBluetoothAcceptThread.join();
                    Log.d(TAG, "mBluetoothAcceptThread.Join()........\n");
                    retry = false;
                    mBluetoothAcceptThread = null;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }// continue processing until the thread ends
            }
        }
    }

    private void stopBluetoothFunctionThreads() {
        for (BluetoothFunctionThread mBluetoothFunctionThread : bluetoothFunctionThreadList) {
            if (mBluetoothFunctionThread != null) {
                mBluetoothFunctionThread.setKeepRunning(false);
                mBluetoothFunctionThread.closeBluetoothSocket();
                boolean retry = true;
                while (retry) {
                    try {
                        mBluetoothFunctionThread.join();
                        Log.d(TAG, "mBluetoothFunctionThread.Join()........\n");
                        retry = false;
                        mBluetoothFunctionThread = null;
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }// continue processing until the thread ends
                }
            }
        }
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
                            ScreenUtil.showToast(context, bluetoothVisibilityIsDisabledString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                            stopBluetoothAcceptThread();
                        }
                    }
                    break;
            }
        }
    }

    private class CreateGameHandler extends Handler {

        private final Looper mLooper;
        private final Context mContext;

        public CreateGameHandler(Looper looper) {
            super(looper);
            mLooper = looper;
            mContext = getApplicationContext();
        }

        @Override
        public void handleMessage(Message msg) {
            // super.handleMessage(msg);

            Bundle data = msg.getData();

            switch (msg.what) {
                case MessageConstants.PlayerNameHasBeenRead:
                    Log.d(TAG, "Player name hsa been read.");
                    ScreenUtil.showToast(mContext, "Player name has been read.", toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    String pName = data.getString("PlayerName");
                    if (pName != null) {
                        if (!pName.isEmpty()) {
                            if (!playerNameList.contains(pName)) {
                                playerNameList.add(pName);
                                twoPlayerListAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                    break;
                case MessageConstants.BluetoothAcceptThreadNoServerSocket:
                    ScreenUtil.showToast(mContext, cannotCreateServerSocketString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    stopBluetoothAcceptThread();
                    break;
                case MessageConstants.BluetoothAcceptThreadStarted:
                    ScreenUtil.showToast(mContext, waitingForConnectionString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    break;
                case MessageConstants.BluetoothAcceptThreadConnected:
                    ScreenUtil.showToast(mContext, serverAcceptedConnectionString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_LONG);

                    mBluetoothSocket = mBluetoothAcceptThread.getBluetoothSocket();
                    stopBluetoothAcceptThread();

                    Log.d(TAG, "Started to read player name.");
                    // start reading data from the other device and writing data to the other device
                    // start communicating
                    BluetoothFunctionThread mBluetoothFunctionThread = new BluetoothFunctionThread(createGameHandler, mBluetoothSocket);
                    mBluetoothFunctionThread.start();
                    bluetoothFunctionThreadList.add(mBluetoothFunctionThread);

                    break;
                case MessageConstants.BluetoothAcceptThreadStopped:
                    ScreenUtil.showToast(mContext, waitingStoppedCancelledString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    stopBluetoothAcceptThread();
                    break;
            }
        }
    }
}
