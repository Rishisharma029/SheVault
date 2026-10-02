package com.shevault.feature.incident.statemachine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Deterministic State Machine governing emergency incident lifecycle.
 *
 * Implements explicit transition rules across all 13 states:
 * - ACTIVE_GRACE -> Safe PIN -> SAFE_CANCELLED
 * - ACTIVE_GRACE -> Duress PIN -> DURESS_CANCELLED
 * - ACTIVE_GRACE -> timer expires -> ESCALATING
 * - Prevents state corruption from scattered booleans across ViewModels.
 */
class IncidentStateMachine(
    initialState: IncidentState = IncidentState.IDLE
) {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<IncidentState> = _state.asStateFlow()

    private val history = CopyOnWriteArrayList<TransitionRecord>()

    val currentState: IncidentState
        get() = _state.value

    /**
     * Attempts to transition the state machine using an explicit event.
     * Returns Success if valid according to state rules, or Invalid if rejected.
     */
    @Synchronized
    fun transition(event: IncidentEvent): IncidentTransitionResult {
        val current = _state.value
        val nextState = resolveNextState(current, event)

        return if (nextState != null) {
            _state.value = nextState
            val record = TransitionRecord(
                fromState = current,
                toState = nextState,
                eventName = event::class.simpleName ?: "UnknownEvent"
            )
            history.add(record)
            IncidentTransitionResult.Success(
                fromState = current,
                toState = nextState,
                event = event,
                timestamp = record.timestamp
            )
        } else {
            IncidentTransitionResult.Invalid(
                currentState = current,
                rejectedEvent = event,
                reason = "Illegal transition from $current via ${event::class.simpleName}"
            )
        }
    }

    private fun resolveNextState(current: IncidentState, event: IncidentEvent): IncidentState? {
        return when (current) {
            IncidentState.IDLE -> when (event) {
                is IncidentEvent.TriggerActivation -> IncidentState.STARTING
                else -> null
            }

            IncidentState.STARTING -> when (event) {
                is IncidentEvent.SessionCreated -> IncidentState.ACTIVE_GRACE
                is IncidentEvent.RequestCancellation -> IncidentState.CANCEL_AUTHENTICATION
                is IncidentEvent.FatalError -> IncidentState.FAILED
                else -> null
            }

            IncidentState.ACTIVE_GRACE -> when (event) {
                is IncidentEvent.RequestCancellation -> IncidentState.CANCEL_AUTHENTICATION
                is IncidentEvent.GraceTimerExpired -> IncidentState.ESCALATING
                is IncidentEvent.SubmitSafePin -> IncidentState.SAFE_CANCELLED
                is IncidentEvent.SubmitDuressPin -> IncidentState.DURESS_CANCELLED
                is IncidentEvent.GraceTimerTick -> current // Remains in ACTIVE_GRACE
                is IncidentEvent.FatalError -> IncidentState.FAILED
                else -> null
            }

            IncidentState.CANCEL_AUTHENTICATION -> when (event) {
                is IncidentEvent.ContinueProtection -> IncidentState.ACTIVE_GRACE
                is IncidentEvent.SubmitSafePin -> IncidentState.SAFE_CANCELLED
                is IncidentEvent.SubmitDuressPin -> IncidentState.DURESS_CANCELLED
                is IncidentEvent.GraceTimerExpired -> IncidentState.ESCALATING
                is IncidentEvent.AuthenticationFailed -> current // Remain in authentication state to allow retry
                is IncidentEvent.FatalError -> IncidentState.FAILED
                else -> null
            }

            IncidentState.SAFE_CANCELLED -> when (event) {
                is IncidentEvent.ResolveIncident -> IncidentState.ENDED
                is IncidentEvent.ResetToIdle -> IncidentState.IDLE
                else -> null
            }

            IncidentState.DURESS_CANCELLED -> when (event) {
                // Covert mode: UI reports cancelled/safe, but backend state escalates silently
                is IncidentEvent.EscalationDispatched -> IncidentState.ESCALATING
                is IncidentEvent.ResolveIncident -> IncidentState.ENDED
                is IncidentEvent.ResetToIdle -> IncidentState.IDLE
                else -> null
            }

            IncidentState.ESCALATING -> when (event) {
                is IncidentEvent.NetworkSuccess -> IncidentState.ESCALATED
                is IncidentEvent.NetworkFailure -> {
                    if (event.retryCount < event.maxRetries) {
                        IncidentState.NETWORK_RETRY
                    } else {
                        IncidentState.FALLBACK_PENDING
                    }
                }
                is IncidentEvent.RequestCancellation -> IncidentState.CANCEL_AUTHENTICATION
                is IncidentEvent.FatalError -> IncidentState.FAILED
                else -> null
            }

            IncidentState.NETWORK_RETRY -> when (event) {
                is IncidentEvent.NetworkSuccess -> IncidentState.ESCALATED
                is IncidentEvent.NetworkRetryAttempted -> IncidentState.NETWORK_RETRY
                is IncidentEvent.NetworkFailure -> {
                    if (event.retryCount >= event.maxRetries) {
                        IncidentState.FALLBACK_PENDING
                    } else {
                        IncidentState.NETWORK_RETRY
                    }
                }
                is IncidentEvent.RequestCancellation -> IncidentState.CANCEL_AUTHENTICATION
                is IncidentEvent.FatalError -> IncidentState.FAILED
                else -> null
            }

            IncidentState.FALLBACK_PENDING -> when (event) {
                is IncidentEvent.FallbackDelivered -> IncidentState.ESCALATED
                is IncidentEvent.DeliveryTimedOut -> IncidentState.DELIVERY_UNKNOWN
                is IncidentEvent.RequestCancellation -> IncidentState.CANCEL_AUTHENTICATION
                is IncidentEvent.FatalError -> IncidentState.FAILED
                else -> null
            }

            IncidentState.ESCALATED -> when (event) {
                is IncidentEvent.DeliveryTimedOut -> IncidentState.DELIVERY_UNKNOWN
                is IncidentEvent.RequestCancellation -> IncidentState.CANCEL_AUTHENTICATION
                is IncidentEvent.ResolveIncident -> IncidentState.ENDED
                is IncidentEvent.FatalError -> IncidentState.FAILED
                else -> null
            }

            IncidentState.DELIVERY_UNKNOWN -> when (event) {
                is IncidentEvent.NetworkSuccess -> IncidentState.ESCALATED
                is IncidentEvent.FallbackDelivered -> IncidentState.ESCALATED
                is IncidentEvent.NetworkRetryAttempted -> IncidentState.NETWORK_RETRY
                is IncidentEvent.RequestCancellation -> IncidentState.CANCEL_AUTHENTICATION
                is IncidentEvent.ResolveIncident -> IncidentState.ENDED
                is IncidentEvent.FatalError -> IncidentState.FAILED
                else -> null
            }

            IncidentState.ENDED -> when (event) {
                is IncidentEvent.ResetToIdle -> IncidentState.IDLE
                is IncidentEvent.TriggerActivation -> IncidentState.STARTING
                else -> null
            }

            IncidentState.FAILED -> when (event) {
                is IncidentEvent.ResetToIdle -> IncidentState.IDLE
                is IncidentEvent.TriggerActivation -> IncidentState.STARTING
                else -> null
            }
        }
    }

    fun getTransitionHistory(): List<TransitionRecord> = history.toList()

    @Synchronized
    fun reset() {
        _state.value = IncidentState.IDLE
        history.clear()
    }
}
