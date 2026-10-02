package com.shevault.core.security

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Result of PIN credential verification.
 */
sealed interface CredentialVerificationResult {
    data object Safe : CredentialVerificationResult
    data object Duress : CredentialVerificationResult
    data class Invalid(
        val remainingAttempts: Int,
        val isLockedOut: Boolean = false
    ) : CredentialVerificationResult
}

/**
 * DuressCredentialManager: Manages Safe credentials and hidden Duress credentials.
 *
 * Implements:
 * 1. Safe credential verification (disarms system cleanly)
 * 2. Duress credential verification (triggers covert COERCED_DURESS protocol)
 * 3. Constant-time digest comparison to prevent side-channel timing attacks
 * 4. Distinct credential constraints (Safe PIN cannot match Duress PIN)
 * 5. Rate limiting / attempt throttling against brute-force attacks
 */
class DuressCredentialManager(
    private val maxFailedAttempts: Int = 5,
    private val lockoutDurationMs: Long = 30_000L
) {

    private val secureRandom = SecureRandom()

    // Default development PINs: Safe = "1234", Duress = "9999"
    private var safeSalt: ByteArray = generateSalt()
    private var safeHash: ByteArray = hashPin("1234", safeSalt)

    private var duressSalt: ByteArray = generateSalt()
    private var duressHash: ByteArray = hashPin("9999", duressSalt)

    private var failedAttempts: Int = 0
    private var lockoutTimestamp: Long = 0L

    /**
     * Verifies the provided PIN against both Safe and Duress credentials.
     * Uses constant-time equality comparisons to prevent timing side channels.
     */
    @Synchronized
    fun verifyPin(enteredPin: String, currentTimeMs: Long = System.currentTimeMillis()): CredentialVerificationResult {
        if (isLockedOut(currentTimeMs)) {
            return CredentialVerificationResult.Invalid(
                remainingAttempts = 0,
                isLockedOut = true
            )
        }

        val enteredSafeHash = hashPin(enteredPin, safeSalt)
        val isSafe = MessageDigest.isEqual(enteredSafeHash, safeHash)

        val enteredDuressHash = hashPin(enteredPin, duressSalt)
        val isDuress = MessageDigest.isEqual(enteredDuressHash, duressHash)

        return when {
            isSafe -> {
                failedAttempts = 0
                CredentialVerificationResult.Safe
            }
            isDuress -> {
                failedAttempts = 0
                CredentialVerificationResult.Duress
            }
            else -> {
                failedAttempts++
                val remaining = (maxFailedAttempts - failedAttempts).coerceAtLeast(0)
                if (failedAttempts >= maxFailedAttempts) {
                    lockoutTimestamp = currentTimeMs
                    CredentialVerificationResult.Invalid(remainingAttempts = 0, isLockedOut = true)
                } else {
                    CredentialVerificationResult.Invalid(remainingAttempts = remaining, isLockedOut = false)
                }
            }
        }
    }

    /**
     * Updates Safe and Duress credentials.
     * Enforces:
     * - Length between 4 and 8 digits
     * - Digits only
     * - Safe PIN != Duress PIN
     */
    @Synchronized
    fun setCredentials(safePin: String, duressPin: String): Boolean {
        if (!isValidPin(safePin) || !isValidPin(duressPin)) {
            return false
        }
        if (safePin == duressPin) {
            return false
        }

        val newSafeSalt = generateSalt()
        val newSafeHash = hashPin(safePin, newSafeSalt)

        val newDuressSalt = generateSalt()
        val newDuressHash = hashPin(duressPin, newDuressSalt)

        this.safeSalt = newSafeSalt
        this.safeHash = newSafeHash
        this.duressSalt = newDuressSalt
        this.duressHash = newDuressHash
        this.failedAttempts = 0
        this.lockoutTimestamp = 0L
        return true
    }

    @Synchronized
    fun resetLockout() {
        failedAttempts = 0
        lockoutTimestamp = 0L
    }

    @Synchronized
    fun isLockedOut(currentTimeMs: Long = System.currentTimeMillis()): Boolean {
        if (lockoutTimestamp == 0L) return false
        val elapsed = currentTimeMs - lockoutTimestamp
        if (elapsed > lockoutDurationMs) {
            lockoutTimestamp = 0L
            failedAttempts = 0
            return false
        }
        return true
    }

    private fun isValidPin(pin: String): Boolean {
        return pin.length in 4..8 && pin.all { it.isDigit() }
    }

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        secureRandom.nextBytes(salt)
        return salt
    }

    private fun hashPin(pin: String, salt: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        return md.digest(pin.toByteArray(Charsets.UTF_8))
    }
}
