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
import com.shevault.core.database.entity.IncidentSessionEntity
import com.shevault.core.database.entity.LocationSampleEntity
import com.shevault.core.database.entity.SensorSampleEntity
import com.shevault.core.database.entity.TrustedContactEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class RoomIncidentJournalTest {

    private lateinit var fakeIncidentDao: FakeIncidentDao
    private lateinit var fakeIncidentEventDao: FakeIncidentEventDao
    private lateinit var fakeLocationSampleDao: FakeLocationSampleDao
    private lateinit var fakeDeliveryAttemptDao: FakeDeliveryAttemptDao
    private lateinit var fakeSensorSampleDao: FakeSensorSampleDao
    private lateinit var fakeTrustedContactDao: FakeTrustedContactDao

    private lateinit var journal: RoomIncidentJournal

    @Before
    fun setup() {
        fakeIncidentDao = FakeIncidentDao()
        fakeIncidentEventDao = FakeIncidentEventDao()
        fakeLocationSampleDao = FakeLocationSampleDao()
        fakeDeliveryAttemptDao = FakeDeliveryAttemptDao()
        fakeSensorSampleDao = FakeSensorSampleDao()
        fakeTrustedContactDao = FakeTrustedContactDao()

        journal = RoomIncidentJournal(
            incidentDao = fakeIncidentDao,
            incidentEventDao = fakeIncidentEventDao,
            locationSampleDao = fakeLocationSampleDao,
            deliveryAttemptDao = fakeDeliveryAttemptDao,
            sensorSampleDao = fakeSensorSampleDao,
            trustedContactDao = fakeTrustedContactDao
        )
    }

    @Test
    fun testCreateIncidentInsertsDaoAndLogsEvent() = runTest {
        val id = UUID.randomUUID().toString()
        val incident = journal.createIncident(
            id = id,
            activationMethod = "SOS_BUTTON",
            batteryAtStart = 90,
            initialLatitude = 12.9,
            initialLongitude = 77.5,
            initialAccuracy = 5f
        )

        assertEquals(id, incident.id)
        val inDao = fakeIncidentDao.getIncidentById(id)
        assertNotNull(inDao)
        assertEquals("ACTIVE", inDao?.status)

        val events = fakeIncidentEventDao.getEventsForIncidentSync(id)
        assertEquals(1, events.size)
        assertEquals(IncidentTimelineEventType.INCIDENT_CREATED, events[0].type)
    }

    @Test
    fun testRecordLocationUpdatesIncidentLocationInDaoAndLogsEvent() = runTest {
        val incidentId = UUID.randomUUID().toString()
        journal.createIncident(id = incidentId, activationMethod = "SOS_BUTTON")

        val sample = LocationSampleEntity(
            id = UUID.randomUUID().toString(),
            incidentId = incidentId,
            timestamp = 2000L,
            latitude = 28.5,
            longitude = 77.1,
            accuracy = 3f,
            speed = 5f
        )
        journal.recordLocation(sample)

        val updated = fakeIncidentDao.getIncidentById(incidentId)
        assertEquals(28.5, updated?.lastKnownLatitude ?: 0.0, 0.001)
        assertEquals(77.1, updated?.lastKnownLongitude ?: 0.0, 0.001)

        val events = fakeIncidentEventDao.getEventsForIncidentSync(incidentId)
        assertEquals(2, events.size)
        assertEquals(IncidentTimelineEventType.LOCATION_CAPTURED, events[1].type)
    }

    @Test
    fun testNonExistentIncidentSafeCancelDoesNotLogPhantomEvents() = runTest {
        val result = journal.cancelIncidentSafe("does-not-exist")
        assertNull(result)

        val events = fakeIncidentEventDao.getEventsForIncidentSync("does-not-exist")
        assertTrue("No phantom events must be logged for non-existent incident", events.isEmpty())
    }

    @Test
    fun testNonExistentIncidentDuressCancelDoesNotLogPhantomEvents() = runTest {
        val result = journal.cancelIncidentDuress("does-not-exist")
        assertNull(result)

        val events = fakeIncidentEventDao.getEventsForIncidentSync("does-not-exist")
        assertTrue("No phantom events must be logged for non-existent incident", events.isEmpty())
    }

    @Test
    fun testSafeCancelUpdatesStatusAndLogsEndedEvent() = runTest {
        val id = UUID.randomUUID().toString()
        journal.createIncident(id = id, activationMethod = "PANIC_GESTURE")

        val result = journal.cancelIncidentSafe(incidentId = id, batteryAtEnd = 80)
        assertNotNull(result)
        assertEquals("SAFE_RESOLVED", result?.status)
        assertEquals("SAFE", result?.cancelType)

        val events = fakeIncidentEventDao.getEventsForIncidentSync(id)
        assertEquals(3, events.size)
        assertEquals(IncidentTimelineEventType.INCIDENT_CREATED, events[0].type)
        assertEquals(IncidentTimelineEventType.SAFE_CANCEL, events[1].type)
        assertEquals(IncidentTimelineEventType.INCIDENT_ENDED, events[2].type)
    }

    @Test
    fun testDuressCancelCovertlyWithholdsIncidentEndedEvent() = runTest {
        val id = UUID.randomUUID().toString()
        journal.createIncident(id = id, activationMethod = "PANIC_GESTURE")

        val result = journal.cancelIncidentDuress(incidentId = id, batteryAtEnd = 75)
        assertNotNull(result)
        assertEquals("COERCED_DURESS", result?.status)
        assertEquals("DURESS", result?.cancelType)

        val events = fakeIncidentEventDao.getEventsForIncidentSync(id)
        assertEquals(2, events.size)
        assertEquals(IncidentTimelineEventType.INCIDENT_CREATED, events[0].type)
        assertEquals(IncidentTimelineEventType.DURESS_CANCEL, events[1].type)
        assertFalse(events.any { it.type == IncidentTimelineEventType.INCIDENT_ENDED })
    }

    @Test
    fun testDeliveryAttemptAndAck() = runTest {
        val id = UUID.randomUUID().toString()
        journal.createIncident(id = id, activationMethod = "SOS_BUTTON")

        val attemptId = UUID.randomUUID().toString()
        val attempt = DeliveryAttemptEntity(
            id = attemptId,
            incidentId = id,
            channel = "HTTP",
            recipient = "https://api.shevault.org",
            status = "PENDING",
            timestamp = 1000L
        )
        journal.recordDeliveryAttempt(attempt)

        var events = fakeIncidentEventDao.getEventsForIncidentSync(id)
        assertEquals(2, events.size)
        assertEquals(IncidentTimelineEventType.ESCALATION_ATTEMPT, events[1].type)

        journal.updateDeliveryAttemptStatus(attemptId, id, "DELIVERED", timestamp = 2000L)
        events = fakeIncidentEventDao.getEventsForIncidentSync(id)
        assertEquals(3, events.size)
        assertEquals(IncidentTimelineEventType.ACK_RECEIVED, events[2].type)
    }

    @Test
    fun testTrustedContactsViaRoomJournal() = runTest {
        val c1 = TrustedContactEntity(
            id = "tc-1",
            name = "Mom",
            phoneNumber = "12345",
            relationship = "Parent",
            isPrimary = true,
            notifySms = true,
            notifyCall = true
        )
        journal.saveTrustedContact(c1)

        val retrieved = journal.getTrustedContact("tc-1")
        assertEquals("Mom", retrieved?.name)

        val primary = journal.getPrimaryContacts()
        assertEquals(1, primary.size)

        journal.deleteTrustedContact("tc-1")
        assertNull(journal.getTrustedContact("tc-1"))
    }
}

