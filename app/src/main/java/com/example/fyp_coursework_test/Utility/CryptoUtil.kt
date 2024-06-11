package com.example.fyp_coursework_test.Utility

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey

import java.security.KeyFactory
import java.security.KeyStore
import java.security.spec.X509EncodedKeySpec

object CryptoUtil {

    // generate RSA key pair, store private key in keystore, return the public key
    fun generateKeyPairAndStoreInKeystore(alias: String): PublicKey {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore"
        )

        keyPairGenerator.initialize(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .setKeySize(2048)
                .build()
        )

        val keyPair = keyPairGenerator.generateKeyPair()
        return keyPair.public
    }

    // retrieve the private key from key store
    fun getPrivateKeyFromKeystore(alias: String): PrivateKey? {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val privateKeyEntry = keyStore.getEntry(alias, null) as? KeyStore.PrivateKeyEntry
        return privateKeyEntry?.privateKey
    }

    // retrieve the AES key from key store
    fun getAesKeyFromKeystore(alias: String): SecretKey? {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val aesKeyEntry = keyStore.getEntry(alias,null) as? KeyStore.SecretKeyEntry
        return aesKeyEntry?.secretKey
    }


    // convert Key to string
    fun encodeKeyToString(key: ByteArray): String = Base64.encodeToString(key, Base64.DEFAULT)

    // convert string to public key
    fun decodeStringToPublicKey(encodedKey: String): PublicKey {
        val publicBytes = Base64.decode(encodedKey, Base64.DEFAULT)
        val keySpec = X509EncodedKeySpec(publicBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec)
    }

    // convert string to aes key
    fun decodeStringToAesKeyByte(encodedKey: String): ByteArray? {
        val decodedKey = Base64.decode(encodedKey, Base64.DEFAULT)
        return decodedKey
    }

    // generate 256 bit Aes key
    fun generateAesKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        return keyGenerator.generateKey()
    }

    // convert string to public key
    fun encryptAesKeyWithPublicKey(aesKey: SecretKey, publicKey: PublicKey): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return cipher.doFinal(aesKey.encoded)
    }

    // decrypt an AES key using private key
    fun decryptAesKeyWithPrivateKey(encryptedAesKey: ByteArray, privateKey: PrivateKey): SecretKey {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val aesKeyBytes = cipher.doFinal(encryptedAesKey)
        return SecretKeySpec(aesKeyBytes, "AES")
    }

    // encrypt a string using Aes key
    fun encryptAes(input: String, key: SecretKey): String {
        val c = Cipher.getInstance("AES")
        c.init(Cipher.ENCRYPT_MODE, key)
        val encVal = c.doFinal(input.toByteArray())
        return Base64.encodeToString(encVal, Base64.DEFAULT)
    }

    // decrypt a string using Aes key
    fun decryptAes(encrypted: String, key: SecretKey): String {
        val c = Cipher.getInstance("AES")
        c.init(Cipher.DECRYPT_MODE, key)
        val decodedValue = Base64.decode(encrypted, Base64.DEFAULT)
        val decValue = c.doFinal(decodedValue)
        return String(decValue)
    }
}
