package com.shevault.core.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.shevault.core.security.KeystoreManager
import java.nio.charset.StandardCharsets

/**
 * Interface managing session tokens.
 */
interface TokenManager {
    fun saveTokens(accessToken: String, refreshToken: String)
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun clearTokens()
    fun hasTokens(): Boolean
}

/**
 * Hardware-backed Encrypted Token Manager.
 * Uses Android Keystore (AES-256 GCM) to encrypt sensitive JWT tokens before persisting
 * in SharedPreferences. Never stores tokens in plaintext.
 */
class SecureTokenManager(
    private val context: Context,
    private val keystoreManager: KeystoreManager = KeystoreManager()
) : TokenManager {

    companion object {
        private const val PREFS_NAME = "shevault_secure_auth_prefs"
        private const val KEY_ACCESS_TOKEN_CIPHER = "enc_access_token"
        private const val KEY_ACCESS_TOKEN_IV = "iv_access_token"
        private const val KEY_REFRESH_TOKEN_CIPHER = "enc_refresh_token"
        private const val KEY_REFRESH_TOKEN_IV = "iv_refresh_token"
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    @Synchronized
    override fun saveTokens(accessToken: String, refreshToken: String) {
        val (accessIv, accessCipher) = keystoreManager.encrypt(accessToken.toByteArray(StandardCharsets.UTF_8))
        val (refreshIv, refreshCipher) = keystoreManager.encrypt(refreshToken.toByteArray(StandardCharsets.UTF_8))

        prefs.edit()
            .putString(KEY_ACCESS_TOKEN_IV, Base64.encodeToString(accessIv, Base64.NO_WRAP))
            .putString(KEY_ACCESS_TOKEN_CIPHER, Base64.encodeToString(accessCipher, Base64.NO_WRAP))
            .putString(KEY_REFRESH_TOKEN_IV, Base64.encodeToString(refreshIv, Base64.NO_WRAP))
            .putString(KEY_REFRESH_TOKEN_CIPHER, Base64.encodeToString(refreshCipher, Base64.NO_WRAP))
            .apply()
    }

    @Synchronized
    override fun getAccessToken(): String? {
        val ivStr = prefs.getString(KEY_ACCESS_TOKEN_IV, null) ?: return null
        val cipherStr = prefs.getString(KEY_ACCESS_TOKEN_CIPHER, null) ?: return null

        return try {
            val iv = Base64.decode(ivStr, Base64.NO_WRAP)
            val cipher = Base64.decode(cipherStr, Base64.NO_WRAP)
            val plainBytes = keystoreManager.decrypt(iv, cipher)
            String(plainBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    @Synchronized
    override fun getRefreshToken(): String? {
        val ivStr = prefs.getString(KEY_REFRESH_TOKEN_IV, null) ?: return null
        val cipherStr = prefs.getString(KEY_REFRESH_TOKEN_CIPHER, null) ?: return null

        return try {
            val iv = Base64.decode(ivStr, Base64.NO_WRAP)
            val cipher = Base64.decode(cipherStr, Base64.NO_WRAP)
            val plainBytes = keystoreManager.decrypt(iv, cipher)
            String(plainBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    @Synchronized
    override fun clearTokens() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN_IV)
            .remove(KEY_ACCESS_TOKEN_CIPHER)
            .remove(KEY_REFRESH_TOKEN_IV)
            .remove(KEY_REFRESH_TOKEN_CIPHER)
            .apply()
    }

    override fun hasTokens(): Boolean {
        return prefs.contains(KEY_ACCESS_TOKEN_CIPHER)
    }
}

/**
 * In-memory Token Manager for unit testing and detached environments.
 */
class InMemoryTokenManager : TokenManager {
    private var accessToken: String? = null
    private var refreshToken: String? = null

    override fun saveTokens(accessToken: String, refreshToken: String) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
    }

    override fun getAccessToken(): String? = accessToken

    override fun getRefreshToken(): String? = refreshToken

    override fun clearTokens() {
        accessToken = null
        refreshToken = null
    }

    override fun hasTokens(): Boolean = accessToken != null
}