// DAO Fakes for testing RoomIncidentJournal coordination
class FakeIncidentDao : IncidentDao {
    private val incidents = mutableMapOf<String, IncidentEntity>()
    private val flow = MutableStateFlow<List<IncidentEntity>>(emptyList())

    override suspend fun insertIncident(incident: IncidentEntity) {
        incidents[incident.id] = incident
        flow.value = incidents.values.toList()
    }

    override suspend fun updateIncident(incident: IncidentEntity) {
        incidents[incident.id] = incident
        flow.value = incidents.values.toList()
    }

    override suspend fun getIncidentById(id: String): IncidentEntity? = incidents[id]

    override fun getIncidentFlow(id: String): Flow<IncidentEntity?> = flow.map { list -> list.firstOrNull { it.id == id } }

    override fun getAllIncidents(): Flow<List<IncidentEntity>> = flow

    override fun getActiveIncidents(): Flow<List<IncidentEntity>> = flow.map { list ->
        list.filter { it.status in listOf("ACTIVE", "ESCALATING", "COERCED_DURESS") }
    }

    override suspend fun updateLastKnownLocation(id: String, lat: Double, lon: Double, acc: Float, timestamp: Long) {
        incidents[id]?.let {
            incidents[id] = it.copy(lastKnownLatitude = lat, lastKnownLongitude = lon, lastKnownAccuracy = acc, updatedAt = timestamp)
            flow.value = incidents.values.toList()
        }
    }

    override suspend fun resolveIncident(id: String, status: String, cancelType: String?, batteryAtEnd: Int?, timestamp: Long) {
        incidents[id]?.let {
            incidents[id] = it.copy(status = status, cancelType = cancelType, batteryAtEnd = batteryAtEnd, updatedAt = timestamp)
            flow.value = incidents.values.toList()
        }
    }

    override suspend fun insertSession(session: IncidentSessionEntity) {}
    override suspend fun getSessionById(incidentId: String): IncidentSessionEntity? = null
    override suspend fun getLatestSession(): IncidentSessionEntity? = null
    override fun getAllSessions(): Flow<List<IncidentSessionEntity>> = flowOf(emptyList())
}

