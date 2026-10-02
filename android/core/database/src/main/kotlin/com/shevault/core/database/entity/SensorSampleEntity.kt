package com.shevault.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tracks hardware sensor readings (accelerometer, gyroscope, barometer, audio level).
 *
 * Implements Task 12 requirement:
 * SensorSample: id, incidentId, timestamp, sensorType, x, y, z, value, metadata.
 */
@Entity(
    tableName = "sensor_samples",
    indices = [
        Index(value = ["incidentId"]),
        Index(value = ["sensorType"]),
        Index(value = ["timestamp"])
    ]
)
data class SensorSampleEntity(
    @PrimaryKey val id: String,
    val incidentId: String,
    val timestamp: Long,
    val sensorType: String, // "ACCELEROMETER", "GYROSCOPE", "PRESSURE", "AUDIO_AMPLITUDE"
    val x: Float? = null,
    val y: Float? = null,
    val z: Float? = null,
    val value: Float? = null,
    val metadata: String? = null
)
