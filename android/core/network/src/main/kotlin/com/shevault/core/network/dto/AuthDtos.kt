package com.shevault.core.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Common generic wrapper matching backend APIResponse[T]:
 * {
 *   "success": true,
 *   "data": { ... },
 *   "message": "...",
 *   "request_id": "..."
 * }
 */
data class ApiResponseDto<T>(
    @SerializedName("success")
    val success: Boolean = true,
    @SerializedName("data")
    val data: T? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("request_id")
    val requestId: String? = null
)

// ==================== AUTH DTOS ====================

data class RegisterRequestDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class LoginRequestDto(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class TokenResponseDto(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("token_type")
    val tokenType: String = "Bearer",
    @SerializedName("expires_in")
    val expiresIn: Int
)

data class RefreshTokenRequestDto(
    @SerializedName("refresh_token")
    val refreshToken: String
)

data class PinSetupRequestDto(
    @SerializedName("safe_pin")
    val safePin: String,
    @SerializedName("duress_pin")
    val duressPin: String
)

data class PinVerifyRequestDto(
    @SerializedName("pin")
    val pin: String
)

data class UserReadDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("has_safe_pin")
    val hasSafePin: Boolean,
    @SerializedName("has_duress_pin")
    val hasDuressPin: Boolean,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class UserUpdateDto(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("phone")
    val phone: String? = null
)
