package com.shevault.core.database.journal

import com.shevault.core.common.incident.IncidentTimelineEventType
import com.shevault.core.database.entity.DeliveryAttemptEntity
import com.shevault.core.database.entity.IncidentEntity
import com.shevault.core.database.entity.IncidentEventEntity
import com.shevault.core.database.entity.LocationSampleEntity
import com.shevault.core.database.entity.SensorSampleEntity
import com.shevault.core.database.entity.TrustedContactEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe, reactive in-memory implementation of LocalIncidentJournal.
 * Suitable for unit tests, offline emulation, and decoupled preview runs.
 */
class InMemoryIncidentJournal : LocalIncidentJournal {

    private val mutex = Mutex()
    private val incidents = ConcurrentHashMap<String, IncidentEntity>()
    private val events = ConcurrentHashMap<String, MutableList<IncidentEventEntity>>()
    private val locationSamples = ConcurrentHashMap<String, MutableList<LocationSampleEntity>>()
    private val deliveryAttempts = ConcurrentHashMap<String, MutableList<DeliveryAttemptEntity>>()
    private val sensorSamples = ConcurrentHashMap<String, MutableList<SensorSampleEntity>>()
    private val trustedContacts = ConcurrentHashMap<String, TrustedContactEntity>()

    private val incidentsFlow = MutableStateFlow<Map<String, IncidentEntity>>(emptyMap())
    private val eventsFlow = MutableStateFlow<Map<String, List<IncidentEventEntity>>>(emptyMap())
    private val locationsFlow = MutableStateFlow<Map<String, List<LocationSampleEntity>>>(emptyMap())
    private val deliveryFlow = MutableStateFlow<Map<String, List<DeliveryAttemptEntity>>>(emptyMap())
    private val sensorsFlow = MutableStateFlow<Map<String, List<SensorSampleEntity>>>(emptyMap())
    private val contactsFlow = MutableStateFlow<List<TrustedContactEntity>>(emptyList())

    private fun syncState() {
        incidentsFlow.value = HashMap(incidents)
        eventsFlow.value = events.mapValues { ArrayList(it.value) }
        locationsFlow.value = locationSamples.mapValues { ArrayList(it.value) }
        deliveryFlow.value = deliveryAttempts.mapValues { ArrayList(it.value) }
        sensorsFlow.value = sensorSamples.mapValues { ArrayList(it.value) }
        contactsFlow.value = trustedContacts.values.sortedWith(
            compareByDescending<TrustedContactEntity> { it.isPrimary }.thenBy { it.name }
        )
    }

    override suspend fun createIncident(
        id: String,
        activationMethod: String,
        batteryAtStart: Int?,
        initialLatitude: Double?,
        initialLongitude: Double?,
        initialAccuracy: Float?,
        createdAt: Long
    ): IncidentEntity = mutex.withLock {
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
        incidents[id] = incident
        events.putIfAbsent(id, ArrayList())
        locationSamples.putIfAbsent(id, ArrayList())
        deliveryAttempts.putIfAbsent(id, ArrayList())
        sensorSamples.putIfAbsent(id, ArrayList())

        val event = IncidentEventEntity(
            id = UUID.randomUUID().toString(),
            incidentId = id,
            timestamp = createdAt,
            type = IncidentTimelineEventType.INCIDENT_CREATED,
            payload = "activationMethod=$activationMethod;battery=$batteryAtStart;lat=$initialLatitude;lon=$initialLongitude"
        )
        events[id]?.add(event)
        syncState()
        incident
    }

    override suspend fun logEvent(
        incidentId: String,
        type: String,
        payload: String?,
        timestamp: Long
    ): IncidentEventEntity = mutex.withLock {
        val event = IncidentEventEntity(
            id = UUID.randomUUID().toString(),
            incidentId = incidentId,
            timestamp = timestamp,
            type = type,
            payload = payload
        )
        events.getOrPut(incidentId) { ArrayList() }.add(event)
        syncState()
        event
    }

