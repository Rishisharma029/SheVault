package com.shevault.core.database.journal

import com.shevault.core.common.incident.IncidentTimelineEventType
import com.shevault.core.database.entity.DeliveryAttemptEntity
import com.shevault.core.database.entity.LocationSampleEntity
import com.shevault.core.database.entity.SensorSampleEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class LocalIncidentJournalTest {

    private lateinit var journal: LocalIncidentJournal

    @Before
    fun setup() {
        journal = InMemoryIncidentJournal()
    }

    @Test
    fun testCreateIncidentLogsIncidentCreatedEventAndInitialState() = runTest {
        val incidentId = UUID.randomUUID().toString()
        val now = 1_000_000L

        val incident = journal.createIncident(
            id = incidentId,
            activationMethod = "PANIC_GESTURE",
            batteryAtStart = 85,
            initialLatitude = 28.6139,
            initialLongitude = 77.2090,
            initialAccuracy = 4.0f,
            createdAt = now
        )

        assertEquals(incidentId, incident.id)
        assertEquals("ACTIVE", incident.status)
        assertNull(incident.cancelType)
        assertEquals(85, incident.batteryAtStart)
        assertNull(incident.batteryAtEnd)
        assertEquals(28.6139, incident.lastKnownLatitude ?: 0.0, 0.0001)
        assertEquals(77.2090, incident.lastKnownLongitude ?: 0.0, 0.0001)

        val timeline = journal.getTimelineSync(incidentId)
        assertEquals(1, timeline.size)
        assertEquals(IncidentTimelineEventType.INCIDENT_CREATED, timeline[0].type)
        assertTrue(timeline[0].payload?.contains("activationMethod=PANIC_GESTURE") == true)
        assertEquals(now, timeline[0].timestamp)
    }

    @Test
    fun testRecordLocationUpdatesIncidentLastKnownAndLogsTimeline() = runTest {
        val incidentId = UUID.randomUUID().toString()
        journal.createIncident(id = incidentId, activationMethod = "SOS_BUTTON")

        val sample = LocationSampleEntity(
            id = UUID.randomUUID().toString(),
            incidentId = incidentId,
            timestamp = 2_000_000L,
            latitude = 28.6200,
            longitude = 77.2150,
            accuracy = 2.5f,
            speed = 3.2f,
            bearing = 90f,
            provider = "fused"
        )
        journal.recordLocation(sample)

        val updatedIncident = journal.getIncident(incidentId)
        assertNotNull(updatedIncident)
        assertEquals(28.6200, updatedIncident?.lastKnownLatitude ?: 0.0, 0.0001)
        assertEquals(77.2150, updatedIncident?.lastKnownLongitude ?: 0.0, 0.0001)
        assertEquals(2.5f, updatedIncident?.lastKnownAccuracy ?: 0f, 0.01f)

        val timeline = journal.getTimelineSync(incidentId)
        assertEquals(2, timeline.size)
        assertEquals(IncidentTimelineEventType.INCIDENT_CREATED, timeline[0].type)
        assertEquals(IncidentTimelineEventType.LOCATION_CAPTURED, timeline[1].type)
        assertTrue(timeline[1].payload?.contains("speed=3.2") == true)

        val samplesFlow = journal.observeLocationSamples(incidentId).first()
        assertEquals(1, samplesFlow.size)
        assertEquals(sample.id, samplesFlow[0].id)
    }

    @Test
    fun testNetworkTransitionsAuditEvents() = runTest {
        val incidentId = UUID.randomUUID().toString()
        journal.createIncident(id = incidentId, activationMethod = "VOICE_TRIGGER")

        journal.logNetworkTransition(incidentId, isConnected = false, details = "Cellular signal dropped")
        journal.logNetworkTransition(incidentId, isConnected = true, details = "WiFi connected")

        val timeline = journal.getTimelineSync(incidentId)
        assertEquals(3, timeline.size)
        assertEquals(IncidentTimelineEventType.INCIDENT_CREATED, timeline[0].type)
        assertEquals(IncidentTimelineEventType.NETWORK_LOST, timeline[1].type)
        assertEquals(IncidentTimelineEventType.NETWORK_RESTORED, timeline[2].type)
    }

    @Test
    fun testDeliveryAttemptsAndAckReceived() = runTest {
        val incidentId = UUID.randomUUID().toString()
        journal.createIncident(id = incidentId, activationMethod = "SOS_BUTTON")

        val attemptId = UUID.randomUUID().toString()
        val attempt = DeliveryAttemptEntity(
            id = attemptId,
            incidentId = incidentId,
            channel = "SMS",
            recipient = "+1234567890",
            status = "PENDING",
            attemptCount = 1,
            timestamp = 3_000_000L
        )
        journal.recordDeliveryAttempt(attempt)

        var timeline = journal.getTimelineSync(incidentId)
        assertEquals(2, timeline.size)
        assertEquals(IncidentTimelineEventType.ESCALATION_ATTEMPT, timeline[1].type)

        // Contact / Gateway confirms delivery
        journal.updateDeliveryAttemptStatus(attemptId, incidentId, status = "DELIVERED")

        timeline = journal.getTimelineSync(incidentId)
        assertEquals(3, timeline.size)
        assertEquals(IncidentTimelineEventType.ACK_RECEIVED, timeline[2].type)
        assertTrue(timeline[2].payload?.contains("status=DELIVERED") == true)
    }

    @Test
    fun testRecordSensorSample() = runTest {
        val incidentId = UUID.randomUUID().toString()
        journal.createIncident(id = incidentId, activationMethod = "SHAKE_SENSOR")

        val sensorSample = SensorSampleEntity(
            id = UUID.randomUUID().toString(),
            incidentId = incidentId,
            timestamp = 4_000_000L,
            sensorType = "ACCELEROMETER",
            x = 0.5f,
            y = 9.8f,
            z = 1.2f,
            value = 9.88f
        )
        journal.recordSensorSample(sensorSample)

        val samples = journal.observeSensorSamples(incidentId).first()
        assertEquals(1, samples.size)
        assertEquals("ACCELEROMETER", samples[0].sensorType)
        assertEquals(9.88f, samples[0].value ?: 0f, 0.01f)
    }

    @Test
    fun testSafeCancelEndsIncidentNormally() = runTest {
        val incidentId = UUID.randomUUID().toString()
        journal.createIncident(id = incidentId, activationMethod = "SOS_BUTTON", batteryAtStart = 90)

        val resolved = journal.cancelIncidentSafe(incidentId = incidentId, batteryAtEnd = 88)
        assertNotNull(resolved)
        assertEquals("SAFE_RESOLVED", resolved?.status)
        assertEquals("SAFE", resolved?.cancelType)
        assertEquals(88, resolved?.batteryAtEnd)

        val timeline = journal.getTimelineSync(incidentId)
        assertEquals(3, timeline.size)
        assertEquals(IncidentTimelineEventType.INCIDENT_CREATED, timeline[0].type)
        assertEquals(IncidentTimelineEventType.SAFE_CANCEL, timeline[1].type)
        assertEquals(IncidentTimelineEventType.INCIDENT_ENDED, timeline[2].type)
    }

    @Test
    fun testDuressCancelSetsCovertStatusWithoutLoggingIncidentEnded() = runTest {
        val incidentId = UUID.randomUUID().toString()
        journal.createIncident(id = incidentId, activationMethod = "PANIC_GESTURE", batteryAtStart = 70)

        val duress = journal.cancelIncidentDuress(incidentId = incidentId, batteryAtEnd = 68)
        assertNotNull(duress)
        assertEquals("COERCED_DURESS", duress?.status)
        assertEquals("DURESS", duress?.cancelType)
        assertEquals(68, duress?.batteryAtEnd)

        val timeline = journal.getTimelineSync(incidentId)
        assertEquals(2, timeline.size)
        assertEquals(IncidentTimelineEventType.INCIDENT_CREATED, timeline[0].type)
        assertEquals(IncidentTimelineEventType.DURESS_CANCEL, timeline[1].type)

        // Vital covert safety guarantee: INCIDENT_ENDED must NOT be logged
        assertFalse(timeline.any { it.type == IncidentTimelineEventType.INCIDENT_ENDED })
    }

    @Test
    fun testEndIncidentLogsIncidentEnded() = runTest {
        val incidentId = UUID.randomUUID().toString()
        journal.createIncident(id = incidentId, activationMethod = "SOS_BUTTON")

        val ended = journal.endIncident(incidentId = incidentId, status = "EXPIRED", batteryAtEnd = 50)
        assertNotNull(ended)
        assertEquals("EXPIRED", ended?.status)

        val timeline = journal.getTimelineSync(incidentId)
        assertEquals(2, timeline.size)
        assertEquals(IncidentTimelineEventType.INCIDENT_ENDED, timeline[1].type)
    }

    @Test
    fun testMultipleIncidentsTimelineIsolation() = runTest {
        val inc1 = UUID.randomUUID().toString()
        val inc2 = UUID.randomUUID().toString()

        journal.createIncident(id = inc1, activationMethod = "SOS_BUTTON")
        journal.createIncident(id = inc2, activationMethod = "PANIC_GESTURE")

        journal.logEvent(incidentId = inc1, type = IncidentTimelineEventType.LOCATION_CAPTURED, payload = "loc1")
        journal.logEvent(incidentId = inc2, type = IncidentTimelineEventType.NETWORK_LOST, payload = "net2")

        val timeline1 = journal.getTimelineSync(inc1)
        val timeline2 = journal.getTimelineSync(inc2)

        assertEquals(2, timeline1.size)
        assertEquals(IncidentTimelineEventType.LOCATION_CAPTURED, timeline1[1].type)

        assertEquals(2, timeline2.size)
        assertEquals(IncidentTimelineEventType.NETWORK_LOST, timeline2[1].type)

        val allIncidents = journal.observeAllIncidents().first()
        assertEquals(2, allIncidents.size)
    }

    @Test
    fun testTrustedContactsOperations() = runTest {
        val contact1 = com.shevault.core.database.entity.TrustedContactEntity(
            id = "c1",
            name = "Alice",
            phoneNumber = "+1111111111",
            relationship = "Sister",
            isPrimary = true,
            notifySms = true,
            notifyCall = true
        )
        val contact2 = com.shevault.core.database.entity.TrustedContactEntity(
            id = "c2",
            name = "Bob",
            phoneNumber = "+2222222222",
            relationship = "Friend",
            isPrimary = false,
            notifySms = true,
            notifyCall = false
        )

        journal.saveTrustedContact(contact1)
        journal.saveTrustedContact(contact2)

        val retrieved1 = journal.getTrustedContact("c1")
        assertEquals("Alice", retrieved1?.name)
        assertTrue(retrieved1?.isPrimary == true)

        val primaryContacts = journal.getPrimaryContacts()
        assertEquals(1, primaryContacts.size)
        assertEquals("Alice", primaryContacts[0].name)

        val observed = journal.observeTrustedContacts().first()
        assertEquals(2, observed.size)
        assertEquals("Alice", observed[0].name) // Primary listed first

        journal.deleteTrustedContact("c1")
        assertNull(journal.getTrustedContact("c1"))
        assertEquals(1, journal.observeTrustedContacts().first().size)
    }

    @Test
    fun testCancelNonExistentIncidentReturnsNull() = runTest {
        val nonExistentId = "non-existent-id"
        val result = journal.cancelIncidentSafe(nonExistentId)
        assertNull(result)

        val duressResult = journal.cancelIncidentDuress(nonExistentId)
        assertNull(duressResult)

        val endResult = journal.endIncident(nonExistentId)
        assertNull(endResult)

        val timeline = journal.getTimelineSync(nonExistentId)
        assertTrue(timeline.isEmpty())
    }

    @Test
    fun testRecordLocationWithoutIncidentId() = runTest {
        val sample = LocationSampleEntity(
            id = UUID.randomUUID().toString(),
            incidentId = null,
            timestamp = 5_000_000L,
            latitude = 28.5,
            longitude = 77.2,
            accuracy = 10f
        )
        val saved = journal.recordLocation(sample)
        assertEquals(sample.id, saved.id)
    }
}
