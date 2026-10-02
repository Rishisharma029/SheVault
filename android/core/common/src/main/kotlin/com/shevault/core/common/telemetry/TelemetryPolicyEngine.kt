package com.shevault.core.common.telemetry

import com.shevault.core.common.movement.MovementState

/**
 * Battery telemetry snapshot input for policy evaluation.
 */
data class BatteryTelemetry(
    val levelPercent: Int,
    val isCharging: Boolean = false,
    val temperatureCelsius: Float? = null
)

/**
 * Network connectivity states affecting dispatch and streaming.
 */
enum class ConnectivityState {
    CONNECTED_WIFI,
    CONNECTED_CELLULAR,
    POOR_CONNECTIVITY,
    DISCONNECTED
}

/**
 * Android device hardware states affecting thermal and power limits.
 */
enum class DeviceTelemetryState {
    NORMAL,
    POWER_SAVE_MODE,
    THERMAL_THROTTLING,
    SCREEN_OFF,
    IN_CALL
}

/**
 * Macro telemetry operating modes.
 */
enum class TelemetryMode {
    NORMAL,
    CONSTRAINED,
    CRITICAL
}

/**
 * Audio evidence recording operational status.
 */
enum class AudioRecordingStatus {
    CONTINUOUS,
    CHUNKED_DUTY_CYCLE, // Record periodically (e.g. 30s every 2 min) to conserve battery/bandwidth
    LOW_BITRATE,
    DISABLED
}

/**
 * Comprehensive input state to the Telemetry Policy Engine.
 *
 * Implements Task 14 requirement:
 * Input: battery, movement, connectivity, incident duration, location accuracy, device state.
 */
data class TelemetryPolicyInput(
    val battery: BatteryTelemetry,
    val movement: MovementState,
    val connectivity: ConnectivityState,
    val incidentDurationMs: Long,
    val locationAccuracyMeters: Float,
    val deviceState: DeviceTelemetryState = DeviceTelemetryState.NORMAL
)

/**
 * Dynamic telemetry schedule computed by the engine.
 *
 * Implements Task 14 requirement:
 * Output: location interval, sensor interval, upload interval, recording status.
 */
data class TelemetryPolicyConfig(
    val mode: TelemetryMode,
    val locationIntervalMs: Long,
    val fastestLocationIntervalMs: Long,
    val sensorIntervalMs: Long,
    /**
     * Upload interval in ms. -1 indicates uploads should be paused and buffered locally
     * until network connectivity is restored.
     */
    val uploadIntervalMs: Long,
    val recordingStatus: AudioRecordingStatus,
    val minDistanceMeters: Float = 0f,
    val reason: String = ""
)

/**
 * Adaptive Telemetry Engine.
 *
 * Eliminates naive hardcoding (e.g. GPS every 3s forever).
 * Balances victim survival time against evidence fidelity.
 * Rapid movement triggers frequent location fixes; stationary triggers battery backoff.
 */
object TelemetryPolicyEngine {

    const val ONE_HOUR_MS = 3_600_000L
    const val FOUR_HOURS_MS = 14_400_000L

    /**
     * Evaluates current device, environmental, and incident conditions
     * to produce an optimal telemetry strategy.
     */
    fun evaluate(input: TelemetryPolicyInput): TelemetryPolicyConfig {
        val mode = determineMode(input)
        val baseConfig = getBaseConfigForModeAndMovement(mode, input.movement)
        return applyEnvironmentalModifiers(baseConfig, input, mode)
    }

