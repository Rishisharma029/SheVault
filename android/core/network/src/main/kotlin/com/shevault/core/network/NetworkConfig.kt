package com.shevault.core.network

/**
 * Global network configuration for SheVault Android client.
 *
 * For local development:
 * - Android emulator maps host machine localhost to 10.0.2.2:8000
 * - Physical phone or LAN client uses reachable IP or localhost proxy
 * - Production builds point to secure HTTPS cloud endpoint
 */
object NetworkConfig {
    /**
     * Default base URL for Android emulator connecting to local FastAPI backend.
     */
    const val EMULATOR_BASE_URL = "http://10.0.2.2:8000/"

    /**
     * Local loopback base URL (used during unit tests or Robolectric / desktop previews).
     */
    const val LOCALHOST_BASE_URL = "http://127.0.0.1:8000/"

    /**
     * Active backend base URL. Configurable at runtime for physical device LAN testing.
     */
    @Volatile
    var activeBaseUrl: String = EMULATOR_BASE_URL

    /**
     * Active WebSocket base URL.
     */
    val activeWebSocketBaseUrl: String
        get() = activeBaseUrl.replace("http://", "ws://").replace("https://", "wss://")

    // Timeouts
    const val CONNECT_TIMEOUT_SECONDS = 15L
    const val READ_TIMEOUT_SECONDS = 20L
    const val WRITE_TIMEOUT_SECONDS = 20L

    // WebSocket heartbeat ping interval
    const val WS_PING_INTERVAL_SECONDS = 15L
}
