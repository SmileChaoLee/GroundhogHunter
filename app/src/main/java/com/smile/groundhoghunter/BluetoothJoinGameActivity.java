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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
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
import java.util.HashSet;

public class BluetoothJoinGameActivity extends AppCompatActivity {

    // private properties
    private static final String TAG = new String(".BluetoothJoinGameActivity");
    private static final int Request_Enable_Bluetooth_For_Discovering = 2; // request to enable bluetooth for discovering
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
    private ArrayList<BluetoothFunctionThread> bluetoothFunctionThreadList;

    private boolean isDefaultBluetoothEnabled;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket;
    private HashSet<BluetoothDevice> bluetoothDeviceHashSet;

    private Handler joinGameHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        joinGameHandler = new JoinGameHandler(Looper.getMainLooper());

        bluetoothDeviceHashSet = new HashSet<BluetoothDevice>();
        playerNameList = new ArrayList<>();
        bluetoothFunctionThreadList = new ArrayList<>();

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

        deviceName = BluetoothUtil.getBluetoothDeviceName(mBluetoothAdapter);

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
                        // write player name to the other device
                        BluetoothFunctionThread mBluetoothConnectToThread = bluetoothFunctionThreadList.get(position);
                        if (mBluetoothConnectToThread != null) {
                            mBluetoothConnectToThread.write(BluetoothConstants.PlayerNameHasBeenRead, playerName);
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
                    bluetoothDeviceHashSet.clear(); // clear HashSet because of starting discovering
                    playerNameList.clear(); // clear name list
                    twoPlayerListAdapter.notifyDataSetChanged();
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
        // recover the status of bluetooth

        stopBluetoothFunctionThreads();

        stopBluetoothDiscoveryTimerThread();
        stopBluetoothConnectToThread(true);
        BluetoothUtil.closeBluetoothSocket(mBluetoothSocket);

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

        if (bluetoothFunctionThreadList.size()>0) { // temporarily set to 1
            BluetoothFunctionThread mBluetoothFunctionThread = bluetoothFunctionThreadList.get(0);
            mBluetoothFunctionThread.write(BluetoothConstants.ClientExitCode,"");
        }

        finish();
    }

    private void startBluetoothDiscovery() {
        playerNameList.clear();
        twoPlayerListAdapter.notifyDataSetChanged();

        stopBluetoothFunctionThreads();

        stopBluetoothConnectToThread(true);
        BluetoothUtil.closeBluetoothSocket(mBluetoothSocket);

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
        bluetoothFunctionThreadList.clear();
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
                    // start to connect to host game
                    if (mBluetoothDevice != null) {
                        if (!bluetoothDeviceHashSet.contains(mBluetoothDevice)) {
                            megString = BluetoothUtil.getBluetoothDeviceName(mBluetoothDevice);
                            Log.d(TAG, megString);
                            // ScreenUtil.showToast(context, BluetoothUtil.getBluetoothDeviceName(mBluetoothDevice), toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                            bluetoothDeviceHashSet.add(mBluetoothDevice);
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
        private final Context mContext;

        public JoinGameHandler(Looper looper) {
            super(looper);
            mLooper = looper;
            mContext = getApplicationContext();
        }

        @Override
        public void handleMessage(Message msg) {
            // super.handleMessage(msg);

            String megString;
            String deviceName = "";
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
                    deviceName = "";
                    if (data != null) {
                        deviceName = data.getString("BluetoothDeviceName");
                    }
                    megString = connectToHostSucceededString + "("+deviceName+")";
                    Log.d(TAG, megString);
                    // ScreenUtil.showToast(mContext, connectToHostSucceededString +"("+deviceName+")", toastTextSize, GroundhogHunterApp.FontSize_Scale_Type, Toast.LENGTH_SHORT);
                    boolean isAddedName = false;
                    if ((deviceName != null) && (!deviceName.isEmpty())) {
                        if (!playerNameList.contains(deviceName)) {
                            playerNameList.add(deviceName);
                            twoPlayerListAdapter.notifyDataSetChanged();
                            mBluetoothSocket = mBluetoothConnectToThread.getBluetoothSocket();
                            isAddedName = true;
                        }
                    }
                    if (isAddedName) {
                        stopBluetoothDiscoveryTimerThread();    // stop discovery
                        stopBluetoothConnectToThread(false);    // do not close the BluetoothSocket
                        // start reading data from the other device and writing data to the other device
                        // start communicating
                        BluetoothFunctionThread mBluetoothFunctionThread = new BluetoothFunctionThread(joinGameHandler, mBluetoothSocket);
                        mBluetoothFunctionThread.start();
                        bluetoothFunctionThreadList.add(mBluetoothFunctionThread);
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
                    // stop communicating to the other
                    stopBluetoothFunctionThreads();
                    break;
            }
        }
    }
}
