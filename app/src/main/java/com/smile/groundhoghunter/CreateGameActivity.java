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

import com.smile.groundhoghunter.AbstractClasses.IoFunctionThread;
import com.smile.groundhoghunter.AbstractClasses.ServerAcceptThread;
import com.smile.groundhoghunter.ArrayAdapters.TwoPlayerListAdapter;
import com.smile.groundhoghunter.Constants.CommonConstants;
import com.smile.groundhoghunter.Interfaces.ConnectDevice;
import com.smile.groundhoghunter.Utilities.ConnectDeviceUtil;
import com.smile.groundhoghunter.Utilities.MessageShowingUtil;
import com.smile.smilelibraries.customized_button.SmileImageButton;
import com.smile.smilelibraries.utilities.FontAndBitmapUtil;
import com.smile.smilelibraries.utilities.ScreenUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class CreateGameActivity extends AppCompatActivity {

    // private properties
    private static final String TAG = new String(".CreateGameActivity");

    private float textFontSize;
    private float fontScale;
    private float toastTextSize;

    private TextView playerNameTextView;
    private String oppositePlayerName;
    private ListView oppositePlayerNameListView;
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
        playerName = callingIntent.getStringExtra("PlayerName");
        if (playerName == null) {
            playerName = "";
        }

        createGameHandler = new CreateGameHandler(Looper.getMainLooper());
        ioFunctionThreadMap = new HashMap<>();
        mServerAcceptThread = null;
        selectedIoFunctionThread = null;

        oppositePlayerName = "";    // empty
        oppositePlayerNameMap = new LinkedHashMap<>();

        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this, GroundhogHunterApp.FontSize_Scale_Type, null);
        textFontSize = ScreenUtil.suitableFontSize(this, defaultTextFontSize, GroundhogHunterApp.FontSize_Scale_Type, 0.0f);
        fontScale = ScreenUtil.suitableFontScale(this, GroundhogHunterApp.FontSize_Scale_Type, 0.0f);
        toastTextSize = textFontSize * 0.8f;

        playerNameCannotBeEmptyString = getString(R.string.playerNameCannotBeEmptyString);
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

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_game);

        // message showing view
        TextView toastMessageTextView = findViewById(R.id.toastMessageTextView);
        ScreenUtil.resizeTextSize(toastMessageTextView,toastTextSize, GroundhogHunterApp.FontSize_Scale_Type);
        showMessage = new MessageShowingUtil(this, toastMessageTextView);

        TextView createGameTitleTextView = findViewById(R.id.createGameTitleTextView);
        createGameTitleTextView.setText(getString(R.string.createBluetoothGameString));
        ScreenUtil.resizeTextSize(createGameTitleTextView, textFontSize * 1.2f, GroundhogHunterApp.FontSize_Scale_Type);

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
                                if (oppName.equals(oppositePlayerName)) {
                                    // found
                                    selectedIoFunctionThread = ioFunctionThread;
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
                startDiscoverability();
                Log.d(TAG, "Refresh --> startDiscoverability()");
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
                if (selectedIoFunctionThread != null) {
                    GroundhogHunterApp.selectedIoFuncThread = selectedIoFunctionThread;
                    stopServerAcceptThread();
                    for (String remoteMacAddress : ioFunctionThreadMap.keySet()) {
                        IoFunctionThread ioFunctionThread = ioFunctionThreadMap.get(remoteMacAddress);
                        if (ioFunctionThread != selectedIoFunctionThread) {
                            ioFunctionThread.write(CommonConstants.TwoPlayerHostExitCode, "");
                            ConnectDeviceUtil.stopIoFunctionThread(ioFunctionThread);
                        }
                    }
                    // clear HashMaps
                    ioFunctionThreadMap.clear();
                    ioFunctionThreadMap = null;
                    oppositePlayerNameMap.clear();
                    oppositePlayerNameMap = null;
                    //
                    // remove all messages from createGameHandler, // added on 2019-05-14
                    createGameHandler.removeCallbacksAndMessages(null);
                    //
                    selectedIoFunctionThread.write(CommonConstants.TwoPlayerHostStartGame, "");
                    
                    startHostGame();
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

        startDiscoverability();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "CreateGameActivity --> Came back from BtHostGameActivity.");

        switch(requestCode) {
            case CommonConstants.TwoPlayerGameByHost:
                oppositePlayerName = "";
                oppositePlayerNameMap = new LinkedHashMap<>();
                mServerAcceptThread = null;
                ioFunctionThreadMap = new HashMap<>();
                selectedIoFunctionThread = null;
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
            ioFunctionThread.write(CommonConstants.TwoPlayerHostExitCode, "");
        }
    }

    private void stopServerAcceptThread() {
        if (mServerAcceptThread != null) {
            mServerAcceptThread.setKeepRunning(false);
            mServerAcceptThread.closeServerSocket();
            boolean retry = true;
            while (retry) {
                try {
                    Log.d(TAG, "mServerAcceptThread.Join()........\n");
                    mServerAcceptThread.join();
                    retry = false;
                    mServerAcceptThread = null;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }// continue processing until the thread ends
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

            ConnectDevice connectDevice;
            IoFunctionThread ioFunctionThread;

            Context mContext = getApplicationContext();
            Bundle data = msg.getData();

            switch (msg.what) {
                case CommonConstants.OppositePlayerNameHasBeenRead:
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
                    ioFunctionThread.setStartRead(true);    // start reading data

                    break;
                case CommonConstants.ServerAcceptThreadNoServerSocket:
                    showMessage.showMessageInTextView(cannotCreateServerSocketString, MessageDuration);
                    break;
                case CommonConstants.ServerAcceptThreadConnected:
                    connectDevice = data.getParcelable("ConnectDevice");
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
                case CommonConstants.ServerAcceptThreadStopped:
                    showMessage.showMessageInTextView(waitingStoppedCancelledString, MessageDuration);
                    break;
                case CommonConstants.TwoPlayerClientExitCode:
                    connectDevice = data.getParcelable("ConnectDevice");
                    remoteMacAddress = connectDevice.getAddress();

                    ioFunctionThread = ioFunctionThreadMap.get(remoteMacAddress);
                    ioFunctionThread.setStartRead(true);    // start reading data

                    // remove the remote connected device from oppositePlayerNameList
                    if (oppositePlayerNameMap.containsKey(remoteMacAddress)) {
                        showMessage.showMessageInTextView(clientLeftGameString, MessageDuration);
                        // added on 2019-06-08 to fix bugs
                        String removedOppName = oppositePlayerNameMap.get(remoteMacAddress);
                        if (removedOppName.equals(oppositePlayerName)) {
                            // selected client has been removed, then changed to no selection
                            oppositePlayerName = "";
                            selectedIoFunctionThread = null;
                        }
                        //
                        oppositePlayerNameMap.remove(remoteMacAddress);
                    }

                    // update list view
                    ArrayList<String> oppNameList = new ArrayList<>(oppositePlayerNameMap.values());
                    twoPlayerListAdapter.updateData(oppNameList);

                    break;
                case CommonConstants.TwoPlayerDefaultReading:
                    Log.d(TAG, "Default reading.");
                    connectDevice = data.getParcelable("ConnectDevice");
                    remoteMacAddress = connectDevice.getAddress();
                    // read the next data
                    ioFunctionThread = ioFunctionThreadMap.get(remoteMacAddress);
                    ioFunctionThread.setStartRead(true);    // start reading data
                    break;
            }
        }
    }
}
