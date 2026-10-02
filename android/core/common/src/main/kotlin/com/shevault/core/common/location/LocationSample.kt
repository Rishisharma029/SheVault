package com.shevault.core.common.location

import java.util.UUID

/**
 * Standard geospatial location sample stored and propagated throughout SheVault.
 *
 * Implements Task 13 requirement:
 * Store: timestamp, latitude, longitude, accuracy, speed, bearing, provider.
 */
data class LocationSample(
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val speed: Float = 0f,
    val bearing: Float = 0f,
    val provider: String = "fused",
    val id: String = UUID.randomUUID().toString(),
    val incidentId: String? = null
)
