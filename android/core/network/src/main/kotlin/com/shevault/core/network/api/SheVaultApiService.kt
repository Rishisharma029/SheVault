package com.shevault.core.network.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class SosAlertRequest(
    val incidentId: String,
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long,
    val triggerSource: String, // MANUAL, VOLUME_BUTTON_SHAKE, VOICE_KEYWORD, GEOFENCE
    val batteryLevel: Int
)

data class SosAlertResponse(
    val status: String,
    val incidentId: String,
    val notifiedContactsCount: Int,
    val emergencyDispatchNotified: Boolean
)

data class LocationBreadcrumbDto(
    val latitude: Double,
    val longitude: Double,
    val speed: Float,
    val accuracy: Float,
    val timestamp: Long
)

interface SheVaultApiService {
    @POST("api/v1/sos/trigger")
    suspend fun triggerSos(@Body request: SosAlertRequest): Response<SosAlertResponse>

    @POST("api/v1/sos/{incidentId}/cancel")
    suspend fun cancelSos(@Path("incidentId") incidentId: String): Response<Unit>

    @POST("api/v1/location/stream")
    suspend fun streamLocation(@Body breadcrumbs: List<LocationBreadcrumbDto>): Response<Unit>

    @GET("api/v1/circle/contacts")
    suspend fun getTrustedContacts(): Response<List<Map<String, Any>>>
}
