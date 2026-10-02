package com.shevault.core.common.location

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory / simulated implementation of LocationRepository.
 * Useful for unit tests, previews, and environments without Google Play Services.
 */
class SimulatedLocationRepository(
    initialLocation: LocationSample? = null
) : LocationRepository {

    private val _currentLocation = MutableStateFlow(initialLocation)
    override val currentLocation: StateFlow<LocationSample?> = _currentLocation.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    override val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _trackingFlow = MutableSharedFlow<LocationSample>(extraBufferCapacity = 64)

    private var currentIntervalMs: Long = 5000L

    override suspend fun getCurrentLocation(): LocationSample? {
        return _currentLocation.value
    }

    override fun startTracking(intervalMs: Long): Flow<LocationSample> {
        currentIntervalMs = intervalMs
        _isTracking.value = true
        return _trackingFlow.asSharedFlow()
    }

    override fun stopTracking() {
        _isTracking.value = false
    }

    override suspend fun getLastKnownLocation(): LocationSample? {
        return _currentLocation.value
    }

    override fun updateTrackingInterval(intervalMs: Long) {
        currentIntervalMs = intervalMs
    }

    fun getTrackingInterval(): Long = currentIntervalMs

    /**
     * Helper to simulate a new GPS fix arriving from hardware.
     */
    suspend fun emitLocation(sample: LocationSample) {
        _currentLocation.value = sample
        if (_isTracking.value) {
            _trackingFlow.emit(sample)
        }
    }
}
