package com.shevault.core.network.dto

import com.google.gson.annotations.SerializedName

data class ContactPermissionsDto(
    @SerializedName("incident_alerts")
    val incidentAlerts: Boolean = true,
    @SerializedName("location")
    val location: Boolean = true,
    @SerializedName("battery")
    val battery: Boolean = true,
    @SerializedName("evidence")
    val evidence: Boolean = false
)

data class ContactCreateDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("relationship")
    val relationship: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("priority")
    val priority: String = "PRIMARY",
    @SerializedName("permissions")
    val permissions: ContactPermissionsDto? = null
)

data class ContactUpdateDto(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("relationship")
    val relationship: String? = null,
    @SerializedName("phone")
    val phone: String? = null,
    @SerializedName("priority")
    val priority: String? = null,
    @SerializedName("is_active")
    val isActive: Boolean? = null,
    @SerializedName("permissions")
    val permissions: ContactPermissionsDto? = null
)

data class ContactReadDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("relationship")
    val relationship: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("priority")
    val priority: String,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("permissions")
    val permissions: ContactPermissionsDto? = null,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)
