package com.shevault.core.network

import com.google.gson.Gson
import com.shevault.core.network.dto.ApiResponseDto
import com.shevault.core.network.dto.RefreshTokenRequestDto
import com.shevault.core.network.dto.TokenResponseDto
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

/**
 * OkHttp Interceptor responsible for:
 * 1. Attaching Authorization: Bearer <token> to requests requiring authentication.
 * 2. Intercepting HTTP 401 Unauthorized responses to perform synchronous token refresh using RefreshToken.
 * 3. Retrying the original request with the fresh token if refresh succeeds.
 * 4. Clearing credentials if refresh token has expired or is invalid.
 */
class AuthInterceptor(
    private val tokenManager: TokenManager,
    private val baseUrlProvider: () -> String = { NetworkConfig.activeBaseUrl },
    private val gson: Gson = Gson()
) : Interceptor {

    private val refreshLock = Any()

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip adding Authorization header for unauthenticated endpoints
        val path = originalRequest.url.encodedPath
        if (isUnauthenticatedPath(path)) {
            return chain.proceed(originalRequest)
        }

        // Attach access token if present
        val token = tokenManager.getAccessToken()
        val authenticatedRequest = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(authenticatedRequest)

        // If 401 Unauthorized, attempt refresh
        if (response.code == 401 && !token.isNullOrBlank()) {
            response.close()

            synchronized(refreshLock) {
                // Check if another thread already refreshed the token
                val currentToken = tokenManager.getAccessToken()
                if (currentToken != null && currentToken != token) {
                    val retriedRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer $currentToken")
                        .build()
                    return chain.proceed(retriedRequest)
                }

                // Attempt refresh via direct synchronous call to avoid interceptor recursion
                val refreshToken = tokenManager.getRefreshToken()
                if (!refreshToken.isNullOrBlank()) {
                    val refreshed = performSyncTokenRefresh(refreshToken)
                    if (refreshed != null) {
                        tokenManager.saveTokens(refreshed.accessToken, refreshed.refreshToken)
                        val retriedRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer ${refreshed.accessToken}")
                            .build()
                        return chain.proceed(retriedRequest)
                    } else {
                        // Refresh failed / expired -> wipe tokens
                        tokenManager.clearTokens()
                    }
                } else {
                    tokenManager.clearTokens()
                }
            }
        }

        return response
    }

    private fun isUnauthenticatedPath(path: String): Boolean {
        return path.endsWith("/auth/login") ||
                path.endsWith("/auth/register") ||
                path.endsWith("/auth/refresh") ||
                path.endsWith("/health")
    }

    private fun performSyncTokenRefresh(refreshToken: String): TokenResponseDto? {
        return try {
            val client = OkHttpClient.Builder()
                .connectTimeout(NetworkConfig.CONNECT_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(NetworkConfig.READ_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val baseUrl = baseUrlProvider().trimEnd('/')
            val refreshUrl = "$baseUrl/api/v1/auth/refresh"

            val bodyJson = gson.toJson(RefreshTokenRequestDto(refreshToken = refreshToken))
            val requestBody = bodyJson.toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(refreshUrl)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBodyStr = response.body?.string() ?: return null
                val type = object : com.google.gson.reflect.TypeToken<ApiResponseDto<TokenResponseDto>>() {}.type
                val parsed: ApiResponseDto<TokenResponseDto> = gson.fromJson(responseBodyStr, type)
                parsed.data
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
