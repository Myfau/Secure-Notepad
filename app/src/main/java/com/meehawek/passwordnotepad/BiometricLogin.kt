package com.meehawek.passwordnotepad

import android.content.Intent
import android.hardware.biometrics.BiometricPrompt
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import com.meehawek.passwordnotepad.biometric.BiometricCallback
import com.meehawek.passwordnotepad.biometric.BiometricManager

class BiometricLogin : AppCompatActivity(), BiometricCallback {
    lateinit private var helper: Helpers

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biometric_login)
        helper = Helpers(applicationContext)
        helper.generateMasterKey()

        findViewById<Button>(R.id.LoginButton).setOnClickListener {
            authenticate()
        }
    }

    fun authenticate(){

        val cipher = helper.getLocalEncryptionCipher()

        BiometricManager.BiometricBuilder(this@BiometricLogin)
            .setTitle("Authorise")
            .setSubtitle("Please, authorise yourself")
            .setDescription("This is needed to perform cryptographic operations.")
            .setNegativeButtonText("Cancel")
            .setCipher(cipher)
            .build()
            .authenticate(this@BiometricLogin)
    }

    override fun onAuthenticationSuccessful(result: FingerprintManagerCompat.AuthenticationResult) {
        startActivity(Intent(this, NoteActivity::class.java))
    }

    override fun onAuthenticationSuccessful(result: BiometricPrompt.AuthenticationResult) {
        startActivity(Intent(this, NoteActivity::class.java))
    }


    override fun onSdkVersionNotSupported() {
        val toast = Toast.makeText(applicationContext, "This type of authentication is not available on your device.", Toast.LENGTH_SHORT)
        toast.show()
    }

    override fun onBiometricAuthenticationNotSupported() {
        val toast = Toast.makeText(applicationContext, "This type of authentication is not available on your device.", Toast.LENGTH_SHORT)
        toast.show()
    }

    override fun onBiometricAuthenticationNotAvailable() {
        val toast = Toast.makeText(applicationContext, "This type of authentication is not available on your device.", Toast.LENGTH_SHORT)
        toast.show()
    }

    override fun onBiometricAuthenticationPermissionNotGranted() {
        val toast = Toast.makeText(applicationContext, "Permissions not granted.", Toast.LENGTH_SHORT)
        toast.show()
    }

    override fun onBiometricAuthenticationInternalError(error: String?) {
        val toast = Toast.makeText(applicationContext, "An authentication error occurred, please try again.", Toast.LENGTH_SHORT)
        toast.show()
    }

    override fun onAuthenticationFailed() {
        val toast = Toast.makeText(applicationContext, "An authentication error occurred, please try again.", Toast.LENGTH_SHORT)
        toast.show()
    }

    override fun onAuthenticationCancelled() {
        val toast = Toast.makeText(applicationContext, "Authentication cancelled.", Toast.LENGTH_SHORT)
        toast.show()
    }

    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
        val toast = Toast.makeText(applicationContext, "The functionality is not implemented yet.", Toast.LENGTH_SHORT)
        toast.show()
    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
        val toast = Toast.makeText(applicationContext, "An authentication error occurred, please try again.", Toast.LENGTH_SHORT)
        toast.show()
    }
}