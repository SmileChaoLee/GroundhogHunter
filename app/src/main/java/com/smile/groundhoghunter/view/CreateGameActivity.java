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
import com.smile.groundhoghunter.abstract_threads.IoFunctionThread;
import com.smile.groundhoghunter.abstract_threads.ServerAcceptThread;
import com.smile.groundhoghunter.adapters.TwoPlayerListAdapter;
import com.smile.groundhoghunter.constants.Constants;
import com.smile.groundhoghunter.interfaces.ConnectDevice;
import com.smile.groundhoghunter.utilities.ConnectDeviceUtil;
import com.smile.groundhoghunter.utilities.MessageShowingUtil;
import com.smile.smilelibraries.customized_button.SmileImageButton;
import com.smile.smilelibraries.utilities.FontAndBitmapUtil;
import com.smile.smilelibraries.utilities.ScreenUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class CreateGameActivity extends AppCompatActivity
        implements TwoPlayerListAdapter.OnItemClickListener {

    private static final String TAG = "CreateGameAct";
    protected static final int MSG_DURATION = 1000;    // 1 seconds
    protected TextView createGameTitleTextView;
    private String oppositePlayerName;
    private LinkedHashMap<String, String> oppositePlayerNameMap;
    private String playerNameCannotBeEmptyString;
    private String cannotCreateServerSocketString;
    private String waitingStoppedCancelledString;
    private String serverAcceptedConnectionString;
    private String clientLeftGameString;
    private String noOppositePlayerString;
    private TwoPlayerListAdapter twoPlayerListAdapter;
    protected String playerName;
    protected MessageShowingUtil showMessage;
    protected Handler createGameHandler;
    protected HashMap<String, IoFunctionThread> ioFunctionThreadMap;
    protected ServerAcceptThread mServerAcceptThread;
    protected IoFunctionThread mIoFuncThread;
    protected ActivityResultLauncher<Intent> hostGameLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent callingIntent = getIntent();
        playerName = callingIntent.getStringExtra(Constants.PLAYER_NAME);
        if (playerName == null) {
            playerName = "";
        }

        createGameHandler = new CreateGameHandler(Looper.getMainLooper());
        ioFunctionThreadMap = new HashMap<>();
        mServerAcceptThread = null;
        mIoFuncThread = null;

        oppositePlayerName = "";    // empty
        oppositePlayerNameMap = new LinkedHashMap<>();

        float textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this);
        float toastTextSize = textFontSize * 0.8f;

        playerNameCannotBeEmptyString = getString(R.string.playerNameCannotBeEmptyString);
        waitingStoppedCancelledString = getString(R.string.waitingStoppedCancelledString);
        serverAcceptedConnectionString = getString(R.string.serverAcceptedConnectionString);
        cannotCreateServerSocketString = getString(R.string.cannotCreateServerSocketString);
        clientLeftGameString = getString(R.string.clientLeftGameString);
        noOppositePlayerString = getString(R.string.noOppositePlayerString);

        int colorDarkRed = ContextCompat.getColor(this, R.color.darkRed);
        int colorDarkGreen = ContextCompat.getColor(this, R.color.darkGreen);
        int colorBlue = Color.BLUE;

        hostGameLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Log.d(TAG, "hostGameLauncher.resultCode = " + resultCode);
                    Log.d(TAG, "hostGameLauncher.Came back from BtHostGameActivity.");
                    startDiscoverability();
                });

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_game);

        // message showing view
        TextView toastMessageTextView = findViewById(R.id.toastMessageTextView);
        ScreenUtil.resizeTextSize(toastMessageTextView, toastTextSize);
        showMessage = new MessageShowingUtil(this, toastMessageTextView);

        createGameTitleTextView = findViewById(R.id.createGameTitleTextView);
        createGameTitleTextView.setText(getString(R.string.createBluetoothGameString));
        ScreenUtil.resizeTextSize(createGameTitleTextView, textFontSize * 1.2f);

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

        SmileImageButton refreshCreateGameButton = findViewById(R.id.refreshCreateGameButton);
        Bitmap refreshCreateGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, getString(R.string.refreshString), colorBlue);
        refreshCreateGameButton.setImageBitmap(refreshCreateGameBitmap);
        refreshCreateGameButton.setOnClickListener(view -> {
            Log.d(TAG, "refreshCreateGameButton.startDiscoverability()");
            startDiscoverability();
        });

        SmileImageButton startCreateGameButton = findViewById(R.id.startCreateGameButton);
        Bitmap startCreateGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, getString(R.string.startString), colorDarkGreen);
        startCreateGameButton.setImageBitmap(startCreateGameBitmap);
        startCreateGameButton.setOnClickListener(view -> {
            Log.d(TAG, "startCreateGameButton");
            if (playerName.isEmpty()) {
                showMessage.showMessageInTextView(playerNameCannotBeEmptyString, MSG_DURATION);
                return;
            }
            if (oppositePlayerName.isEmpty()) {
                showMessage.showMessageInTextView(noOppositePlayerString, MSG_DURATION);
                return;
            }
            // Notify client to start game
            Log.d(TAG, "startCreateGameButton.mIoFuncThread = " + mIoFuncThread);
            if (mIoFuncThread != null) {
                GHogHunterApp.selectedIoFuncThread = mIoFuncThread;
                stopServerAcceptThread();
                for (String remoteMacAddress : ioFunctionThreadMap.keySet()) {
                    IoFunctionThread ioFunctionThread = ioFunctionThreadMap.get(remoteMacAddress);
                    if (ioFunctionThread != null && ioFunctionThread != mIoFuncThread) {
                        ioFunctionThread.write(Constants.TWO_PLAY_HOST_EX_CODE, "");
                        ConnectDeviceUtil.stopIoFunctionThread(ioFunctionThread);
                    }
                }
                // clear HashMaps
                ioFunctionThreadMap.clear();
                oppositePlayerNameMap.clear();
                createGameHandler.removeCallbacksAndMessages(null);
                Log.d(TAG, "startCreateGameButton.mIoFuncThread.write(TWO_PLAY_HOST_ST_GAME)");
                mIoFuncThread.write(Constants.TWO_PLAY_HOST_ST_GAME, "");
                startHostGame();
            }
        });

        SmileImageButton cancelCreateGameButton = findViewById(R.id.cancelCreateGameButton);
        Bitmap cancelCreateGameGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, getString(R.string.cancelString), colorDarkRed);
        cancelCreateGameButton.setImageBitmap(cancelCreateGameGameBitmap);
        cancelCreateGameButton.setOnClickListener(view -> returnToPrevious());

        startDiscoverability();
    }

    @Override
    public void onItemClick(int position, @NotNull String key, @NotNull String value) {
        Log.d(TAG, "onItemClick.position = " + position);
        Log.d(TAG, "onItemClick.key = " + key + ", value = " + value);
        if (ioFunctionThreadMap == null) return;
        oppositePlayerName = value;
        mIoFuncThread = ioFunctionThreadMap.get(key);
        Log.d(TAG, "onItemClick.mIoFuncThread = " + mIoFuncThread);
        twoPlayerListAdapter.myNotifyItemChanged(position);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hostLeavingNotification();
        if (oppositePlayerNameMap != null) {
            oppositePlayerNameMap.clear();
        }
        stopServerAcceptThread();
        if (ioFunctionThreadMap != null) {
            ArrayList<IoFunctionThread> threadList = new ArrayList<>(ioFunctionThreadMap.values());
            ConnectDeviceUtil.stopIoFunctionThreads(threadList);
            ioFunctionThreadMap.clear();
        }
        if (twoPlayerListAdapter != null) {
            twoPlayerListAdapter.clear();
        }
        if (createGameHandler != null) {
            createGameHandler.removeCallbacksAndMessages(null);
        }
    }

    protected void returnToPrevious() {
        // if it Still is connecting to other device
        // then notify the other device leaving
        finish();
    }

    protected void startDiscoverability() {
        hostLeavingNotification();
        stopServerAcceptThread();
        if (ioFunctionThreadMap != null) {
            ArrayList<IoFunctionThread> threadList = new ArrayList<>(ioFunctionThreadMap.values());
            ConnectDeviceUtil.stopIoFunctionThreads(threadList);
            ioFunctionThreadMap.clear();
        }
        if (createGameHandler != null) {
            createGameHandler.removeCallbacksAndMessages(null); // added on 2019-05-14
        }
        if (oppositePlayerNameMap != null) {
            oppositePlayerNameMap.clear();
        }
        oppositePlayerName = "";
        mIoFuncThread = null;
        if (twoPlayerListAdapter != null) {
            twoPlayerListAdapter.clear();
        }
    }

    protected void startHostGame() {
        Log.d(TAG, "startHostGame.do nothing");
    }

    private void hostLeavingNotification() {
        Log.d(TAG, "hostLeavingNotification.ioFunctionThreadMap = " + ioFunctionThreadMap);
        if (ioFunctionThreadMap == null) return;
        for (IoFunctionThread ioFunctionThread : ioFunctionThreadMap.values()) {
            ioFunctionThread.write(Constants.TWO_PLAY_HOST_EX_CODE, "");
        }
    }

    private void stopServerAcceptThread() {
        Log.d(TAG, "stopServerAcceptThread");
        if (mServerAcceptThread != null) {
            mServerAcceptThread.setKeepRunning(false);
            mServerAcceptThread.closeServerSocket();
            boolean retry = true;
            while (retry) {
                try {
                    Log.d(TAG, "stopServerAcceptThread.mServerAcceptThread.Join()");
                    mServerAcceptThread.join();
                    retry = false;
                    mServerAcceptThread = null;
                } catch (InterruptedException ex) {
                    Log.e(TAG,"stopServerAcceptThread.exception: ", ex);
                }
            }
        }
    }

    private class CreateGameHandler extends Handler {

        public CreateGameHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            // super.handleMessage(msg);
            Bundle data = msg.getData();
            Log.d(TAG, "handleMessage.Message = " + msg.what);
            String megString;
            String deviceName;
            String remoteMacAddress;
            IoFunctionThread ioFunctionThread;
            ConnectDevice connectDevice;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                connectDevice = data.getParcelable("ConnectDevice", ConnectDevice.class);
            } else {
                connectDevice = data.getParcelable("ConnectDevice");
            }
            switch (msg.what) {
                case Constants.OPPOS_PLAYER_NAME_READ:
                    Log.d(TAG, "handleMessage.OPPOS_PLAYER_NAME_READ");
                    megString = "has been read.";
                    String oppositeName = data.getString("OppositePlayerName");
                    megString = oppositeName + " " + megString;
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView(megString, MSG_DURATION);
                    remoteMacAddress = "";
                    if (connectDevice != null) {
                        remoteMacAddress = connectDevice.getAddress();
                        if (oppositeName != null) {
                            if (!oppositeName.isEmpty()) {
                                if (!oppositePlayerNameMap.containsKey(remoteMacAddress)) {
                                    oppositePlayerNameMap.put(remoteMacAddress, oppositeName);
                                    twoPlayerListAdapter.addItem(remoteMacAddress, oppositeName);
                                }
                            }
                        }
                    } else {
                        Log.d(TAG, "connectDevice is  null.");
                    }
                    // read the next data
                    ioFunctionThread = ioFunctionThreadMap.get(remoteMacAddress);
                    if (ioFunctionThread != null) {
                        ioFunctionThread.setStartRead(true);    // start reading data
                    }
                    break;
                case Constants.SER_ACCEPT_TH_NO_SER_SOCKET:
                    Log.d(TAG, "handleMessage.SER_ACCEPT_TH_NO_SER_SOCKET");
                    showMessage.showMessageInTextView(cannotCreateServerSocketString, MSG_DURATION);
                    break;
                case Constants.SER_ACCEPT_TH_CONNECTED:
                    Log.d(TAG, "handleMessage.SER_ACCEPT_TH_CONNECTED");
                    if (connectDevice == null) break;
                    deviceName = ConnectDeviceUtil.getConnectDeviceName(connectDevice);
                    remoteMacAddress = connectDevice.getAddress();
                    megString = serverAcceptedConnectionString + "(" + deviceName+ ")";
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView(serverAcceptedConnectionString, MSG_DURATION);
                    // start reading data from the other device and writing data to the other device
                    ioFunctionThread = mServerAcceptThread.getIoFunctionThread(connectDevice);
                    ioFunctionThread.setStartRead(true);    // start reading data
                    if (!ioFunctionThreadMap.containsKey(remoteMacAddress)) {
                        ioFunctionThreadMap.put(remoteMacAddress, ioFunctionThread);
                    }
                    break;
                case Constants.SER_ACCEPT_TH_STOPPED:
                    Log.d(TAG, "handleMessage.SER_ACCEPT_TH_STOPPED");
                    showMessage.showMessageInTextView(waitingStoppedCancelledString, MSG_DURATION);
                    break;
                case Constants.TWO_PLAY_CLIENT_EX_CODE:
                    Log.d(TAG, "handleMessage.TWO_PLAY_CLIENT_EX_CODE");
                    if (connectDevice == null) break;
                    remoteMacAddress = connectDevice.getAddress();
                    ioFunctionThread = ioFunctionThreadMap.get(remoteMacAddress);
                    if (ioFunctionThread == null) break;
                    ioFunctionThread.setStartRead(true);    // start reading data
                    // remove the remote connected device from oppositePlayerNameList
                    if (oppositePlayerNameMap.containsKey(remoteMacAddress)) {
                        showMessage.showMessageInTextView(clientLeftGameString, MSG_DURATION);
                        // added on 2019-06-08 to fix bugs
                        String removedOppName = oppositePlayerNameMap.get(remoteMacAddress);
                        if (removedOppName != null){
                            if (removedOppName.equals(oppositePlayerName)) {
                                // selected client has been removed, then changed to no selection
                                oppositePlayerName = "";
                                mIoFuncThread = null;
                            }
                            oppositePlayerNameMap.remove(remoteMacAddress);
                        }
                        if (mServerAcceptThread != null) {
                            mServerAcceptThread.decrementConnections();
                        }
                    }
                    // update list view
                    twoPlayerListAdapter.removeItem(remoteMacAddress);

                    break;
                case Constants.TWO_PLAY_DEF_READ:
                    Log.d(TAG, "handleMessage.TWO_PLAY_DEF_READ");
                    if (connectDevice != null) {
                        remoteMacAddress = connectDevice.getAddress();
                        // read the next data
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
