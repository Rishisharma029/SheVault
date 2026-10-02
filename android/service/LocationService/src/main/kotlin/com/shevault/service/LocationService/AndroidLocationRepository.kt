package com.shevault.service.LocationService

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.shevault.core.common.location.LocationRepository
import com.shevault.core.common.location.LocationSample
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Concrete Android implementation of LocationRepository wrapping FusedLocationProviderClient.
 *
 * Implements Task 13 requirement:
 * Separate location abstraction isolating UI components from GPS hardware.
 */
class AndroidLocationRepository(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
) : LocationRepository {

    private val _currentLocation = MutableStateFlow<LocationSample?>(null)
    override val currentLocation: StateFlow<LocationSample?> = _currentLocation.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    override val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val activeCallbacks = java.util.concurrent.CopyOnWriteArrayList<LocationCallback>()
    private var activeIntervalMs: Long = 5000L

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): LocationSample? {
        val cts = CancellationTokenSource()
        return try {
            val task = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cts.token
            )
            val location = task.awaitTask(cts)
            location?.toLocationSample()?.also {
                _currentLocation.value = it
            }
        } catch (_: Exception) {
            _currentLocation.value
        }
    }

    @SuppressLint("MissingPermission")
    override fun startTracking(intervalMs: Long): Flow<LocationSample> = callbackFlow {
        activeIntervalMs = intervalMs
        _isTracking.value = true

        val request = buildLocationRequest(intervalMs)
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (androidLocation in result.locations) {
                    val sample = androidLocation.toLocationSample()
                    _currentLocation.value = sample
                    trySend(sample)
                }
            }
        }
        activeCallbacks.add(callback)

        val looper = Looper.getMainLooper() ?: Looper.myLooper()
        try {
            if (looper != null) {
                fusedLocationClient.requestLocationUpdates(request, callback, looper)
            }
        } catch (_: SecurityException) {
            activeCallbacks.remove(callback)
            if (activeCallbacks.isEmpty()) {
                _isTracking.value = false
            }
            close()
        }

        awaitClose {
            try {
                fusedLocationClient.removeLocationUpdates(callback)
            } catch (_: Exception) {}
            activeCallbacks.remove(callback)
            if (activeCallbacks.isEmpty()) {
                _isTracking.value = false
            }
        }
    }

    override fun stopTracking() {
        for (cb in activeCallbacks) {
            try {
                fusedLocationClient.removeLocationUpdates(cb)
            } catch (_: Exception) {}
        }
        activeCallbacks.clear()
        _isTracking.value = false
    }

    @SuppressLint("MissingPermission")
    override suspend fun getLastKnownLocation(): LocationSample? {
        return try {
            val location = fusedLocationClient.lastLocation.awaitTask()
            val sample = location?.toLocationSample() ?: _currentLocation.value
            if (sample != null) {
                _currentLocation.value = sample
            }
            sample
        } catch (_: Exception) {
            _currentLocation.value
        }
    }

    @SuppressLint("MissingPermission")
    override fun updateTrackingInterval(intervalMs: Long) {
        if (activeIntervalMs == intervalMs || !_isTracking.value) return
        activeIntervalMs = intervalMs

        val looper = Looper.getMainLooper() ?: Looper.myLooper() ?: return
        val request = buildLocationRequest(intervalMs)
        for (cb in activeCallbacks) {
            try {
                fusedLocationClient.removeLocationUpdates(cb)
                fusedLocationClient.requestLocationUpdates(request, cb, looper)
            } catch (_: SecurityException) {
                // Permission lost
            }
        }
    }

    private fun buildLocationRequest(intervalMs: Long): LocationRequest {
        val fastestInterval = (intervalMs / 2).coerceAtLeast(1000L)
        return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(fastestInterval)
            .setMinUpdateDistanceMeters(0f)
            .build()
    }

    private fun android.location.Location.toLocationSample(): LocationSample {
        return LocationSample(
            timestamp = time,
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            speed = speed,
            bearing = bearing,
            provider = provider ?: "fused"
        )
    }

    private suspend fun <T> Task<T>.awaitTask(cancellationTokenSource: CancellationTokenSource? = null): T? = suspendCancellableCoroutine { cont ->
        cancellationTokenSource?.let { cts ->
            cont.invokeOnCancellation { cts.cancel() }
        }
        addOnSuccessListener { result ->
            cont.resume(result)
        }
        addOnFailureListener { exception ->
            cont.resumeWithException(exception)
        }
        addOnCanceledListener {
            cont.cancel()
        }
    }
}
