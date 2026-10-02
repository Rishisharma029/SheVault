package com.shevault.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tracks multi-channel emergency dispatch attempts (SMS, backend HTTP, MQTT, etc.).
 *
 * Implements Task 12 requirement:
 * DeliveryAttempt: id, incidentId, eventId, channel, recipient, status, attemptCount, timestamp, error.
 */
@Entity(
    tableName = "delivery_attempts",
    indices = [
        Index(value = ["incidentId"]),
        Index(value = ["status"]),
        Index(value = ["timestamp"])
    ]
)
data class DeliveryAttemptEntity(
    @PrimaryKey val id: String,
    val incidentId: String,
    val eventId: String? = null,
    val channel: String, // "SMS", "HTTP", "MQTT", "WEBSOCKET"
    val recipient: String,
    val status: String,  // "PENDING", "SENT", "DELIVERED", "FAILED"
    val attemptCount: Int = 1,
    val timestamp: Long,
    val error: String? = null
)
