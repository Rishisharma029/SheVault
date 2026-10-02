package com.shevault.core.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DuressCredentialManagerTest {

    private lateinit var credentialManager: DuressCredentialManager

    @Before
    fun setup() {
        credentialManager = DuressCredentialManager(maxFailedAttempts = 5, lockoutDurationMs = 10_000L)
    }

    @Test
    fun testDefaultSafeAndDuressVerification() {
        // Default safe PIN: 1234
        val safeResult = credentialManager.verifyPin("1234")
        assertTrue("Entering default safe PIN must yield Safe result", safeResult is CredentialVerificationResult.Safe)

        // Default duress PIN: 9999
        val duressResult = credentialManager.verifyPin("9999")
        assertTrue("Entering default duress PIN must yield Duress result", duressResult is CredentialVerificationResult.Duress)

        // Wrong PIN
        val invalidResult = credentialManager.verifyPin("0000")
        assertTrue("Entering wrong PIN must yield Invalid result", invalidResult is CredentialVerificationResult.Invalid)
        assertEquals(4, (invalidResult as CredentialVerificationResult.Invalid).remainingAttempts)
        assertFalse(invalidResult.isLockedOut)
    }

    @Test
    fun testConfiguringCustomCredentials() {
        val configured = credentialManager.setCredentials("5678", "4321")
        assertTrue("Setting distinct valid 4-digit PINs must succeed", configured)

        assertEquals(CredentialVerificationResult.Safe, credentialManager.verifyPin("5678"))
        assertEquals(CredentialVerificationResult.Duress, credentialManager.verifyPin("4321"))

        // Old PINs must no longer work
        val oldSafeResult = credentialManager.verifyPin("1234")
        assertTrue(oldSafeResult is CredentialVerificationResult.Invalid)
    }

    @Test
    fun testIdenticalSafeAndDuressRejected() {
        val samePins = credentialManager.setCredentials("1234", "1234")
        assertFalse("Safe PIN and Duress PIN cannot be identical", samePins)
    }

    @Test
    fun testInvalidPinLengthRejected() {
        assertFalse("PIN shorter than 4 digits must be rejected", credentialManager.setCredentials("123", "9999"))
        assertFalse("PIN longer than 8 digits must be rejected", credentialManager.setCredentials("123456789", "9999"))
        assertFalse("Non-digit characters must be rejected", credentialManager.setCredentials("12a4", "9999"))
    }

    @Test
    fun testLockoutTriggeredAfterConsecutiveFailures() {
        for (i in 1..4) {
            val res = credentialManager.verifyPin("0000")
            assertTrue(res is CredentialVerificationResult.Invalid)
            assertFalse((res as CredentialVerificationResult.Invalid).isLockedOut)
        }

        // 5th failed attempt triggers lockout
        val fifthRes = credentialManager.verifyPin("0000")
        assertTrue(fifthRes is CredentialVerificationResult.Invalid)
        assertTrue((fifthRes as CredentialVerificationResult.Invalid).isLockedOut)
        assertEquals(0, fifthRes.remainingAttempts)

        // Even with correct PIN, verification fails during lockout
        val lockedRes = credentialManager.verifyPin("1234")
        assertTrue(lockedRes is CredentialVerificationResult.Invalid)
        assertTrue((lockedRes as CredentialVerificationResult.Invalid).isLockedOut)

        // Manual reset clears lockout
        credentialManager.resetLockout()
        val afterReset = credentialManager.verifyPin("1234")
        assertTrue(afterReset is CredentialVerificationResult.Safe)
    }
}