    private fun determineMode(input: TelemetryPolicyInput): TelemetryMode {
        val bat = input.battery.levelPercent.coerceIn(0, 100)
        val charging = input.battery.isCharging
        val duration = input.incidentDurationMs.coerceAtLeast(0L)
        val temp = input.battery.temperatureCelsius

        // Battery overheating threshold applies regardless of charging state
        if (temp != null && temp >= 48f) return TelemetryMode.CRITICAL
        if (temp != null && temp >= 42f) return TelemetryMode.CONSTRAINED

        // 1. Critical conditions: device battery imminent exhaustion or thermal emergency
        if (!charging && bat <= 15) return TelemetryMode.CRITICAL
        if (!charging && bat <= 25 && (input.deviceState == DeviceTelemetryState.POWER_SAVE_MODE || input.deviceState == DeviceTelemetryState.THERMAL_THROTTLING)) {
            return TelemetryMode.CRITICAL
        }
        if (!charging && duration > FOUR_HOURS_MS && bat <= 30) {
            return TelemetryMode.CRITICAL
        }

        // 2. Constrained conditions: degraded battery, poor network, or prolonged incident
        if (!charging && bat <= 35) return TelemetryMode.CONSTRAINED
        if (input.connectivity == ConnectivityState.POOR_CONNECTIVITY || input.connectivity == ConnectivityState.DISCONNECTED) {
            return TelemetryMode.CONSTRAINED
        }
        if (input.deviceState == DeviceTelemetryState.POWER_SAVE_MODE || input.deviceState == DeviceTelemetryState.THERMAL_THROTTLING) {
            return TelemetryMode.CONSTRAINED
        }
        if (!charging && duration > ONE_HOUR_MS && bat <= 50) {
            return TelemetryMode.CONSTRAINED
        }

        // 3. Normal conditions
        return TelemetryMode.NORMAL
    }

