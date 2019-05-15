package com.smile.groundhoghunter.Constants;

public final class CommonConstants {
    private CommonConstants() {
    }

    public static final int TwoPlayerFailedReading = 255;   // failed to read
    public static final int TwoPlayerDefaultReading = 254;    // data outside the range

    public static final int OppositePlayerNameHasBeenRead = 0;
    public static final int TwoPlayerHostStartGame = 1;
    public static final int TwoPlayerOppositeLeftGame = 2;
    public static final int TwoPlayerStartGameButton = 3;
    public static final int TwoPlayerPauseGameButton = 4;
    public static final int TwoPlayerResumeGameButton = 5;
    public static final int TwoPlayerNewGameButton = 6;
    public static final int TwoPlayerHostExitCode = 7;
    public static final int TwoPlayerClientExitCode = 8;
    public static final int TwoPlayerClientGameTimerRead = 10;
    public static final int TwoPlayerClientGameGroundhogRead = 11;
    public static final int TwoPlayerGameGroundhogHit = 12;
    public static final int TwoPlayerGameScoreReceived = 13;

    public static final int ServerAcceptThreadNoServerSocket = 21;
    public static final int ServerAcceptThreadStopped = 22;
    public static final int ServerAcceptThreadConnected = 23;

    public static final int BluetoothAcceptThreadNoServerSocket = 41;
    public static final int BluetoothAcceptThreadStopped = 42;
    public static final int BluetoothAcceptThreadConnected = 43;
    public static final int BluetoothConnectToThreadNoClientSocket = 44;
    public static final int BluetoothConnectToThreadConnected = 45;
    public static final int BluetoothConnectToThreadFailedToConnect = 46;
    public static final int BluetoothDiscoveryTimerHasReached = 47;
    public static final int BluetoothDiscoveryTimerHasBeenDismissed = 48;

    public static final int GameBySinglePlayer = 1000;
    public static final int TwoPlayerGameByHost = 1001;
    public static final int TwoPlayerGameByClient = 1002;
    public static final int BluetoothGameByHost = 1003;
    public static final int BluetoothGameByClient = 1004;
}
