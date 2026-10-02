package com.shevault.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Granular timeline event for forensic and auditable incident tracking.
 *
 * Implements Task 12 requirement:
 * IncidentEvent: id, incidentId, timestamp, type, payload.
 * Types include: INCIDENT_CREATED, LOCATION_CAPTURED, NETWORK_LOST,
 * NETWORK_RESTORED, ESCALATION_ATTEMPT, ACK_RECEIVED, SAFE_CANCEL,
 * DURESS_CANCEL, INCIDENT_ENDED.
 */
@Entity(
    tableName = "incident_events",
    indices = [
        Index(value = ["incidentId"]),
        Index(value = ["timestamp"]),
        Index(value = ["type"])
    ]
)
data class IncidentEventEntity(
    @PrimaryKey val id: String,
    val incidentId: String,
    val timestamp: Long,
    val type: String,
    val payload: String? = null
)
