package com.shevault.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Android Keystore hardware-backed encryption manager for sensitive emergency data,
 * credentials, decoy PINs, and encrypted offline evidence vaults.
 */
class KeystoreManager {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$ENCRYPTION_PADDING"
        private const val GCM_TAG_LENGTH = 128
        private const val MASTER_KEY_ALIAS = "SheVaultMasterKey"
    }

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    private fun getOrCreateKey(): SecretKey {
        if (keyStore.containsAlias(MASTER_KEY_ALIAS)) {
            val entry = keyStore.getEntry(MASTER_KEY_ALIAS, null) as KeyStore.SecretKeyEntry
            return entry.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(ALGORITHM, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(BLOCK_MODE)
            .setEncryptionPaddings(ENCRYPTION_PADDING)
            .setKeySize(256)
            .setUserAuthenticationRequired(false)
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    fun encrypt(plainText: ByteArray): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        val cipherText = cipher.doFinal(plainText)
        return Pair(iv, cipherText)
    }

    fun decrypt(iv: ByteArray, cipherText: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), spec)
        return cipher.doFinal(cipherText)
    }
}
