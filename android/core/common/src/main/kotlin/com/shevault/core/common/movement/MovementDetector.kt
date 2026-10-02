package com.shevault.core.common.movement

import com.shevault.core.common.location.LocationSample
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Detects user mobility state to dynamically tune telemetry strategies.
 *
 * Implements Task 15 requirement:
 * Classifies movement into STATIONARY, WALKING, RUNNING, VEHICLE, UNKNOWN.
 * Moving rapidly justifies more frequent location sampling than being stationary.
 */
class MovementDetector(
    private val smoothingWindowSize: Int = 3
) {

    private val _currentMovement = MutableStateFlow(MovementState.UNKNOWN)
    val currentMovement: StateFlow<MovementState> = _currentMovement.asStateFlow()

    private var lastLocationSample: LocationSample? = null
    private val recentClassifications = ArrayDeque<MovementState>(smoothingWindowSize)

    companion object {
        const val SPEED_STATIONARY_MAX_MPS = 0.5f // ~1.8 km/h
        const val SPEED_WALKING_MAX_MPS = 2.5f    // ~9.0 km/h
        const val SPEED_RUNNING_MAX_MPS = 6.0f    // ~21.6 km/h

        /**
         * Pure function to classify raw speed in meters per second.
         */
        fun classifySpeed(speedMps: Float): MovementState {
            if (speedMps.isNaN() || speedMps < 0f) return MovementState.UNKNOWN
            return when {
                speedMps < SPEED_STATIONARY_MAX_MPS -> MovementState.STATIONARY
                speedMps < SPEED_WALKING_MAX_MPS -> MovementState.WALKING
                speedMps < SPEED_RUNNING_MAX_MPS -> MovementState.RUNNING
                else -> MovementState.VEHICLE
            }
        }

        /**
         * Haversine distance in meters between two coordinates.
         */
        fun calculateDistanceMeters(
            lat1: Double, lon1: Double,
            lat2: Double, lon2: Double
        ): Double {
            val r = 6371000.0 // Earth radius in meters
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return r * c
        }

        /**
         * Pure function to classify movement from displacement across time.
         * Accounts for GPS jitter threshold (displacement smaller than threshold is treated as stationary).
         */
        fun classifyDisplacement(
            distanceMeters: Double,
            timeDeltaMs: Long,
            jitterThresholdMeters: Float = 0f
        ): MovementState {
            if (timeDeltaMs <= 0L || distanceMeters < 0.0 || distanceMeters.isNaN() || distanceMeters.isInfinite()) {
                return MovementState.UNKNOWN
            }
            if (distanceMeters <= jitterThresholdMeters) {
                return MovementState.STATIONARY
            }
            val speedMps = (distanceMeters / (timeDeltaMs / 1000.0)).toFloat()
            return classifySpeed(speedMps)
        }

        /**
         * Classify movement from accelerometer variance (useful indoors / no GPS fix).
         * @param accelerationMagnitudes list of sqrt(x^2 + y^2 + z^2) samples
         */
        fun classifyAccelerometerVariance(accelerationMagnitudes: List<Float>): MovementState {
            if (accelerationMagnitudes.size < 3) return MovementState.UNKNOWN
            val mean = accelerationMagnitudes.average()
            val variance = accelerationMagnitudes.map { (it - mean) * (it - mean) }.average()

            return when {
                variance < 0.3 -> MovementState.STATIONARY
                variance < 2.5 -> MovementState.WALKING
                variance < 25.0 -> MovementState.RUNNING
                else -> MovementState.VEHICLE
            }
        }
    }

    /**
     * Process an incoming location sample and update movement classification.
     */
    @Synchronized
    fun processLocation(sample: LocationSample): MovementState {
        val prev = lastLocationSample
        val rawState: MovementState = if (sample.speed > 0f) {
            classifySpeed(sample.speed)
        } else {
            if (prev != null && sample.timestamp > prev.timestamp) {
                val dist = calculateDistanceMeters(
                    prev.latitude, prev.longitude,
                    sample.latitude, sample.longitude
                )
                val deltaMs = sample.timestamp - prev.timestamp
                // Noise threshold: displacement within half of fix accuracy (or at least 3m) is GPS jitter
                val jitterThreshold = maxOf(sample.accuracy * 0.5f, 3.0f)
                classifyDisplacement(dist, deltaMs, jitterThreshold)
            } else {
                MovementState.STATIONARY
            }
        }

        if (prev == null || sample.timestamp >= prev.timestamp) {
            lastLocationSample = sample
        }
        val smoothed = applySmoothing(rawState)
        _currentMovement.value = smoothed
        return smoothed
    }

    /**
     * Directly update movement state with a raw speed reading.
     */
    @Synchronized
    fun processSpeed(speedMps: Float): MovementState {
        val rawState = classifySpeed(speedMps)
        val smoothed = applySmoothing(rawState)
        _currentMovement.value = smoothed
        return smoothed
    }

    /**
     * Force reset internal movement tracker.
     */
    @Synchronized
    fun reset() {
        lastLocationSample = null
        recentClassifications.clear()
        _currentMovement.value = MovementState.UNKNOWN
    }

    private fun applySmoothing(newState: MovementState): MovementState {
        if (recentClassifications.size >= smoothingWindowSize) {
            recentClassifications.removeFirst()
        }
        recentClassifications.addLast(newState)

        // Find majority classification in the window
        val frequencyMap = recentClassifications.groupingBy { it }.eachCount()
        val maxCount = frequencyMap.values.maxOrNull() ?: 1
        val candidates = frequencyMap.filter { it.value == maxCount }.keys

        return if (candidates.size == 1) {
            candidates.first()
        } else {
            // In case of a tie, choose the most recent state
            newState
        }
    }
}