    private fun getBaseConfigForModeAndMovement(
        mode: TelemetryMode,
        movement: MovementState
    ): TelemetryPolicyConfig {
        return when (mode) {
            TelemetryMode.NORMAL -> when (movement) {
                MovementState.VEHICLE -> TelemetryPolicyConfig(
                    mode = mode,
                    locationIntervalMs = 2_000L,
                    fastestLocationIntervalMs = 1_000L,
                    sensorIntervalMs = 500L,
                    uploadIntervalMs = 3_000L,
                    recordingStatus = AudioRecordingStatus.CONTINUOUS,
                    minDistanceMeters = 10f,
                    reason = "Normal mode: rapid vehicle movement requires high-frequency GPS tracking"
                )
                MovementState.RUNNING -> TelemetryPolicyConfig(
                    mode = mode,
                    locationIntervalMs = 3_000L,
                    fastestLocationIntervalMs = 1_500L,
                    sensorIntervalMs = 1_000L,
                    uploadIntervalMs = 5_000L,
                    recordingStatus = AudioRecordingStatus.CONTINUOUS,
                    minDistanceMeters = 5f,
                    reason = "Normal mode: running speed requires active 3s GPS sampling"
                )
                MovementState.WALKING -> TelemetryPolicyConfig(
                    mode = mode,
                    locationIntervalMs = 5_000L,
                    fastestLocationIntervalMs = 3_000L,
                    sensorIntervalMs = 2_000L,
                    uploadIntervalMs = 10_000L,
                    recordingStatus = AudioRecordingStatus.CONTINUOUS,
                    minDistanceMeters = 3f,
                    reason = "Normal mode: pedestrian movement tracked at standard 5s cadence"
                )
                MovementState.STATIONARY -> TelemetryPolicyConfig(
                    mode = mode,
                    locationIntervalMs = 30_000L,
                    fastestLocationIntervalMs = 15_000L,
                    sensorIntervalMs = 5_000L,
                    uploadIntervalMs = 30_000L,
                    recordingStatus = AudioRecordingStatus.CONTINUOUS,
                    minDistanceMeters = 0f,
                    reason = "Normal mode: stationary device backs off GPS to 30s to conserve power"
                )
                MovementState.UNKNOWN -> TelemetryPolicyConfig(
                    mode = mode,
                    locationIntervalMs = 5_000L,
                    fastestLocationIntervalMs = 3_000L,
                    sensorIntervalMs = 2_000L,
                    uploadIntervalMs = 10_000L,
                    recordingStatus = AudioRecordingStatus.CONTINUOUS,
                    minDistanceMeters = 3f,
                    reason = "Normal mode: unknown movement defaults to standard 5s sampling"
                )
            }

            TelemetryMode.CONSTRAINED -> when (movement) {
                MovementState.VEHICLE -> TelemetryPolicyConfig(
                    mode = mode,
                    locationIntervalMs = 6_000L,
                    fastestLocationIntervalMs = 3_000L,
                    sensorIntervalMs = 2_000L,
                    uploadIntervalMs = 10_000L,
                    recordingStatus = AudioRecordingStatus.CHUNKED_DUTY_CYCLE,
                    minDistanceMeters = 15f,
                    reason = "Constrained mode: vehicle tracking throttled to 6s with duty-cycled audio"
                )
                MovementState.RUNNING -> TelemetryPolicyConfig(
                    mode = mode,
                    locationIntervalMs = 10_000L,
                    fastestLocationIntervalMs = 5_000L,
                    sensorIntervalMs = 3_000L,
                    uploadIntervalMs = 15_000L,
                    recordingStatus = AudioRecordingStatus.CHUNKED_DUTY_CYCLE,
                    minDistanceMeters = 10f,
                    reason = "Constrained mode: running tracking throttled to 10s"
                )
                MovementState.WALKING -> TelemetryPolicyConfig(
                    mode = mode,
                    locationIntervalMs = 15_000L,
                    fastestLocationIntervalMs = 8_000L,
                    sensorIntervalMs = 5_000L,
                    uploadIntervalMs = 20_000L,
                    recordingStatus = AudioRecordingStatus.CHUNKED_DUTY_CYCLE,
                    minDistanceMeters = 5f,
                    reason = "Constrained mode: walking tracking throttled to 15s"
                )
                MovementState.STATIONARY -> TelemetryPolicyConfig(
                    mode = mode,
                    locationIntervalMs = 60_000L,
                    fastestLocationIntervalMs = 30_000L,
                    sensorIntervalMs = 10_000L,
                    uploadIntervalMs = 60_000L,
                    recordingStatus = AudioRecordingStatus.CHUNKED_DUTY_CYCLE,
                    minDistanceMeters = 0f,
                    reason = "Constrained mode: stationary device backs off GPS to 60s"
                )
                MovementState.UNKNOWN -> TelemetryPolicyConfig(
                    mode = mode,
                    locationIntervalMs = 15_000L,
                    fastestLocationIntervalMs = 8_000L,
                    sensorIntervalMs = 5_000L,
                    uploadIntervalMs = 20_000L,
                    recordingStatus = AudioRecordingStatus.CHUNKED_DUTY_CYCLE,
                    minDistanceMeters = 5f,
                    reason = "Constrained mode: unknown movement throttled to 15s"
                )
            }

            TelemetryMode.CRITICAL -> when (movement) {
                MovementState.VEHICLE -> TelemetryPolicyConfig(
                    mode = mode,
                    locationIntervalMs = 15_000L,
                    fastestLocationIntervalMs = 10_000L,
                    sensorIntervalMs = 5_000L,
                    uploadIntervalMs = 20_000L,
                    recordingStatus = AudioRecordingStatus.CHUNKED_DUTY_CYCLE,
                    minDistanceMeters = 25f,
                    reason = "Critical mode: vehicle emergency tracking at 15s preservation cadence"
                )
                MovementState.RUNNING -> TelemetryPolicyConfig(
                    mode = mode,
                    locationIntervalMs = 20_000L,
                    fastestLocationIntervalMs = 10_000L,
                    sensorIntervalMs = 10_000L,
                    uploadIntervalMs = 30_000L,
                    recordingStatus = AudioRecordingStatus.CHUNKED_DUTY_CYCLE,
                    minDistanceMeters = 15f,
                    reason = "Critical mode: running emergency tracking at 20s cadence"
                )
                MovementState.WALKING -> TelemetryPolicyConfig(
                    mode = mode,
                    locationIntervalMs = 30_000L,
                    fastestLocationIntervalMs = 15_000L,
                    sensorIntervalMs = 15_000L,
                    uploadIntervalMs = 45_000L,
                    recordingStatus = AudioRecordingStatus.LOW_BITRATE,
                    minDistanceMeters = 10f,
                    reason = "Critical mode: walking emergency tracking at 30s with low-bitrate audio"
                )
                MovementState.STATIONARY -> TelemetryPolicyConfig(
                    mode = mode,
                    locationIntervalMs = 120_000L,
                    fastestLocationIntervalMs = 60_000L,
                    sensorIntervalMs = 30_000L,
                    uploadIntervalMs = 120_000L,
                    recordingStatus = AudioRecordingStatus.DISABLED,
                    minDistanceMeters = 0f,
                    reason = "Critical mode: stationary battery preservation - 2min GPS, audio disabled"
                )
                MovementState.UNKNOWN -> TelemetryPolicyConfig(
                    mode = mode,
                    locationIntervalMs = 30_000L,
                    fastestLocationIntervalMs = 15_000L,
                    sensorIntervalMs = 15_000L,
                    uploadIntervalMs = 45_000L,
                    recordingStatus = AudioRecordingStatus.LOW_BITRATE,
                    minDistanceMeters = 10f,
                    reason = "Critical mode: unknown movement throttled to 30s"
                )
            }
        }
    }

