package com.shevault.core.network

import com.shevault.core.network.dto.ApiResponseDto
import com.shevault.core.network.dto.CheckInCreateDto
import com.shevault.core.network.dto.CheckInReadDto
import com.shevault.core.network.dto.ContactCreateDto
import com.shevault.core.network.dto.ContactPermissionsDto
import com.shevault.core.network.dto.ContactReadDto
import com.shevault.core.network.dto.ContactUpdateDto
import com.shevault.core.network.dto.DeviceStateCreateDto
import com.shevault.core.network.dto.DeviceStateReadDto
import com.shevault.core.network.dto.IncidentCancelRequestDto
import com.shevault.core.network.dto.IncidentCreateRequestDto
import com.shevault.core.network.dto.IncidentCreateResponseDto
import com.shevault.core.network.dto.IncidentDisarmResponseDto
import com.shevault.core.network.dto.IncidentEventCreateDto
import com.shevault.core.network.dto.IncidentEventReadDto
import com.shevault.core.network.dto.IncidentReadDto
import com.shevault.core.network.dto.LocationBatchCreateDto
import com.shevault.core.network.dto.LocationSampleCreateDto
import com.shevault.core.network.dto.LocationSampleReadDto
import com.shevault.core.network.dto.LoginRequestDto
import com.shevault.core.network.dto.PinSetupRequestDto
import com.shevault.core.network.dto.RefreshTokenRequestDto
import com.shevault.core.network.dto.RegisterRequestDto
import com.shevault.core.network.dto.TelemetryPolicyResponseDto
import com.shevault.core.network.dto.TokenResponseDto
import com.shevault.core.network.dto.UserReadDto
import com.shevault.core.network.dto.UserUpdateDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Complete SheVault Retrofit API contract aligned with FastAPI /api/v1 endpoints.
 */
interface ApiService {

    // ==================== AUTH ====================

    @POST("api/v1/auth/register")
    suspend fun register(
        @Body request: RegisterRequestDto
    ): Response<ApiResponseDto<TokenResponseDto>>

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): Response<ApiResponseDto<TokenResponseDto>>

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequestDto
    ): Response<ApiResponseDto<TokenResponseDto>>

    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<ApiResponseDto<Boolean>>

    @GET("api/v1/auth/me")
    suspend fun getCurrentUser(): Response<ApiResponseDto<UserReadDto>>

    @POST("api/v1/auth/pins")
    suspend fun setupPins(
        @Body request: PinSetupRequestDto
    ): Response<ApiResponseDto<Boolean>>

    // ==================== USERS & PROFILE ====================

    @PUT("api/v1/users/profile")
    suspend fun updateProfile(
        @Body request: UserUpdateDto
    ): Response<ApiResponseDto<UserReadDto>>

    // ==================== INCIDENTS & SOS ====================

    @POST("api/v1/incidents")
    suspend fun createIncident(
        @Body request: IncidentCreateRequestDto,
        @Header("Idempotency-Key") idempotencyKey: String? = null
    ): Response<ApiResponseDto<IncidentCreateResponseDto>>

    @GET("api/v1/incidents/{incident_id}")
    suspend fun getIncident(
        @Path("incident_id") incidentId: String
    ): Response<ApiResponseDto<IncidentReadDto>>

    @GET("api/v1/incidents")
    suspend fun listIncidents(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<ApiResponseDto<List<IncidentReadDto>>>

    @POST("api/v1/incidents/{incident_id}/cancel")
    suspend fun cancelIncident(
        @Path("incident_id") incidentId: String,
        @Body request: IncidentCancelRequestDto
    ): Response<ApiResponseDto<IncidentDisarmResponseDto>>

    @POST("api/v1/incidents/{incident_id}/duress")
    suspend fun triggerDuress(
        @Path("incident_id") incidentId: String,
        @Body request: IncidentCancelRequestDto
    ): Response<ApiResponseDto<IncidentDisarmResponseDto>>

    @GET("api/v1/incidents/{incident_id}/policy")
    suspend fun getTelemetryPolicy(
        @Path("incident_id") incidentId: String
    ): Response<ApiResponseDto<TelemetryPolicyResponseDto>>

    // ==================== INCIDENT EVENTS ====================

    @POST("api/v1/incidents/{incident_id}/events")
    suspend fun recordIncidentEvent(
        @Path("incident_id") incidentId: String,
        @Body request: IncidentEventCreateDto
    ): Response<ApiResponseDto<IncidentEventReadDto>>

    @GET("api/v1/incidents/{incident_id}/events")
    suspend fun listIncidentEvents(
        @Path("incident_id") incidentId: String
    ): Response<ApiResponseDto<List<IncidentEventReadDto>>>

    // ==================== LOCATION & TELEMETRY ====================

    @POST("api/v1/incidents/{incident_id}/locations")
    suspend fun uploadLocation(
        @Path("incident_id") incidentId: String,
        @Body request: LocationSampleCreateDto
    ): Response<ApiResponseDto<LocationSampleReadDto>>

    @POST("api/v1/incidents/{incident_id}/locations/batch")
    suspend fun uploadLocationBatch(
        @Path("incident_id") incidentId: String,
        @Body request: LocationBatchCreateDto
    ): Response<ApiResponseDto<Int>>

    @GET("api/v1/incidents/{incident_id}/locations/latest")
    suspend fun getLatestLocation(
        @Path("incident_id") incidentId: String
    ): Response<ApiResponseDto<LocationSampleReadDto>>

    @POST("api/v1/incidents/{incident_id}/device-state")
    suspend fun updateDeviceState(
        @Path("incident_id") incidentId: String,
        @Body request: DeviceStateCreateDto
    ): Response<ApiResponseDto<DeviceStateReadDto>>

    // ==================== TRUSTED CONTACTS ====================

    @GET("api/v1/contacts")
    suspend fun listContacts(): Response<ApiResponseDto<List<ContactReadDto>>>

    @POST("api/v1/contacts")
    suspend fun createContact(
        @Body request: ContactCreateDto
    ): Response<ApiResponseDto<ContactReadDto>>

    @PUT("api/v1/contacts/{contact_id}")
    suspend fun updateContact(
        @Path("contact_id") contactId: String,
        @Body request: ContactUpdateDto
    ): Response<ApiResponseDto<ContactReadDto>>

    @PUT("api/v1/contacts/{contact_id}/permissions")
    suspend fun updateContactPermissions(
        @Path("contact_id") contactId: String,
        @Body request: ContactPermissionsDto
    ): Response<ApiResponseDto<ContactPermissionsDto>>

    @DELETE("api/v1/contacts/{contact_id}")
    suspend fun deleteContact(
        @Path("contact_id") contactId: String
    ): Response<ApiResponseDto<Boolean>>

    // ==================== CHECK-INS ====================

    @POST("api/v1/checkins")
    suspend fun createCheckIn(
        @Body request: CheckInCreateDto
    ): Response<ApiResponseDto<CheckInReadDto>>

    @GET("api/v1/checkins")
    suspend fun listCheckIns(): Response<ApiResponseDto<List<CheckInReadDto>>>

    @POST("api/v1/checkins/{checkin_id}/complete")
    suspend fun completeCheckIn(
        @Path("checkin_id") checkinId: String
    ): Response<ApiResponseDto<CheckInReadDto>>

    @POST("api/v1/checkins/{checkin_id}/cancel")
    suspend fun cancelCheckIn(
        @Path("checkin_id") checkinId: String
    ): Response<ApiResponseDto<CheckInReadDto>>
}
