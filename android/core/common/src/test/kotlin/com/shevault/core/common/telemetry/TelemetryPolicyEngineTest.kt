package com.shevault.core.common.telemetry

import com.shevault.core.common.movement.MovementState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TelemetryPolicyEngineTest {

    @Test
    fun testNormalModeStationaryBacksOffLocationSampling() {
        val input = TelemetryPolicyInput(
            battery = BatteryTelemetry(levelPercent = 90, isCharging = false),
            movement = MovementState.STATIONARY,
            connectivity = ConnectivityState.CONNECTED_CELLULAR,
            incidentDurationMs = 60_000L,
            locationAccuracyMeters = 3.0f,
            deviceState = DeviceTelemetryState.NORMAL
        )

        val policy = TelemetryPolicyEngine.evaluate(input)

        assertEquals(TelemetryMode.NORMAL, policy.mode)
        assertEquals(30_000L, policy.locationIntervalMs)
        assertEquals(15_000L, policy.fastestLocationIntervalMs)
        assertEquals(AudioRecordingStatus.CONTINUOUS, policy.recordingStatus)
    }

    @Test
    fun testRapidVehicleMovementJustifiesHighFrequencySampling() {
        val input = TelemetryPolicyInput(
            battery = BatteryTelemetry(levelPercent = 90, isCharging = false),
            movement = MovementState.VEHICLE,
            connectivity = ConnectivityState.CONNECTED_CELLULAR,
            incidentDurationMs = 60_000L,
            locationAccuracyMeters = 5.0f,
            deviceState = DeviceTelemetryState.NORMAL
        )

        val policy = TelemetryPolicyEngine.evaluate(input)

        assertEquals(TelemetryMode.NORMAL, policy.mode)
        assertEquals(2_000L, policy.locationIntervalMs)
        assertEquals(1_000L, policy.fastestLocationIntervalMs)
        assertEquals(500L, policy.sensorIntervalMs)
        assertEquals(3_000L, policy.uploadIntervalMs)
        assertTrue(policy.minDistanceMeters >= 10f)
    }

    @Test
    fun testRunningAndWalkingIntervalsScaleProportionally() {
        val runningInput = TelemetryPolicyInput(
            battery = BatteryTelemetry(levelPercent = 80),
            movement = MovementState.RUNNING,
            connectivity = ConnectivityState.CONNECTED_CELLULAR,
            incidentDurationMs = 60_000L,
            locationAccuracyMeters = 4f
        )
        val walkingInput = runningInput.copy(movement = MovementState.WALKING)

        val runningPolicy = TelemetryPolicyEngine.evaluate(runningInput)
        val walkingPolicy = TelemetryPolicyEngine.evaluate(walkingInput)

        assertTrue("Running must sample more frequently than walking",
            runningPolicy.locationIntervalMs < walkingPolicy.locationIntervalMs)
        assertEquals(3_000L, runningPolicy.locationIntervalMs)
        assertEquals(5_000L, walkingPolicy.locationIntervalMs)
    }

    @Test
    fun testLowBatteryEntersCriticalModeWithAggressiveConservation() {
        val input = TelemetryPolicyInput(
            battery = BatteryTelemetry(levelPercent = 12, isCharging = false),
            movement = MovementState.STATIONARY,
            connectivity = ConnectivityState.CONNECTED_CELLULAR,
            incidentDurationMs = 300_000L,
            locationAccuracyMeters = 5.0f
        )

        val policy = TelemetryPolicyEngine.evaluate(input)

        assertEquals(TelemetryMode.CRITICAL, policy.mode)
        assertEquals(120_000L, policy.locationIntervalMs)
        assertEquals(AudioRecordingStatus.DISABLED, policy.recordingStatus)
    }

    @Test
    fun testChargingOverridesLowBatteryCriticalState() {
        val input = TelemetryPolicyInput(
            battery = BatteryTelemetry(levelPercent = 10, isCharging = true),
            movement = MovementState.WALKING,
            connectivity = ConnectivityState.CONNECTED_CELLULAR,
            incidentDurationMs = 300_000L,
            locationAccuracyMeters = 5.0f
        )

        val policy = TelemetryPolicyEngine.evaluate(input)

        assertEquals(TelemetryMode.NORMAL, policy.mode)
        assertEquals(5_000L, policy.locationIntervalMs)
        assertEquals(AudioRecordingStatus.CONTINUOUS, policy.recordingStatus)
    }

    @Test
    fun testDisconnectedConnectivityBuffersUploadsLocally() {
        val input = TelemetryPolicyInput(
            battery = BatteryTelemetry(levelPercent = 70),
            movement = MovementState.WALKING,
            connectivity = ConnectivityState.DISCONNECTED,
            incidentDurationMs = 120_000L,
            locationAccuracyMeters = 5.0f
        )

        val policy = TelemetryPolicyEngine.evaluate(input)

        assertEquals(TelemetryMode.CONSTRAINED, policy.mode)
        assertEquals(-1L, policy.uploadIntervalMs)
    }

    @Test
    fun testPoorConnectivityBatchesUploadInterval() {
        val cellularInput = TelemetryPolicyInput(
            battery = BatteryTelemetry(levelPercent = 30),
            movement = MovementState.WALKING,
            connectivity = ConnectivityState.CONNECTED_CELLULAR,
            incidentDurationMs = 120_000L,
            locationAccuracyMeters = 5.0f
        )
        val poorInput = cellularInput.copy(connectivity = ConnectivityState.POOR_CONNECTIVITY)

        val cellularPolicy = TelemetryPolicyEngine.evaluate(cellularInput)
        val poorPolicy = TelemetryPolicyEngine.evaluate(poorInput)

        assertTrue(poorPolicy.uploadIntervalMs > cellularPolicy.uploadIntervalMs)
    }

    @Test
    fun testThermalThrottlingDutyCyclesAudioAndBacksOffSensors() {
        val normalInput = TelemetryPolicyInput(
            battery = BatteryTelemetry(levelPercent = 80),
            movement = MovementState.WALKING,
            connectivity = ConnectivityState.CONNECTED_WIFI,
            incidentDurationMs = 60_000L,
            locationAccuracyMeters = 5f,
            deviceState = DeviceTelemetryState.NORMAL
        )
        val thermalInput = normalInput.copy(deviceState = DeviceTelemetryState.THERMAL_THROTTLING)

        val thermalPolicy = TelemetryPolicyEngine.evaluate(thermalInput)

        assertEquals(TelemetryMode.CONSTRAINED, thermalPolicy.mode)
        assertEquals(AudioRecordingStatus.CHUNKED_DUTY_CYCLE, thermalPolicy.recordingStatus)
        assertTrue(thermalPolicy.sensorIntervalMs > 2_000L)
    }

    @Test
    fun testDegradedLocationAccuracyAdjustsSamplingWait() {
        val accurateInput = TelemetryPolicyInput(
            battery = BatteryTelemetry(levelPercent = 80),
            movement = MovementState.WALKING,
            connectivity = ConnectivityState.CONNECTED_CELLULAR,
            incidentDurationMs = 60_000L,
            locationAccuracyMeters = 5f
        )
        val degradedInput = accurateInput.copy(locationAccuracyMeters = 85f)

        val accuratePolicy = TelemetryPolicyEngine.evaluate(accurateInput)
        val degradedPolicy = TelemetryPolicyEngine.evaluate(degradedInput)

        assertTrue("Degraded GPS accuracy should extend interval to prevent GPS hunting",
            degradedPolicy.locationIntervalMs > accuratePolicy.locationIntervalMs)
        assertTrue(degradedPolicy.minDistanceMeters > accuratePolicy.minDistanceMeters)
    }

    @Test
    fun testProlongedIncidentDurationTransitionsToConstrained() {
        val shortIncident = TelemetryPolicyInput(
            battery = BatteryTelemetry(levelPercent = 45),
            movement = MovementState.WALKING,
            connectivity = ConnectivityState.CONNECTED_CELLULAR,
            incidentDurationMs = 600_000L, // 10 min
            locationAccuracyMeters = 5f
        )
        val longIncident = shortIncident.copy(
            incidentDurationMs = 4_000_000L // > 1 hour
        )

        val shortPolicy = TelemetryPolicyEngine.evaluate(shortIncident)
        val longPolicy = TelemetryPolicyEngine.evaluate(longIncident)

        assertEquals(TelemetryMode.NORMAL, shortPolicy.mode)
        assertEquals(TelemetryMode.CONSTRAINED, longPolicy.mode)
    }

    @Test
    fun testBatteryOverheatingTriggersCriticalEvenWhenCharging() {
        val input = TelemetryPolicyInput(
            battery = BatteryTelemetry(levelPercent = 90, isCharging = true, temperatureCelsius = 49.5f),
            movement = MovementState.WALKING,
            connectivity = ConnectivityState.CONNECTED_CELLULAR,
            incidentDurationMs = 60_000L,
            locationAccuracyMeters = 5f
        )

        val policy = TelemetryPolicyEngine.evaluate(input)
        assertEquals(TelemetryMode.CRITICAL, policy.mode)
    }

    @Test
    fun testInCallDisablesAudioRecordingToAvoidTelephonyConflicts() {
        val input = TelemetryPolicyInput(
            battery = BatteryTelemetry(levelPercent = 80),
            movement = MovementState.WALKING,
            connectivity = ConnectivityState.CONNECTED_CELLULAR,
            incidentDurationMs = 60_000L,
            locationAccuracyMeters = 5f,
            deviceState = DeviceTelemetryState.IN_CALL
        )

        val policy = TelemetryPolicyEngine.evaluate(input)
        assertEquals(AudioRecordingStatus.DISABLED, policy.recordingStatus)
    }

    @Test
    fun testScreenOffRelaxesSensorSamplingForDozeMode() {
        val normalInput = TelemetryPolicyInput(
            battery = BatteryTelemetry(levelPercent = 80),
            movement = MovementState.WALKING,
            connectivity = ConnectivityState.CONNECTED_CELLULAR,
            incidentDurationMs = 60_000L,
            locationAccuracyMeters = 5f,
            deviceState = DeviceTelemetryState.NORMAL
        )
        val screenOffInput = normalInput.copy(deviceState = DeviceTelemetryState.SCREEN_OFF)

        val normalPolicy = TelemetryPolicyEngine.evaluate(normalInput)
        val screenOffPolicy = TelemetryPolicyEngine.evaluate(screenOffInput)

        assertTrue(screenOffPolicy.sensorIntervalMs > normalPolicy.sensorIntervalMs)
    }

    @Test
    fun testInputBoundaryClamping() {
        val input = TelemetryPolicyInput(
            battery = BatteryTelemetry(levelPercent = -10), // Corrupted negative percent
            movement = MovementState.WALKING,
            connectivity = ConnectivityState.CONNECTED_CELLULAR,
            incidentDurationMs = -5000L,
            locationAccuracyMeters = 5f
        )

        val policy = TelemetryPolicyEngine.evaluate(input)
        assertEquals(TelemetryMode.CRITICAL, policy.mode)
    }
}
