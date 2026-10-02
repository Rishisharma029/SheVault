package com.shevault.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Root Incident model for persistent emergency records.
 *
 * Implements Task 12 requirement:
 * Incident: id, createdAt, activationMethod, status, cancelType,
 * batteryAtStart, batteryAtEnd, lastKnownLatitude, lastKnownLongitude, lastKnownAccuracy.
 */
@Entity(
    tableName = "incidents",
    indices = [
        Index(value = ["createdAt"]),
        Index(value = ["status"])
    ]
)
data class IncidentEntity(
    @PrimaryKey val id: String,
    val createdAt: Long,
    val activationMethod: String,
    val status: String,
    val cancelType: String? = null,
    val batteryAtStart: Int? = null,
    val batteryAtEnd: Int? = null,
    val lastKnownLatitude: Double? = null,
    val lastKnownLongitude: Double? = null,
    val lastKnownAccuracy: Float? = null,
    val updatedAt: Long = createdAt
)
