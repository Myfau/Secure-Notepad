package com.meehawek.passwordnotepad.biometric;

import android.hardware.biometrics.BiometricPrompt;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;

public interface BiometricCallback {

    void onSdkVersionNotSupported();

    void onBiometricAuthenticationNotSupported();

    void onBiometricAuthenticationNotAvailable();

    void onBiometricAuthenticationPermissionNotGranted();

    void onBiometricAuthenticationInternalError(String error);

    void onAuthenticationFailed();

    void onAuthenticationCancelled();

    void onAuthenticationSuccessful(FingerprintManagerCompat.AuthenticationResult result);

    void onAuthenticationSuccessful(BiometricPrompt.AuthenticationResult result);

    void onAuthenticationHelp(int helpCode, CharSequence helpString);

    void onAuthenticationError(int errorCode, CharSequence errString);
}
