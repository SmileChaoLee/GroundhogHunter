package com.smile.groundhoghunter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.smile.groundhoghunter.AbstractClasses.ClientConnectToThread;
import com.smile.groundhoghunter.AbstractClasses.IoFunctionThread;
import com.smile.groundhoghunter.ArrayAdapters.TwoPlayerListAdapter;
import com.smile.groundhoghunter.Constants.CommonConstants;
import com.smile.groundhoghunter.Interfaces.ConnectDevice;
import com.smile.groundhoghunter.Threads.ClientDiscoveryTimerThread;
import com.smile.groundhoghunter.Utilities.ConnectDeviceUtil;
import com.smile.groundhoghunter.Utilities.MessageShowingUtil;
import com.smile.smilelibraries.customized_button.SmileImageButton;
import com.smile.smilelibraries.utilities.FontAndBitmapUtil;
import com.smile.smilelibraries.utilities.ScreenUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class JoinGameActivity extends AppCompatActivity {

    // private properties
    private static final String TAG = new String(".JoinGameActivity");
    private static final int DurationForBluetoothDiscovery = 15000;  // 15 seconds one time

    private float textFontSize;
    private float fontScale;
    private float toastTextSize;

    private TextView playerNameTextView;
    private String oppositePlayerName;
    private ListView oppositePlayerNameListView;
    private LinkedHashMap<String, String> oppositePlayerNameMap;

    private String cannotCreateClientSocketString;
    private String connectToHostSucceededString;
    private String connectToHostFailedString;
    private String hostLeftGameString;
    private String discoveryTimeHasReachedString;
    private String discoveryWasDismissedString;
    private String hasBeenReadString;
    private TwoPlayerListAdapter twoPlayerListAdapter;

    protected static final int MessageDuration = 1000;    // 1 second

    protected String playerName;
    protected MessageShowingUtil showMessage;
    protected Handler joinGameHandler;
    protected ConnectDevice clientConnectDevice;
    protected ClientDiscoveryTimerThread discoveryTimerThread;
    protected HashMap<String, ClientConnectToThread> discoveredDeviceMap;
    protected HashMap<String, IoFunctionThread> ioFunctionThreadMap;
    protected IoFunctionThread selectedIoFunctionThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent callingIntent = getIntent();
        playerName = callingIntent.getStringExtra("PlayerName");
        if (playerName == null) {
            playerName = "";
        }

        joinGameHandler = new JoinGameHandler(Looper.getMainLooper());
        discoveredDeviceMap = new HashMap<>();
        ioFunctionThreadMap = new HashMap<>();
        selectedIoFunctionThread = null;

        oppositePlayerNameMap = new LinkedHashMap<>();

        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this);
        fontScale = ScreenUtil.getPxFontScale(this);
        toastTextSize = textFontSize * 0.8f;

        cannotCreateClientSocketString = getString(R.string.cannotCreateClientSocketString);
        connectToHostSucceededString = getString(R.string.connectToHostSucceededString);
        connectToHostFailedString = getString(R.string.connectToHostFailedString);
        hostLeftGameString = getString(R.string.hostLeftGameString);
        discoveryTimeHasReachedString = getString(R.string.discoveryTimeHasReachedString);
        discoveryWasDismissedString = getString(R.string.discoveryWasDismissedString);
        hasBeenReadString = getString(R.string.hasBeenReadString);

        // int colorDarkOrange = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkOrange);
        // int colorRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.red);
        int colorDarkRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkRed);
        int colorDarkGreen = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkGreen);
        int colorBlue = Color.BLUE;

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_join_game);

        // message showing view
        TextView toastMessageTextView = findViewById(R.id.toastMessageTextView);
        ScreenUtil.resizeTextSize(toastMessageTextView,toastTextSize, GroundhogHunterApp.FontSize_Scale_Type);
        showMessage = new MessageShowingUtil(this, toastMessageTextView);

        TextView joinGameTitleTextView = findViewById(R.id.joinGameTitleTextView);
        joinGameTitleTextView.setText(getString(R.string.joinBluetoothGameString));
        ScreenUtil.resizeTextSize(joinGameTitleTextView, textFontSize * 1.2f, GroundhogHunterApp.FontSize_Scale_Type);

        TextView playerNameStringTextView = findViewById(R.id.playerNameStringTextView);
        ScreenUtil.resizeTextSize(playerNameStringTextView, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);

        playerNameTextView = findViewById(R.id.playerNameTextView);
        playerNameTextView.setText(playerName);
        ScreenUtil.resizeTextSize(playerNameTextView, textFontSize, GroundhogHunterApp.FontSize_Scale_Type);

        oppositePlayerNameListView = findViewById(R.id.oppositePlayerNameListView);
        ArrayList<String> oppNameList = new ArrayList<>();
        twoPlayerListAdapter = new TwoPlayerListAdapter(this, R.layout.player_list_item_layout, R.id.playerNameTextView, oppNameList, textFontSize);
        twoPlayerListAdapter.setNotifyOnChange(false);  // do not call notifyDataSetChanged() method automatically
        oppositePlayerNameListView.setAdapter(twoPlayerListAdapter);
        oppositePlayerNameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long rowId) {
                String temp;
                if (adapterView != null) {
                    Object item = adapterView.getItemAtPosition(position);
                    if (item != null) {
                        temp = item.toString();
                        oppositePlayerName = temp;
                        String oppName;
                        // get remote mac address of remote device
                        for (String remoteMacAddress : oppositePlayerNameMap.keySet()) {
                            IoFunctionThread ioFunctionThread = ioFunctionThreadMap.get(remoteMacAddress);
                            if (ioFunctionThread != null) {
                                oppName = oppositePlayerNameMap.get(remoteMacAddress);
                                if (oppName.equals(oppositePlayerName)) {
                                    selectedIoFunctionThread = ioFunctionThread;
                                    selectedIoFunctionThread.write(CommonConstants.OppositePlayerNameHasBeenRead, playerName);
                                    view.setSelected(true);
                                } else {
                                    ioFunctionThread.write(CommonConstants.TwoPlayerClientExitCode, remoteMacAddress);
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
                startDiscovery();
                Log.d(TAG, "Refresh --> startDiscovery()");
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

        startDiscovery();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "JoinGameActivity --> Came back from BtJoinGameActivity.");
        String megString;
        switch(requestCode) {
            case CommonConstants.TwoPlayerGameByClient:
                oppositePlayerName = "";
                discoveredDeviceMap = new HashMap<>();
                ioFunctionThreadMap = new HashMap<>();
                selectedIoFunctionThread = null;
                oppositePlayerNameMap = new LinkedHashMap<>();
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
        ArrayList<IoFunctionThread> threadList = new ArrayList<>(ioFunctionThreadMap.values());
        ConnectDeviceUtil.stopIoFunctionThreads(threadList);
        ioFunctionThreadMap.clear();
        ioFunctionThreadMap = null;

        stopClientConnectToThreadAndClearClientDiscoveredMap();
        discoveredDeviceMap = null;

        oppositePlayerNameMap.clear();
        oppositePlayerNameMap = null;

        selectedIoFunctionThread = null;

        twoPlayerListAdapter.clear();
        twoPlayerListAdapter = null;

        stopClientDiscoveryTimerThread();

        if (joinGameHandler != null) {
            joinGameHandler.removeCallbacksAndMessages(null);
            joinGameHandler = null;
        }
    }

    protected void returnToPrevious() {

        // if it Still is connecting to other device
        // then notify the other device leaving

        finish();
    }

    protected void startDiscovery() {

        stopClientDiscoveryTimerThread();  // stop discovering devices (servers)

        clientLeavingNotification();

        ArrayList<IoFunctionThread> threadList = new ArrayList<>(ioFunctionThreadMap.values());
        ConnectDeviceUtil.stopIoFunctionThreads(threadList);
        ioFunctionThreadMap.clear();

        stopClientConnectToThreadAndClearClientDiscoveredMap();

        joinGameHandler.removeCallbacksAndMessages(null);   // added on 2019-05-14

        oppositePlayerNameMap.clear();
        twoPlayerListAdapter.clear();
        twoPlayerListAdapter.notifyDataSetChanged();

        startClientDiscoveryTimerThread();  // start discovering devices (servers)
    }

    protected void startClientGame() {
    }

    private void clientLeavingNotification() {
        if (clientConnectDevice != null) {
            String macAddress = clientConnectDevice.getAddress();
            for (IoFunctionThread btFunctionThread : ioFunctionThreadMap.values()) {
                btFunctionThread.write(CommonConstants.TwoPlayerClientExitCode, "");
            }
        }
    }

    private void startClientDiscoveryTimerThread() {
        int timerPeriod = DurationForBluetoothDiscovery;
        discoveryTimerThread = new ClientDiscoveryTimerThread(joinGameHandler, timerPeriod);
        discoveryTimerThread.start();
    }

    private void stopClientDiscoveryTimerThread() {
        if (discoveryTimerThread != null) {
            discoveryTimerThread.dismissTimerThread();
            boolean retry = true;
            while (retry) {
                try {
                    discoveryTimerThread.join();
                    Log.d(TAG, "discoveryTimerThread.Join()........\n");
                    retry = false;
                    discoveryTimerThread = null;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }// continue processing until the thread ends
            }
        }
    }

    private void stopClientConnectToThreadAndClearClientDiscoveredMap() {
        for(ClientConnectToThread connectToThread : discoveredDeviceMap.values()) {
            stopClientConnectToThread(connectToThread, true);
            connectToThread = null;
        }
        discoveredDeviceMap.clear(); // clear HashSet because of starting discovering
    }

    private void stopClientConnectToThread(ClientConnectToThread connectToThread, boolean isCloseClientSocket) {
        if (connectToThread != null) {
            if (isCloseClientSocket) {
                connectToThread.closeClientSocket();
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

            ClientConnectToThread connectToThread;
            IoFunctionThread ioFunctionThread;

            Log.d(TAG, "Message = " + msg.what);
            Context mContext = getApplicationContext();
            Bundle data = msg.getData();
            ConnectDevice connectDevice;
            String remoteMacAddress;

            switch (msg.what) {
                case CommonConstants.ClientDiscoveryTimerHasReached:
                    megString = discoveryTimeHasReachedString;
                    Log.d(TAG, megString);
                    if (clientConnectDevice.isDiscovering()) {
                        clientConnectDevice.cancelDiscovery();
                    }
                    showMessage.showMessageInTextView(megString, MessageDuration);
                    // start to connect all the device that were found
                    for (ClientConnectToThread connectThread : discoveredDeviceMap.values()) {
                        connectThread.start();
                    }
                    break;
                case CommonConstants.ClientDiscoveryTimerHasBeenDismissed:
                    megString = discoveryWasDismissedString;
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView(megString, MessageDuration);
                    break;
                case CommonConstants.ClientConnectToThreadNoClientSocket:
                    connectDevice = data.getParcelable("ConnectDevice");
                    remoteMacAddress = connectDevice.getAddress();
                    deviceName = ConnectDeviceUtil.getConnectDeviceName(connectDevice);
                    megString = cannotCreateClientSocketString + "(" + deviceName +")";
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView(megString, MessageDuration);
                    connectToThread = discoveredDeviceMap.get(remoteMacAddress);
                    stopClientConnectToThread(connectToThread,true);
                    break;
                case CommonConstants.ClientConnectToThreadConnected:
                    connectDevice = data.getParcelable("ConnectDevice");
                    remoteMacAddress = connectDevice.getAddress();
                    deviceName = ConnectDeviceUtil.getConnectDeviceName(connectDevice);
                    megString = connectToHostSucceededString + "(" + deviceName + ")";
                    Log.d(TAG, megString);
                    // start reading data from the other device and writing data to the other device
                    connectToThread = discoveredDeviceMap.get(remoteMacAddress);
                    ioFunctionThread = connectToThread.getIoFunctionThread();
                    ioFunctionThread.setStartRead(true);    // start reading data

                    if (!ioFunctionThreadMap.containsKey(remoteMacAddress)) {
                        ioFunctionThreadMap.put(remoteMacAddress, ioFunctionThread);
                    }

                    stopClientConnectToThread(connectToThread, false);
                    break;
                case CommonConstants.OppositePlayerNameHasBeenRead:
                    connectDevice = data.getParcelable("ConnectDevice");
                    remoteMacAddress = connectDevice.getAddress();
                    String oppositeName = data.getString("OppositePlayerName");
                    megString = oppositeName + " " + hasBeenReadString + ".";
                    showMessage.showMessageInTextView(megString , MessageDuration);
                    Log.d(TAG, megString);
                    if (oppositeName != null) {
                        if (!oppositeName.isEmpty()) {
                            if (!oppositePlayerNameMap.containsKey(remoteMacAddress)) {
                                oppositePlayerNameMap.put(remoteMacAddress, oppositeName);
                                ArrayList<String> oppNameList = new ArrayList<>(oppositePlayerNameMap.values());
                                twoPlayerListAdapter.updateData(oppNameList);
                            }
                        }
                    }

                    ioFunctionThread = ioFunctionThreadMap.get(remoteMacAddress);
                    ioFunctionThread.setStartRead(true);    // read next data data

                    break;
                case CommonConstants.ClientConnectToThreadFailedToConnect:
                    connectDevice = data.getParcelable("ConnectDevice");
                    remoteMacAddress = connectDevice.getAddress();
                    deviceName = ConnectDeviceUtil.getConnectDeviceName(connectDevice);
                    megString = connectToHostFailedString + "(" + deviceName + ")";
                    Log.d(TAG, megString);
                    if (discoveredDeviceMap != null) {
                        connectToThread = discoveredDeviceMap.get(remoteMacAddress);
                        stopClientConnectToThread(connectToThread, true);
                    }
                    break;
                case CommonConstants.TwoPlayerHostExitCode:
                    connectDevice = data.getParcelable("ConnectDevice");
                    remoteMacAddress = connectDevice.getAddress();
                    showMessage.showMessageInTextView(hostLeftGameString, MessageDuration);

                    ioFunctionThread = ioFunctionThreadMap.get(remoteMacAddress);
                    ioFunctionThread.setStartRead(true);    // start reading data

                    // remove the remote connected device from oppositePlayerNameList
                    oppositePlayerNameMap.remove(remoteMacAddress);

                    // update list view
                    ArrayList<String> oppNameList = new ArrayList<>(oppositePlayerNameMap.values());
                    twoPlayerListAdapter.updateData(oppNameList);

                    break;
                case CommonConstants.TwoPlayerHostStartGame:
                    if (selectedIoFunctionThread != null) {
                        GroundhogHunterApp.selectedIoFuncThread = selectedIoFunctionThread;
                        for (String remoteMac : ioFunctionThreadMap.keySet()) {
                            IoFunctionThread ioFuncThread = ioFunctionThreadMap.get(remoteMac);
                            connectToThread = discoveredDeviceMap.get(remoteMac);
                            if (ioFuncThread != selectedIoFunctionThread) {
                                ioFuncThread.write(CommonConstants.TwoPlayerClientExitCode, "");
                                ConnectDeviceUtil.stopIoFunctionThread(ioFuncThread);
                                stopClientConnectToThread(connectToThread, true);
                            } else {
                                stopClientConnectToThread(connectToThread, false);
                            }
                        }

                        // clear HashMaps
                        discoveredDeviceMap = null;
                        ioFunctionThreadMap.clear();
                        ioFunctionThreadMap = null;
                        oppositePlayerNameMap.clear();
                        oppositePlayerNameMap = null;
                        //
                        // remove all message from joinGameHandler, // added on 2019-05-14
                        joinGameHandler.removeCallbacksAndMessages(null);
                        //

                        // start game by different medias
                        startClientGame();
                    }
                    break;
                case CommonConstants.TwoPlayerDefaultReading:
                    Log.d(TAG, "Default reading.");
                    // read the next data
                    connectDevice = data.getParcelable("ConnectDevice");
                    remoteMacAddress = connectDevice.getAddress();
                    ioFunctionThread = ioFunctionThreadMap.get(remoteMacAddress);
                    ioFunctionThread.setStartRead(true);    // start reading data
                    break;
            }
        }
    }
}