class FakeIncidentEventDao : IncidentEventDao {
    private val events = mutableListOf<IncidentEventEntity>()

    override suspend fun insertEvent(event: IncidentEventEntity) { events.add(event) }
    override suspend fun insertEvents(events: List<IncidentEventEntity>) { this.events.addAll(events) }
    override fun getEventsForIncident(incidentId: String): Flow<List<IncidentEventEntity>> =
        flowOf(events.filter { it.incidentId == incidentId })
    override suspend fun getEventsForIncidentSync(incidentId: String): List<IncidentEventEntity> =
        events.filter { it.incidentId == incidentId }
    override fun getAllEvents(): Flow<List<IncidentEventEntity>> = flowOf(events)
    override suspend fun getEventsByType(incidentId: String, type: String): List<IncidentEventEntity> =
        events.filter { it.incidentId == incidentId && it.type == type }
}

class FakeLocationSampleDao : LocationSampleDao {
    private val samples = mutableListOf<LocationSampleEntity>()

    override suspend fun insertSample(sample: LocationSampleEntity) { samples.add(sample) }
    override suspend fun insertSamples(samples: List<LocationSampleEntity>) { this.samples.addAll(samples) }
    override fun getSamplesForIncident(incidentId: String): Flow<List<LocationSampleEntity>> =
        flowOf(samples.filter { it.incidentId == incidentId })
    override suspend fun getLatestSampleForIncident(incidentId: String): LocationSampleEntity? =
        samples.filter { it.incidentId == incidentId }.maxByOrNull { it.timestamp }
    override suspend fun getLatestSample(): LocationSampleEntity? = samples.maxByOrNull { it.timestamp }
    override fun getAllSamples(): Flow<List<LocationSampleEntity>> = flowOf(samples)
}

class FakeDeliveryAttemptDao : DeliveryAttemptDao {
    private val attempts = mutableListOf<DeliveryAttemptEntity>()

    override suspend fun insertAttempt(attempt: DeliveryAttemptEntity) { attempts.add(attempt) }
    override suspend fun updateAttempt(attempt: DeliveryAttemptEntity) {
        val idx = attempts.indexOfFirst { it.id == attempt.id }
        if (idx != -1) attempts[idx] = attempt
    }
    override fun getAttemptsForIncident(incidentId: String): Flow<List<DeliveryAttemptEntity>> =
        flowOf(attempts.filter { it.incidentId == incidentId })
    override suspend fun getAttemptsByStatus(incidentId: String, status: String): List<DeliveryAttemptEntity> =
        attempts.filter { it.incidentId == incidentId && it.status == status }
    override suspend fun getPendingAttempts(): List<DeliveryAttemptEntity> =
        attempts.filter { it.status == "PENDING" }
    override suspend fun updateStatus(id: String, status: String, error: String?, timestamp: Long) {
        val idx = attempts.indexOfFirst { it.id == id }
        if (idx != -1) {
            val item = attempts[idx]
            attempts[idx] = item.copy(status = status, error = error, timestamp = timestamp, attemptCount = item.attemptCount + 1)
        }
    }
}

class FakeSensorSampleDao : SensorSampleDao {
    private val samples = mutableListOf<SensorSampleEntity>()

    override suspend fun insertSample(sample: SensorSampleEntity) { samples.add(sample) }
    override suspend fun insertSamples(samples: List<SensorSampleEntity>) { this.samples.addAll(samples) }
    override fun getSamplesForIncident(incidentId: String): Flow<List<SensorSampleEntity>> =
        flowOf(samples.filter { it.incidentId == incidentId })
    override suspend fun getLatestSamplesByType(incidentId: String, sensorType: String, limit: Int): List<SensorSampleEntity> =
        samples.filter { it.incidentId == incidentId && it.sensorType == sensorType }.take(limit)
}

class FakeTrustedContactDao : TrustedContactDao {
    private val contacts = mutableMapOf<String, TrustedContactEntity>()
    private val flow = MutableStateFlow<List<TrustedContactEntity>>(emptyList())

    override fun getAllContacts(): Flow<List<TrustedContactEntity>> = flow
    override suspend fun getPrimaryContacts(): List<TrustedContactEntity> = contacts.values.filter { it.isPrimary }
    override suspend fun getContactById(id: String): TrustedContactEntity? = contacts[id]
    override suspend fun insertContacts(contacts: List<TrustedContactEntity>) {
        contacts.forEach { this.contacts[it.id] = it }
        flow.value = this.contacts.values.toList()
    }
    override suspend fun insertContact(contact: TrustedContactEntity) {
        contacts[contact.id] = contact
        flow.value = contacts.values.toList()
    }
    override suspend fun deleteContact(id: String) {
        contacts.remove(id)
        flow.value = contacts.values.toList()
    }
}
