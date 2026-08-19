package com.yusufemre.ivirbyyuinn.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.security.MessageDigest
import java.security.SecureRandom

// PIN'in KENDİSİNİ hiçbir zaman diske yazmıyoruz, sadece rastgele bir
// "salt" (tuz) ile birlikte tek yönlü bir hash'ini. Tek yönlü demek: hash'i
// okuyan biri PIN'i geri elde edemez, sadece "girilen PIN aynı hash'i
// üretiyor mu" diye karşılaştırma yapabilir. Bu, .NET tarafında
// `ASP.NET Identity`'nin şifreleri `PasswordHasher` ile saklamasıyla aynı
// fikir: kullanıcı adı/şifre tablosunda da şifrenin kendisi değil, hash'i
// durur.
//
// Salt'ın işi: iki farklı kullanıcı aynı PIN'i (ör. "1234") seçse bile
// hash'leri farklı çıksın. Salt olmasaydı, hash'i önceden hesaplanmış bir
// "rainbow table" ile kırmak kolaylaşırdı. Her kullanıcı/kurulum kendi
// rastgele salt'ını üretiyor.
class AppLockRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ThemePreferencesRepository'deki ile birebir aynı desen: SharedPreferences
    // kendi başına reaktif değil, callbackFlow ile Room'un Flow'larıyla aynı
    // dile çeviriyoruz.
    fun isLockEnabled(): Flow<Boolean> = callbackFlow {
        trySend(readIsEnabled())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_ENABLED) {
                trySend(readIsEnabled())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)

        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    // MainActivity açılışta hemen "kilitli mi" diye sorması gerekiyor,
    // Flow'un ilk değerini beklemek yerine (Compose ilk kare çizilmeden
    // önce) senkron bir okuma da lazım.
    fun isLockEnabledNow(): Boolean = readIsEnabled()

    // Yeni bir PIN belirlerken (ya da PIN değiştirirken) çağrılıyor,
    // hem yeni salt/hash'i kaydediyor hem kilidi otomatik açık yapıyor.
    fun setPin(pin: String) {
        val salt = ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val hash = hashPin(pin, salt)
        prefs.edit {
            putString(KEY_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            putString(KEY_HASH, hash)
            putBoolean(KEY_ENABLED, true)
        }
    }

    fun verifyPin(pin: String): Boolean {
        val saltEncoded = prefs.getString(KEY_SALT, null) ?: return false
        val expectedHash = prefs.getString(KEY_HASH, null) ?: return false
        val salt = Base64.decode(saltEncoded, Base64.NO_WRAP)
        return hashPin(pin, salt) == expectedHash
    }

    // Kilidi kapatırken salt/hash'i de siliyoruz: yarım kalmış bir "kilit
    // kapalı ama eski PIN hâlâ diskte duruyor" durumuna düşmesin, kullanıcı
    // tekrar açtığında zaten yeni bir PIN belirleyecek.
    fun disableLock() {
        prefs.edit {
            putBoolean(KEY_ENABLED, false)
            remove(KEY_SALT)
            remove(KEY_HASH)
        }
    }

    private fun readIsEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, false)

    private fun hashPin(pin: String, salt: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        val hashBytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }

    private companion object {
        const val PREFS_NAME = "ivir_prefs"
        const val KEY_ENABLED = "app_lock_enabled"
        const val KEY_SALT = "app_lock_salt"
        const val KEY_HASH = "app_lock_hash"
        const val SALT_LENGTH_BYTES = 16
    }
}
