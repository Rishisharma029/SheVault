package com.shevault.core.database.journal

import com.shevault.core.common.incident.IncidentTimelineEventType
import com.shevault.core.database.dao.DeliveryAttemptDao
import com.shevault.core.database.dao.IncidentDao
import com.shevault.core.database.dao.IncidentEventDao
import com.shevault.core.database.dao.LocationSampleDao
import com.shevault.core.database.dao.SensorSampleDao
import com.shevault.core.database.dao.TrustedContactDao
import com.shevault.core.database.entity.DeliveryAttemptEntity
import com.shevault.core.database.entity.IncidentEntity
import com.shevault.core.database.entity.IncidentEventEntity
import com.shevault.core.database.entity.LocationSampleEntity
import com.shevault.core.database.entity.SensorSampleEntity
import com.shevault.core.database.entity.TrustedContactEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

/**
 * Room-backed implementation of LocalIncidentJournal.
 */
class RoomIncidentJournal(
    private val incidentDao: IncidentDao,
    private val incidentEventDao: IncidentEventDao,
    private val locationSampleDao: LocationSampleDao,
    private val deliveryAttemptDao: DeliveryAttemptDao,
    private val sensorSampleDao: SensorSampleDao,
    private val trustedContactDao: TrustedContactDao? = null
) : LocalIncidentJournal {

    override suspend fun createIncident(
        id: String,
        activationMethod: String,
        batteryAtStart: Int?,
        initialLatitude: Double?,
        initialLongitude: Double?,
        initialAccuracy: Float?,
        createdAt: Long
    ): IncidentEntity {
        val incident = IncidentEntity(
            id = id,
            createdAt = createdAt,
            activationMethod = activationMethod,
            status = "ACTIVE",
            cancelType = null,
            batteryAtStart = batteryAtStart,
            batteryAtEnd = null,
            lastKnownLatitude = initialLatitude,
            lastKnownLongitude = initialLongitude,
            lastKnownAccuracy = initialAccuracy,
            updatedAt = createdAt
        )
        incidentDao.insertIncident(incident)

        // Automatically log audit event
        logEvent(
            incidentId = id,
            type = IncidentTimelineEventType.INCIDENT_CREATED,
            payload = "activationMethod=$activationMethod;battery=$batteryAtStart;lat=$initialLatitude;lon=$initialLongitude",
            timestamp = createdAt
        )

        return incident
    }

    override suspend fun logEvent(
        incidentId: String,
        type: String,
        payload: String?,
        timestamp: Long
    ): IncidentEventEntity {
        val event = IncidentEventEntity(
            id = UUID.randomUUID().toString(),
            incidentId = incidentId,
            timestamp = timestamp,
            type = type,
            payload = payload
        )
        incidentEventDao.insertEvent(event)
        return event
    }

    override suspend fun recordLocation(sample: LocationSampleEntity): LocationSampleEntity {
        locationSampleDao.insertSample(sample)

        val incidentId = sample.incidentId
        if (incidentId != null) {
            incidentDao.updateLastKnownLocation(
                id = incidentId,
                lat = sample.latitude,
                lon = sample.longitude,
                acc = sample.accuracy,
                timestamp = sample.timestamp
            )
            logEvent(
                incidentId = incidentId,
                type = IncidentTimelineEventType.LOCATION_CAPTURED,
                payload = "lat=${sample.latitude};lon=${sample.longitude};acc=${sample.accuracy};speed=${sample.speed};provider=${sample.provider}",
                timestamp = sample.timestamp
            )
        }

        return sample
    }

    override suspend fun recordDeliveryAttempt(attempt: DeliveryAttemptEntity): DeliveryAttemptEntity {
        deliveryAttemptDao.insertAttempt(attempt)
        logEvent(
            incidentId = attempt.incidentId,
            type = IncidentTimelineEventType.ESCALATION_ATTEMPT,
            payload = "channel=${attempt.channel};recipient=${attempt.recipient};status=${attempt.status};count=${attempt.attemptCount}",
            timestamp = attempt.timestamp
        )
        return attempt
    }

    override suspend fun updateDeliveryAttemptStatus(
        attemptId: String,
        incidentId: String,
        status: String,
        error: String?,
        timestamp: Long
    ) {
        deliveryAttemptDao.updateStatus(attemptId, status, error, timestamp)
        if (status == "DELIVERED") {
            logEvent(
                incidentId = incidentId,
                type = IncidentTimelineEventType.ACK_RECEIVED,
                payload = "attemptId=$attemptId;status=$status",
                timestamp = timestamp
            )
        }
    }

    override suspend fun recordSensorSample(sample: SensorSampleEntity): SensorSampleEntity {
        sensorSampleDao.insertSample(sample)
        return sample
    }

    override suspend fun logNetworkTransition(
        incidentId: String,
        isConnected: Boolean,
        details: String?,
        timestamp: Long
    ) {
        val type = if (isConnected) {
            IncidentTimelineEventType.NETWORK_RESTORED
        } else {
            IncidentTimelineEventType.NETWORK_LOST
        }
        logEvent(incidentId = incidentId, type = type, payload = details, timestamp = timestamp)
    }

    override suspend fun cancelIncidentSafe(
        incidentId: String,
        batteryAtEnd: Int?,
        timestamp: Long
    ): IncidentEntity? {
        val existing = incidentDao.getIncidentById(incidentId) ?: return null
        incidentDao.resolveIncident(
            id = incidentId,
            status = "SAFE_RESOLVED",
            cancelType = "SAFE",
            batteryAtEnd = batteryAtEnd,
            timestamp = timestamp
        )
        logEvent(
            incidentId = incidentId,
            type = IncidentTimelineEventType.SAFE_CANCEL,
            payload = "batteryAtEnd=$batteryAtEnd",
            timestamp = timestamp
        )
        logEvent(
            incidentId = incidentId,
            type = IncidentTimelineEventType.INCIDENT_ENDED,
            payload = "outcome=SAFE_RESOLVED",
            timestamp = timestamp
        )
        return incidentDao.getIncidentById(incidentId)
    }

    override suspend fun cancelIncidentDuress(
        incidentId: String,
        batteryAtEnd: Int?,
        timestamp: Long
    ): IncidentEntity? {
        val existing = incidentDao.getIncidentById(incidentId) ?: return null
        incidentDao.resolveIncident(
            id = incidentId,
            status = "COERCED_DURESS",
            cancelType = "DURESS",
            batteryAtEnd = batteryAtEnd,
            timestamp = timestamp
        )
        logEvent(
            incidentId = incidentId,
            type = IncidentTimelineEventType.DURESS_CANCEL,
            payload = "batteryAtEnd=$batteryAtEnd;covertDuress=true",
            timestamp = timestamp
        )
        // Note: INCIDENT_ENDED is NOT logged during duress cancel so backend continues covert dispatch
        return incidentDao.getIncidentById(incidentId)
    }

    override suspend fun endIncident(
        incidentId: String,
        status: String,
        batteryAtEnd: Int?,
        timestamp: Long
    ): IncidentEntity? {
        val existing = incidentDao.getIncidentById(incidentId) ?: return null
        incidentDao.resolveIncident(
            id = incidentId,
            status = status,
            cancelType = null,
            batteryAtEnd = batteryAtEnd,
            timestamp = timestamp
        )
        logEvent(
            incidentId = incidentId,
            type = IncidentTimelineEventType.INCIDENT_ENDED,
            payload = "status=$status;batteryAtEnd=$batteryAtEnd",
            timestamp = timestamp
        )
        return incidentDao.getIncidentById(incidentId)
    }

    override suspend fun getIncident(id: String): IncidentEntity? = incidentDao.getIncidentById(id)

    override fun observeIncident(id: String): Flow<IncidentEntity?> = incidentDao.getIncidentFlow(id)

    override fun observeTimeline(incidentId: String): Flow<List<IncidentEventEntity>> =
        incidentEventDao.getEventsForIncident(incidentId)

    override suspend fun getTimelineSync(incidentId: String): List<IncidentEventEntity> =
        incidentEventDao.getEventsForIncidentSync(incidentId)

    override fun observeLocationSamples(incidentId: String): Flow<List<LocationSampleEntity>> =
        locationSampleDao.getSamplesForIncident(incidentId)

    override fun observeDeliveryAttempts(incidentId: String): Flow<List<DeliveryAttemptEntity>> =
        deliveryAttemptDao.getAttemptsForIncident(incidentId)

    override fun observeSensorSamples(incidentId: String): Flow<List<SensorSampleEntity>> =
        sensorSampleDao.getSamplesForIncident(incidentId)

    override fun observeAllIncidents(): Flow<List<IncidentEntity>> = incidentDao.getAllIncidents()

    override suspend fun saveTrustedContact(contact: TrustedContactEntity) {
        trustedContactDao?.insertContact(contact)
    }

    override suspend fun saveTrustedContacts(contacts: List<TrustedContactEntity>) {
        trustedContactDao?.insertContacts(contacts)
    }

    override suspend fun getTrustedContact(id: String): TrustedContactEntity? =
        trustedContactDao?.getContactById(id)

    override suspend fun getPrimaryContacts(): List<TrustedContactEntity> =
        trustedContactDao?.getPrimaryContacts() ?: emptyList()

    override fun observeTrustedContacts(): Flow<List<TrustedContactEntity>> =
        trustedContactDao?.getAllContacts() ?: flowOf(emptyList())

    override suspend fun deleteTrustedContact(id: String) {
        trustedContactDao?.deleteContact(id)
    }
}
