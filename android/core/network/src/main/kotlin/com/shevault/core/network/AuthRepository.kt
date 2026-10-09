package com.shevault.core.network

import com.shevault.core.network.dto.LoginRequestDto
import com.shevault.core.network.dto.PinSetupRequestDto
import com.shevault.core.network.dto.RegisterRequestDto
import com.shevault.core.network.dto.TokenResponseDto
import com.shevault.core.network.dto.UserReadDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Interface defining complete authentication operations.
 */
interface AuthRepository {
    val currentUser: StateFlow<UserReadDto?>
    val isAuthenticated: StateFlow<Boolean>

    suspend fun register(name: String, phone: String, email: String, password: String): NetworkResult<TokenResponseDto>
    suspend fun login(email: String, password: String): NetworkResult<TokenResponseDto>
    suspend fun restoreSession(): NetworkResult<UserReadDto>
    suspend fun setupPins(safePin: String, duressPin: String): NetworkResult<Boolean>
    suspend fun logout(): NetworkResult<Boolean>
}

/**
 * Production AuthRepository implementing authenticated communication with FastAPI backend.
 * Stores tokens securely using Keystore-backed TokenManager.
 */
class AuthRepositoryImpl(
    private val apiClient: ApiClient,
    private val tokenManager: TokenManager
) : AuthRepository {

    private val _currentUser = MutableStateFlow<UserReadDto?>(null)
    override val currentUser: StateFlow<UserReadDto?> = _currentUser.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(tokenManager.hasTokens())
    override val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    override suspend fun register(
        name: String,
        phone: String,
        email: String,
        password: String
    ): NetworkResult<TokenResponseDto> {
        val result = apiClient.safeApiCall {
            register(
                RegisterRequestDto(
                    name = name,
                    phone = phone,
                    email = email,
                    password = password
                )
            )
        }

        if (result is NetworkResult.Success) {
            tokenManager.saveTokens(result.data.accessToken, result.data.refreshToken)
            _isAuthenticated.value = true
            // Load user profile
            restoreSession()
        }

        return result
    }

    override suspend fun login(email: String, password: String): NetworkResult<TokenResponseDto> {
        val result = apiClient.safeApiCall {
            login(LoginRequestDto(email = email, password = password))
        }

        if (result is NetworkResult.Success) {
            tokenManager.saveTokens(result.data.accessToken, result.data.refreshToken)
            _isAuthenticated.value = true
            restoreSession()
        }

        return result
    }

    override suspend fun restoreSession(): NetworkResult<UserReadDto> {
        if (!tokenManager.hasTokens()) {
            _isAuthenticated.value = false
            _currentUser.value = null
            return NetworkResult.ApiError(
                code = "NO_SAVED_SESSION",
                message = "No authentication credentials found",
                httpCode = 401
            )
        }

        val result = apiClient.safeApiCall { getCurrentUser() }
        if (result is NetworkResult.Success) {
            _currentUser.value = result.data
            _isAuthenticated.value = true
        } else if (result is NetworkResult.ApiError && result.httpCode == 401) {
            tokenManager.clearTokens()
            _isAuthenticated.value = false
            _currentUser.value = null
        }

        return result
    }

    override suspend fun setupPins(safePin: String, duressPin: String): NetworkResult<Boolean> {
        return apiClient.safeApiCall {
            setupPins(PinSetupRequestDto(safePin = safePin, duressPin = duressPin))
        }
    }

    override suspend fun logout(): NetworkResult<Boolean> {
        val result = apiClient.safeApiCall { logout() }
        tokenManager.clearTokens()
        _currentUser.value = null
        _isAuthenticated.value = false
        return result
    }
}
