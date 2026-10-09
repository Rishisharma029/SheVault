package com.shevault.core.network.dto

import com.google.gson.annotations.SerializedName

data class CheckInCreateDto(
    @SerializedName("title")
    val title: String,
    @SerializedName("destination")
    val destination: String? = null,
    @SerializedName("expected_at")
    val expectedAt: String, // ISO-8601 string
    @SerializedName("contact_id")
    val contactId: String? = null
)

data class CheckInUpdateDto(
    @SerializedName("status")
    val status: String? = null, // ACTIVE, COMPLETED, OVERDUE, CANCELLED
    @SerializedName("completed_at")
    val completedAt: String? = null
)

data class CheckInReadDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("contact_id")
    val contactId: String? = null,
    @SerializedName("title")
    val title: String,
    @SerializedName("destination")
    val destination: String? = null,
    @SerializedName("expected_at")
    val expectedAt: String,
    @SerializedName("started_at")
    val startedAt: String,
    @SerializedName("completed_at")
    val completedAt: String? = null,
    @SerializedName("status")
    val status: String,
    @SerializedName("created_at")
    val createdAt: String
)
