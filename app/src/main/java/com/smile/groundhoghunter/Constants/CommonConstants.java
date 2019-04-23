package com.smile.groundhoghunter.Constants;

public final class CommonConstants {
    private CommonConstants() {
    }

    public static final int BluetoothFailedReading = 255;   // failed to read
    public static final int BluetoothDefaultReading = 254;    // data outside the range

    public static final int OppositePlayerNameHasBeenRead = 0;
    public static final int BluetoothAcceptThreadNoServerSocket = 1;
    public static final int BluetoothAcceptThreadStopped = 3;
    public static final int BluetoothAcceptThreadConnected = 4;
    public static final int BluetoothConnectToThreadNoClientSocket = 11;
    public static final int BluetoothConnectToThreadConnected = 13;
    public static final int BluetoothConnectToThreadFailedToConnect = 14;
    public static final int BluetoothDiscoveryTimerHasReached = 31;
    public static final int BluetoothDiscoveryTimerHasBeenDismissed = 32;
    public static final int BluetoothHostExitCode = 35;
    public static final int BluetoothClientExitCode = 36;

    public static final int BluetoothStartGame = 40;
    public static final int BluetoothLeaveGame = 41;
    public static final int BluetoothStartGameButton = 43;
    public static final int BluetoothPauseGameButton = 44;
    public static final int BluetoothResumeGameButton = 45;
    public static final int BluetoothNewGameButton = 46;

    public static final int GameBySinglePlayer = 1000;
    public static final int BluetoothGameByHost = 1001;
    public static final int BluetoothGameByClient = 1002;
}
