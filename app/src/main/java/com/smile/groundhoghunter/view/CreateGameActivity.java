package com.smile.groundhoghunter.view;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class CreateGameActivity extends AppCompatActivity {

    private static final String TAG ="CreateGameAct";
    private String oppositePlayerName;
    private LinkedHashMap<String, String> oppositePlayerNameMap;
    private String playerNameCannotBeEmptyString;
    private String cannotCreateServerSocketString;
    private String waitingStoppedCancelledString;
    private String serverAcceptedConnectionString;
    private String clientLeftGameString;
    private String noOppositePlayerString;
    private TwoPlayerListAdapter twoPlayerListAdapter;

    protected static final int MessageDuration = 1000;    // 1 seconds

    protected String playerName;
    protected MessageShowingUtil showMessage;
    protected Handler createGameHandler;
    protected HashMap<String, IoFunctionThread> ioFunctionThreadMap;
    protected ServerAcceptThread mServerAcceptThread;
    protected IoFunctionThread selectedIoFunctionThread;

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
        selectedIoFunctionThread = null;

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

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_game);

        // message showing view
        TextView toastMessageTextView = findViewById(R.id.toastMessageTextView);
        ScreenUtil.resizeTextSize(toastMessageTextView, toastTextSize);
        showMessage = new MessageShowingUtil(this, toastMessageTextView);

        TextView createGameTitleTextView = findViewById(R.id.createGameTitleTextView);
        createGameTitleTextView.setText(getString(R.string.createBluetoothGameString));
        ScreenUtil.resizeTextSize(createGameTitleTextView, textFontSize * 1.2f);

        TextView playerNameStringTextView = findViewById(R.id.playerNameStringTextView);
        ScreenUtil.resizeTextSize(playerNameStringTextView, textFontSize);

        TextView playerNameTextView = findViewById(R.id.playerNameTextView);
        playerNameTextView.setText(playerName);
        ScreenUtil.resizeTextSize(playerNameTextView, textFontSize);

        ListView oppositePlayerNameListView = findViewById(R.id.oppositePlayerNameListView);
        ArrayList<String> oppNameList = new ArrayList<>();
        twoPlayerListAdapter = new TwoPlayerListAdapter(this, R.layout.player_list_item_layout, R.id.playerNameTextView, oppNameList, textFontSize);
        twoPlayerListAdapter.setNotifyOnChange(false);  // do not call notifyDataSetChanged() method automatically
        oppositePlayerNameListView.setAdapter(twoPlayerListAdapter);
        oppositePlayerNameListView.setOnItemClickListener((adapterView,
                                                           view, position, rowId) -> {
            if (adapterView != null) {
                Object item = adapterView.getItemAtPosition(position);
                if (item != null) {
                    String temp = item.toString();
                    Log.d(TAG, "adapterView.getItemAtPosition(position) = " + temp);
                    oppositePlayerName = temp;
                    for (String remoteMacAddress : oppositePlayerNameMap.keySet()) {
                        IoFunctionThread ioFunctionThread = ioFunctionThreadMap.get(remoteMacAddress);
                        if (ioFunctionThread != null) {
                            String oppName = oppositePlayerNameMap.get(remoteMacAddress);
                            if (oppName != null && oppName.equals(oppositePlayerName)) {
                                // found
                                selectedIoFunctionThread = ioFunctionThread;
                                view.setSelected(true);
                            }
                        }
                    }
                }
            }
        });

        SmileImageButton refreshCreateGameButton = findViewById(R.id.refreshCreateGameButton);
        Bitmap refreshCreateGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, getString(R.string.refreshString), colorBlue);
        refreshCreateGameButton.setImageBitmap(refreshCreateGameBitmap);
        refreshCreateGameButton.setOnClickListener(view -> {
            startDiscoverability();
            Log.d(TAG, "Refresh --> startDiscoverability()");
        });

        SmileImageButton startCreateGameButton = findViewById(R.id.startCreateGameButton);
        Bitmap startCreateGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(this, R.drawable.normal_button_image, getString(R.string.startString), colorDarkGreen);
        startCreateGameButton.setImageBitmap(startCreateGameBitmap);
        startCreateGameButton.setOnClickListener(view -> {
            if (playerName.isEmpty()) {
                showMessage.showMessageInTextView(playerNameCannotBeEmptyString, MessageDuration);
                return;
            }
            if (oppositePlayerName.isEmpty()) {
                showMessage.showMessageInTextView(noOppositePlayerString, MessageDuration);
                return;
            }
            // Notify client to start game
            if (selectedIoFunctionThread != null) {
                GHogHunterApp.selectedIoFuncThread = selectedIoFunctionThread;
                stopServerAcceptThread();
                for (String remoteMacAddress : ioFunctionThreadMap.keySet()) {
                    IoFunctionThread ioFunctionThread = ioFunctionThreadMap.get(remoteMacAddress);
                    if (ioFunctionThread != null && ioFunctionThread != selectedIoFunctionThread) {
                        ioFunctionThread.write(Constants.TWO_PLAY_HOST_EX_CODE, "");
                        ConnectDeviceUtil.stopIoFunctionThread(ioFunctionThread);
                    }
                }
                // clear HashMaps
                ioFunctionThreadMap.clear();
                ioFunctionThreadMap = null;
                oppositePlayerNameMap.clear();
                oppositePlayerNameMap = null;
                createGameHandler.removeCallbacksAndMessages(null);
                selectedIoFunctionThread.write(Constants.TWO_PLAY_HOST_ST_GAME, "");
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "CreateGameActivity --> Came back from BtHostGameActivity.");

        if (requestCode == Constants.TWO_PLAY_GAME_BY_HOST) {
            oppositePlayerName = "";
            oppositePlayerNameMap = new LinkedHashMap<>();
            mServerAcceptThread = null;
            ioFunctionThreadMap = new HashMap<>();
            selectedIoFunctionThread = null;
            // update list view
            ArrayList<String> oppNameList = new ArrayList<>(oppositePlayerNameMap.values());
            twoPlayerListAdapter.updateData(oppNameList);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        hostLeavingNotification();

        oppositePlayerNameMap.clear();
        oppositePlayerNameMap = null;

        stopServerAcceptThread();

        ArrayList<IoFunctionThread> threadList = new ArrayList<>(ioFunctionThreadMap.values());
        ConnectDeviceUtil.stopIoFunctionThreads(threadList);
        ioFunctionThreadMap.clear();
        ioFunctionThreadMap = null;

        selectedIoFunctionThread = null;

        twoPlayerListAdapter.clear();
        twoPlayerListAdapter = null;

        if (createGameHandler != null) {
            createGameHandler.removeCallbacksAndMessages(null);
            createGameHandler = null;
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

        ArrayList<IoFunctionThread> threadList = new ArrayList<>(ioFunctionThreadMap.values());
        ConnectDeviceUtil.stopIoFunctionThreads(threadList);
        ioFunctionThreadMap.clear();

        createGameHandler.removeCallbacksAndMessages(null); // added on 2019-05-14

        oppositePlayerNameMap.clear();
        twoPlayerListAdapter.clear();
        twoPlayerListAdapter.notifyDataSetChanged();
    }

    protected void startHostGame() {

    }

    private void hostLeavingNotification() {
        for (IoFunctionThread ioFunctionThread : ioFunctionThreadMap.values()) {
            ioFunctionThread.write(Constants.TWO_PLAY_HOST_EX_CODE, "");
        }
    }

    private void stopServerAcceptThread() {
        if (mServerAcceptThread != null) {
            mServerAcceptThread.setKeepRunning(false);
            mServerAcceptThread.closeServerSocket();
            boolean retry = true;
            while (retry) {
                try {
                    Log.d(TAG, "stopServerAcceptThread.mServerAcceptThread.Join90");
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
            String megString;
            String deviceName;
            String remoteMacAddress;
            ConnectDevice connectDevice;
            IoFunctionThread ioFunctionThread;
            Bundle data = msg.getData();

            switch (msg.what) {
                case Constants.OPPOS_PLAYER_NAME_READ:
                    megString = "has been read.";
                    String oppositeName = data.getString("OppositePlayerName");
                    megString = oppositeName + " " + megString;
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView(megString, MessageDuration);
                    connectDevice = data.getParcelable("ConnectDevice");
                    remoteMacAddress = "";
                    if (connectDevice != null) {
                        remoteMacAddress = connectDevice.getAddress();
                        if (oppositeName != null) {
                            if (!oppositeName.isEmpty()) {
                                if (!oppositePlayerNameMap.containsKey(remoteMacAddress)) {
                                    oppositePlayerNameMap.put(remoteMacAddress, oppositeName);
                                    ArrayList<String> oppNameList = new ArrayList<>(oppositePlayerNameMap.values());
                                    twoPlayerListAdapter.updateData(oppNameList);
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
                    showMessage.showMessageInTextView(cannotCreateServerSocketString, MessageDuration);
                    break;
                case Constants.SER_ACCEPT_TH_CONNECTED:
                    connectDevice = data.getParcelable("ConnectDevice");
                    if (connectDevice == null) break;
                    deviceName = ConnectDeviceUtil.getConnectDeviceName(connectDevice);
                    remoteMacAddress = connectDevice.getAddress();
                    megString = serverAcceptedConnectionString + "(" + deviceName+ ")";
                    Log.d(TAG, megString);
                    showMessage.showMessageInTextView(serverAcceptedConnectionString, MessageDuration);
                    // start reading data from the other device and writing data to the other device
                    ioFunctionThread = mServerAcceptThread.getIoFunctionThread(connectDevice);
                    ioFunctionThread.setStartRead(true);    // start reading data
                    if (!ioFunctionThreadMap.containsKey(remoteMacAddress)) {
                        ioFunctionThreadMap.put(remoteMacAddress, ioFunctionThread);
                    }
                    break;
                case Constants.SER_ACCEPT_TH_STOPPED:
                    showMessage.showMessageInTextView(waitingStoppedCancelledString, MessageDuration);
                    break;
                case Constants.TWO_PLAY_CLIENT_EX_CODE:
                    connectDevice = data.getParcelable("ConnectDevice");
                    if (connectDevice == null) break;
                    remoteMacAddress = connectDevice.getAddress();
                    ioFunctionThread = ioFunctionThreadMap.get(remoteMacAddress);
                    if (ioFunctionThread == null) break;
                    ioFunctionThread.setStartRead(true);    // start reading data
                    // remove the remote connected device from oppositePlayerNameList
                    if (oppositePlayerNameMap.containsKey(remoteMacAddress)) {
                        showMessage.showMessageInTextView(clientLeftGameString, MessageDuration);
                        // added on 2019-06-08 to fix bugs
                        String removedOppName = oppositePlayerNameMap.get(remoteMacAddress);
                        if (removedOppName != null){
                            if (removedOppName.equals(oppositePlayerName)) {
                                // selected client has been removed, then changed to no selection
                                oppositePlayerName = "";
                                selectedIoFunctionThread = null;
                            }
                            oppositePlayerNameMap.remove(remoteMacAddress);
                        }
                    }
                    // update list view
                    ArrayList<String> oppNameList = new ArrayList<>(oppositePlayerNameMap.values());
                    twoPlayerListAdapter.updateData(oppNameList);
                    break;
                case Constants.TWO_PLAY_DEF_READ:
                    Log.d(TAG, "Default reading.");
                    connectDevice = data.getParcelable("ConnectDevice");
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