    override suspend fun recordLocation(sample: LocationSampleEntity): LocationSampleEntity = mutex.withLock {
        val incidentId = sample.incidentId
        if (incidentId != null) {
            locationSamples.getOrPut(incidentId) { ArrayList() }.add(sample)
            incidents[incidentId]?.let { existing ->
                incidents[incidentId] = existing.copy(
                    lastKnownLatitude = sample.latitude,
                    lastKnownLongitude = sample.longitude,
                    lastKnownAccuracy = sample.accuracy,
                    updatedAt = sample.timestamp
                )
            }
            val event = IncidentEventEntity(
                id = UUID.randomUUID().toString(),
                incidentId = incidentId,
                timestamp = sample.timestamp,
                type = IncidentTimelineEventType.LOCATION_CAPTURED,
                payload = "lat=${sample.latitude};lon=${sample.longitude};acc=${sample.accuracy};speed=${sample.speed};provider=${sample.provider}"
            )
            events.getOrPut(incidentId) { ArrayList() }.add(event)
        } else {
            // Track unassociated samples under empty string key
            locationSamples.getOrPut("") { ArrayList() }.add(sample)
        }
        syncState()
        sample
    }

    override suspend fun recordDeliveryAttempt(attempt: DeliveryAttemptEntity): DeliveryAttemptEntity = mutex.withLock {
        deliveryAttempts.getOrPut(attempt.incidentId) { ArrayList() }.add(attempt)
        val event = IncidentEventEntity(
            id = UUID.randomUUID().toString(),
            incidentId = attempt.incidentId,
            timestamp = attempt.timestamp,
            type = IncidentTimelineEventType.ESCALATION_ATTEMPT,
            payload = "channel=${attempt.channel};recipient=${attempt.recipient};status=${attempt.status};count=${attempt.attemptCount}"
        )
        events.getOrPut(attempt.incidentId) { ArrayList() }.add(event)
        syncState()
        attempt
    }

    override suspend fun updateDeliveryAttemptStatus(
        attemptId: String,
        incidentId: String,
        status: String,
        error: String?,
        timestamp: Long
    ) = mutex.withLock {
        val list = deliveryAttempts[incidentId]
        val index = list?.indexOfFirst { it.id == attemptId } ?: -1
        if (index != -1 && list != null) {
            val item = list[index]
            list[index] = item.copy(status = status, error = error, timestamp = timestamp, attemptCount = item.attemptCount + 1)
        }
        if (status == "DELIVERED") {
            val event = IncidentEventEntity(
                id = UUID.randomUUID().toString(),
                incidentId = incidentId,
                timestamp = timestamp,
                type = IncidentTimelineEventType.ACK_RECEIVED,
                payload = "attemptId=$attemptId;status=$status"
            )
            events.getOrPut(incidentId) { ArrayList() }.add(event)
        }
        syncState()
    }

    override suspend fun recordSensorSample(sample: SensorSampleEntity): SensorSampleEntity = mutex.withLock {
        sensorSamples.getOrPut(sample.incidentId) { ArrayList() }.add(sample)
        syncState()
        sample
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
    ): IncidentEntity? = mutex.withLock {
        val existing = incidents[incidentId] ?: return@withLock null
        val updated = existing.copy(
            status = "SAFE_RESOLVED",
            cancelType = "SAFE",
            batteryAtEnd = batteryAtEnd,
            updatedAt = timestamp
        )
        incidents[incidentId] = updated
        val cancelEvent = IncidentEventEntity(
            id = UUID.randomUUID().toString(),
            incidentId = incidentId,
            timestamp = timestamp,
            type = IncidentTimelineEventType.SAFE_CANCEL,
            payload = "batteryAtEnd=$batteryAtEnd"
        )
        val endEvent = IncidentEventEntity(
            id = UUID.randomUUID().toString(),
            incidentId = incidentId,
            timestamp = timestamp,
            type = IncidentTimelineEventType.INCIDENT_ENDED,
            payload = "outcome=SAFE_RESOLVED"
        )
        events.getOrPut(incidentId) { ArrayList() }.add(cancelEvent)
        events.getOrPut(incidentId) { ArrayList() }.add(endEvent)
        syncState()
        updated
    }

