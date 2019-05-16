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
import android.widget.ListView;
import android.widget.TextView;

import com.smile.groundhoghunter.ArrayAdatpers.TwoPlayerListAdapter;
import com.smile.groundhoghunter.Constants.CommonConstants;
import com.smile.groundhoghunter.Models.SmileImageButton;
import com.smile.groundhoghunter.Threads.BluetoothAcceptThread_OLD;
import com.smile.groundhoghunter.Threads.BluetoothFunctionThread_OLD;
import com.smile.groundhoghunter.Utilities.BluetoothUtil_OLD;
import com.smile.groundhoghunter.Utilities.MessageShowingUtil;
import com.smile.smilepublicclasseslibrary.utilities.FontAndBitmapUtil;
import com.smile.smilepublicclasseslibrary.utilities.ScreenUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class BluetoothCreateGameActivity_OLD extends AppCompatActivity {

    // private properties
    private static final String TAG = new String(".BluetoothCreateGameActivity");
    private static final int Request_Enable_Bluetooth_For_Being_Discovered = 1; // request to enable bluetooth for being discovered
    private static final int Request_Enable_Bluetooth_Discoverability = 3;
    private static final int DurationForBluetoothVisible = 120;  // 120 seconds
    private static final int MessageDuration = 1000;    // 1 seconds

    private float textFontSize;
    private float fontScale;
    private float toastTextSize;

    private TextView playerNameTextView;
    private String playerName;
    private String oppositePlayerName;
    private ListView oppositePlayerNameListView;
    private MessageShowingUtil showMessage;
    private LinkedHashMap<String, String> oppositePlayerNameMap;

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
    private String noOppositePlayerString;

    private TwoPlayerListAdapter twoPlayerListAdapter;
    private BluetoothCreateGameBroadcastReceiver btCreateGameReceiver;
    private BluetoothAcceptThread_OLD mBluetoothAcceptThread;
    private HashMap<String, BluetoothFunctionThread_OLD> btMacFunctionThreadMap;
    private BluetoothFunctionThread_OLD selectedBtFunctionThread;

    private boolean isDefaultBluetoothEnabled;
    private BluetoothAdapter mBluetoothAdapter;

    private Handler createGameHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        createGameHandler = new CreateGameHandler(Looper.getMainLooper());

        oppositePlayerName = "";    // empty
        oppositePlayerNameMap = new LinkedHashMap<>();
        mBluetoothAcceptThread = null;
        btMacFunctionThreadMap = new HashMap<>();
        selectedBtFunctionThread = null;

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
                + "(" + DurationForBluetoothVisible + " " + getString(R.string.secondString) + ")";
        bluetoothVisibilityIsDisabledString = getString(R.string.bluetoothVisibilityIsDisabledString);
        bluetoothCannotBeTurnedOnString = getString(R.string.bluetoothCannotBeTurnedOnString);
        bluetoothCannotBeVisibleString = getString(R.string.bluetoothCannotBeVisibleString);
        waitingForConnectionString = getString(R.string.waitingForConnectionString);
        waitingStoppedCancelledString = getString(R.string.waitingStoppedCancelledString);
        serverAcceptedConnectionString = getString(R.string.serverAcceptedConnectionString);
        cannotCreateServerSocketString = getString(R.string.cannotCreateServerSocketString);
        clientLeftGameString = getString(R.string.clientLeftGameString);
        noOppositePlayerString = getString(R.string.noOppositePlayerString);

        // int colorDarkOrange = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkOrange);
        // int colorRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.red);
        int colorDarkRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkRed);
        int colorDarkGreen = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkGreen);
        int colorBlue = Color.BLUE;

        Intent callingIntent = getIntent();
        playerName = callingIntent.getStringExtra("PlayerName");
        if (playerName == null) {
            playerName = "";
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_game);

        // message showing view
        TextView toastMessageTextView = findViewById(R.id.toastMessageTextView);
        ScreenUtil.resizeTextSize(toastMessageTextView,toastTextSize, GroundhogHunterApp.FontSize_Scale_Type);
        showMessage = new MessageShowingUtil(this, toastMessageTextView);

        // device detecting
        // Bluetooth
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

        TextView createGameTitleTextView = findViewById(R.id.createGameTitleTextView);
        createGameTitleTextView.setText(getString(R.string.createBluetoothGameString));
        ScreenUtil.resizeTextSize(createGameTitleTextView, textFontSize * 1.2f, GroundhogHunterApp.FontSize_Scale_Type);

        TextView playerNameStringTextView = findViewById(R.id.playerNameStringTextView);
        ScreenUtil.resizeTextSize(playerNameStringTextView, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);

        playerNameTextView = findViewById(R.id.playerNameTextView);
        playerNameTextView.setEnabled(true);
        playerNameTextView.setText("");
        playerNameTextView.append(playerName);
        ScreenUtil.resizeTextSize(playerNameTextView, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);

        oppositePlayerNameListView = findViewById(R.id.oppositePlayerNameListView);
        ArrayList<String> oppNameList = new ArrayList<>();
        twoPlayerListAdapter = new TwoPlayerListAdapter(this, R.layout.player_list_item_layout, R.id.playerNameTextView, oppNameList, textFontSize);
        twoPlayerListAdapter.setNotifyOnChange(false);  // do not call notifyDataSetChanged() method automatically
        oppositePlayerNameListView.setAdapter(twoPlayerListAdapter);
        oppositePlayerNameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long rowId) {
                if (adapterView != null) {
                    Object item = adapterView.getItemAtPosition(position);
                    if (item != null) {
                        String temp = item.toString();
                        Log.d(TAG, "adapterView.getItemAtPosition(position) = " + temp);
                        oppositePlayerName = temp;
                        for (String remoteMacAddress : oppositePlayerNameMap.keySet()) {
                            String oppName = oppositePlayerNameMap.get(remoteMacAddress);
                            if (oppName.equals(oppositePlayerName)) {
                                // found
                                BluetoothFunctionThread_OLD btFunctionThread = btMacFunctionThreadMap.get(remoteMacAddress);
                                if (btFunctionThread != null) {
                                    selectedBtFunctionThread = btFunctionThread;
                                    view.setSelected(true);
                                }
                            }
                        }
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
                    // ScreenUtil.showToast(BluetoothCreateGameActivity.this, playerNameCannotBeEmptyString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);    // removed on 2019-04-11
                    showMessage.showMessageInTextView(playerNameCannotBeEmptyString, MessageDuration);
                    return;
                }
                if (oppositePlayerName.isEmpty()) {
                    showMessage.showMessageInTextView(noOppositePlayerString, MessageDuration);
                    return;
                }
                // Notify client to start game
                if (selectedBtFunctionThread != null) {
                    GroundhogHunterApp.selectedIoFuncThread = selectedBtFunctionThread;
                    // stop BluetoothAcceptThread and close server socket
                    stopBluetoothAcceptThread();
                    // stop other BluetoothFunctionThreads
                    for (String remoteMacAddress : btMacFunctionThreadMap.keySet()) {
                        BluetoothFunctionThread_OLD btFunctionThread = btMacFunctionThreadMap.get(remoteMacAddress);
                        if (btFunctionThread != selectedBtFunctionThread) {
                            // btFunctionThread.write(CommonConstants.BluetoothHostExitCode, remoteMacAddress);
                            btFunctionThread.write(CommonConstants.TwoPlayerHostExitCode, "");
                            BluetoothUtil_OLD.stopBluetoothFunctionThread(btFunctionThread);
                        }
                    }
                    // clear HashMaps
                    btMacFunctionThreadMap.clear();
                    btMacFunctionThreadMap = null;
                    oppositePlayerNameMap.clear();
                    oppositePlayerNameMap = null;
                    //
                    // remove all messages from createGameHandler, // added on 2019-05-14
                    createGameHandler.removeCallbacksAndMessages(null);
                    //
                    selectedBtFunctionThread.write(CommonConstants.TwoPlayerHostStartGame, "");
                    Intent gameIntent = new Intent(getApplicationContext(), BtHostGameActivity.class);
                    gameIntent.putExtra("GameType", CommonConstants.TwoPlayerGameByHost);
                    startActivityForResult(gameIntent, CommonConstants.TwoPlayerGameByHost);
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

                    mBluetoothAcceptThread = new BluetoothAcceptThread_OLD(createGameHandler, mBluetoothAdapter, playerName, GroundhogHunterApp.ApplicationUUID);
                    mBluetoothAcceptThread.start();
                } else {
                    // ScreenUtil.showToast(this, bluetoothCannotBeVisibleString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_LONG);
                    showMessage.showMessageInTextView(bluetoothCannotBeVisibleString, MessageDuration);
                }
                break;
            case CommonConstants.TwoPlayerGameByHost:
                oppositePlayerName = "";
                oppositePlayerNameMap = new LinkedHashMap<>();
                mBluetoothAcceptThread = null;
                btMacFunctionThreadMap = new HashMap<>();
                selectedBtFunctionThread = null;
                // update list view
                ArrayList<String> oppNameList = new ArrayList<>(oppositePlayerNameMap.values());
                twoPlayerListAdapter.updateData(oppNameList);

                break;

            default:
                break;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        hostLeavingNotification();

        oppositePlayerNameMap.clear();
        oppositePlayerNameMap = null;

        stopBluetoothAcceptThread();

        ArrayList<BluetoothFunctionThread_OLD> threadList = new ArrayList<>(btMacFunctionThreadMap.values());
        BluetoothUtil_OLD.stopBluetoothFunctionThreads(threadList);
        btMacFunctionThreadMap.clear();
        btMacFunctionThreadMap = null;

        selectedBtFunctionThread = null;

        twoPlayerListAdapter.clear();
        twoPlayerListAdapter = null;

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

        hostLeavingNotification();

        stopBluetoothAcceptThread();

        ArrayList<BluetoothFunctionThread_OLD> threadList = new ArrayList<>(btMacFunctionThreadMap.values());
        BluetoothUtil_OLD.stopBluetoothFunctionThreads(threadList);
        btMacFunctionThreadMap.clear();

        createGameHandler.removeCallbacksAndMessages(null); // added on 2019-05-14

        oppositePlayerNameMap.clear();
        twoPlayerListAdapter.clear();
        twoPlayerListAdapter.notifyDataSetChanged();

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, Request_Enable_Bluetooth_For_Being_Discovered);
    }

    private void hostLeavingNotification() {
        if (mBluetoothAdapter != null) {
            String macAddress = mBluetoothAdapter.getAddress();
            for (BluetoothFunctionThread_OLD btFunctionThread : btMacFunctionThreadMap.values()) {
                btFunctionThread.write(CommonConstants.TwoPlayerHostExitCode, "");
            }
        }
    }

    private void stopBluetoothAcceptThread() {
        if (mBluetoothAcceptThread != null) {
            mBluetoothAcceptThread.setKeepRunning(false);
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

    private class CreateGameHandler extends Handler {

        private final Looper mLooper;

        public CreateGameHandler(Looper looper) {
            super(looper);
            mLooper = looper;
        }

        @Override
        public void handleMessage(Message msg) {
            // super.handleMessage(msg);

            String megString;
            String deviceName = "";
            String remoteMacAddress;

            BluetoothDevice btDevice;
            BluetoothFunctionThread_OLD btFunctionThread;

            Context mContext = getApplicationContext();
            Bundle data = msg.getData();

            switch (msg.what) {
                case CommonConstants.OppositePlayerNameHasBeenRead:
                    megString = "has been read.";
                    String oppositeName = data.getString("OppositePlayerName");
                    megString = oppositeName + " " + megString;
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView(megString, MessageDuration);
                    btDevice = data.getParcelable("BluetoothDevice");
                    remoteMacAddress = btDevice.getAddress();
                    if (oppositeName != null) {
                        if (!oppositeName.isEmpty()) {
                            if (!oppositePlayerNameMap.containsKey(remoteMacAddress)) {
                                oppositePlayerNameMap.put(remoteMacAddress, oppositeName);
                                ArrayList<String> oppNameList = new ArrayList<>(oppositePlayerNameMap.values());
                                twoPlayerListAdapter.updateData(oppNameList);
                            }
                        }
                    }

                    // read the next data
                    // btFunctionThread = mBluetoothAcceptThread.getBtFunctionThread(btDevice);
                    btFunctionThread = btMacFunctionThreadMap.get(remoteMacAddress);
                    btFunctionThread.setStartRead(true);    // start reading data

                    break;
                case CommonConstants.ServerAcceptThreadNoServerSocket:
                    showMessage.showMessageInTextView(cannotCreateServerSocketString, MessageDuration);
                    break;
                case CommonConstants.ServerAcceptThreadConnected:
                    btDevice = data.getParcelable("BluetoothDevice");
                    deviceName = BluetoothUtil_OLD.getBluetoothDeviceName(btDevice);
                    remoteMacAddress = btDevice.getAddress();
                    megString = serverAcceptedConnectionString + "(" + deviceName+ ")";
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView(serverAcceptedConnectionString, MessageDuration);

                    // start reading data from the other device and writing data to the other device
                    btFunctionThread = mBluetoothAcceptThread.getBtFunctionThread(btDevice);
                    btFunctionThread.setStartRead(true);    // start reading data

                    if (!btMacFunctionThreadMap.containsKey(remoteMacAddress)) {
                        btMacFunctionThreadMap.put(remoteMacAddress, btFunctionThread);
                    }

                    break;
                case CommonConstants.ServerAcceptThreadStopped:
                    showMessage.showMessageInTextView(waitingStoppedCancelledString, MessageDuration);
                    break;
                case CommonConstants.TwoPlayerClientExitCode:
                    btDevice = data.getParcelable("BluetoothDevice");
                    remoteMacAddress = btDevice.getAddress();
                    btFunctionThread = btMacFunctionThreadMap.get(remoteMacAddress);
                    btMacFunctionThreadMap.remove(remoteMacAddress);
                    // release btFunctionThread (stop communicating)
                    BluetoothUtil_OLD.stopBluetoothFunctionThread(btFunctionThread);

                    // remove the remote connected device from oppositePlayerNameList
                    if (oppositePlayerNameMap.containsKey(remoteMacAddress)) {
                        showMessage.showMessageInTextView(clientLeftGameString, MessageDuration);
                        oppositePlayerNameMap.remove(remoteMacAddress);
                    }

                    // update list view
                    ArrayList<String> oppNameList = new ArrayList<>(oppositePlayerNameMap.values());
                    twoPlayerListAdapter.updateData(oppNameList);

                    break;
                case CommonConstants.TwoPlayerDefaultReading:
                    Log.d(TAG, "Default reading.");
                    btDevice = data.getParcelable("BluetoothDevice");
                    remoteMacAddress = btDevice.getAddress();
                    // read the next data
                    // btFunctionThread = mBluetoothAcceptThread.getBtFunctionThread(btDevice);
                    btFunctionThread = btMacFunctionThreadMap.get(remoteMacAddress);
                    btFunctionThread.setStartRead(true);    // start reading data
                    break;
            }
        }
    }
}