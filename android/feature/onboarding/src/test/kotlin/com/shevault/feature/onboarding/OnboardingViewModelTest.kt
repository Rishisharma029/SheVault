package com.shevault.feature.onboarding

import com.shevault.core.network.AuthRepository
import com.shevault.core.network.NetworkResult
import com.shevault.core.network.dto.TokenResponseDto
import com.shevault.core.network.dto.UserReadDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeAuthRepository : AuthRepository {
    private val _currentUser = MutableStateFlow<UserReadDto?>(null)
    override val currentUser: StateFlow<UserReadDto?> = _currentUser.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    override val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    var registerCalled = false
    var pinsSetupCalled = false
    var shouldFail = false

    override suspend fun register(
        name: String,
        phone: String,
        email: String,
        password: String
    ): NetworkResult<TokenResponseDto> {
        registerCalled = true
        if (shouldFail) {
            return NetworkResult.ApiError(code = "INVALID_DATA", message = "Registration failed", httpCode = 400)
        }
        val token = TokenResponseDto("fake_access", "fake_refresh", "Bearer", 3600)
        _isAuthenticated.value = true
        return NetworkResult.Success(token)
    }

    override suspend fun login(email: String, password: String): NetworkResult<TokenResponseDto> {
        return NetworkResult.Success(TokenResponseDto("fake_access", "fake_refresh", "Bearer", 3600))
    }

    override suspend fun restoreSession(): NetworkResult<UserReadDto> {
        return NetworkResult.ApiError("NO_SESSION", "No session", 401)
    }

    override suspend fun setupPins(safePin: String, duressPin: String): NetworkResult<Boolean> {
        pinsSetupCalled = true
        return NetworkResult.Success(true)
    }

    override suspend fun logout(): NetworkResult<Boolean> {
        _isAuthenticated.value = false
        _currentUser.value = null
        return NetworkResult.Success(true)
    }
}

class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var fakeAuthRepo: FakeAuthRepository
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        fakeAuthRepo = FakeAuthRepository()
        viewModel = OnboardingViewModel(fakeAuthRepo, testScope)
    }

    @Test
    fun testOnboardingStepProgression() {
        assertEquals(1, viewModel.uiState.value.step)
        viewModel.nextStep()
        assertEquals(2, viewModel.uiState.value.step)
        viewModel.previousStep()
        assertEquals(1, viewModel.uiState.value.step)
    }

    @Test
    fun testPinEntryAndBackendRegistration() = runTest(testDispatcher) {
        // Step 6 pin setup
        viewModel.onPinDigit("1")
        viewModel.onPinDigit("2")
        viewModel.onPinDigit("3")
        viewModel.onPinDigit("4")
        assertEquals("1234", viewModel.uiState.value.currentPinInput)

        // Confirm safe PIN -> transitions to step 2 for duress PIN
        viewModel.onPinConfirmed()
        assertEquals(2, viewModel.uiState.value.pinSetupStep)
        assertEquals("1234", viewModel.uiState.value.safePin)
        assertEquals("", viewModel.uiState.value.currentPinInput)

        // Enter duress PIN
        viewModel.onPinDigit("9")
        viewModel.onPinDigit("9")
        viewModel.onPinDigit("9")
        viewModel.onPinDigit("9")
        viewModel.onPinConfirmed()

        testScheduler.advanceUntilIdle()

        assertTrue(fakeAuthRepo.registerCalled)
        assertTrue(fakeAuthRepo.pinsSetupCalled)
        assertTrue(viewModel.uiState.value.isCompleted)
        assertEquals(7, viewModel.uiState.value.step)
    }
}
