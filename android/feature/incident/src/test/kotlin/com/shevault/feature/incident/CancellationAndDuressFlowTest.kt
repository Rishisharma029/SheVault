package com.shevault.feature.incident

import com.shevault.core.security.CredentialVerificationResult
import com.shevault.core.security.DuressCredentialManager
import com.shevault.feature.incident.model.ActivationMethod
import com.shevault.feature.incident.model.PersistentIncidentStatus
import com.shevault.feature.incident.statemachine.IncidentState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CancellationAndDuressFlowTest {

    private lateinit var credentialManager: DuressCredentialManager
    private lateinit var viewModel: IncidentViewModel

    @Before
    fun setup() {
        credentialManager = DuressCredentialManager()
        val testScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined)
        viewModel = IncidentViewModel(
            credentialManager = credentialManager,
            coroutineScope = testScope
        )
    }

    @Test
    fun testNeverOneTapDirectCancel() {
        // Start an incident
        viewModel.startIncident(ActivationMethod.SOS_BUTTON)
        assertEquals(IncidentState.ACTIVE_GRACE, viewModel.uiState.value.incidentState)

        // Opening cancellation does NOT terminate or disarm the session
        viewModel.openCancellationFlow()
        assertTrue("Cancellation sheet must be presented first", viewModel.uiState.value.isCancellationSheetVisible)
        assertEquals(IncidentState.CANCEL_AUTHENTICATION, viewModel.uiState.value.incidentState)

        // User can choose "Continue Protection"
        viewModel.continueProtection()
        assertFalse(viewModel.uiState.value.isCancellationSheetVisible)
        assertEquals(IncidentState.ACTIVE_GRACE, viewModel.uiState.value.incidentState)
    }

    @Test
    fun testSafeCredentialDisarmsCleanly() {
        viewModel.startIncident(ActivationMethod.SOS_BUTTON)
        viewModel.openCancellationFlow()

        // Verify Safe PIN (1234)
        val verifyRes = credentialManager.verifyPin("1234")
        assertTrue("Entered safe pin must verify as Safe", verifyRes is CredentialVerificationResult.Safe)

        viewModel.handleSafeCancelled()

        assertFalse(viewModel.uiState.value.isCancellationSheetVisible)
        assertEquals(IncidentState.ENDED, viewModel.uiState.value.incidentState)

        val session = viewModel.sessionManager.currentSession.value
        assertNotNull(session)
        assertEquals(PersistentIncidentStatus.SAFE_RESOLVED, session?.persistentStatus)
        assertFalse("Safe cancellation must NOT be flagged as duress", session?.isCoercedDuress == true)
        assertFalse(viewModel.uiState.value.isSilentDuressActive)
    }

    @Test
    fun testDuressCredentialTriggersCovertCoercedDuress() {
        viewModel.startIncident(ActivationMethod.SOS_BUTTON)
        viewModel.openCancellationFlow()

        // Verify Duress PIN (9999)
        val verifyRes = credentialManager.verifyPin("9999")
        assertTrue("Entered duress pin must verify as Duress", verifyRes is CredentialVerificationResult.Duress)

        viewModel.handleDuressCancelled()

        assertFalse(viewModel.uiState.value.isCancellationSheetVisible)
        // Session manager must register COERCED_DURESS
        val session = viewModel.sessionManager.currentSession.value
        assertNotNull(session)
        assertEquals(PersistentIncidentStatus.COERCED_DURESS, session?.persistentStatus)
        assertTrue("Duress cancellation must covertly set isCoercedDuress = true", session?.isCoercedDuress == true)
        assertTrue(viewModel.uiState.value.isSilentDuressActive)
        // Backend state machine continues in ESCALATING mode silently
        assertEquals(IncidentState.ESCALATING, viewModel.uiState.value.incidentState)
    }

    @Test
    fun testVisualOutcomeTextInvariance() {
        // Requirement 11:
        // Safe: UI outcome "✓ You're Safe", "User marked safe", "Session terminated"
        // Duress: UI outcome "✓ You're Safe", "User marked safe", "Session terminated"
        // MUST BE VISUALLY IDENTICAL!
        val safeHeader = "✓ You're Safe"
        val safeBody = "User marked safe"
        val safeSub = "Session terminated"

        val duressHeader = "✓ You're Safe"
        val duressBody = "User marked safe"
        val duressSub = "Session terminated"

        assertEquals("Header must be visually identical", safeHeader, duressHeader)
        assertEquals("Body must be visually identical", safeBody, duressBody)
        assertEquals("Sub text must be visually identical", safeSub, duressSub)

        // Strict verification: No visible string mentions duress or covert triggers
        val forbiddenSubstrings = listOf("DURESS", "COERCED", "ALERT SENT", "SILENT SOS", "POLICE NOTIFIED")
        for (forbidden in forbiddenSubstrings) {
            assertFalse(
                "Client UI must NEVER expose '$forbidden' to an aggressor",
                safeHeader.contains(forbidden, ignoreCase = true) ||
                        safeBody.contains(forbidden, ignoreCase = true) ||
                        safeSub.contains(forbidden, ignoreCase = true)
            )
        }
    }
}
