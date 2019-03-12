package com.smile.groundhoghunter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
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
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.smile.groundhoghunter.ArrayAdatpers.TwoPlayerListAdapter;
import com.smile.groundhoghunter.Constants.BluetoothConstants;
import com.smile.groundhoghunter.Models.SmileImageButton;
import com.smile.groundhoghunter.Threads.BluetoothConnectToThread;
import com.smile.groundhoghunter.Threads.BluetoothDiscoveryTimerThread;
import com.smile.groundhoghunter.Threads.BluetoothFunctionThread;
import com.smile.groundhoghunter.Utilities.BluetoothUtil;
import com.smile.smilepublicclasseslibrary.utilities.FontAndBitmapUtil;
import com.smile.smilepublicclasseslibrary.utilities.ScreenUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class BluetoothJoinGameActivity extends AppCompatActivity {

    // private properties
    private static final String TAG = new String(".BluetoothJoinGameActivity");
    private static final int Request_Enable_Bluetooth_For_Discovering = 2; // request to enable bluetooth for discovering
    private static final int durationForBluetoothVisible = GroundhogHunterApp.durationForBluetoothVisible;

    private float textFontSize;
    private float fontScale;
    private float toastTextSize;

    private TextView playerNameTextView;
    private String playerName;
    private String oppositePlayerName;
    private ListView oppositePlayerListView;
    private HashMap<String, String> oppositePlayerNameMap;

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

    private TwoPlayerListAdapter twoPlayerListAdapter;
    private BluetoothJoinGameBroadcastReceiver btJoinGameReceiver;
    private BluetoothDiscoveryTimerThread mBluetoothDiscoveryTimerThread;
    private BluetoothConnectToThread mBluetoothConnectToThread;
    private HashMap<String, BluetoothFunctionThread> btMacAddressThreadMap;

    private boolean isDefaultBluetoothEnabled;
    private BluetoothAdapter mBluetoothAdapter;
    private HashSet<BluetoothDevice> btDeviceDiscoveredHashSet;

    private Handler joinGameHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        joinGameHandler = new JoinGameHandler(Looper.getMainLooper());

        btDeviceDiscoveredHashSet = new HashSet<BluetoothDevice>();
        oppositePlayerNameMap = new HashMap<>();
        btMacAddressThreadMap = new HashMap<>();

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

        oppositePlayerListView = findViewById(R.id.playerListView);
        ArrayList<String> oppNameList = new ArrayList<>();
        twoPlayerListAdapter = new TwoPlayerListAdapter(this, R.layout.player_list_item_layout, R.id.playerNameTextView, oppNameList, textFontSize);
        twoPlayerListAdapter.setNotifyOnChange(false);  // do not call notifyDataSetChanged() method automatically
        oppositePlayerListView.setAdapter(twoPlayerListAdapter);
        oppositePlayerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long rowId) {
                if (adapterView != null) {
                    Object item = adapterView.getItemAtPosition(position);
                    if (item != null) {
                        view.setSelected(true);
                        String temp = item.toString();
                        oppositePlayerName = temp;
                        String oppName;
                        // get remote mac address of remote device
                        for (String remoteMacAddress : oppositePlayerNameMap.keySet()) {
                            oppName = oppositePlayerNameMap.get(remoteMacAddress);
                            if (oppName.equals(oppositePlayerName)) {
                                BluetoothFunctionThread btFunctionThread = btMacAddressThreadMap.get(remoteMacAddress);
                                if (btFunctionThread != null) {
                                    btFunctionThread.write(BluetoothConstants.OppositePlayerNameHasBeenRead, playerName);
                                }
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

        BluetoothUtil.stopBluetoothFunctionThreads(btMacAddressThreadMap);
        btMacAddressThreadMap.clear();
        btMacAddressThreadMap = null;

        btDeviceDiscoveredHashSet.clear(); // clear HashSet because of starting discovering
        btDeviceDiscoveredHashSet = null;
        oppositePlayerNameMap.clear();
        oppositePlayerNameMap = null;
        twoPlayerListAdapter.clear();
        twoPlayerListAdapter = null;

        stopBluetoothDiscoveryTimerThread();
        stopBluetoothConnectToThread(true);

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

        if (mBluetoothAdapter != null) {
            String macAddress = mBluetoothAdapter.getAddress();
            for (String remoteMacAddress : btMacAddressThreadMap.keySet()) {
                BluetoothFunctionThread btFunctionThread = btMacAddressThreadMap.get(remoteMacAddress);
                btFunctionThread.write(BluetoothConstants.ClientExitCode, macAddress);
            }
        }

        finish();
    }

    private void startBluetoothDiscovery() {
        btDeviceDiscoveredHashSet.clear(); // clear HashSet because of starting discovering
        BluetoothUtil.stopBluetoothFunctionThreads(btMacAddressThreadMap);
        btMacAddressThreadMap.clear();
        oppositePlayerNameMap.clear();
        twoPlayerListAdapter.clear();
        twoPlayerListAdapter.notifyDataSetChanged();

        stopBluetoothConnectToThread(true);

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, Request_Enable_Bluetooth_For_Discovering);
    }

    private void startBluetoothDiscoveryTimerThread() {
        int timerPeriod = durationForBluetoothVisible * 1000;   // transfer to mini seconds
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

    private void stopBluetoothConnectToThread(boolean isCloseBluetoothSocket) {
        if (mBluetoothConnectToThread != null) {
            if (isCloseBluetoothSocket) {
                mBluetoothConnectToThread.closeBluetoothSocket();
            }
            boolean retry = true;
            while (retry) {
                try {
                    mBluetoothConnectToThread.join();
                    Log.d(TAG, "mBluetoothAcceptThread.Join()........\n");
                    retry = false;
                    mBluetoothConnectToThread = null;
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
            String action = "";
            if (intent != null) {
                action = intent.getAction();
            }
            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    BluetoothDevice mBluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    megString = BluetoothUtil.getBluetoothDeviceName(mBluetoothDevice);
                    ScreenUtil.showToast(context, megString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    Log.d(TAG, "Found bluetooth device: " + megString);
                    // start to connect to host game
                    if (mBluetoothDevice != null) {
                        if (!btDeviceDiscoveredHashSet.contains(mBluetoothDevice)) {
                            // ScreenUtil.showToast(context, BluetoothUtil.getBluetoothDeviceName(mBluetoothDevice), toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                            btDeviceDiscoveredHashSet.add(mBluetoothDevice);
                            mBluetoothAdapter.cancelDiscovery();    // stop discovering to speed up connection
                            stopBluetoothConnectToThread(true);
                            mBluetoothConnectToThread = new BluetoothConnectToThread(joinGameHandler, mBluetoothDevice, GroundhogHunterApp.ApplicationUUID);
                            mBluetoothConnectToThread.start();
                        }
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    megString = scanBluetoothStartedString;
                    Log.d(TAG, megString);
                    ScreenUtil.showToast(context, scanBluetoothStartedString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    megString = scanBluetoothFinishedString;
                    Log.d(TAG, megString);
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
            BluetoothSocket mBluetoothSocket;
            BluetoothFunctionThread btFunctionThread;

            Context mContext = getApplicationContext();
            Bundle data = msg.getData();

            switch (msg.what) {
                case BluetoothConstants.BluetoothConnectToThreadNoClientSocket:
                    megString = cannotCreateClientSocketString;
                    Log.d(TAG, megString);
                    ScreenUtil.showToast(mContext, cannotCreateClientSocketString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    stopBluetoothDiscoveryTimerThread();
                    stopBluetoothConnectToThread(true);
                    break;
                case BluetoothConstants.BluetoothConnectToThreadStarted:
                    deviceName = "";
                    if (data != null) {
                        deviceName = data.getString("BluetoothDeviceName");
                    }
                    megString = startedConnectingToHostString + "("+deviceName+")";
                    Log.d(TAG, megString);
                    // ScreenUtil.showToast(context, startedConnectingToHostString + "("+deviceName+")", toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    break;
                case BluetoothConstants.BluetoothConnectToThreadConnected:
                    mBluetoothSocket = mBluetoothConnectToThread.getBluetoothSocket();
                    btDevice = mBluetoothSocket.getRemoteDevice();
                    deviceName = BluetoothUtil.getBluetoothDeviceName(btDevice);    // deviceName will not empty
                    remoteMacAddress = btDevice.getAddress();
                    megString = connectToHostSucceededString + "("+deviceName+")";
                    Log.d(TAG, megString);
                    // start reading data from the other device and writing data to the other device
                    // start communicating
                    btFunctionThread = mBluetoothConnectToThread.getBtFunctionThread();
                    synchronized (btFunctionThread) {
                        btFunctionThread.setStartRead(true);    // start reading data
                        btFunctionThread.notifyAll();
                    }
                    if (!btMacAddressThreadMap.containsKey(remoteMacAddress)) {
                        btMacAddressThreadMap.put(remoteMacAddress, btFunctionThread);
                    }
                    break;
                case BluetoothConstants.OppositePlayerNameHasBeenRead:
                    megString = data.getString("OppositePlayerName");
                    ScreenUtil.showToast(mContext, megString + " hsa been read.", toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    Log.d(TAG, "OppositePlayerNameHasBeenRead: " + megString);
                    mBluetoothSocket = mBluetoothConnectToThread.getBluetoothSocket();
                    btDevice = mBluetoothSocket.getRemoteDevice();
                    deviceName = BluetoothUtil.getBluetoothDeviceName(btDevice);
                    remoteMacAddress = btDevice.getAddress();
                    boolean isAddedName = false;
                    String oppositeName = data.getString("OppositePlayerName");
                    if (oppositeName != null) {
                        if (!oppositeName.isEmpty()) {
                            if (!oppositePlayerNameMap.containsKey(remoteMacAddress)) {
                                oppositePlayerNameMap.put(remoteMacAddress, oppositeName);
                                ArrayList<String> oppNameList = new ArrayList<>();
                                for (String macAddress : oppositePlayerNameMap.keySet()) {
                                    oppNameList.add(oppositePlayerNameMap.get(macAddress));
                                }
                                twoPlayerListAdapter.updateData(oppNameList);
                                isAddedName = true;
                            }
                        }
                    }
                    if (isAddedName) {
                        stopBluetoothDiscoveryTimerThread();    // stop discovery
                        stopBluetoothConnectToThread(false);    // do not close the BluetoothSocket
                    } else {
                        stopBluetoothConnectToThread(true);    // close the BluetoothSocket
                    }
                    break;
                case BluetoothConstants.BluetoothConnectToThreadFailedToConnect:
                    deviceName = "";
                    if (data != null) {
                        deviceName = data.getString("BluetoothDeviceName");
                    }
                    megString = connectToHostFailedString + "("+deviceName+")";
                    Log.d(TAG, megString);
                    // ScreenUtil.showToast(context, connectToHostFailedString + "("+deviceName+")", toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    mBluetoothAdapter.startDiscovery(); // start discovering again
                    stopBluetoothConnectToThread(true);
                    break;
                case BluetoothConstants.DiscoveryTimerHasReached:
                    megString = "Discovery time has reached.";
                    Log.d(TAG, megString);
                    ScreenUtil.showToast(mContext, megString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    if (mBluetoothAdapter.isDiscovering()) {
                        mBluetoothAdapter.cancelDiscovery();
                    }
                    stopBluetoothConnectToThread(true);
                    break;
                case BluetoothConstants.DiscoveryTimerHasBeenDismissed:
                    megString = "Discovery timer has been dismissed.";
                    Log.d(TAG, megString);
                    ScreenUtil.showToast(mContext, megString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    break;
                case BluetoothConstants.HostExitCode:
                    ScreenUtil.showToast(mContext, hostLeftGameString, toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    remoteMacAddress = data.getString("BluetoothMacAddress");
                    btFunctionThread = btMacAddressThreadMap.get(remoteMacAddress);
                    btMacAddressThreadMap.remove(remoteMacAddress);

                    // release btFunctionThread (stop communicating)
                    BluetoothUtil.stopBluetoothFunctionThread(btFunctionThread);

                    // remove the remote connected device from oppositePlayerNameList
                    oppositePlayerNameMap.remove(remoteMacAddress);

                    // update list view
                    ArrayList<String> oppNameList = new ArrayList<>();
                    for (String macAddress : oppositePlayerNameMap.keySet()) {
                        oppNameList.add(oppositePlayerNameMap.get(macAddress));
                    }
                    twoPlayerListAdapter.updateData(oppNameList);

                    break;
            }
        }
    }
}
