package com.shevault.core.common.location

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Separate location abstraction isolating GPS hardware from UI and domain components.
 *
 * Implements Task 13 requirement:
 * Do not let UI components directly access GPS.
 * Exposes:
 * - getCurrentLocation()
 * - startTracking()
 * - stopTracking()
 * - getLastKnownLocation()
 */
interface LocationRepository {

    /**
     * Observable stream of the most recent location sample.
     */
    val currentLocation: StateFlow<LocationSample?>

    /**
     * Whether active background/foreground tracking is currently running.
     */
    val isTracking: StateFlow<Boolean>

    /**
     * Request an immediate one-shot high-accuracy current location fix.
     */
    suspend fun getCurrentLocation(): LocationSample?

    /**
     * Start continuous location tracking with the specified interval.
     * Returns a Flow emitting new location samples as they arrive.
     */
    fun startTracking(intervalMs: Long = 5000L): Flow<LocationSample>

    /**
     * Stop continuous location tracking and release location listeners.
     */
    fun stopTracking()

    /**
     * Retrieve the cached last known location fix without waking GPS hardware.
     */
    suspend fun getLastKnownLocation(): LocationSample?

    /**
     * Dynamically adjust location polling interval (e.g. from TelemetryPolicyEngine).
     */
    fun updateTrackingInterval(intervalMs: Long)
}
