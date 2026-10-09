package com.shevault.core.network

import com.google.gson.Gson
import com.shevault.core.network.dto.ApiResponseDto
import com.shevault.core.network.dto.LoginRequestDto
import com.shevault.core.network.dto.TokenResponseDto
import com.shevault.core.network.dto.UserReadDto
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NetworkLayerUnitTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var tokenManager: InMemoryTokenManager
    private lateinit var apiClient: ApiClient

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        tokenManager = InMemoryTokenManager()
        val mockBaseUrl = mockWebServer.url("/").toString()
        apiClient = ApiClient(
            tokenManager = tokenManager,
            baseUrl = mockBaseUrl,
            enableLogging = false
        )
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testTokenManager_save_and_clear() {
        assertFalse(tokenManager.hasTokens())
        tokenManager.saveTokens("access_123", "refresh_456")
        assertTrue(tokenManager.hasTokens())
        assertEquals("access_123", tokenManager.getAccessToken())
        assertEquals("refresh_456", tokenManager.getRefreshToken())

        tokenManager.clearTokens()
        assertFalse(tokenManager.hasTokens())
        assertNull(tokenManager.getAccessToken())
    }

    @Test
    fun testSafeApiCall_successfulResponse() = runBlocking {
        val userDto = UserReadDto(
            id = "usr-001",
            name = "Rishi Sharma",
            phone = "+919876543210",
            email = "rishi@example.com",
            status = "ACTIVE",
            hasSafePin = true,
            hasDuressPin = true,
            createdAt = "2026-10-09T18:00:00Z",
            updatedAt = "2026-10-09T18:00:00Z"
        )
        val envelope = ApiResponseDto(
            success = true,
            data = userDto,
            message = "Success",
            requestId = "req-1"
        )

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(Gson().toJson(envelope))
        )

        tokenManager.saveTokens("valid_token", "refresh_token")
        val result = apiClient.safeApiCall { getCurrentUser() }

        assertTrue(result is NetworkResult.Success)
        val data = (result as NetworkResult.Success).data
        assertEquals("usr-001", data.id)
        assertEquals("Rishi Sharma", data.name)

        // Verify Authorization header was sent
        val recorded = mockWebServer.takeRequest()
        assertEquals("Bearer valid_token", recorded.getHeader("Authorization"))
    }

    @Test
    fun testSafeApiCall_apiErrorParsed() = runBlocking {
        val errorJson = """
            {
                "error": {
                    "code": "INVALID_CREDENTIALS",
                    "message": "Incorrect email or password",
                    "request_id": "req-999",
                    "details": null
                }
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setHeader("Content-Type", "application/json")
                .setBody(errorJson)
        )

        val result = apiClient.safeApiCall {
            login(LoginRequestDto(email = "test@example.com", password = "wrong"))
        }

        assertTrue(result is NetworkResult.ApiError)
        val apiError = result as NetworkResult.ApiError
        assertEquals("INVALID_CREDENTIALS", apiError.code)
        assertEquals("Incorrect email or password", apiError.message)
        assertEquals(401, apiError.httpCode)
        assertEquals("req-999", apiError.requestId)
    }

    @Test
    fun testAuthInterceptor_refreshesTokenOn401() = runBlocking {
        tokenManager.saveTokens("expired_token", "valid_refresh")

        // First call to /auth/me returns 401
        val unauthorizedError = """
            {
                "error": {
                    "code": "TOKEN_EXPIRED",
                    "message": "Access token expired"
                }
            }
        """.trimIndent()
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setHeader("Content-Type", "application/json")
                .setBody(unauthorizedError)
        )

        // Refresh endpoint response
        val refreshEnvelope = ApiResponseDto(
            success = true,
            data = TokenResponseDto(
                accessToken = "new_refreshed_token",
                refreshToken = "new_refresh_token_2",
                tokenType = "Bearer",
                expiresIn = 3600
            )
        )
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(Gson().toJson(refreshEnvelope))
        )

        // Retried call with new token returns 200
        val userEnvelope = ApiResponseDto(
            success = true,
            data = UserReadDto(
                id = "usr-001",
                name = "Rishi Sharma",
                phone = "+919876543210",
                email = "rishi@example.com",
                status = "ACTIVE",
                hasSafePin = true,
                hasDuressPin = true,
                createdAt = "2026-10-09T18:00:00Z",
                updatedAt = "2026-10-09T18:00:00Z"
            )
        )
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(Gson().toJson(userEnvelope))
        )

        val result = apiClient.safeApiCall { getCurrentUser() }

        assertTrue(result is NetworkResult.Success)
        assertEquals("new_refreshed_token", tokenManager.getAccessToken())
        assertEquals("new_refresh_token_2", tokenManager.getRefreshToken())
    }
}
