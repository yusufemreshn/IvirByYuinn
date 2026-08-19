package com.yusufemre.ivirbyyuinn.ui.lock

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

// BiometricPrompt bilerek cihazın kendi ekran kilidini (DEVICE_CREDENTIAL)
// DEĞİL, sadece gerçek biyometrik donanımı (BIOMETRIC_WEAK: parmak izi/
// yüz) kabul ediyor. Sebep: kullanıcı bizim uygulamamızın PIN'ini
// belirliyor, cihazın ekran kilidiyle karıştırmak istemiyoruz. Biyometrik
// başarısız ya da kullanılamaz olursa her zaman kendi PIN ekranımıza
// düşülüyor, cihazın PIN'ine asla değil.
private const val ALLOWED_AUTHENTICATORS = BiometricManager.Authenticators.BIOMETRIC_WEAK

// Cihazda kullanılabilir (donanım var + en az bir parmak izi/yüz kayıtlı)
// biyometrik doğrulama var mı diye soruyoruz, yoksa AppLockScreen
// "Parmak izi ile aç" butonunu hiç göstermiyor, PIN her zaman ana yol.
fun isBiometricAvailable(context: Context): Boolean {
    val manager = BiometricManager.from(context)
    return manager.canAuthenticate(ALLOWED_AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS
}

fun showBiometricPrompt(
    activity: FragmentActivity,
    title: String,
    negativeButtonText: String,
    onSuccess: () -> Unit,
    onError: () -> Unit,
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            onSuccess()
        }

        // Kullanıcı iptal etse de (ör. negatif butona basarsa), donanım
        // hata verse de aynı şekilde ele alıyoruz: her iki durumda da
        // sonuç aynı: kendi PIN ekranımıza dönülüyor.
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            onError()
        }
    }

    val prompt = BiometricPrompt(activity, executor, callback)
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setNegativeButtonText(negativeButtonText)
        .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
        .build()

    prompt.authenticate(promptInfo)
}
