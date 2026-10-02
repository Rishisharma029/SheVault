package com.shevault.core.common.location

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationRepositoryTest {

    @Test
    fun testInitialStateAndGetLocation() = runTest {
        val initial = LocationSample(
            timestamp = 1000L,
            latitude = 12.9716,
            longitude = 77.5946,
            accuracy = 3.5f,
            speed = 1.2f,
            bearing = 45f,
            provider = "fused"
        )
        val repository = SimulatedLocationRepository(initialLocation = initial)

        assertEquals(initial, repository.currentLocation.value)
        assertEquals(initial, repository.getLastKnownLocation())
        assertEquals(initial, repository.getCurrentLocation())
        assertFalse(repository.isTracking.value)
    }

    @Test
    fun testStartAndStopTrackingFlow() = runTest {
        val repository = SimulatedLocationRepository()
        assertFalse(repository.isTracking.value)

        val flow = repository.startTracking(intervalMs = 3000L)
        assertTrue(repository.isTracking.value)
        assertEquals(3000L, repository.getTrackingInterval())

        val emittedSamples = mutableListOf<LocationSample>()
        val job = launch {
            flow.collect { emittedSamples.add(it) }
        }
        testScheduler.runCurrent()

        val sample1 = LocationSample(
            timestamp = 2000L,
            latitude = 28.7041,
            longitude = 77.1025,
            accuracy = 5.0f
        )
        repository.emitLocation(sample1)
        testScheduler.runCurrent()

        assertEquals(1, emittedSamples.size)
        assertEquals(sample1, emittedSamples[0])
        assertEquals(sample1, repository.currentLocation.value)

        repository.updateTrackingInterval(10_000L)
        assertEquals(10_000L, repository.getTrackingInterval())

        repository.stopTracking()
        assertFalse(repository.isTracking.value)

        job.cancel()
    }

    @Test
    fun testLocationSampleAttributesContract() {
        val sample = LocationSample(
            timestamp = 5000L,
            latitude = 19.0760,
            longitude = 72.8777,
            accuracy = 4.2f,
            speed = 12.5f,
            bearing = 180f,
            provider = "gps",
            incidentId = "INC-123"
        )

        assertNotNull(sample.id)
        assertEquals(5000L, sample.timestamp)
        assertEquals(19.0760, sample.latitude, 0.0001)
        assertEquals(72.8777, sample.longitude, 0.0001)
        assertEquals(4.2f, sample.accuracy, 0.01f)
        assertEquals(12.5f, sample.speed, 0.01f)
        assertEquals(180f, sample.bearing, 0.01f)
        assertEquals("gps", sample.provider)
        assertEquals("INC-123", sample.incidentId)
    }
}
