package com.shevault.feature.incident

import com.shevault.feature.incident.model.ActivationMethod
import com.shevault.feature.incident.model.BatterySnapshot
import com.shevault.feature.incident.model.IncidentSession
import com.shevault.feature.incident.model.LocationSnapshot
import com.shevault.feature.incident.model.PersistentIncidentStatus
import com.shevault.feature.incident.session.IncidentSessionManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IncidentSessionTest {

    private lateinit var sessionManager: IncidentSessionManager

    @Before
    fun setup() {
        sessionManager = IncidentSessionManager(
            deviceIdProvider = { "MOCK-DEVICE-ABC123" },
            batteryProvider = { BatterySnapshot(levelPercent = 75, isCharging = true, temperatureCelsius = 31.5f) },
            locationProvider = {
                LocationSnapshot(
                    latitude = 12.9716,
                    longitude = 77.5946,
                    accuracyMeters = 2.4f,
                    altitudeMeters = 920.0
                )
            }
        )
    }

    @Test
    fun testCreateIncidentGeneratesAllRequiredTelemetry() {
        val session = sessionManager.createIncident(ActivationMethod.PANIC_GESTURE)

        assertNotNull("incidentId must be generated", session.incidentId)
        assertTrue("incidentId must not be blank", session.incidentId.isNotBlank())

        assertNotNull("sessionId must be generated", session.sessionId)
        assertTrue("sessionId must not be blank", session.sessionId.isNotBlank())

        assertTrue("createdAt must be a valid epoch timestamp", session.createdAt > 0L)
        assertEquals(ActivationMethod.PANIC_GESTURE, session.activationMethod)
        assertEquals("MOCK-DEVICE-ABC123", session.deviceId)

        // Initial battery telemetry
        assertEquals(75, session.initialBattery.levelPercent)
        assertTrue(session.initialBattery.isCharging)
        assertEquals(31.5f, session.initialBattery.temperatureCelsius)

        // Initial location telemetry
        assertNotNull(session.initialLocation)
        assertEquals(12.9716, session.initialLocation?.latitude ?: 0.0, 0.0001)
        assertEquals(77.5946, session.initialLocation?.longitude ?: 0.0, 0.0001)
        assertEquals(2.4f, session.initialLocation?.accuracyMeters ?: 0f, 0.01f)

        // Initial persistent state
        assertEquals(PersistentIncidentStatus.INCIDENT_CREATED, session.persistentStatus)
        assertFalse(session.isCoercedDuress)
    }

    @Test
    fun testPersistentStateTransitionsFromCreatedToActive() {
        val createdSession = sessionManager.createIncident(ActivationMethod.SOS_BUTTON)
        assertEquals(PersistentIncidentStatus.INCIDENT_CREATED, createdSession.persistentStatus)

        // Transition from INCIDENT_CREATED -> ACTIVE
        val activeSession = sessionManager.activateSession()
        assertNotNull(activeSession)
        assertEquals(PersistentIncidentStatus.ACTIVE, activeSession?.persistentStatus)
        assertEquals("ACTIVE_GRACE", activeSession?.currentState)
    }

    @Test
    fun testDuressCancellationSetsCoercedDuressPersistentState() {
        sessionManager.createIncident(ActivationMethod.PANIC_GESTURE)
        sessionManager.activateSession()

        val duressSession = sessionManager.markDuressCancelled()
        assertNotNull(duressSession)
        assertEquals(PersistentIncidentStatus.COERCED_DURESS, duressSession?.persistentStatus)
        assertTrue("isCoercedDuress must be set to true on duress PIN", duressSession?.isCoercedDuress == true)
        assertEquals("DURESS_CANCELLED", duressSession?.currentState)
    }

    @Test
    fun testSafeCancellationSetsSafeResolvedPersistentState() {
        sessionManager.createIncident(ActivationMethod.SOS_BUTTON)
        sessionManager.activateSession()

        val safeSession = sessionManager.markSafeCancelled()
        assertNotNull(safeSession)
        assertEquals(PersistentIncidentStatus.SAFE_RESOLVED, safeSession?.persistentStatus)
        assertFalse(safeSession?.isCoercedDuress == true)
        assertEquals("SAFE_CANCELLED", safeSession?.currentState)
    }

    @Test
    fun testEntityConversionPreservesAllTokens() {
        val original = sessionManager.createIncident(ActivationMethod.HARDWARE_KEY)
        val entity = original.toEntity()

        assertEquals(original.incidentId, entity.incidentId)
        assertEquals(original.sessionId, entity.sessionId)
        assertEquals(original.createdAt, entity.createdAt)
        assertEquals(original.activationMethod.name, entity.activationMethod)
        assertEquals(original.deviceId, entity.deviceId)
        assertEquals(original.initialBattery.levelPercent, entity.initialBatteryLevel)
        assertEquals(original.initialBattery.isCharging, entity.initialBatteryCharging)
        assertEquals(original.initialLocation?.latitude, entity.initialLatitude)
        assertEquals(original.initialLocation?.longitude, entity.initialLongitude)
        assertEquals(original.initialLocation?.accuracyMeters, entity.initialAccuracy)
        assertEquals(original.persistentStatus.name, entity.persistentStatus)
        assertEquals(original.isCoercedDuress, entity.isCoercedDuress)

        val restored = IncidentSession.fromEntity(entity)
        assertEquals(original.incidentId, restored.incidentId)
        assertEquals(original.sessionId, restored.sessionId)
        assertEquals(original.activationMethod, restored.activationMethod)
        assertEquals(original.persistentStatus, restored.persistentStatus)
        assertEquals(original.initialBattery.levelPercent, restored.initialBattery.levelPercent)
        assertEquals(original.initialLocation?.latitude, restored.initialLocation?.latitude)
    }
}
