package com.smile.groundhoghunter.Constants;

public final class CommonConstants {
    private CommonConstants() {
    }

    public static final int OppositePlayerNameHasBeenRead = 0;

    public static final int BluetoothAcceptThreadNoServerSocket = 1;
    public static final int BluetoothAcceptThreadStarted = 2;
    public static final int BluetoothAcceptThreadStopped = 3;
    public static final int BluetoothAcceptThreadConnected = 4;
    public static final int BluetoothConnectToThreadNoClientSocket = 11;
    public static final int BluetoothConnectToThreadStarted = 12;
    public static final int BluetoothConnectToThreadConnected = 13;
    public static final int BluetoothConnectToThreadFailedToConnect = 14;
    public static final int DiscoveryTimerHasReached = 31;
    public static final int DiscoveryTimerHasBeenDismissed = 32;
    public static final int BluetoothHostExitCode = 35;
    public static final int BluetoothClientExitCode = 36;

    public static final int BluetoothStartGame = 40;
    public static final int BluetoothLeaveGame = 41;

    public static final int GameBySinglePlayer = 1000;
    public static final int BluetoothGameByHost = 1001;
    public static final int BluetoothGameByClient = 1002;
}
