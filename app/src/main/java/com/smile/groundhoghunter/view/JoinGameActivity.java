package com.smile.groundhoghunter.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.util.Log;
import android.widget.TextView;

import com.smile.groundhoghunter.GHogHunterApp;
import com.smile.groundhoghunter.R;
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

import java.util.HashMap;
import java.util.LinkedHashMap;

abstract public class JoinGameActivity extends AppCompatActivity
        implements TwoPlayerListAdapter.OnItemClickListener {

    // private properties
    private static final String TAG = "JoinGameAct";
    // 20 seconds one time
    private static final int DURATION_BT_DISCOVER = 20000;
    protected static final int TEMP_MSG_DURATION = 1000;    // 1 second
    protected static final int CONNECTING_MSG_DURATION = 30000;    // 30 seconds
    protected boolean isDiscoveryFinished = false;
    protected boolean isConnectingFinished = true;
    protected TwoPlayerListAdapter twoPlayerListAdapter;
    protected TextView joinGameTitleTextView;
    // (Mac address, device name)
    protected LinkedHashMap<String, String> oppositePlayerNameMap = new LinkedHashMap<>();
    protected String playerName;
    protected MessageShowingUtil showMessage;
    protected Handler joinGameHandler;
    protected ConnectDevice clientConnectDevice;
    protected ClientDiscoveryTimerThread discoveryTimerThread;
    protected HashMap<String, ConnectDevice> discoveredDeviceMap = new HashMap<>();
    protected ClientConnectToThread mClConnToThread = null;
    protected IoFunctionThread mIoFuncThread = null;
    protected String mConnectedMacAddress = "";
    protected ActivityResultLauncher<Intent> clientGameLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent callingIntent = getIntent();
        playerName = callingIntent.getStringExtra(Constants.PLAYER_NAME);
        if (playerName == null) {
            playerName = "";
        }

        joinGameHandler = new JoinGameHandler(Looper.getMainLooper());
        mIoFuncThread = null;
        mConnectedMacAddress = "";

        float textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this);
        float toastTextSize = textFontSize * 0.8f;

        int colorDarkRed = ContextCompat.getColor(this, R.color.darkRed);
        int colorBlue = Color.BLUE;

        clientGameLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Log.d(TAG, "clientGameLauncher.resultCode = " + resultCode);
                    Log.d(TAG, "clientGameLauncher.Came back from BtClientGameActivity.");
                    startDiscovery();
                });

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_join_game);

        // message showing view
        TextView toastMessageTextView = findViewById(R.id.toastMessageTextView);
        ScreenUtil.resizeTextSize(toastMessageTextView, toastTextSize);
        showMessage = new MessageShowingUtil(this, toastMessageTextView);

        joinGameTitleTextView = findViewById(R.id.joinGameTitleTextView);
        joinGameTitleTextView.setText(getString(R.string.joinBluetoothGameString));
        ScreenUtil.resizeTextSize(joinGameTitleTextView, textFontSize * 1.2f);

        TextView playerNameStringTextView = findViewById(R.id.playerNameStringTextView);
        ScreenUtil.resizeTextSize(playerNameStringTextView, textFontSize);

        TextView playerNameTextView = findViewById(R.id.playerNameTextView);
        playerNameTextView.setText(playerName);
        ScreenUtil.resizeTextSize(playerNameTextView, textFontSize);

        RecyclerView oppositePlayerNameListView = findViewById(R.id.oppositePlayerNameListView);
        twoPlayerListAdapter = new TwoPlayerListAdapter(oppositePlayerNameMap, textFontSize, this);
        SimpleItemAnimator animator = (SimpleItemAnimator)(oppositePlayerNameListView.getItemAnimator());
        if (animator != null) {
            animator.setSupportsChangeAnimations(false);
        }
        oppositePlayerNameListView.setLayoutManager(new LinearLayoutManager(this));
        oppositePlayerNameListView.setAdapter(twoPlayerListAdapter);

        SmileImageButton refreshJoinGameButton = findViewById(R.id.refreshJoinGameButton);
        Bitmap refreshJoinGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, getString(R.string.refreshString), colorBlue);
        refreshJoinGameButton.setImageBitmap(refreshJoinGameBitmap);
        refreshJoinGameButton.setOnClickListener(view -> {
            Log.d(TAG, "Refresh.startDiscovery()");
            startDiscovery();
        });

        SmileImageButton cancelJoinGameButton = findViewById(R.id.cancelJoinGameButton);
        Bitmap cancelJoinGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, getString(R.string.cancelString), colorDarkRed);
        cancelJoinGameButton.setImageBitmap(cancelJoinGameBitmap);
        cancelJoinGameButton.setOnClickListener(view -> returnToPrevious());

        startDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        clientLeavingNotification();
        ConnectDeviceUtil.stopIoFunctionThread(mIoFuncThread);
        stopClientConnectToThreadAndClearClientDiscoveredMap();
        oppositePlayerNameMap.clear();
        twoPlayerListAdapter.clear();
        stopClientDiscoveryTimerThread();
        if (joinGameHandler != null) {
            joinGameHandler.removeCallbacksAndMessages(null);
        }
    }

    protected void returnToPrevious() {

        // if it Still is connecting to other device
        // then notify the other device leaving

        finish();
    }

    protected void startDiscovery() {
        isDiscoveryFinished = false;
        stopClientDiscoveryTimerThread();  // stop discovering devices (servers)
        clientLeavingNotification();
        ConnectDeviceUtil.stopIoFunctionThread(mIoFuncThread);
        stopClientConnectToThreadAndClearClientDiscoveredMap();
        joinGameHandler.removeCallbacksAndMessages(null);   // added on 2019-05-14
        oppositePlayerNameMap.clear();
        twoPlayerListAdapter.clear();
        startClientDiscoveryTimerThread();  // start discovering devices (servers)
        // showMessage.showMessageInTextView(getString(R.string.discoverPlayerString), MSG_DURATION);
        showMessage.showMessageInTextView(getString(R.string.discoverPlayerString), DURATION_BT_DISCOVER);
    }

    protected void startClientGame() {
        Log.d(TAG, "startClientGame.do nothing");
    }

    // Called when the host's player name arrives via OPPOS_PLAYER_NAME_READ.
    // BT subclass: no-op (items are added via ACTION_FOUND BroadcastReceiver).
    // WiFi subclass: overrides this to add the host to the RecyclerView because
    //   direct-connect bypasses P2P discovery so PEERS_CHANGED never fires.
    protected void onOppositePlayerNameRead(String deviceAddress, String playerName) {
        // default no-op
    }

    private void clientLeavingNotification() {
        Log.d(TAG, "clientLeavingNotification.selectedIoFuncTh = " + mIoFuncThread);
        if (mIoFuncThread != null) {
            mIoFuncThread.write(Constants.TWO_PLAY_CLIENT_EX_CODE, "");
        }
        mConnectedMacAddress = "";
    }

    private void startClientDiscoveryTimerThread() {
        discoveryTimerThread = new ClientDiscoveryTimerThread(joinGameHandler,
                DURATION_BT_DISCOVER);
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
        if (mClConnToThread != null) {
            stopClientConnectToThread(mClConnToThread, true);
            mClConnToThread = null;
        }
        // clear HashSet because of starting discovering
        discoveredDeviceMap.clear();
    }

    protected void stopClientConnectToThread(ClientConnectToThread connectToThread,
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
            String logStr = "JoinGameHandler";
            // super.handleMessage(msg);
            Log.d(TAG, logStr + ".Message = " + msg.what);
            Bundle data = msg.getData();
            String megString;
            String deviceName = "";
            ConnectDevice connectDevice;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                connectDevice = data.getParcelable("ConnectDevice", ConnectDevice.class);
            } else {
                connectDevice = data.getParcelable("ConnectDevice");
            }
            if (connectDevice != null) {
                deviceName = ConnectDeviceUtil.getConnectDeviceName(connectDevice);
                Log.d(TAG, logStr + ".deviceName = " + deviceName);
            }
            switch (msg.what) {
                case Constants.CL_DISCOVER_TIMER_END:
                    megString = logStr + ".CL_DISCOVER_TIMER_END.deviceName = " + deviceName;
                    Log.d(TAG, megString);
                    isDiscoveryFinished = true;
                    showMessage.showMessageInTextView(getString(R.string.discoveryTimeHasReachedString),
                            TEMP_MSG_DURATION);
                    break;
                case Constants.CL_DISCOVER_TIMER_DISMISSED:
                    megString = logStr + ".CL_DISCOVER_TIMER_DISMISSED.deviceName = " + deviceName;
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView(getString(R.string.discoveryWasDismissedString),
                            TEMP_MSG_DURATION);
                    isDiscoveryFinished = true;
                    break;
                case Constants.CL_CONN_TO_TH_NO_CL_SOCKET:
                    megString = logStr + ".CL_CONN_TO_TH_NO_CL_SOCKET.deviceName = " + deviceName;
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView(getString(R.string.cannotCreateClientSocketString),
                            TEMP_MSG_DURATION);
                    if (mClConnToThread != null) {
                        stopClientConnectToThread(mClConnToThread, true);
                        mClConnToThread = null;
                    }
                    mIoFuncThread = null;
                    mConnectedMacAddress = "";
                    GHogHunterApp.selectedIoFuncThread = null;
                    isConnectingFinished = true;
                    break;
                case Constants.CL_CONN_TO_TH_CONNECTED:
                    megString = logStr + ".CL_CONN_TO_TH_CONNECTED.deviceName = " + deviceName;
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView(getString(R.string.connectToHostSucceededString),
                            TEMP_MSG_DURATION);
                    if (mClConnToThread != null) {
                        mIoFuncThread = mClConnToThread.getIoFunctionThread();
                        Log.d(TAG, logStr + "CL_CONN_TO_TH_CONNECTED.mIoFuncThread = " + mIoFuncThread);
                        if (mIoFuncThread != null) {
                            mIoFuncThread.setStartRead(true);    // start reading data
                            mIoFuncThread.write(Constants.OPPOS_PLAYER_NAME_READ, playerName);
                        }
                        stopClientConnectToThread(mClConnToThread, false);
                        mClConnToThread = null;
                    }
                    isConnectingFinished = true;
                    break;
                case Constants.OPPOS_PLAYER_NAME_READ:
                    megString = logStr + ".OPPOS_PLAYER_NAME_READ.deviceName = " + deviceName;
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView(getString(R.string.hasBeenReadString),
                            TEMP_MSG_DURATION);
                    // Give subclasses a chance to populate the RecyclerView.
                    // BT adds items via BroadcastReceiver (ACTION_FOUND) so its override is a no-op.
                    // WiFi with direct-connect never fires PEERS_CHANGED, so it adds the host here.
                    String oppositeName = data.getString("OppositePlayerName");
                    onOppositePlayerNameRead(deviceName, oppositeName);
                    // Re-enable reading so the IoFunctionThread can receive the next signal
                    // (e.g., TWO_PLAY_HOST_ST_GAME)
                    if (mIoFuncThread != null) {
                        mIoFuncThread.setStartRead(true);   // read next data data
                    }
                    break;
                case Constants.CL_CONN_TO_TH_FAILED_CONNECT:
                    megString = logStr + ".CL_CONN_TO_TH_FAILED_CONNECT.deviceName = " + deviceName;
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView(getString(R.string.connectToHostFailedString),
                            TEMP_MSG_DURATION);
                    if (mClConnToThread != null) {
                        stopClientConnectToThread(mClConnToThread, true);
                        mClConnToThread = null;
                    }
                    mIoFuncThread = null;
                    mConnectedMacAddress = "";
                    GHogHunterApp.selectedIoFuncThread = null;
                    isConnectingFinished = true;
                    break;
                case Constants.TWO_PLAY_HOST_EX_CODE:
                    megString = logStr + ".TWO_PLAY_HOST_EX_CODE.deviceName = " + deviceName;
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView(getString(R.string.hostLeftGameString),
                            TEMP_MSG_DURATION);
                    if (mClConnToThread != null) {
                        stopClientConnectToThread(mClConnToThread, true);
                        mClConnToThread = null;
                    }
                    if (mIoFuncThread != null) {
                        mIoFuncThread.setStartRead(true);    // start reading data
                    }
                    // mIoFuncThread = null;
                    mConnectedMacAddress = "";
                    GHogHunterApp.selectedIoFuncThread = null;
                    isConnectingFinished = true;
                    break;
                case Constants.TWO_PLAY_HOST_ST_GAME:
                    megString = logStr + ".TWO_PLAY_HOST_ST_GAME.deviceName = " + deviceName;
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView(getString(R.string.hostStartGameString),
                            TEMP_MSG_DURATION);
                    if (mIoFuncThread != null) {
                        GHogHunterApp.selectedIoFuncThread = mIoFuncThread;
                        // clear HashMaps
                        oppositePlayerNameMap.clear();
                        // remove all message from joinGameHandler, // added on 2019-05-14
                        joinGameHandler.removeCallbacksAndMessages(null);
                        // start game by different medias
                        startClientGame();
                    }
                    break;
                case Constants.TWO_PLAY_DEF_READ:
                    megString = logStr + ".TWO_PLAY_DEF_READ.deviceName = " + deviceName;
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView("READ" + deviceName, TEMP_MSG_DURATION);
                    // read the next data
                    if (mIoFuncThread != null) {
                        mIoFuncThread.setStartRead(true);    // start reading data
                    }
                    break;
            }
        }
    }
}
