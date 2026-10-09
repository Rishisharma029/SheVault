package com.shevault.core.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

sealed interface WebSocketState {
    data object Disconnected : WebSocketState
    data object Connecting : WebSocketState
    data class Connected(val incidentId: String) : WebSocketState
    data class Error(val throwable: Throwable) : WebSocketState
}

/**
 * WebSocket client for real-time telemetry streaming and incident updates.
 *
 * Connects to ws://<host>/api/v1/ws/incidents/{incidentId}?token={JWT}
 * Automatically handles reconnection with exponential backoff and heartbeat pings.
 */
class IncidentWebSocketClient(
    private val tokenManager: TokenManager,
    private val baseWsUrlProvider: () -> String = { NetworkConfig.activeWebSocketBaseUrl },
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val _connectionState = MutableStateFlow<WebSocketState>(WebSocketState.Disconnected)
    val connectionState: StateFlow<WebSocketState> = _connectionState.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<String> = _incomingMessages.asSharedFlow()

    private var webSocket: WebSocket? = null
    private var activeIncidentId: String? = null
    private val isClosedIntentionally = AtomicBoolean(false)
    private var reconnectJob: Job? = null
    private var heartbeatJob: Job? = null
    private var retryAttempt = 0

    private val okHttpClient = OkHttpClient.Builder()
        .pingInterval(NetworkConfig.WS_PING_INTERVAL_SECONDS, TimeUnit.SECONDS)
        .connectTimeout(NetworkConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // Keep-alive for WS
        .build()

    /**
     * Connect to live incident stream.
     */
    fun connect(incidentId: String) {
        isClosedIntentionally.set(false)
        activeIncidentId = incidentId
        reconnectJob?.cancel()
        initiateConnection(incidentId)
    }

    private fun initiateConnection(incidentId: String) {
        val token = tokenManager.getAccessToken()
        if (token.isNullOrBlank()) {
            _connectionState.value = WebSocketState.Error(IllegalStateException("No authentication token available for WebSocket connection"))
            return
        }

        _connectionState.value = WebSocketState.Connecting

        val base = baseWsUrlProvider().trimEnd('/')
        val url = "$base/api/v1/ws/incidents/$incidentId?token=$token"

        val request = Request.Builder()
            .url(url)
            .build()

        webSocket?.close(1000, "Reconnecting")
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                retryAttempt = 0
                _connectionState.value = WebSocketState.Connected(incidentId)
                startHeartbeat()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                scope.launch {
                    _incomingMessages.emit(text)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                heartbeatJob?.cancel()
                if (!isClosedIntentionally.get()) {
                    scheduleReconnect()
                } else {
                    _connectionState.value = WebSocketState.Disconnected
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                heartbeatJob?.cancel()
                _connectionState.value = WebSocketState.Error(t)
                if (!isClosedIntentionally.get()) {
                    scheduleReconnect()
                }
            }
        })
    }

    /**
     * Send payload or telemetry message through active socket.
     */
    fun send(message: String): Boolean {
        return webSocket?.send(message) ?: false
    }

    /**
     * Gracefully close websocket.
     */
    fun disconnect() {
        isClosedIntentionally.set(true)
        reconnectJob?.cancel()
        heartbeatJob?.cancel()
        activeIncidentId = null
        webSocket?.close(1000, "Client disconnected")
        webSocket = null
        _connectionState.value = WebSocketState.Disconnected
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (_connectionState.value is WebSocketState.Connected) {
                delay(NetworkConfig.WS_PING_INTERVAL_SECONDS * 1000)
                webSocket?.send("ping")
            }
        }
    }

    private fun scheduleReconnect() {
        val incidentId = activeIncidentId ?: return
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            retryAttempt++
            // Exponential backoff: 1s, 2s, 4s, up to 16s max
            val backoffSeconds = (1L shl (retryAttempt.coerceAtMost(4))).coerceAtMost(16L)
            delay(backoffSeconds * 1000)
            if (!isClosedIntentionally.get()) {
                initiateConnection(incidentId)
            }
        }
    }
}
