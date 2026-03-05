package com.smile.groundhoghunter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import com.smile.groundhoghunter.abstract_threads.ClientConnectToThread;
import com.smile.groundhoghunter.abstract_threads.IoFunctionThread;
import com.smile.groundhoghunter.adapters.TwoPlayerListAdapter;
import com.smile.groundhoghunter.constants.Constants;
import com.smile.groundhoghunter.interfaces.ConnectDevice;
import com.smile.groundhoghunter.threads.ClientDiscoveryTimerThread;
import com.smile.groundhoghunter.utilities.ConnectDeviceUtil;
import com.smile.groundhoghunter.utilities.MessageShowingUtil;
import com.smile.smilelibraries.customized_button.SmileImageButton;
import com.smile.smilelibraries.utilities.FontAndBitmapUtil;
import com.smile.smilelibraries.utilities.ScreenUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class JoinGameActivity extends AppCompatActivity {

    // private properties
    private static final String TAG = "JoinGameAct";
    // 20 seconds one time
    private static final int DurationForBluetoothDiscovery = 20000;
    private String oppositePlayerName;
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
        playerName = callingIntent.getStringExtra(Constants.PLAYER_NAME);
        if (playerName == null) {
            playerName = "";
        }

        joinGameHandler = new JoinGameHandler(Looper.getMainLooper());
        discoveredDeviceMap = new HashMap<>();
        ioFunctionThreadMap = new HashMap<>();
        selectedIoFunctionThread = null;

        oppositePlayerNameMap = new LinkedHashMap<>();

        float textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this);
        float toastTextSize = textFontSize * 0.8f;

        cannotCreateClientSocketString = getString(R.string.cannotCreateClientSocketString);
        connectToHostSucceededString = getString(R.string.connectToHostSucceededString);
        connectToHostFailedString = getString(R.string.connectToHostFailedString);
        hostLeftGameString = getString(R.string.hostLeftGameString);
        discoveryTimeHasReachedString = getString(R.string.discoveryTimeHasReachedString);
        discoveryWasDismissedString = getString(R.string.discoveryWasDismissedString);
        hasBeenReadString = getString(R.string.hasBeenReadString);

        int colorDarkRed = ContextCompat.getColor(GroundhogHunterApp.AppContext, R.color.darkRed);
        int colorBlue = Color.BLUE;

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_join_game);

        // message showing view
        TextView toastMessageTextView = findViewById(R.id.toastMessageTextView);
        ScreenUtil.resizeTextSize(toastMessageTextView, toastTextSize);
        showMessage = new MessageShowingUtil(this, toastMessageTextView);

        TextView joinGameTitleTextView = findViewById(R.id.joinGameTitleTextView);
        joinGameTitleTextView.setText(getString(R.string.joinBluetoothGameString));
        ScreenUtil.resizeTextSize(joinGameTitleTextView, textFontSize * 1.2f);

        TextView playerNameStringTextView = findViewById(R.id.playerNameStringTextView);
        ScreenUtil.resizeTextSize(playerNameStringTextView, textFontSize);

        TextView playerNameTextView = findViewById(R.id.playerNameTextView);
        playerNameTextView.setText(playerName);
        ScreenUtil.resizeTextSize(playerNameTextView, textFontSize);

        ListView oppositePlayerNameListView = findViewById(R.id.oppositePlayerNameListView);
        ArrayList<String> oppNameList = new ArrayList<>();
        twoPlayerListAdapter = new TwoPlayerListAdapter(this,
                R.layout.player_list_item_layout, R.id.playerNameTextView, oppNameList, textFontSize);
        // do not call notifyDataSetChanged() method automatically
        twoPlayerListAdapter.setNotifyOnChange(false);
        oppositePlayerNameListView.setAdapter(twoPlayerListAdapter);
        oppositePlayerNameListView.setOnItemClickListener((adapterView, view, position, rowId) -> {
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
                            if (oppName != null) {
                                if (oppName.equals(oppositePlayerName)) {
                                    selectedIoFunctionThread = ioFunctionThread;
                                    selectedIoFunctionThread.write(Constants.OPPOS_PLAYER_NAME_READ, playerName);
                                    view.setSelected(true);
                                } else {
                                    ioFunctionThread.write(Constants.TWO_PLAY_CLIENT_EX_CODE, remoteMacAddress);
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
        refreshJoinGameButton.setOnClickListener(view -> {
            startDiscovery();
            Log.d(TAG, "Refresh --> startDiscovery()");
        });

        SmileImageButton cancelJoinGameButton = findViewById(R.id.cancelJoinGameButton);
        Bitmap cancelJoinGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, getString(R.string.cancelString), colorDarkRed);
        cancelJoinGameButton.setImageBitmap(cancelJoinGameBitmap);
        cancelJoinGameButton.setOnClickListener(view -> returnToPrevious());

        startDiscovery();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "JoinGameActivity --> Came back from BtJoinGameActivity.");
        if (requestCode == Constants.TWO_PLAY_GAME_BY_CLIENT) {
            oppositePlayerName = "";
            discoveredDeviceMap = new HashMap<>();
            ioFunctionThreadMap = new HashMap<>();
            selectedIoFunctionThread = null;
            oppositePlayerNameMap = new LinkedHashMap<>();
            // update list view
            ArrayList<String> oppNameList = new ArrayList<>(oppositePlayerNameMap.values());
            twoPlayerListAdapter.updateData(oppNameList);
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
            for (IoFunctionThread btFunctionThread : ioFunctionThreadMap.values()) {
                btFunctionThread.write(Constants.TWO_PLAY_CLIENT_EX_CODE, "");
            }
        }
    }

    private void startClientDiscoveryTimerThread() {
        discoveryTimerThread = new ClientDiscoveryTimerThread(joinGameHandler,
                DurationForBluetoothDiscovery);
        discoveryTimerThread.start();
    }

    private void stopClientDiscoveryTimerThread() {
        if (discoveryTimerThread != null) {
            discoveryTimerThread.dismissTimerThread();
            boolean retry = true;
            while (retry) {
                try {
                    discoveryTimerThread.join();
                    Log.d(TAG, "stopClientDiscoveryTimerThread.discoveryTimerThread.Join()");
                    retry = false;
                    discoveryTimerThread = null;
                } catch (InterruptedException ex) {
                    Log.e(TAG, "stopClientDiscoveryTimerThread.Exception: ", ex);
                }
            }
        }
    }

    private void stopClientConnectToThreadAndClearClientDiscoveredMap() {
        for(ClientConnectToThread connectToThread : discoveredDeviceMap.values()) {
            stopClientConnectToThread(connectToThread, true);
        }
        discoveredDeviceMap.clear(); // clear HashSet because of starting discovering
    }

    private void stopClientConnectToThread(ClientConnectToThread connectToThread,
                                           boolean isCloseClientSocket) {
        if (connectToThread != null) {
            if (isCloseClientSocket) {
                connectToThread.closeClientSocket();
            }
            boolean retry = true;
            while (retry) {
                try {
                    connectToThread.join();
                    Log.d(TAG, "stopClientConnectToThread.connectToThread.Join()");
                    retry = false;
                    connectToThread = null;
                } catch (InterruptedException ex) {
                    Log.e(TAG, "stopClientConnectToThread.Exception: ", ex);
                }
            }
        }
    }

    private class JoinGameHandler extends Handler {
        public JoinGameHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            // super.handleMessage(msg);
            String megString;
            String deviceName;
            ClientConnectToThread connectToThread;
            IoFunctionThread ioFunctionThread;
            Log.d(TAG, "handleMessage.Message = " + msg.what);
            Bundle data = msg.getData();
            ConnectDevice connectDevice;
            String remoteMacAddress;

            switch (msg.what) {
                case Constants.CL_DISCOVER_TIMER_END:
                    Log.d(TAG, "handleMessage.ClientDiscoveryTimerHasReached");
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
                case Constants.CL_DISCOVER_TIMER_DISMISSED:
                    Log.d(TAG, "handleMessage.ClientDiscoveryTimerHasBeenDismissed");
                    megString = discoveryWasDismissedString;
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView(megString, MessageDuration);
                    break;
                case Constants.CL_CONN_TO_TH_NO_CL_SOCKET:
                    Log.d(TAG, "handleMessage.ClientConnectToThreadNoClientSocket");
                    connectDevice = data.getParcelable("ConnectDevice");
                    if (connectDevice == null) break;
                    remoteMacAddress = connectDevice.getAddress();
                    deviceName = ConnectDeviceUtil.getConnectDeviceName(connectDevice);
                    megString = cannotCreateClientSocketString + "(" + deviceName +")";
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView(megString, MessageDuration);
                    connectToThread = discoveredDeviceMap.get(remoteMacAddress);
                    stopClientConnectToThread(connectToThread,true);
                    break;
                case Constants.CL_CONN_TO_TH_CONNECTED:
                    Log.d(TAG, "handleMessage.ClientConnectToThreadConnected");
                    connectDevice = data.getParcelable("ConnectDevice");
                    if (connectDevice == null) break;
                    remoteMacAddress = connectDevice.getAddress();
                    deviceName = ConnectDeviceUtil.getConnectDeviceName(connectDevice);
                    megString = connectToHostSucceededString + "(" + deviceName + ")";
                    Log.d(TAG, megString);
                    // start reading data from the other device and writing data to the other device
                    connectToThread = discoveredDeviceMap.get(remoteMacAddress);
                    if (connectToThread == null) break;
                    ioFunctionThread = connectToThread.getIoFunctionThread();
                    ioFunctionThread.setStartRead(true);    // start reading data
                    if (!ioFunctionThreadMap.containsKey(remoteMacAddress)) {
                        ioFunctionThreadMap.put(remoteMacAddress, ioFunctionThread);
                    }
                    stopClientConnectToThread(connectToThread, false);
                    break;
                case Constants.OPPOS_PLAYER_NAME_READ:
                    Log.d(TAG, "handleMessage.OppositePlayerNameHasBeenRead");
                    connectDevice = data.getParcelable("ConnectDevice");
                    if (connectDevice == null) break;
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
                    if (ioFunctionThread == null) break;
                    ioFunctionThread.setStartRead(true);    // read next data data
                    break;
                case Constants.CL_CONN_TO_TH_FAILED_CONNECT:
                    Log.d(TAG, "handleMessage.ClientConnectToThreadFailedToConnect");
                    connectDevice = data.getParcelable("ConnectDevice");
                    if (connectDevice == null) break;
                    remoteMacAddress = connectDevice.getAddress();
                    deviceName = ConnectDeviceUtil.getConnectDeviceName(connectDevice);
                    megString = connectToHostFailedString + "(" + deviceName + ")";
                    Log.d(TAG, megString);
                    if (discoveredDeviceMap != null) {
                        connectToThread = discoveredDeviceMap.get(remoteMacAddress);
                        stopClientConnectToThread(connectToThread, true);
                    }
                    break;
                case Constants.TWO_PLAY_HOST_EX_CODE:
                    Log.d(TAG, "handleMessage.TwoPlayerHostExitCode");
                    connectDevice = data.getParcelable("ConnectDevice");
                    if (connectDevice == null) break;
                    remoteMacAddress = connectDevice.getAddress();
                    showMessage.showMessageInTextView(hostLeftGameString, MessageDuration);
                    ioFunctionThread = ioFunctionThreadMap.get(remoteMacAddress);
                    if (ioFunctionThread == null) break;
                    ioFunctionThread.setStartRead(true);    // start reading data
                    // remove the remote connected device from oppositePlayerNameList
                    oppositePlayerNameMap.remove(remoteMacAddress);
                    // update list view
                    ArrayList<String> oppNameList = new ArrayList<>(oppositePlayerNameMap.values());
                    twoPlayerListAdapter.updateData(oppNameList);
                    break;
                case Constants.TWO_PLAY_HOST_ST_GAME:
                    Log.d(TAG, "handleMessage.TwoPlayerHostStartGame");
                    if (selectedIoFunctionThread != null) {
                        GroundhogHunterApp.selectedIoFuncThread = selectedIoFunctionThread;
                        for (String remoteMac : ioFunctionThreadMap.keySet()) {
                            IoFunctionThread ioFuncThread = ioFunctionThreadMap.get(remoteMac);
                            connectToThread = discoveredDeviceMap.get(remoteMac);
                            if (ioFuncThread != null) {
                                if (ioFuncThread != selectedIoFunctionThread) {
                                    ioFuncThread.write(Constants.TWO_PLAY_CLIENT_EX_CODE, "");
                                    ConnectDeviceUtil.stopIoFunctionThread(ioFuncThread);
                                    stopClientConnectToThread(connectToThread, true);
                                } else {
                                    stopClientConnectToThread(connectToThread, false);
                                }
                            }
                        }

                        // clear HashMaps
                        discoveredDeviceMap = null;
                        ioFunctionThreadMap.clear();
                        ioFunctionThreadMap = null;
                        oppositePlayerNameMap.clear();
                        oppositePlayerNameMap = null;
                        // remove all message from joinGameHandler, // added on 2019-05-14
                        joinGameHandler.removeCallbacksAndMessages(null);
                        // start game by different medias
                        startClientGame();
                    }
                    break;
                case Constants.TWO_PLAY_DEF_READ:
                    Log.d(TAG, "handleMessage.TwoPlayerDefaultReading");
                    // read the next data
                    connectDevice = data.getParcelable("ConnectDevice");
                    if (connectDevice != null) {
                        remoteMacAddress = connectDevice.getAddress();
                        ioFunctionThread = ioFunctionThreadMap.get(remoteMacAddress);
                        if (ioFunctionThread != null) {
                            ioFunctionThread.setStartRead(true);    // start reading data
                        }
                    }
                    break;
            }
        }
    }
}
