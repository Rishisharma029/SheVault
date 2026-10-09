package com.shevault.core.network.dto

import com.google.gson.annotations.SerializedName

data class IncidentLocationInputDto(
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("accuracy")
    val accuracy: Float,
    @SerializedName("speed")
    val speed: Float? = null,
    @SerializedName("bearing")
    val bearing: Float? = null
)

data class IncidentCreateRequestDto(
    @SerializedName("client_session_id")
    val clientSessionId: String,
    @SerializedName("device_id")
    val deviceId: String? = null,
    @SerializedName("activation_method")
    val activationMethod: String = "HOLD", // HOLD, PANIC_GESTURE, CHECK_IN_ESCALATION, SYSTEM
    @SerializedName("started_at")
    val startedAt: String, // ISO-8601 string
    @SerializedName("battery")
    val battery: Int? = null,
    @SerializedName("location")
    val location: IncidentLocationInputDto? = null,
    @SerializedName("connectivity_state")
    val connectivityState: String = "ONLINE"
)

data class IncidentCreateResponseDto(
    @SerializedName("incident_id")
    val incidentId: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("server_time")
    val serverTime: String,
    @SerializedName("accepted")
    val accepted: Boolean = true
)

data class IncidentCancelRequestDto(
    @SerializedName("pin")
    val pin: String
)

data class IncidentDisarmResponseDto(
    @SerializedName("accepted")
    val accepted: Boolean = true,
    @SerializedName("status")
    val status: String = "SAFE_CANCELLED",
    @SerializedName("server_time")
    val serverTime: String,
    @SerializedName("message")
    val message: String = "Safety session terminated"
)

data class IncidentReadDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("device_id")
    val deviceId: String? = null,
    @SerializedName("session_id")
    val sessionId: String,
    @SerializedName("activation_method")
    val activationMethod: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("cancel_type")
    val cancelType: String? = null,
    @SerializedName("started_at")
    val startedAt: String,
    @SerializedName("grace_started_at")
    val graceStartedAt: String? = null,
    @SerializedName("escalated_at")
    val escalatedAt: String? = null,
    @SerializedName("ended_at")
    val endedAt: String? = null,
    @SerializedName("initial_battery")
    val initialBattery: Int? = null,
    @SerializedName("last_battery")
    val lastBattery: Int? = null,
    @SerializedName("last_latitude")
    val lastLatitude: Double? = null,
    @SerializedName("last_longitude")
    val lastLongitude: Double? = null,
    @SerializedName("last_accuracy")
    val lastAccuracy: Float? = null,
    @SerializedName("connectivity_state")
    val connectivityState: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class TelemetryPolicyResponseDto(
    @SerializedName("location_interval_seconds")
    val locationIntervalSeconds: Int = 15,
    @SerializedName("upload_interval_seconds")
    val uploadIntervalSeconds: Int = 10,
    @SerializedName("movement_mode")
    val movementMode: String = "ACTIVE",
    @SerializedName("audio_enabled")
    val audioEnabled: Boolean = false
)

data class IncidentEventCreateDto(
    @SerializedName("event_type")
    val eventType: String,
    @SerializedName("occurred_at")
    val occurredAt: String,
    @SerializedName("payload")
    val payload: Map<String, Any?>? = null
)

data class IncidentEventReadDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("incident_id")
    val incidentId: String,
    @SerializedName("event_type")
    val eventType: String,
    @SerializedName("occurred_at")
    val occurredAt: String,
    @SerializedName("sequence_number")
    val sequenceNumber: Int,
    @SerializedName("payload_json")
    val payloadJson: String? = null,
    @SerializedName("created_at")
    val createdAt: String
)
