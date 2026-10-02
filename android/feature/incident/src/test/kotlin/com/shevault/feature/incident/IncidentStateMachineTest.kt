package com.shevault.feature.incident

import com.shevault.feature.incident.model.ActivationMethod
import com.shevault.feature.incident.statemachine.IncidentEvent
import com.shevault.feature.incident.statemachine.IncidentState
import com.shevault.feature.incident.statemachine.IncidentStateMachine
import com.shevault.feature.incident.statemachine.IncidentTransitionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IncidentStateMachineTest {

    private lateinit var sm: IncidentStateMachine

    @Before
    fun setup() {
        sm = IncidentStateMachine(initialState = IncidentState.IDLE)
    }

    @Test
    fun testAllRequiredStatesExist() {
        val required = listOf(
            "IDLE",
            "STARTING",
            "ACTIVE_GRACE",
            "CANCEL_AUTHENTICATION",
            "SAFE_CANCELLED",
            "DURESS_CANCELLED",
            "ESCALATING",
            "NETWORK_RETRY",
            "FALLBACK_PENDING",
            "ESCALATED",
            "DELIVERY_UNKNOWN",
            "ENDED",
            "FAILED"
        )
        for (name in required) {
            val state = IncidentState.valueOf(name)
            assertNotNull("State $name must be present in IncidentState enum", state)
        }
        assertEquals("IncidentState must have exactly 13 states", 13, IncidentState.values().size)
    }

    @Test
    fun testInitialStateIsIdle() {
        assertEquals(IncidentState.IDLE, sm.currentState)
    }

    @Test
    fun testActivationFlowToActiveGrace() {
        // IDLE -> Trigger -> STARTING
        val triggerRes = sm.transition(IncidentEvent.TriggerActivation(ActivationMethod.SOS_BUTTON))
        assertTrue(triggerRes is IncidentTransitionResult.Success)
        assertEquals(IncidentState.STARTING, sm.currentState)

        // STARTING -> SessionCreated -> ACTIVE_GRACE
        val createdRes = sm.transition(IncidentEvent.SessionCreated)
        assertTrue(createdRes is IncidentTransitionResult.Success)
        assertEquals(IncidentState.ACTIVE_GRACE, sm.currentState)
    }

    @Test
    fun testExplicitTransitionActiveGraceToSafeCancelled() {
        // Setup state to ACTIVE_GRACE
        sm.transition(IncidentEvent.TriggerActivation(ActivationMethod.SOS_BUTTON))
        sm.transition(IncidentEvent.SessionCreated)
        assertEquals(IncidentState.ACTIVE_GRACE, sm.currentState)

        // ACTIVE_GRACE -> Safe PIN -> SAFE_CANCELLED
        val res = sm.transition(IncidentEvent.SubmitSafePin("1234"))
        assertTrue(res is IncidentTransitionResult.Success)
        assertEquals(IncidentState.SAFE_CANCELLED, sm.currentState)

        // SAFE_CANCELLED -> Resolve -> ENDED
        sm.transition(IncidentEvent.ResolveIncident)
        assertEquals(IncidentState.ENDED, sm.currentState)
    }

    @Test
    fun testExplicitTransitionActiveGraceToDuressCancelled() {
        // Setup state to ACTIVE_GRACE
        sm.transition(IncidentEvent.TriggerActivation(ActivationMethod.PANIC_GESTURE))
        sm.transition(IncidentEvent.SessionCreated)
        assertEquals(IncidentState.ACTIVE_GRACE, sm.currentState)

        // ACTIVE_GRACE -> Duress PIN -> DURESS_CANCELLED
        val res = sm.transition(IncidentEvent.SubmitDuressPin("9999"))
        assertTrue(res is IncidentTransitionResult.Success)
        assertEquals(IncidentState.DURESS_CANCELLED, sm.currentState)

        // DURESS_CANCELLED -> EscalationDispatched -> ESCALATING (covert background escalation)
        val silentEscalate = sm.transition(IncidentEvent.EscalationDispatched)
        assertTrue(silentEscalate is IncidentTransitionResult.Success)
        assertEquals(IncidentState.ESCALATING, sm.currentState)
    }

    @Test
    fun testExplicitTransitionActiveGraceToEscalatingOnTimerExpiry() {
        sm.transition(IncidentEvent.TriggerActivation(ActivationMethod.SOS_BUTTON))
        sm.transition(IncidentEvent.SessionCreated)
        assertEquals(IncidentState.ACTIVE_GRACE, sm.currentState)

        // ACTIVE_GRACE -> timer expires -> ESCALATING
        val res = sm.transition(IncidentEvent.GraceTimerExpired)
        assertTrue(res is IncidentTransitionResult.Success)
        assertEquals(IncidentState.ESCALATING, sm.currentState)
    }

    @Test
    fun testCancellationAuthenticationFlow() {
        sm.transition(IncidentEvent.TriggerActivation(ActivationMethod.SOS_BUTTON))
        sm.transition(IncidentEvent.SessionCreated)
        assertEquals(IncidentState.ACTIVE_GRACE, sm.currentState)

        // ACTIVE_GRACE -> Request Cancellation -> CANCEL_AUTHENTICATION
        val cancelReq = sm.transition(IncidentEvent.RequestCancellation)
        assertTrue(cancelReq is IncidentTransitionResult.Success)
        assertEquals(IncidentState.CANCEL_AUTHENTICATION, sm.currentState)

        // CANCEL_AUTHENTICATION -> Continue Protection -> ACTIVE_GRACE
        val continueRes = sm.transition(IncidentEvent.ContinueProtection)
        assertTrue(continueRes is IncidentTransitionResult.Success)
        assertEquals(IncidentState.ACTIVE_GRACE, sm.currentState)

        // Go back to auth and test Safe PIN
        sm.transition(IncidentEvent.RequestCancellation)
        sm.transition(IncidentEvent.SubmitSafePin("1234"))
        assertEquals(IncidentState.SAFE_CANCELLED, sm.currentState)
    }

    @Test
    fun testNetworkRetryAndFallbackPendingFlow() {
        sm.transition(IncidentEvent.TriggerActivation(ActivationMethod.SOS_BUTTON))
        sm.transition(IncidentEvent.SessionCreated)
        sm.transition(IncidentEvent.GraceTimerExpired)
        assertEquals(IncidentState.ESCALATING, sm.currentState)

        // Escalating -> Network Failure (attempt 1 of 3) -> NETWORK_RETRY
        sm.transition(IncidentEvent.NetworkFailure(retryCount = 1, maxRetries = 3))
        assertEquals(IncidentState.NETWORK_RETRY, sm.currentState)

        // Retry exhausted (attempt 3 of 3) -> FALLBACK_PENDING
        sm.transition(IncidentEvent.NetworkFailure(retryCount = 3, maxRetries = 3))
        assertEquals(IncidentState.FALLBACK_PENDING, sm.currentState)

        // Fallback delivered -> ESCALATED
        sm.transition(IncidentEvent.FallbackDelivered)
        assertEquals(IncidentState.ESCALATED, sm.currentState)

        // Delivery timeout -> DELIVERY_UNKNOWN
        sm.transition(IncidentEvent.DeliveryTimedOut)
        assertEquals(IncidentState.DELIVERY_UNKNOWN, sm.currentState)

        // Network recovers -> ESCALATED
        sm.transition(IncidentEvent.NetworkSuccess)
        assertEquals(IncidentState.ESCALATED, sm.currentState)
    }

    @Test
    fun testIllegalTransitionsRejected() {
        // Attempting to submit PIN from IDLE
        val invalidPin = sm.transition(IncidentEvent.SubmitSafePin("1234"))
        assertTrue(invalidPin is IncidentTransitionResult.Invalid)
        assertEquals(IncidentState.IDLE, sm.currentState)

        // Attempting timer expiry from IDLE
        val invalidTimer = sm.transition(IncidentEvent.GraceTimerExpired)
        assertTrue(invalidTimer is IncidentTransitionResult.Invalid)
        assertEquals(IncidentState.IDLE, sm.currentState)
    }

    @Test
    fun testTransitionAuditHistory() {
        sm.transition(IncidentEvent.TriggerActivation(ActivationMethod.SOS_BUTTON))
        sm.transition(IncidentEvent.SessionCreated)
        sm.transition(IncidentEvent.RequestCancellation)
        sm.transition(IncidentEvent.SubmitSafePin("1234"))
        sm.transition(IncidentEvent.ResolveIncident)

        val history = sm.getTransitionHistory()
        assertEquals(5, history.size)
        assertEquals(IncidentState.IDLE, history[0].fromState)
        assertEquals(IncidentState.STARTING, history[0].toState)
        assertEquals(IncidentState.STARTING, history[1].fromState)
        assertEquals(IncidentState.ACTIVE_GRACE, history[1].toState)
        assertEquals(IncidentState.ACTIVE_GRACE, history[2].fromState)
        assertEquals(IncidentState.CANCEL_AUTHENTICATION, history[2].toState)
        assertEquals(IncidentState.CANCEL_AUTHENTICATION, history[3].fromState)
        assertEquals(IncidentState.SAFE_CANCELLED, history[3].toState)
        assertEquals(IncidentState.SAFE_CANCELLED, history[4].fromState)
        assertEquals(IncidentState.ENDED, history[4].toState)
    }
}
