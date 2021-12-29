package com.meehawek.passwordnotepad

import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.RequiresApi
import com.meehawek.passwordnotepad.biometric.BiometricManager
import java.security.SecureRandom
import javax.crypto.spec.GCMParameterSpec

class Helpers(ctx: Context) {
    companion object {
        private var KEY_ALIAS = "kluczyk"
        private var KEY_ALIAS_2 = "kluczyk2"
        private var KEY_SIZE = 256
        private var IV_SIZE = 16
    }

    private var applicationContext = ctx



    fun generateMasterKey() {
        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)
        if (!ks.containsAlias(KEY_ALIAS)) generateKey()
        if (!ks.containsAlias(KEY_ALIAS_2)) generateKey2()
    }

    fun generateIV(size: Int=16): ByteArray {
        val random = SecureRandom()
        val iv = ByteArray(size)
        random.nextBytes(iv)
        return iv
    }

    private fun generateKey() {
        val keygen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val builder = KeyGenParameterSpec.Builder(KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE)
            .setUserAuthenticationRequired(true)
            .setUserAuthenticationValidityDurationSeconds(-1)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setInvalidatedByBiometricEnrollment(true)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)) {
                builder.setUnlockedDeviceRequired(true)
                    .setIsStrongBoxBacked(true)
            }
        }
        keygen.init(builder.build())
        keygen.generateKey()
    }

    private fun generateKey2() {
        val keygen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val builder = KeyGenParameterSpec.Builder(KEY_ALIAS_2,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE)
//            .setUserAuthenticationRequired(true)
//            .setUserAuthenticationValidityDurationSeconds(-1)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            builder.setInvalidatedByBiometricEnrollment(true)
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)) {
                builder.setUnlockedDeviceRequired(true)
                    .setIsStrongBoxBacked(true)
            }
        }
        keygen.init(builder.build())
        keygen.generateKey()
    }


    fun encryptString(plaintext: String, sharedPrefs: SharedPreferences): String? {
        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)

        val key = ks.getKey(KEY_ALIAS_2, null)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val ciphertext: ByteArray = cipher.doFinal(plaintext.toByteArray())
        val iv = cipher.iv

        sharedPrefs.edit().putString("iv", byteArrayToHex(iv)).apply()

        return Base64.encodeToString(ciphertext, Base64.NO_WRAP)
    }


    fun decryptString(secret: String, sharedPrefs: SharedPreferences): ByteArray{
        val sec2 = Base64.decode(secret, Base64.NO_WRAP)
        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)
        val key = ks.getKey(KEY_ALIAS_2, null)

        val iv = hexToByteArray(sharedPrefs.getString("iv", null)!!)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))

        val xd = cipher.doFinal(sec2)

        return xd
    }

    fun byteArrayToHex(bytes: ByteArray) : String{
        val hexChars = "0123456789ABCDEF".toCharArray()
        val result = StringBuffer()

        bytes.forEach {
            val octet = it.toInt()
            val firstIndex = (octet and 0xF0).ushr(4)
            val secondIndex = octet and 0x0F
            result.append(hexChars[firstIndex])
            result.append(hexChars[secondIndex])
        }

        return result.toString()
    }

    fun hexToByteArray(hex: String) : ByteArray{
        val hexChars = "0123456789ABCDEF".toCharArray()
        val result = ByteArray(hex.length / 2)

        for (i in 0 until hex.length step 2) {
            val firstIndex = hexChars.indexOf(hex[i]);
            val secondIndex = hexChars.indexOf(hex[i + 1]);

            val octet = firstIndex.shl(4).or(secondIndex)
            result.set(i.shr(1), octet.toByte())
        }

        return result
    }


    fun generateHash(password : CharArray, sharedPrefs: SharedPreferences) : String {
        val salt: String
        if (sharedPrefs.contains("salt")) salt = sharedPrefs.getString("salt", null)!!
        else {
            salt = generateIV(16).toString()
            sharedPrefs.edit().putString("salt", salt).apply()
        }

        val iterations = 1000
        val keyLength = 256
        val secretKeyFactory : SecretKeyFactory = SecretKeyFactory.getInstance("PBKDF2withHmacSHA256")
        val keySpec : KeySpec = PBEKeySpec(password, salt.toByteArray(), iterations, keyLength)
        val secretKey : SecretKey = secretKeyFactory.generateSecret(keySpec)
        return Base64.encodeToString(secretKey.encoded, Base64.NO_WRAP)
    }

    fun getLocalEncryptionCipher() : Cipher {
        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)
        val key = ks.getKey(KEY_ALIAS, null)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val preferences = applicationContext.getSharedPreferences("com.meehawek.passwordnotepad", Context.MODE_PRIVATE)
        var iv : ByteArray
        if (preferences.contains("iv")){
            iv = hexToByteArray(preferences.getString("iv", "")!!)
            val spec = GCMParameterSpec(IV_SIZE * Byte.SIZE_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            return cipher
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, key, cipher.parameters)
            val editor = preferences.edit()
            editor.putString("iv", byteArrayToHex(cipher.iv))
            editor.apply()
            return cipher
        }
    }
}