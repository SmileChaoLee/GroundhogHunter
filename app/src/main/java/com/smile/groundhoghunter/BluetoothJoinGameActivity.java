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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.smile.groundhoghunter.ArrayAdatpers.TwoPlayerListAdapter;
import com.smile.groundhoghunter.Constants.CommonConstants;
import com.smile.groundhoghunter.Models.SmileImageButton;
import com.smile.groundhoghunter.Threads.BluetoothConnectToThread;
import com.smile.groundhoghunter.Threads.BluetoothDiscoveryTimerThread;
import com.smile.groundhoghunter.Threads.BluetoothFunctionThread;
import com.smile.groundhoghunter.Utilities.BluetoothUtil;
import com.smile.groundhoghunter.Utilities.MessageShowingUtil;
import com.smile.smilepublicclasseslibrary.utilities.FontAndBitmapUtil;
import com.smile.smilepublicclasseslibrary.utilities.ScreenUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class BluetoothJoinGameActivity extends AppCompatActivity {

    // private properties
    private static final String TAG = new String(".BluetoothJoinGameActivity");
    private static final int Request_Enable_Bluetooth_For_Discovering = 2; // request to enable bluetooth for discovering
    private static final int DurationForBluetoothDiscovery = 15000;  // 15 seconds one time
    private static final int MessageDuration = 1000;    // 1 second

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
    private String bluetoothCannotBeTurnedOnString;
    private String scanBluetoothStartedString;
    private String scanBluetoothFinishedString;
    private String cannotCreateClientSocketString;
    private String startedConnectingToHostString;
    private String connectToHostSucceededString;
    private String connectToHostFailedString;
    private String hostLeftGameString;
    private String discoveryTimeHasReachedString;
    private String discoveryWasDismissedString;
    private String foundDeviceString;
    private String hasBeenReadString;

    private TwoPlayerListAdapter twoPlayerListAdapter;
    private BluetoothJoinGameBroadcastReceiver btJoinGameReceiver;
    private BluetoothDiscoveryTimerThread mBluetoothDiscoveryTimerThread;
    private HashMap<String, BluetoothConnectToThread> btDiscoveredMap;
    private HashMap<String, BluetoothFunctionThread> btMacFunctionThreadMap;
    private BluetoothFunctionThread selectedBtFunctionThread;

    private boolean isDefaultBluetoothEnabled;
    private BluetoothAdapter mBluetoothAdapter;

    private Handler joinGameHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        joinGameHandler = new JoinGameHandler(Looper.getMainLooper());

        btDiscoveredMap = new HashMap<>();
        oppositePlayerNameMap = new LinkedHashMap<>();
        btMacFunctionThreadMap = new HashMap<>();

        // BroadcastReceiver and register it
        btJoinGameReceiver = new BluetoothJoinGameBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
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
        cannotCreateClientSocketString = getString(R.string.cannotCreateClientSocketString);
        startedConnectingToHostString = getString(R.string.startedConnectingToHostString);
        connectToHostSucceededString = getString(R.string.connectToHostSucceededString);
        connectToHostFailedString = getString(R.string.connectToHostFailedString);
        hostLeftGameString = getString(R.string.hostLeftGameString);
        discoveryTimeHasReachedString = getString(R.string.discoveryTimeHasReachedString);
        discoveryWasDismissedString = getString(R.string.discoveryWasDismissedString);
        foundDeviceString = getString(R.string.foundDeviceString);
        hasBeenReadString = getString(R.string.hasBeenReadString);

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

        setContentView(R.layout.activity_join_game);

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
            returnToPrevious();
        }

        isDefaultBluetoothEnabled = mBluetoothAdapter.isEnabled();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        TextView joinGameTitleTextView = findViewById(R.id.joinGameTitleTextView);
        joinGameTitleTextView.setText(getString(R.string.joinBluetoothGameString));
        ScreenUtil.resizeTextSize(joinGameTitleTextView, textFontSize * 1.2f, GroundhogHunterApp.FontSize_Scale_Type);

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
                        oppositePlayerName = temp;
                        String oppName;
                        // get remote mac address of remote device
                        for (String remoteMacAddress : oppositePlayerNameMap.keySet()) {
                            oppName = oppositePlayerNameMap.get(remoteMacAddress);
                            if (oppName.equals(oppositePlayerName)) {
                                BluetoothFunctionThread btFunctionThread = btMacFunctionThreadMap.get(remoteMacAddress);
                                if (btFunctionThread != null) {
                                    selectedBtFunctionThread = btFunctionThread;
                                    selectedBtFunctionThread.write(CommonConstants.OppositePlayerNameHasBeenRead, playerName);
                                    view.setSelected(true);
                                }
                                return;
                            }
                        }
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
                startBluetoothDiscovery();
                Log.d(TAG, "Refresh --> startBluetoothDiscovery()");
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

        startBluetoothDiscovery();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Response from bluetooth activity.");
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
                    stopBluetoothDiscoveryTimerThread();
                    // start discovery
                    mBluetoothAdapter.startDiscovery();
                    startBluetoothDiscoveryTimerThread();
                }else {
                    megString = bluetoothCannotBeTurnedOnString;
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView(bluetoothCannotBeTurnedOnString, MessageDuration);
                }
                break;
            case CommonConstants.BluetoothGameByClient:
                oppositePlayerName = "";
                btDiscoveredMap = new HashMap<>();
                btMacFunctionThreadMap = new HashMap<>();
                oppositePlayerNameMap = new LinkedHashMap<>();
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

        clientLeavingNotification();
        ArrayList<BluetoothFunctionThread> threadList = new ArrayList<>(btMacFunctionThreadMap.values());
        BluetoothUtil.stopBluetoothFunctionThreads(threadList);
        btMacFunctionThreadMap.clear();
        btMacFunctionThreadMap = null;

        stopBtConnectToThreadAndClearBtDiscoveredHashMap();
        btDiscoveredMap = null;

        oppositePlayerNameMap.clear();
        oppositePlayerNameMap = null;

        selectedBtFunctionThread = null;

        twoPlayerListAdapter.clear();
        twoPlayerListAdapter = null;

        stopBluetoothDiscoveryTimerThread();

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

        if (joinGameHandler != null) {
            joinGameHandler.removeCallbacksAndMessages(null);
        }
    }

    private void returnToPrevious() {

        // if it Still is connecting to other device
        // then notify the other device leaving

        finish();
    }

    private void startBluetoothDiscovery() {

        clientLeavingNotification();
        ArrayList<BluetoothFunctionThread> threadList = new ArrayList<>(btMacFunctionThreadMap.values());
        BluetoothUtil.stopBluetoothFunctionThreads(threadList);
        btMacFunctionThreadMap.clear();

        stopBtConnectToThreadAndClearBtDiscoveredHashMap();
        oppositePlayerNameMap.clear();
        twoPlayerListAdapter.clear();
        twoPlayerListAdapter.notifyDataSetChanged();

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, Request_Enable_Bluetooth_For_Discovering);
    }

    private void clientLeavingNotification() {
        if (mBluetoothAdapter != null) {
            String macAddress = mBluetoothAdapter.getAddress();
            for (BluetoothFunctionThread btFunctionThread : btMacFunctionThreadMap.values()) {
                btFunctionThread.write(CommonConstants.BluetoothClientExitCode, macAddress);
            }
        }
    }

    private void startBluetoothDiscoveryTimerThread() {
        int timerPeriod = DurationForBluetoothDiscovery;
        mBluetoothDiscoveryTimerThread = new BluetoothDiscoveryTimerThread(joinGameHandler, timerPeriod);
        mBluetoothDiscoveryTimerThread.start();
    }

    private void stopBluetoothDiscoveryTimerThread() {
        if (mBluetoothDiscoveryTimerThread != null) {
            mBluetoothDiscoveryTimerThread.dismissTimerThread();
            boolean retry = true;
            while (retry) {
                try {
                    mBluetoothDiscoveryTimerThread.join();
                    Log.d(TAG, "mBluetoothDiscoveryTimerThread.Join()........\n");
                    retry = false;
                    mBluetoothDiscoveryTimerThread = null;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }// continue processing until the thread ends
            }
        }
    }

    private void stopBtConnectToThreadAndClearBtDiscoveredHashMap() {
        for(BluetoothConnectToThread connectToThread : btDiscoveredMap.values()) {
            stopBluetoothConnectToThread(connectToThread, true);
        }
        btDiscoveredMap.clear(); // clear HashSet because of starting discovering
    }

    private void stopBluetoothConnectToThread(BluetoothConnectToThread connectToThread, boolean isCloseBluetoothSocket) {
        if (connectToThread != null) {
            if (isCloseBluetoothSocket) {
                connectToThread.closeBluetoothSocket();
            }
            boolean retry = true;
            while (retry) {
                try {
                    connectToThread.join();
                    Log.d(TAG, "connectToThread.Join()........\n");
                    retry = false;
                    connectToThread = null;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }// continue processing until the thread ends
            }
        }
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
                    // ScreenUtil.showToast(context, megString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    Log.d(TAG, megString);
                    // start to connect to host game
                    /*
                    if (btDevice != null) {
                        if (!btDiscoveredMap.containsKey(btDevice)) {
                            // ScreenUtil.showToast(context, BluetoothUtil.getBluetoothDeviceName(mBluetoothDevice), toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                            showMessage.showMessageInTextView(megString, MessageDuration);
                            BluetoothConnectToThread connectToThread = new BluetoothConnectToThread(joinGameHandler, btDevice, GroundhogHunterApp.ApplicationUUID);
                            btDiscoveredMap.put(btDevice, connectToThread);
                        }
                    }
                    */
                    if (btDevice != null) {
                        remoteMacAddress = btDevice.getAddress();
                        if (!btDiscoveredMap.containsKey(remoteMacAddress)) {
                            showMessage.showMessageInTextView(megString, MessageDuration);
                            BluetoothConnectToThread connectToThread = new BluetoothConnectToThread(joinGameHandler, btDevice, GroundhogHunterApp.ApplicationUUID);
                            btDiscoveredMap.put(remoteMacAddress, connectToThread);
                        }
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    megString = scanBluetoothStartedString;
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView(scanBluetoothStartedString, MessageDuration);
                    // ScreenUtil.showToast(context, scanBluetoothStartedString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    megString = scanBluetoothFinishedString;
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView(scanBluetoothFinishedString, MessageDuration);
                    // ScreenUtil.showToast(context, scanBluetoothFinishedString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    break;
            }
        }
    }

    private class JoinGameHandler extends Handler {

        private final Looper mLooper;

        public JoinGameHandler(Looper looper) {
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
            BluetoothConnectToThread connectToThread;
            BluetoothFunctionThread btFunctionThread;

            Context mContext = getApplicationContext();
            Bundle data = msg.getData();

            switch (msg.what) {
                case CommonConstants.BluetoothDiscoveryTimerHasReached:
                    megString = discoveryTimeHasReachedString;
                    Log.d(TAG, megString);
                    // ScreenUtil.showToast(mContext, megString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    if (mBluetoothAdapter.isDiscovering()) {
                        mBluetoothAdapter.cancelDiscovery();
                    }
                    showMessage.showMessageInTextView(megString, MessageDuration);
                    // start to connect all the device that were found
                    for (BluetoothConnectToThread connectThread : btDiscoveredMap.values()) {
                        connectThread.start();
                    }
                    break;
                case CommonConstants.BluetoothDiscoveryTimerHasBeenDismissed:
                    megString = discoveryWasDismissedString;
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView(megString, MessageDuration);
                    // ScreenUtil.showToast(mContext, megString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    break;
                case CommonConstants.BluetoothConnectToThreadNoClientSocket:
                    btDevice = data.getParcelable("BluetoothDevice");
                    deviceName = BluetoothUtil.getBluetoothDeviceName(btDevice);
                    remoteMacAddress = btDevice.getAddress();
                    megString = cannotCreateClientSocketString + "(" + deviceName +")";
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView(megString, MessageDuration);
                    // connectToThread = btDiscoveredMap.get(btDevice);
                    connectToThread = btDiscoveredMap.get(remoteMacAddress);
                    stopBluetoothConnectToThread(connectToThread,true);
                    break;
                case CommonConstants.BluetoothConnectToThreadStarted:
                    btDevice = data.getParcelable("BluetoothDevice");
                    deviceName = BluetoothUtil.getBluetoothDeviceName(btDevice);
                    megString = startedConnectingToHostString + "(" + deviceName + ")";
                    Log.d(TAG, megString);
                    // ScreenUtil.showToast(context, startedConnectingToHostString + "("+deviceName+")", toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    break;
                case CommonConstants.BluetoothConnectToThreadConnected:
                    btDevice = data.getParcelable("BluetoothDevice");
                    deviceName = BluetoothUtil.getBluetoothDeviceName(btDevice);
                    megString = connectToHostSucceededString + "(" + deviceName + ")";
                    remoteMacAddress = btDevice.getAddress();
                    Log.d(TAG, megString);
                    // start reading data from the other device and writing data to the other device
                    // start communicating
                    // connectToThread = btDiscoveredMap.get(btDevice);
                    connectToThread = btDiscoveredMap.get(remoteMacAddress);
                    btFunctionThread = connectToThread.getBtFunctionThread();
                    synchronized (btFunctionThread) {
                        btFunctionThread.setStartRead(true);    // start reading data
                        btFunctionThread.notifyAll();
                    }
                    if (!btMacFunctionThreadMap.containsKey(remoteMacAddress)) {
                        btMacFunctionThreadMap.put(remoteMacAddress, btFunctionThread);
                    }

                    stopBluetoothConnectToThread(connectToThread, false);
                    break;
                case CommonConstants.OppositePlayerNameHasBeenRead:
                    btDevice = data.getParcelable("BluetoothDevice");
                    String oppositeName = data.getString("OppositePlayerName");
                    megString = oppositeName + hasBeenReadString + ".";
                    showMessage.showMessageInTextView(megString , MessageDuration);
                    Log.d(TAG, megString);
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
                    break;
                case CommonConstants.BluetoothConnectToThreadFailedToConnect:
                    btDevice = data.getParcelable("BluetoothDevice");
                    remoteMacAddress = btDevice.getAddress();
                    deviceName = BluetoothUtil.getBluetoothDeviceName(btDevice);
                    megString = connectToHostFailedString + "(" + deviceName + ")";
                    Log.d(TAG, megString);
                    if (btDiscoveredMap != null) {
                        // connectToThread = btDiscoveredMap.get(btDevice);
                        connectToThread = btDiscoveredMap.get(remoteMacAddress);
                        stopBluetoothConnectToThread(connectToThread, true);
                    }
                    break;
                case CommonConstants.BluetoothHostExitCode:
                    showMessage.showMessageInTextView(hostLeftGameString, MessageDuration);
                    // ScreenUtil.showToast(mContext, hostLeftGameString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    remoteMacAddress = data.getString("BluetoothMacAddress");
                    btFunctionThread = btMacFunctionThreadMap.get(remoteMacAddress);
                    btMacFunctionThreadMap.remove(remoteMacAddress);

                    // release btFunctionThread (stop communicating)
                    BluetoothUtil.stopBluetoothFunctionThread(btFunctionThread);

                    // remove the remote connected device from oppositePlayerNameList
                    oppositePlayerNameMap.remove(remoteMacAddress);

                    // update list view
                    ArrayList<String> oppNameList = new ArrayList<>(oppositePlayerNameMap.values());
                    twoPlayerListAdapter.updateData(oppNameList);

                    break;
                case CommonConstants.BluetoothStartGame:
                    if (selectedBtFunctionThread != null) {
                        GroundhogHunterApp.selectedBtFuncThread = selectedBtFunctionThread;
                        // stop other BluetoothFunctionThreads
                        for (String remoteMac : btMacFunctionThreadMap.keySet()) {
                            BluetoothFunctionThread btFuncThread = btMacFunctionThreadMap.get(remoteMac);
                            connectToThread = btDiscoveredMap.get(remoteMac);
                            if (btFuncThread != selectedBtFunctionThread) {
                                btFuncThread.write(CommonConstants.BluetoothClientExitCode, remoteMac);
                                BluetoothUtil.stopBluetoothFunctionThread(btFuncThread);
                                stopBluetoothConnectToThread(connectToThread, true);
                            } else {
                                stopBluetoothConnectToThread(connectToThread, false);
                            }
                        }

                        // clear HashMaps
                        btDiscoveredMap = null;
                        btMacFunctionThreadMap.clear();
                        btMacFunctionThreadMap = null;
                        oppositePlayerNameMap.clear();
                        oppositePlayerNameMap = null;
                        //
                        Intent gameIntent = new Intent(getApplicationContext(), GroundhogActivity.class);
                        gameIntent.putExtra("GameType", CommonConstants.BluetoothGameByClient);
                        startActivityForResult(gameIntent, CommonConstants.BluetoothGameByClient);
                    }
                    break;
            }
        }
    }
}
