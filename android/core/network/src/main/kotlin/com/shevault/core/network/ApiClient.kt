package com.shevault.core.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.shevault.core.network.dto.ApiResponseDto
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Factory and client builder for SheVault API service.
 */
class ApiClient(
    val tokenManager: TokenManager,
    private val baseUrl: String = NetworkConfig.activeBaseUrl,
    private val enableLogging: Boolean = true
) {
    val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        .create()

    private val authInterceptor = AuthInterceptor(
        tokenManager = tokenManager,
        baseUrlProvider = { baseUrl },
        gson = gson
    )

    val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(NetworkConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(NetworkConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(NetworkConfig.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)

        if (enableLogging) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(logging)
        }

        builder.build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    /**
     * Executes an API call safely, unwrapping ApiResponseDto<T> and capturing
     * network failures or server errors into NetworkResult<T>.
     */
    suspend fun <T> safeApiCall(call: suspend ApiService.() -> Response<ApiResponseDto<T>>): NetworkResult<T> {
        return try {
            val response = apiService.call()
            if (response.isSuccessful) {
                val envelope = response.body()
                if (envelope != null && envelope.success && envelope.data != null) {
                    NetworkResult.Success(envelope.data)
                } else if (envelope != null && envelope.data == null && envelope.success) {
                    // E.g. Unit or empty data with success
                    @Suppress("UNCHECKED_CAST")
                    NetworkResult.Success(Unit as T)
                } else {
                    NetworkResult.ApiError(
                        code = "RESPONSE_PAYLOAD_EMPTY",
                        message = envelope?.message ?: "Empty or unparseable response payload",
                        httpCode = response.code(),
                        requestId = envelope?.requestId
                    )
                }
            } else {
                val errorBodyStr = response.errorBody()?.string()
                val parsedError = parseErrorBody(errorBodyStr, response.code())
                NetworkResult.ApiError(
                    code = parsedError.code,
                    message = parsedError.message,
                    httpCode = parsedError.httpCode,
                    requestId = parsedError.requestId,
                    details = parsedError.details
                )
            }
        } catch (e: Exception) {
            NetworkResult.NetworkFailure(e)
        }
    }

    private fun parseErrorBody(errorBodyStr: String?, httpCode: Int): ApiException {
        if (!errorBodyStr.isNullOrBlank()) {
            try {
                val envelope = gson.fromJson(errorBodyStr, ApiErrorEnvelope::class.java)
                if (envelope?.error != null) {
                    return ApiException(
                        code = envelope.error.code,
                        message = envelope.error.message,
                        requestId = envelope.error.requestId,
                        httpCode = httpCode,
                        details = envelope.error.details
                    )
                }
            } catch (_: Exception) {
                // Failed to parse backend error envelope, fall back to raw message
            }
        }

        val fallbackCode = when (httpCode) {
            400 -> "BAD_REQUEST"
            401 -> "UNAUTHORIZED"
            403 -> "FORBIDDEN"
            404 -> "NOT_FOUND"
            409 -> "CONFLICT"
            422 -> "VALIDATION_ERROR"
            500 -> "INTERNAL_SERVER_ERROR"
            else -> "HTTP_$httpCode"
        }
        val fallbackMessage = errorBodyStr ?: "HTTP error $httpCode occurred"
        return ApiException(
            code = fallbackCode,
            message = fallbackMessage,
            httpCode = httpCode
        )
    }
}
