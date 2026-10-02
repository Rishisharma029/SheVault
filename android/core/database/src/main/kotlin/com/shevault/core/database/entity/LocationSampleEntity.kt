package com.shevault.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.shevault.core.common.location.LocationSample

/**
 * Room entity for persisted geospatial breadcrumbs.
 *
 * Implements Task 12 & 13 requirements:
 * LocationSample: timestamp, latitude, longitude, accuracy, speed, bearing, provider.
 */
@Entity(
    tableName = "location_samples",
    indices = [
        Index(value = ["incidentId"]),
        Index(value = ["timestamp"])
    ]
)
data class LocationSampleEntity(
    @PrimaryKey val id: String,
    val incidentId: String? = null,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val speed: Float = 0f,
    val bearing: Float = 0f,
    val provider: String = "fused"
) {
    fun toDomain(): LocationSample = LocationSample(
        id = id,
        incidentId = incidentId,
        timestamp = timestamp,
        latitude = latitude,
        longitude = longitude,
        accuracy = accuracy,
        speed = speed,
        bearing = bearing,
        provider = provider
    )

    companion object {
        fun fromDomain(sample: LocationSample): LocationSampleEntity = LocationSampleEntity(
            id = sample.id,
            incidentId = sample.incidentId,
            timestamp = sample.timestamp,
            latitude = sample.latitude,
            longitude = sample.longitude,
            accuracy = sample.accuracy,
            speed = sample.speed,
            bearing = sample.bearing,
            provider = sample.provider
        )
    }
}