    private fun applyEnvironmentalModifiers(
        base: TelemetryPolicyConfig,
        input: TelemetryPolicyInput,
        mode: TelemetryMode
    ): TelemetryPolicyConfig {
        var locationInterval = base.locationIntervalMs
        var sensorInterval = base.sensorIntervalMs
        var uploadInterval = base.uploadIntervalMs
        var recordingStatus = base.recordingStatus
        var minDistance = base.minDistanceMeters

        // 1. Connectivity adjustments:
        if (input.connectivity == ConnectivityState.DISCONNECTED) {
            // Buffer updates locally; do not wake cellular radio while disconnected
            uploadInterval = -1L
        } else if (input.connectivity == ConnectivityState.POOR_CONNECTIVITY) {
            // Batch payloads to limit radio reconnection overhead
            uploadInterval = base.uploadIntervalMs * 2
        }

        // 2. Location accuracy degradation:
        // Poor accuracy (> 50m) indicates indoor multipath; increase min distance & interval
        if (input.locationAccuracyMeters > 50f) {
            locationInterval = (locationInterval * 1.5).toLong()
            minDistance += 10f
        }

        // 3. Thermal throttling adjustments:
        if (input.deviceState == DeviceTelemetryState.THERMAL_THROTTLING) {
            sensorInterval = (sensorInterval * 1.5).toLong()
            if (recordingStatus == AudioRecordingStatus.CONTINUOUS) {
                recordingStatus = AudioRecordingStatus.CHUNKED_DUTY_CYCLE
            }
        }

        // 4. Power save mode adjustments:
        if (input.deviceState == DeviceTelemetryState.POWER_SAVE_MODE) {
            if (locationInterval < 10_000L) {
                locationInterval = 10_000L
            }
            if (recordingStatus == AudioRecordingStatus.CONTINUOUS) {
                recordingStatus = AudioRecordingStatus.CHUNKED_DUTY_CYCLE
            }
        }

        // 5. In-call adjustments:
        // Do not attempt background audio recording during active telephony
        // to avoid microphone hardware lockups and conflicts with emergency calls.
        if (input.deviceState == DeviceTelemetryState.IN_CALL) {
            recordingStatus = AudioRecordingStatus.DISABLED
        }

        // 6. Screen off adjustments:
        // Screen is dark (stowed/pocketed); relax sensor cadence to permit CPU Doze batching.
        if (input.deviceState == DeviceTelemetryState.SCREEN_OFF) {
            sensorInterval = (sensorInterval * 1.5).toLong().coerceAtLeast(2_000L)
        }

        val fastest = (locationInterval / 2).coerceAtLeast(1_000L)

        return base.copy(
            locationIntervalMs = locationInterval,
            fastestLocationIntervalMs = fastest,
            sensorIntervalMs = sensorInterval,
            uploadIntervalMs = uploadInterval,
            recordingStatus = recordingStatus,
            minDistanceMeters = minDistance
        )
    }
}
