package com.shevault.feature.incident.statemachine

import com.shevault.feature.incident.model.ActivationMethod
import com.shevault.feature.incident.model.BatterySnapshot
import com.shevault.feature.incident.model.LocationSnapshot

/**
 * Incident State Machine States.
 *
 * All 13 canonical states required for explicit, deterministic safety lifecycle management.
 */
enum class IncidentState {
    IDLE,
    STARTING,
    ACTIVE_GRACE,
    CANCEL_AUTHENTICATION,
    SAFE_CANCELLED,
    DURESS_CANCELLED,
    ESCALATING,
    NETWORK_RETRY,
    FALLBACK_PENDING,
    ESCALATED,
    DELIVERY_UNKNOWN,
    ENDED,
    FAILED
}

/**
 * Events that trigger transitions between IncidentStates.
 */
sealed class IncidentEvent {
    data class TriggerActivation(
        val method: ActivationMethod,
        val battery: BatterySnapshot? = null,
        val location: LocationSnapshot? = null
    ) : IncidentEvent()

    data object SessionCreated : IncidentEvent()
    data class GraceTimerTick(val remainingSeconds: Int) : IncidentEvent()
    data object GraceTimerExpired : IncidentEvent()
    data object RequestCancellation : IncidentEvent()
    data object ContinueProtection : IncidentEvent()
    data class SubmitSafePin(val pin: String = "") : IncidentEvent()
    data class SubmitDuressPin(val pin: String = "") : IncidentEvent()
    data class AuthenticationFailed(val reason: String = "Authentication Failed") : IncidentEvent()
    data object EscalationDispatched : IncidentEvent()
    data object NetworkSuccess : IncidentEvent()
    data class NetworkFailure(val retryCount: Int = 0, val maxRetries: Int = 3) : IncidentEvent()
    data object NetworkRetryAttempted : IncidentEvent()
    data object FallbackTriggered : IncidentEvent()
    data object FallbackDelivered : IncidentEvent()
    data object DeliveryTimedOut : IncidentEvent()
    data object ResolveIncident : IncidentEvent()
    data object ResetToIdle : IncidentEvent()
    data class FatalError(val reason: String) : IncidentEvent()
}

/**
 * Result of attempting a transition.
 */
sealed interface IncidentTransitionResult {
    data class Success(
        val fromState: IncidentState,
        val toState: IncidentState,
        val event: IncidentEvent,
        val timestamp: Long = System.currentTimeMillis()
    ) : IncidentTransitionResult

    data class Invalid(
        val currentState: IncidentState,
        val rejectedEvent: IncidentEvent,
        val reason: String
    ) : IncidentTransitionResult
}

/**
 * Immutable record of a state transition for forensic auditing.
 */
data class TransitionRecord(
    val fromState: IncidentState,
    val toState: IncidentState,
    val eventName: String,
    val timestamp: Long = System.currentTimeMillis()
)
