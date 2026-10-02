package com.shevault.feature.incident.session

import com.shevault.core.database.IncidentDao
import com.shevault.feature.incident.model.ActivationMethod
import com.shevault.feature.incident.model.BatterySnapshot
import com.shevault.feature.incident.model.IncidentSession
import com.shevault.feature.incident.model.LocationSnapshot
import com.shevault.feature.incident.model.PersistentIncidentStatus
import com.shevault.feature.incident.statemachine.IncidentEvent
import com.shevault.feature.incident.statemachine.IncidentState
import com.shevault.feature.incident.statemachine.IncidentStateMachine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Interface to supply hardware identifier.
 */
fun interface DeviceIdProvider {
    fun getDeviceId(): String
}

/**
 * Interface to supply current battery telemetry snapshot.
 */
fun interface BatterySnapshotProvider {
    fun getBatterySnapshot(): BatterySnapshot
}

/**
 * Interface to supply current location fix snapshot.
 */
fun interface LocationSnapshotProvider {
    fun getLocationSnapshot(): LocationSnapshot?
}

/**
 * IncidentSessionManager: The central engine managing emergency incident sessions.
 *
 * Implements Task 8 & 9 requirements:
 * 1. Creates full IncidentSession as soon as activation occurs.
 * 2. Generates: incidentId, sessionId, createdAt, activationMethod, deviceId, initial battery, initial location.
 * 3. Manages persistent state: INCIDENT_CREATED -> then: ACTIVE (not simple boolean sos = true).
 * 4. Coordinates with deterministic IncidentStateMachine and persistent Room DAO.
 */
class IncidentSessionManager(
    val stateMachine: IncidentStateMachine = IncidentStateMachine(),
    private val incidentDao: IncidentDao? = null,
    private val deviceIdProvider: DeviceIdProvider = DeviceIdProvider { "DEV-" + UUID.randomUUID().toString().take(8) },
    private val batteryProvider: BatterySnapshotProvider = BatterySnapshotProvider {
        BatterySnapshot(levelPercent = 88, isCharging = false)
    },
    private val locationProvider: LocationSnapshotProvider = LocationSnapshotProvider {
        LocationSnapshot(
            latitude = 28.6139,
            longitude = 77.2090,
            accuracyMeters = 3.5f,
            timestamp = System.currentTimeMillis()
        )
    },
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val _currentSession = MutableStateFlow<IncidentSession?>(null)
    val currentSession: StateFlow<IncidentSession?> = _currentSession.asStateFlow()

    /**
     * Creates a new persistent incident session on emergency activation.
     * Generates all required telemetry tokens and sets state to INCIDENT_CREATED.
     */
    @Synchronized
    fun createIncident(
        activationMethod: ActivationMethod,
        overrideBattery: BatterySnapshot? = null,
        overrideLocation: LocationSnapshot? = null
    ): IncidentSession {
        val now = System.currentTimeMillis()
        val battery = overrideBattery ?: batteryProvider.getBatterySnapshot()
        val location = overrideLocation ?: locationProvider.getLocationSnapshot()
        val deviceId = deviceIdProvider.getDeviceId()

        val session = IncidentSession(
            incidentId = UUID.randomUUID().toString(),
            sessionId = UUID.randomUUID().toString(),
            createdAt = now,
            activationMethod = activationMethod,
            deviceId = deviceId,
            initialBattery = battery,
            initialLocation = location,
            currentState = IncidentState.STARTING.name,
            persistentStatus = PersistentIncidentStatus.INCIDENT_CREATED,
            isCoercedDuress = false,
            updatedAt = now
        )

        _currentSession.value = session
        stateMachine.transition(
            IncidentEvent.TriggerActivation(
                method = activationMethod,
                battery = battery,
                location = location
            )
        )

        persistSession(session)
        return session
    }

    /**
     * Transitions persistent incident state from INCIDENT_CREATED to ACTIVE.
     */
    @Synchronized
    fun activateSession(): IncidentSession? {
        val existing = _currentSession.value ?: return null
        val updated = existing.copy(
            persistentStatus = PersistentIncidentStatus.ACTIVE,
            currentState = IncidentState.ACTIVE_GRACE.name,
            updatedAt = System.currentTimeMillis()
        )
        _currentSession.value = updated
        stateMachine.transition(IncidentEvent.SessionCreated)
        persistSession(updated)
        return updated
    }

    /**
     * Marks session as cancelled by Safe PIN/Biometric authentication.
     */
    @Synchronized
    fun markSafeCancelled(): IncidentSession? {
        val existing = _currentSession.value ?: return null
        val updated = existing.copy(
            persistentStatus = PersistentIncidentStatus.SAFE_RESOLVED,
            currentState = IncidentState.SAFE_CANCELLED.name,
            updatedAt = System.currentTimeMillis()
        )
        _currentSession.value = updated
        stateMachine.transition(IncidentEvent.SubmitSafePin())
        persistSession(updated)
        return updated
    }

    /**
     * Marks session as cancelled via Duress PIN (COERCED_DURESS).
     * Silently records covert duress while presenting normal cancellation to UI.
     */
    @Synchronized
    fun markDuressCancelled(): IncidentSession? {
        val existing = _currentSession.value ?: return null
        val updated = existing.copy(
            persistentStatus = PersistentIncidentStatus.COERCED_DURESS,
            currentState = IncidentState.DURESS_CANCELLED.name,
            isCoercedDuress = true,
            updatedAt = System.currentTimeMillis()
        )
        _currentSession.value = updated
        stateMachine.transition(IncidentEvent.SubmitDuressPin())
        persistSession(updated)
        return updated
    }

    /**
     * Escalates session when grace period countdown expires.
     */
    @Synchronized
    fun markEscalating(): IncidentSession? {
        val existing = _currentSession.value ?: return null
        val updated = existing.copy(
            persistentStatus = PersistentIncidentStatus.ACTIVE,
            currentState = IncidentState.ESCALATING.name,
            updatedAt = System.currentTimeMillis()
        )
        _currentSession.value = updated
        stateMachine.transition(IncidentEvent.GraceTimerExpired)
        persistSession(updated)
        return updated
    }

    /**
     * Updates session to reflect state machine transitions.
     */
    @Synchronized
    fun syncWithStateMachine(newState: IncidentState): IncidentSession? {
        val existing = _currentSession.value ?: return null
        val updated = existing.copy(
            currentState = newState.name,
            updatedAt = System.currentTimeMillis()
        )
        _currentSession.value = updated
        persistSession(updated)
        return updated
    }

    private fun persistSession(session: IncidentSession) {
        if (incidentDao != null) {
            scope.launch {
                runCatching {
                    incidentDao.insertSession(session.toEntity())
                }
            }
        }
    }

    @Synchronized
    fun clearSession() {
        _currentSession.value = null
        stateMachine.reset()
    }
}
