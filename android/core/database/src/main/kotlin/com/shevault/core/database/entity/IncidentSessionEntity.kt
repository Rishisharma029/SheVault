package com.shevault.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Session telemetry metadata entity preserved for backward compatibility.
 */
@Entity(tableName = "incident_sessions")
data class IncidentSessionEntity(
    @PrimaryKey val incidentId: String,
    val sessionId: String,
    val createdAt: Long,
    val activationMethod: String,
    val deviceId: String,
    val initialBatteryLevel: Int,
    val initialBatteryCharging: Boolean,
    val initialLatitude: Double?,
    val initialLongitude: Double?,
    val initialAccuracy: Float?,
    val currentState: String,
    val persistentStatus: String,
    val isCoercedDuress: Boolean,
    val updatedAt: Long
)
