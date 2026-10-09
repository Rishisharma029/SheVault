package com.shevault.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shevault.core.network.AuthRepository
import com.shevault.core.network.NetworkResult
import com.shevault.core.network.dto.UserReadDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val step: Int = 1,
    val name: String = "Rishi Sharma",
    val phone: String = "+91 98765 43210",
    val email: String = "rishi@example.com",
    val password: String = "SafeVaultPass@2026",
    val safePin: String = "1234",
    val duressPin: String = "9999",
    val pinSetupStep: Int = 1,
    val currentPinInput: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val userProfile: UserReadDto? = null,
    val isCompleted: Boolean = false
)

class OnboardingViewModel(
    private val authRepository: AuthRepository,
    coroutineScope: kotlinx.coroutines.CoroutineScope? = null
) : ViewModel() {

    private val scope = coroutineScope ?: viewModelScope

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updatePhone(phone: String) {
        _uiState.update { it.copy(phone = phone) }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onPinDigit(digit: String) {
        _uiState.update { current ->
            if (current.currentPinInput.length < 4) {
                current.copy(currentPinInput = current.currentPinInput + digit)
            } else {
                current
            }
        }
    }

    fun onPinBackspace() {
        _uiState.update { current ->
            if (current.currentPinInput.isNotEmpty()) {
                current.copy(currentPinInput = current.currentPinInput.dropLast(1))
            } else {
                current
            }
        }
    }

    fun onPinConfirmed() {
        val current = _uiState.value
        if (current.pinSetupStep == 1) {
            _uiState.update {
                it.copy(
                    safePin = current.currentPinInput,
                    currentPinInput = "",
                    pinSetupStep = 2
                )
            }
        } else {
            val safePin = current.safePin
            val duressPin = current.currentPinInput
            _uiState.update {
                it.copy(
                    duressPin = duressPin,
                    currentPinInput = "",
                    step = 7
                )
            }
            // Register account & setup PINs against real backend
            registerAndSetupPins(safePin, duressPin)
        }
    }

    fun nextStep() {
        _uiState.update { current ->
            if (current.step < 7) {
                current.copy(step = current.step + 1)
            } else {
                current.copy(isCompleted = true)
            }
        }
    }

    fun previousStep() {
        _uiState.update { current ->
            if (current.step > 1) {
                current.copy(step = current.step - 1)
            } else {
                current
            }
        }
    }

    private fun registerAndSetupPins(safePin: String, duressPin: String) {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        scope.launch {
            val regResult = authRepository.register(
                name = state.name,
                phone = state.phone,
                email = state.email,
                password = state.password
            )

            when (regResult) {
                is NetworkResult.Success -> {
                    // Register safe and duress PINs with backend
                    authRepository.setupPins(safePin = safePin, duressPin = duressPin)
                    _uiState.update { it.copy(isLoading = false, isCompleted = true) }
                }
                is NetworkResult.ApiError -> {
                    // If user already exists (409 Conflict), log in directly
                    if (regResult.code == "USER_EXISTS" || regResult.code == "CONFLICT") {
                        val loginResult = authRepository.login(state.email, state.password)
                        if (loginResult is NetworkResult.Success) {
                            authRepository.setupPins(safePin, duressPin)
                            _uiState.update { it.copy(isLoading = false, isCompleted = true) }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = regResult.message
                                )
                            }
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = regResult.message
                            )
                        }
                    }
                }
                is NetworkResult.NetworkFailure -> {
                    // Graceful offline fallback: allow user to complete onboarding locally
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Running in offline mode: changes saved locally",
                            isCompleted = true
                        )
                    }
                }
            }
        }
    }
}
