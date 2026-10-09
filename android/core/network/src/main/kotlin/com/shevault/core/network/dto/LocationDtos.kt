package com.shevault.core.network.dto

import com.google.gson.annotations.SerializedName

data class LocationSampleCreateDto(
    @SerializedName("timestamp")
    val timestamp: String, // ISO-8601 string
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("accuracy")
    val accuracy: Float,
    @SerializedName("speed")
    val speed: Float? = null,
    @SerializedName("bearing")
    val bearing: Float? = null,
    @SerializedName("provider")
    val provider: String? = "FUSED"
)

data class LocationBatchCreateDto(
    @SerializedName("locations")
    val locations: List<LocationSampleCreateDto>
)

data class LocationSampleReadDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("incident_id")
    val incidentId: String,
    @SerializedName("timestamp")
    val timestamp: String,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("accuracy")
    val accuracy: Float,
    @SerializedName("speed")
    val speed: Float? = null,
    @SerializedName("bearing")
    val bearing: Float? = null,
    @SerializedName("provider")
    val provider: String,
    @SerializedName("server_received_at")
    val serverReceivedAt: String
)

data class DeviceStateCreateDto(
    @SerializedName("battery_percent")
    val batteryPercent: Int,
    @SerializedName("is_charging")
    val isCharging: Boolean = false,
    @SerializedName("network")
    val network: String = "CELLULAR",
    @SerializedName("connectivity")
    val connectivity: String = "ONLINE",
    @SerializedName("movement_state")
    val movementState: String = "WALKING"
)

data class DeviceStateReadDto(
    @SerializedName("battery_percent")
    val batteryPercent: Int,
    @SerializedName("is_charging")
    val isCharging: Boolean,
    @SerializedName("network")
    val network: String,
    @SerializedName("connectivity")
    val connectivity: String,
    @SerializedName("movement_state")
    val movementState: String,
    @SerializedName("updated_at")
    val updatedAt: String
)
