package com.smile.groundhoghunter.constants

object CommonConstants {
    const val TwoPlayerFailedReading: Int = 255 // failed to read
    const val TwoPlayerDefaultReading: Int = 254 // data outside the range
    const val OppositePlayerNameHasBeenRead: Int = 0
    const val TwoPlayerHostStartGame: Int = 1
    const val TwoPlayerOppositeLeftGame: Int = 2
    const val TwoPlayerStartGameButton: Int = 3
    const val TwoPlayerPauseGameButton: Int = 4
    const val TwoPlayerResumeGameButton: Int = 5
    const val TwoPlayerNewGameButton: Int = 6
    const val TwoPlayerHostExitCode: Int = 7
    const val TwoPlayerClientExitCode: Int = 8
    const val TwoPlayerClientGameTimerRead: Int = 10
    const val TwoPlayerClientGameGroundhogRead: Int = 11
    const val TwoPlayerGameGroundhogHit: Int = 12
    const val TwoPlayerGameScoreReceived: Int = 13
    const val ServerAcceptThreadNoServerSocket: Int = 21
    const val ServerAcceptThreadStopped: Int = 22
    const val ServerAcceptThreadConnected: Int = 23
    const val ClientConnectToThreadNoClientSocket: Int = 24
    const val ClientConnectToThreadConnected: Int = 25
    const val ClientConnectToThreadFailedToConnect: Int = 26
    const val ClientDiscoveryTimerHasReached: Int = 27
    const val ClientDiscoveryTimerHasBeenDismissed: Int = 48
    const val GameBySinglePlayer: Int = 1000
    const val TwoPlayerGameByHost: Int = 1001
    const val TwoPlayerGameByClient: Int = 1002
    const val BluetoothGameByHost: Int = 1003
    const val BluetoothGameByClient: Int = 1004
    const val PLAYER_NAME = "PlayerName"
    const val GAME_TYPE = "GameType"
    const val HOST_SCORE = "HostScore"
    const val HOST_HIT_NUM = "HostHitNum"
    const val CLIENT_SCORE = "ClientScore"
    const val CLIENT_HIT_NUM = "ClientHitNum"
}
