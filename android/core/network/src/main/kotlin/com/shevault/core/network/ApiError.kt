package com.shevault.core.network

import com.google.gson.annotations.SerializedName

/**
 * Standard API error model matching FastAPI backend response structure:
 * {
 *   "error": {
 *     "code": "AUTHENTICATION_FAILED",
 *     "message": "Invalid email or password",
 *     "request_id": "req-123",
 *     "details": {}
 *   }
 * }
 */
data class ApiErrorEnvelope(
    @SerializedName("error")
    val error: ApiErrorDetail
)

data class ApiErrorDetail(
    @SerializedName("code")
    val code: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("request_id")
    val requestId: String? = null,
    @SerializedName("details")
    val details: Map<String, Any?>? = null
)

/**
 * Parsed application exception representing API-level errors.
 */
class ApiException(
    val code: String,
    override val message: String,
    val requestId: String? = null,
    val httpCode: Int = 0,
    val details: Map<String, Any?>? = null
) : Exception("[$code] $message (HTTP $httpCode)")