    override suspend fun cancelIncidentDuress(
        incidentId: String,
        batteryAtEnd: Int?,
        timestamp: Long
    ): IncidentEntity? = mutex.withLock {
        val existing = incidents[incidentId] ?: return@withLock null
        val updated = existing.copy(
            status = "COERCED_DURESS",
            cancelType = "DURESS",
            batteryAtEnd = batteryAtEnd,
            updatedAt = timestamp
        )
        incidents[incidentId] = updated
        val cancelEvent = IncidentEventEntity(
            id = UUID.randomUUID().toString(),
            incidentId = incidentId,
            timestamp = timestamp,
            type = IncidentTimelineEventType.DURESS_CANCEL,
            payload = "batteryAtEnd=$batteryAtEnd;covertDuress=true"
        )
        // Covert: do NOT add INCIDENT_ENDED event
        events.getOrPut(incidentId) { ArrayList() }.add(cancelEvent)
        syncState()
        updated
    }

    override suspend fun endIncident(
        incidentId: String,
        status: String,
        batteryAtEnd: Int?,
        timestamp: Long
    ): IncidentEntity? = mutex.withLock {
        val existing = incidents[incidentId] ?: return@withLock null
        val updated = existing.copy(
            status = status,
            batteryAtEnd = batteryAtEnd,
            updatedAt = timestamp
        )
        incidents[incidentId] = updated
        val endEvent = IncidentEventEntity(
            id = UUID.randomUUID().toString(),
            incidentId = incidentId,
            timestamp = timestamp,
            type = IncidentTimelineEventType.INCIDENT_ENDED,
            payload = "status=$status;batteryAtEnd=$batteryAtEnd"
        )
        events.getOrPut(incidentId) { ArrayList() }.add(endEvent)
        syncState()
        updated
    }

    override suspend fun getIncident(id: String): IncidentEntity? = incidents[id]

    override fun observeIncident(id: String): Flow<IncidentEntity?> =
        incidentsFlow.map { it[id] }

    override fun observeTimeline(incidentId: String): Flow<List<IncidentEventEntity>> =
        eventsFlow.map { it[incidentId]?.toList() ?: emptyList() }

    override suspend fun getTimelineSync(incidentId: String): List<IncidentEventEntity> =
        events[incidentId]?.toList() ?: emptyList()

    override fun observeLocationSamples(incidentId: String): Flow<List<LocationSampleEntity>> =
        locationsFlow.map { it[incidentId]?.toList() ?: emptyList() }

    override fun observeDeliveryAttempts(incidentId: String): Flow<List<DeliveryAttemptEntity>> =
        deliveryFlow.map { it[incidentId]?.toList() ?: emptyList() }

    override fun observeSensorSamples(incidentId: String): Flow<List<SensorSampleEntity>> =
        sensorsFlow.map { it[incidentId]?.toList() ?: emptyList() }

    override fun observeAllIncidents(): Flow<List<IncidentEntity>> =
        incidentsFlow.map { it.values.sortedByDescending { inc -> inc.createdAt } }

    override suspend fun saveTrustedContact(contact: TrustedContactEntity) = mutex.withLock {
        trustedContacts[contact.id] = contact
        syncState()
    }

    override suspend fun saveTrustedContacts(contacts: List<TrustedContactEntity>) = mutex.withLock {
        contacts.forEach { trustedContacts[it.id] = it }
        syncState()
    }

    override suspend fun getTrustedContact(id: String): TrustedContactEntity? = trustedContacts[id]

    override suspend fun getPrimaryContacts(): List<TrustedContactEntity> =
        trustedContacts.values.filter { it.isPrimary }

    override fun observeTrustedContacts(): Flow<List<TrustedContactEntity>> = contactsFlow.asStateFlow()

    override suspend fun deleteTrustedContact(id: String) = mutex.withLock {
        trustedContacts.remove(id)
        syncState()
    }
}
