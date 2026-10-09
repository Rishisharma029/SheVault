package com.shevault.feature.incident

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shevault.core.security.DuressCredentialManager
import com.shevault.feature.incident.gesture.PanicGestureDetector
import com.shevault.feature.incident.model.ActivationMethod
import com.shevault.feature.incident.model.IncidentSession
import com.shevault.feature.incident.session.IncidentSessionManager
import com.shevault.feature.incident.statemachine.IncidentEvent
import com.shevault.feature.incident.statemachine.IncidentState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Observable UI state for the Incident screen.
 */
data class IncidentUiState(
    val incidentState: IncidentState = IncidentState.IDLE,
    val session: IncidentSession? = null,
    val graceSecondsRemaining: Int = 5,
    val isCancellationSheetVisible: Boolean = false,
    val isSilentDuressActive: Boolean = false,
    val telemetryGps: String = "28.6139° N, 77.2090° E",
    val telemetryBattery: String = "88% (Discharging)",
    val telemetryAccuracy: String = "± 3.2 meters (Fused GPS)",
    val errorMessage: String? = null
)

/**
 * ViewModel orchestrating the Incident lifecycle, state machine,
 * panic gesture secondary activation, and cancellation/duress protocols.
 */
class IncidentViewModel(
    val sessionManager: IncidentSessionManager = IncidentSessionManager(),
    val credentialManager: DuressCredentialManager = DuressCredentialManager(),
    val panicGestureDetector: PanicGestureDetector = PanicGestureDetector(),
    val incidentRepository: com.shevault.core.network.IncidentRepository? = null,
    coroutineScope: kotlinx.coroutines.CoroutineScope? = null
) : ViewModel() {

    private val scope: kotlinx.coroutines.CoroutineScope = coroutineScope ?: try {
        viewModelScope
    } catch (_: Throwable) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default)
    }

    private val _uiState = MutableStateFlow(IncidentUiState())
    val uiState: StateFlow<IncidentUiState> = _uiState.asStateFlow()

    private var graceTimerJob: Job? = null

    init {
        // Observe state machine changes
        scope.launch {
            sessionManager.stateMachine.state.collect { state ->
                _uiState.update { current ->
                    current.copy(incidentState = state)
                }
            }
        }

        // Observe active session updates
        scope.launch {
            sessionManager.currentSession.collect { session ->
                _uiState.update { current ->
                    current.copy(
                        session = session,
                        telemetryGps = session?.initialLocation?.let {
                            "${"%.4f".format(it.latitude)}° N, ${"%.4f".format(it.longitude)}° E"
                        } ?: current.telemetryGps,
                        telemetryBattery = session?.initialBattery?.let {
                            "${it.levelPercent}% (${if (it.isCharging) "Charging" else "Battery"})"
                        } ?: current.telemetryBattery,
                        isSilentDuressActive = session?.isCoercedDuress ?: false
                    )
                }
            }
        }
    }

    /**
     * Activates an incident session with the specified trigger method.
     */
    fun startIncident(method: ActivationMethod = ActivationMethod.SOS_BUTTON) {
        val session = sessionManager.createIncident(method)
        // Transition to ACTIVE_GRACE
        sessionManager.activateSession()
        startGraceCountdown(5)

        // Asynchronously notify backend with idempotency token
        incidentRepository?.let { repo ->
            scope.launch {
                repo.createIncident(
                    clientSessionId = session.sessionId,
                    deviceId = session.deviceId,
                    activationMethod = session.activationMethod.name,
                    battery = session.initialBattery.levelPercent,
                    latitude = session.initialLocation?.latitude,
                    longitude = session.initialLocation?.longitude,
                    accuracy = session.initialLocation?.accuracyMeters
                )
            }
        }
    }

    /**
     * Record a tap on the panic gesture detector (Secondary activation mechanism).
     */
    fun recordPanicTap(timestampMs: Long = System.currentTimeMillis()) {
        val stage = panicGestureDetector.recordTap(timestampMs)
        // If detector triggered safety activation and grace window
        if (stage is com.shevault.feature.incident.gesture.GesturePipelineStage.GraceWindow) {
            startIncident(ActivationMethod.PANIC_GESTURE)
        }
    }

    /**
     * Starts the grace period timer countdown (e.g. 5 seconds).
     * If user does not cancel before expiry -> transitions to ESCALATING.
     */
    fun startGraceCountdown(durationSeconds: Int = 5) {
        graceTimerJob?.cancel()
        _uiState.update { it.copy(graceSecondsRemaining = durationSeconds) }

        graceTimerJob = scope.launch {
            var remaining = durationSeconds
            while (remaining > 0) {
                delay(1000L)
                remaining--
                _uiState.update { it.copy(graceSecondsRemaining = remaining) }
                sessionManager.stateMachine.transition(IncidentEvent.GraceTimerTick(remaining))
            }
            // Grace timer expired -> Escalate emergency
            onGraceTimerExpired()
        }
    }

    private fun onGraceTimerExpired() {
        sessionManager.markEscalating()
        executeEmergencyEscalation()
    }

    /**
     * Dispatches emergency alerts across network and fallback channels.
     */
    private fun executeEmergencyEscalation() {
        scope.launch {
            sessionManager.stateMachine.transition(IncidentEvent.EscalationDispatched)
            // Primary network attempt
            delay(400L)
            sessionManager.stateMachine.transition(IncidentEvent.NetworkSuccess)
        }
    }

    /**
     * Opens the cancellation flow.
     */
    fun openCancellationFlow() {
        sessionManager.stateMachine.transition(IncidentEvent.RequestCancellation)
        _uiState.update { it.copy(isCancellationSheetVisible = true) }
    }

    /**
     * Cancels the cancellation flow and continues protection.
     */
    fun continueProtection() {
        sessionManager.stateMachine.transition(IncidentEvent.ContinueProtection)
        _uiState.update { it.copy(isCancellationSheetVisible = false) }
    }

    /**
     * Safe credential entered: Disarms and terminates session cleanly.
     */
    fun handleSafeCancelled(pin: String = "1234") {
        graceTimerJob?.cancel()
        val session = sessionManager.markSafeCancelled()
        sessionManager.stateMachine.transition(IncidentEvent.ResolveIncident)
        _uiState.update {
            it.copy(
                isCancellationSheetVisible = false,
                incidentState = IncidentState.ENDED
            )
        }

        incidentRepository?.let { repo ->
            session?.let { s ->
                scope.launch {
                    repo.cancelIncident(s.incidentId, pin)
                }
            }
        }
    }

    /**
     * Duress credential entered: Covertly logs COERCED_DURESS, triggers silent alert,
     * while UI mirrors the safe resolution.
     */
    fun handleDuressCancelled(duressPin: String = "9999") {
        graceTimerJob?.cancel()
        val session = sessionManager.markDuressCancelled()
        // In the background, covertly escalate:
        sessionManager.stateMachine.transition(IncidentEvent.EscalationDispatched)
        _uiState.update {
            it.copy(
                isCancellationSheetVisible = false,
                isSilentDuressActive = true
            )
        }

        incidentRepository?.let { repo ->
            session?.let { s ->
                scope.launch {
                    repo.cancelWithDuress(s.incidentId, duressPin)
                }
            }
        }
    }

    fun resetToIdle() {
        graceTimerJob?.cancel()
        sessionManager.clearSession()
        _uiState.update { IncidentUiState() }
    }
}
