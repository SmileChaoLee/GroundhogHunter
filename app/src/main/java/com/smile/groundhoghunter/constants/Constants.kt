package com.smile.groundhoghunter.constants

import java.util.UUID

object Constants {
    const val NO_HIT_STATUS: Int = 0
    const val SINGLE_PLAY_HIT_STATUS: Int = 1
    const val HOST_PLAY_HIT_STATUS: Int = 1
    const val CLIENT_PLAY_HIT_STATUS: Int = 2
    const val TWO_PLAY_DEF_READ: Int = 254 // data outside the range
    const val OPPOS_PLAYER_NAME_READ: Int = 0
    const val TWO_PLAY_HOST_ST_GAME: Int = 1
    const val TWO_PLAY_OPPOS_LF_GAME: Int = 2
    const val TWO_PLAY_ST_GAME_BUT: Int = 3
    const val TWO_PLAY_PAU_GAME_BUT: Int = 4
    const val TWO_PLAY_RES_GAME_BUT: Int = 5
    const val TWO_PLAY_NEW_GAME_BUT: Int = 6
    const val TWO_PLAY_HOST_EX_CODE: Int = 7
    const val TWO_PLAY_CLIENT_EX_CODE: Int = 8
    const val TWO_PLAY_CL_GAME_TIMER_READ: Int = 10
    const val TWO_PLAY_CL_GAME_G_HOG_READ: Int = 11
    const val TWO_PLAY_GAME_G_HOG_HIT: Int = 12
    const val TWO_PLAY_GAME_SCORE_RECEIVED: Int = 13
    const val SER_ACCEPT_TH_NO_SER_SOCKET: Int = 21
    const val SER_ACCEPT_TH_STOPPED: Int = 22
    const val SER_ACCEPT_TH_CONNECTED: Int = 23
    const val CL_CONN_TO_TH_NO_CL_SOCKET: Int = 24
    const val CL_CONN_TO_TH_CONNECTED: Int = 25
    const val CL_CONN_TO_TH_FAILED_CONNECT: Int = 26
    const val CL_DISCOVER_TIMER_END: Int = 27
    const val CL_DISCOVER_TIMER_DISMISSED: Int = 48
    const val GAME_BY_SINGLE_PLAY: Int = 1000
    const val TWO_PLAY_GAME_BY_HOST: Int = 1001
    const val TWO_PLAY_GAME_BY_CLIENT: Int = 1002
    const val GROUNDHOG_GAME_ID = "2"
    const val GAME_ID = "GameId"
    const val GAME_TYPE = "GameType"
    const val PLAYER_NAME = "PlayerName"
    const val SCORE = "Score"
    const val HOST_SCORE = "HostScore"
    const val HOST_HIT_NUM = "HostHitNum"
    const val CLIENT_SCORE = "ClientScore"
    const val CLIENT_HIT_NUM = "ClientHitNum"
    const val DATABASE_NAME = "groundhog_hunter.db"
    const val ADMOB_BANNER_ID = "ca-app-pub-8354869049759576/7169443235"
    @JvmField
    val APP_UUID: UUID = UUID.fromString("b5af9bad-42e0-4d0d-8546-ebeb97e1abfa")

    // WiFi Direct P2P group — must start with "DIRECT-" (≤ 32 chars),
    // passphrase must be 8–63 ASCII chars.
    // Both sides share these so the Join side can connect directly without
    // going through P2P peer discovery (API 29+).
    const val WIFI_P2P_NETWORK_NAME = "DIRECT-GroundHogHunter"
    const val WIFI_P2P_PASSPHRASE   = "GroundHog2026"
}
