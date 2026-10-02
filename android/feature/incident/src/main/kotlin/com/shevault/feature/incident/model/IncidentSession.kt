package com.shevault.feature.incident.model

import com.shevault.core.database.IncidentSessionEntity

/**
 * Triggers that can activate an emergency incident.
 */
enum class ActivationMethod {
    PANIC_GESTURE,
    SOS_BUTTON,
    HARDWARE_KEY,
    SHAKE_SENSOR,
    FALL_DETECTION,
    VOICE_TRIGGER,
    SAFE_ROUTE_ANOMALY
}

/**
 * Snapshot of device battery telemetry at the moment of incident initiation.
 */
data class BatterySnapshot(
    val levelPercent: Int,
    val isCharging: Boolean,
    val temperatureCelsius: Float? = null
)

/**
 * Snapshot of geospatial coordinates and accuracy at the moment of incident initiation.
 */
data class LocationSnapshot(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val altitudeMeters: Double? = null,
    val provider: String = "fused",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Persistent lifecycle status for an incident session.
 * Used for persistent storage, backend synchronization, and forensic auditing.
 */
enum class PersistentIncidentStatus {
    INCIDENT_CREATED,
    ACTIVE,
    SAFE_RESOLVED,
    COERCED_DURESS,
    CANCELLED,
    FAILED
}

/**
 * Internal Incident Session: The authoritative runtime state of an emergency event.
 *
 * Generated immediately upon safety activation with:
 * - incidentId: Unique UUID representing the emergency incident
 * - sessionId: Unique UUID for the telemetry session
 * - createdAt: Epoch timestamp
 * - activationMethod: Secondary panic gesture, hardware trigger, or direct UI button
 * - deviceId: Cryptographic device/hardware identifier
 * - initialBattery: Device power telemetry snapshot
 * - initialLocation: GPS breadcrumb fix snapshot
 * - persistentStatus: INCIDENT_CREATED -> ACTIVE -> SAFE_RESOLVED / COERCED_DURESS
 * - isCoercedDuress: Covert duress tracking flag
 */
data class IncidentSession(
    val incidentId: String,
    val sessionId: String,
    val createdAt: Long,
    val activationMethod: ActivationMethod,
    val deviceId: String,
    val initialBattery: BatterySnapshot,
    val initialLocation: LocationSnapshot?,
    val currentState: String = "STARTING",
    val persistentStatus: PersistentIncidentStatus = PersistentIncidentStatus.INCIDENT_CREATED,
    val isCoercedDuress: Boolean = false,
    val updatedAt: Long = createdAt
) {
    fun toEntity(): IncidentSessionEntity = IncidentSessionEntity(
        incidentId = incidentId,
        sessionId = sessionId,
        createdAt = createdAt,
        activationMethod = activationMethod.name,
        deviceId = deviceId,
        initialBatteryLevel = initialBattery.levelPercent,
        initialBatteryCharging = initialBattery.isCharging,
        initialLatitude = initialLocation?.latitude,
        initialLongitude = initialLocation?.longitude,
        initialAccuracy = initialLocation?.accuracyMeters,
        currentState = currentState,
        persistentStatus = persistentStatus.name,
        isCoercedDuress = isCoercedDuress,
        updatedAt = updatedAt
    )

    companion object {
        fun fromEntity(entity: IncidentSessionEntity): IncidentSession {
            val method = runCatching {
                ActivationMethod.valueOf(entity.activationMethod)
            }.getOrDefault(ActivationMethod.SOS_BUTTON)

            val status = runCatching {
                PersistentIncidentStatus.valueOf(entity.persistentStatus)
            }.getOrDefault(PersistentIncidentStatus.INCIDENT_CREATED)

            val lat = entity.initialLatitude
            val lon = entity.initialLongitude
            val location = if (lat != null && lon != null) {
                LocationSnapshot(
                    latitude = lat,
                    longitude = lon,
                    accuracyMeters = entity.initialAccuracy ?: 0f,
                    timestamp = entity.createdAt
                )
            } else null

            return IncidentSession(
                incidentId = entity.incidentId,
                sessionId = entity.sessionId,
                createdAt = entity.createdAt,
                activationMethod = method,
                deviceId = entity.deviceId,
                initialBattery = BatterySnapshot(
                    levelPercent = entity.initialBatteryLevel,
                    isCharging = entity.initialBatteryCharging
                ),
                initialLocation = location,
                currentState = entity.currentState,
                persistentStatus = status,
                isCoercedDuress = entity.isCoercedDuress,
                updatedAt = entity.updatedAt
            )
        }
    }
}
