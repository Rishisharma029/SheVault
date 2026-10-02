package com.shevault.core.database.journal

import com.shevault.core.database.entity.DeliveryAttemptEntity
import com.shevault.core.database.entity.IncidentEntity
import com.shevault.core.database.entity.IncidentEventEntity
import com.shevault.core.database.entity.LocationSampleEntity
import com.shevault.core.database.entity.SensorSampleEntity
import com.shevault.core.database.entity.TrustedContactEntity
import kotlinx.coroutines.flow.Flow

/**
 * Local Incident Journal: Authoritative offline-first journal of emergency incidents.
 *
 * Implements Task 12 requirement:
 * Backend-independent feature providing an auditable incident timeline capturing:
 * - INCIDENT_CREATED
 * - LOCATION_CAPTURED
 * - NETWORK_LOST
 * - NETWORK_RESTORED
 * - ESCALATION_ATTEMPT
 * - ACK_RECEIVED
 * - SAFE_CANCEL
 * - DURESS_CANCEL
 * - INCIDENT_ENDED
 */
interface LocalIncidentJournal {

    /**
     * Creates and records an incident, immediately logging an INCIDENT_CREATED timeline event.
     */
    suspend fun createIncident(
        id: String,
        activationMethod: String,
        batteryAtStart: Int? = null,
        initialLatitude: Double? = null,
        initialLongitude: Double? = null,
        initialAccuracy: Float? = null,
        createdAt: Long = System.currentTimeMillis()
    ): IncidentEntity

    /**
     * Appends a granular auditable event to the incident timeline.
     */
    suspend fun logEvent(
        incidentId: String,
        type: String,
        payload: String? = null,
        timestamp: Long = System.currentTimeMillis()
    ): IncidentEventEntity

    /**
     * Records a location breadcrumb fix, updates the parent incident's last known position,
     * and records a LOCATION_CAPTURED timeline event.
     */
    suspend fun recordLocation(sample: LocationSampleEntity): LocationSampleEntity

    /**
     * Records an outbound emergency dispatch attempt and logs ESCALATION_ATTEMPT / ACK_RECEIVED.
     */
    suspend fun recordDeliveryAttempt(attempt: DeliveryAttemptEntity): DeliveryAttemptEntity

    /**
     * Updates delivery attempt status (e.g. ACK_RECEIVED).
     */
    suspend fun updateDeliveryAttemptStatus(
        attemptId: String,
        incidentId: String,
        status: String,
        error: String? = null,
        timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Records a sensor reading (accelerometer, barometer, etc.).
     */
    suspend fun recordSensorSample(sample: SensorSampleEntity): SensorSampleEntity

    /**
     * Records network connectivity transition (NETWORK_LOST or NETWORK_RESTORED).
     */
    suspend fun logNetworkTransition(
        incidentId: String,
        isConnected: Boolean,
        details: String? = null,
        timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Marks incident as safely resolved with user PIN/biometrics (SAFE_CANCEL).
     */
    suspend fun cancelIncidentSafe(
        incidentId: String,
        batteryAtEnd: Int? = null,
        timestamp: Long = System.currentTimeMillis()
    ): IncidentEntity?

    /**
     * Marks incident as cancelled under duress (DURESS_CANCEL).
     */
    suspend fun cancelIncidentDuress(
        incidentId: String,
        batteryAtEnd: Int? = null,
        timestamp: Long = System.currentTimeMillis()
    ): IncidentEntity?

    /**
     * Marks incident lifecycle as concluded (INCIDENT_ENDED).
     */
    suspend fun endIncident(
        incidentId: String,
        status: String = "ENDED",
        batteryAtEnd: Int? = null,
        timestamp: Long = System.currentTimeMillis()
    ): IncidentEntity?

    suspend fun getIncident(id: String): IncidentEntity?
    fun observeIncident(id: String): Flow<IncidentEntity?>
    fun observeTimeline(incidentId: String): Flow<List<IncidentEventEntity>>
    suspend fun getTimelineSync(incidentId: String): List<IncidentEventEntity>
    fun observeLocationSamples(incidentId: String): Flow<List<LocationSampleEntity>>
    fun observeDeliveryAttempts(incidentId: String): Flow<List<DeliveryAttemptEntity>>
    fun observeSensorSamples(incidentId: String): Flow<List<SensorSampleEntity>>
    fun observeAllIncidents(): Flow<List<IncidentEntity>>

    // Trusted contacts management
    suspend fun saveTrustedContact(contact: TrustedContactEntity)
    suspend fun saveTrustedContacts(contacts: List<TrustedContactEntity>)
    suspend fun getTrustedContact(id: String): TrustedContactEntity?
    suspend fun getPrimaryContacts(): List<TrustedContactEntity>
    fun observeTrustedContacts(): Flow<List<TrustedContactEntity>>
    suspend fun deleteTrustedContact(id: String)
}
