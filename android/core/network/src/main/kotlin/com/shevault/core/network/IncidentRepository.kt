package com.shevault.core.network

import com.shevault.core.network.dto.IncidentCancelRequestDto
import com.shevault.core.network.dto.IncidentCreateRequestDto
import com.shevault.core.network.dto.IncidentCreateResponseDto
import com.shevault.core.network.dto.IncidentDisarmResponseDto
import com.shevault.core.network.dto.IncidentEventCreateDto
import com.shevault.core.network.dto.IncidentLocationInputDto
import com.shevault.core.network.dto.IncidentReadDto
import com.shevault.core.network.dto.LocationBatchCreateDto
import com.shevault.core.network.dto.LocationSampleCreateDto
import com.shevault.core.network.dto.TelemetryPolicyResponseDto
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * Interface defining complete cloud Incident operations.
 */
interface IncidentRepository {
    suspend fun createIncident(
        clientSessionId: String,
        deviceId: String?,
        activationMethod: String,
        battery: Int?,
        latitude: Double?,
        longitude: Double?,
        accuracy: Float?
    ): NetworkResult<IncidentCreateResponseDto>

    suspend fun cancelIncident(incidentId: String, pin: String): NetworkResult<IncidentDisarmResponseDto>
    suspend fun cancelWithDuress(incidentId: String, duressPin: String): NetworkResult<IncidentDisarmResponseDto>
    suspend fun getIncident(incidentId: String): NetworkResult<IncidentReadDto>
    suspend fun listIncidents(limit: Int = 50, offset: Int = 0): NetworkResult<List<IncidentReadDto>>
    suspend fun getTelemetryPolicy(incidentId: String): NetworkResult<TelemetryPolicyResponseDto>
    suspend fun streamLocation(
        incidentId: String,
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        speed: Float? = null,
        bearing: Float? = null
    ): NetworkResult<Unit>
}

/**
 * Implementation of IncidentRepository communicating with FastAPI backend.
 * Provides idempotent activation via Idempotency-Key header.
 */
class IncidentRepositoryImpl(
    private val apiClient: ApiClient
) : IncidentRepository {

    override suspend fun createIncident(
        clientSessionId: String,
        deviceId: String?,
        activationMethod: String,
        battery: Int?,
        latitude: Double?,
        longitude: Double?,
        accuracy: Float?
    ): NetworkResult<IncidentCreateResponseDto> {
        val locationInput = if (latitude != null && longitude != null && accuracy != null) {
            IncidentLocationInputDto(
                latitude = latitude,
                longitude = longitude,
                accuracy = accuracy
            )
        } else null

        val nowIso = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        val req = IncidentCreateRequestDto(
            clientSessionId = clientSessionId,
            deviceId = deviceId,
            activationMethod = activationMethod,
            startedAt = nowIso,
            battery = battery,
            location = locationInput,
            connectivityState = "ONLINE"
        )

        return apiClient.safeApiCall {
            createIncident(req, idempotencyKey = clientSessionId)
        }
    }

    override suspend fun cancelIncident(
        incidentId: String,
        pin: String
    ): NetworkResult<IncidentDisarmResponseDto> {
        return apiClient.safeApiCall {
            cancelIncident(incidentId, IncidentCancelRequestDto(pin = pin))
        }
    }

    override suspend fun cancelWithDuress(
        incidentId: String,
        duressPin: String
    ): NetworkResult<IncidentDisarmResponseDto> {
        return apiClient.safeApiCall {
            triggerDuress(incidentId, IncidentCancelRequestDto(pin = duressPin))
        }
    }

    override suspend fun getIncident(incidentId: String): NetworkResult<IncidentReadDto> {
        return apiClient.safeApiCall {
            getIncident(incidentId)
        }
    }

    override suspend fun listIncidents(limit: Int, offset: Int): NetworkResult<List<IncidentReadDto>> {
        return apiClient.safeApiCall {
            listIncidents(limit = limit, offset = offset)
        }
    }

    override suspend fun getTelemetryPolicy(incidentId: String): NetworkResult<TelemetryPolicyResponseDto> {
        return apiClient.safeApiCall {
            getTelemetryPolicy(incidentId)
        }
    }

    override suspend fun streamLocation(
        incidentId: String,
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        speed: Float?,
        bearing: Float?
    ): NetworkResult<Unit> {
        val nowIso = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        val sample = LocationSampleCreateDto(
            timestamp = nowIso,
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            speed = speed,
            bearing = bearing,
            provider = "FUSED"
        )

        val result = apiClient.safeApiCall {
            uploadLocation(incidentId, sample)
        }
        return result.map { }
    }
}
