package com.shevault.core.common.movement

import com.shevault.core.common.location.LocationSample
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MovementDetectorTest {

    private lateinit var detector: MovementDetector

    @Before
    fun setup() {
        detector = MovementDetector(smoothingWindowSize = 3)
    }

    @Test
    fun testClassifySpeedThresholds() {
        assertEquals(MovementState.STATIONARY, MovementDetector.classifySpeed(0.0f))
        assertEquals(MovementState.STATIONARY, MovementDetector.classifySpeed(0.4f))
        assertEquals(MovementState.WALKING, MovementDetector.classifySpeed(1.2f))
        assertEquals(MovementState.WALKING, MovementDetector.classifySpeed(2.4f))
        assertEquals(MovementState.RUNNING, MovementDetector.classifySpeed(3.5f))
        assertEquals(MovementState.RUNNING, MovementDetector.classifySpeed(5.9f))
        assertEquals(MovementState.VEHICLE, MovementDetector.classifySpeed(6.1f))
        assertEquals(MovementState.VEHICLE, MovementDetector.classifySpeed(25.0f))
        assertEquals(MovementState.UNKNOWN, MovementDetector.classifySpeed(-1.0f))
        assertEquals(MovementState.UNKNOWN, MovementDetector.classifySpeed(Float.NaN))
    }

    @Test
    fun testDisplacementSpeedClassification() {
        // 100 meters in 10 seconds = 10 m/s -> VEHICLE
        val state = MovementDetector.classifyDisplacement(100.0, 10_000L)
        assertEquals(MovementState.VEHICLE, state)

        // 15 meters in 10 seconds = 1.5 m/s -> WALKING
        val walking = MovementDetector.classifyDisplacement(15.0, 10_000L)
        assertEquals(MovementState.WALKING, walking)

        // 0.5 meters in 10 seconds = 0.05 m/s -> STATIONARY
        val stationary = MovementDetector.classifyDisplacement(0.5, 10_000L)
        assertEquals(MovementState.STATIONARY, stationary)
    }

    @Test
    fun testProcessLocationSuccessiveSamples() {
        val now = 1_000_000L
        val sample1 = LocationSample(
            timestamp = now,
            latitude = 28.6139,
            longitude = 77.2090,
            accuracy = 3f,
            speed = 0f
        )
        val s1 = detector.processLocation(sample1)
        assertEquals(MovementState.STATIONARY, s1)

        // 10 seconds later, 200m away (20 m/s)
        val sample2 = LocationSample(
            timestamp = now + 10_000L,
            latitude = 28.6157, // ~200m north
            longitude = 77.2090,
            accuracy = 3f,
            speed = 20f
        )
        val s2 = detector.processLocation(sample2)
        assertEquals(MovementState.VEHICLE, s2)
        assertEquals(MovementState.VEHICLE, detector.currentMovement.value)
    }

    @Test
    fun testSmoothingSuppressesSpuriousNoise() {
        detector.processSpeed(1.2f) // walking
        detector.processSpeed(1.4f) // walking
        // A single 0.1s stationary jitter
        val state = detector.processSpeed(0.1f)
        // Majority in window [walking, walking, stationary] is walking
        assertEquals(MovementState.WALKING, state)
    }

    @Test
    fun testAccelerometerVarianceClassification() {
        // Flat line stationary
        val stationarySamples = listOf(9.8f, 9.81f, 9.79f, 9.8f)
        assertEquals(MovementState.STATIONARY, MovementDetector.classifyAccelerometerVariance(stationarySamples))

        // Walking rhythmic swing
        val walkingSamples = listOf(8.5f, 10.5f, 8.8f, 11.2f, 9.3f)
        assertEquals(MovementState.WALKING, MovementDetector.classifyAccelerometerVariance(walkingSamples))

        // Running sharp impacts
        val runningSamples = listOf(6.0f, 14.5f, 5.5f, 16.0f, 7.0f)
        assertEquals(MovementState.RUNNING, MovementDetector.classifyAccelerometerVariance(runningSamples))
    }

    @Test
    fun testResetClearsState() {
        detector.processSpeed(15.0f)
        assertEquals(MovementState.VEHICLE, detector.currentMovement.value)

        detector.reset()
        assertEquals(MovementState.UNKNOWN, detector.currentMovement.value)
    }

    @Test
    fun testStationarySuppressesGpsJitterDrift() {
        val now = 10_000L
        // Sample 1 at coordinates
        val s1 = LocationSample(
            timestamp = now,
            latitude = 28.613900,
            longitude = 77.209000,
            accuracy = 8.0f,
            speed = 0f
        )
        detector.processLocation(s1)

        // Sample 2 after 3 seconds with small GPS coordinate drift (2 meters north)
        val s2 = LocationSample(
            timestamp = now + 3000L,
            latitude = 28.613918, // ~2.0m north
            longitude = 77.209000,
            accuracy = 8.0f,
            speed = 0f
        )
        val state2 = detector.processLocation(s2)

        // Must remain STATIONARY rather than falsely registering walking speed
        assertEquals(MovementState.STATIONARY, state2)
    }

    @Test
    fun testSpuriousVehicleSpikeSuppressed() {
        detector.processSpeed(1.2f) // walking
        detector.processSpeed(1.4f) // walking
        // A single anomalous GPS jump
        val spike = detector.processSpeed(30.0f) // vehicle spike
        // Majority in window [walking, walking, vehicle] is walking
        assertEquals(MovementState.WALKING, spike)

        // Second high-speed reading establishes sustained vehicular motion
        val sustained = detector.processSpeed(30.0f) // window [walking, vehicle, vehicle]
        assertEquals(MovementState.VEHICLE, sustained)
    }

    @Test
    fun testOutOfOrderSamplesIgnored() {
        val now = 20_000L
        val sample1 = LocationSample(timestamp = now, latitude = 28.61, longitude = 77.20, accuracy = 4f, speed = 1.0f)
        detector.processLocation(sample1)

        // Stale out-of-order sample from the past
        val staleSample = LocationSample(timestamp = now - 5000L, latitude = 28.60, longitude = 77.19, accuracy = 4f, speed = 0f)
        detector.processLocation(staleSample)

        // Subsequent valid sample
        val nextSample = LocationSample(timestamp = now + 3000L, latitude = 28.61003, longitude = 77.20, accuracy = 4f, speed = 1.2f)
        val result = detector.processLocation(nextSample)
        assertEquals(MovementState.WALKING, result)
    }
}
