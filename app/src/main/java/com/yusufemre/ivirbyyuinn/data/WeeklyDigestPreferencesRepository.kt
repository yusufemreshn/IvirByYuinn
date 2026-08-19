package com.yusufemre.ivirbyyuinn.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// ThemePreferencesRepository ile aynı desen: tek bir boolean, varsayılan
// KAPALI (opt-in). Kullanıcı elle açmadıkça haftalık bildirim asla
// gönderilmiyor.
class WeeklyDigestPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isOptedIn(): Flow<Boolean> = callbackFlow {
        trySend(readCurrentValue())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_OPT_IN) {
                trySend(readCurrentValue())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)

        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    // IvirApplication açılışta WorkManager kaydının hâlâ ayakta olduğundan
    // emin olmak için (bkz. WeeklyDigestScheduler.schedule'ın KEEP politikası)
    // senkron bir okumaya ihtiyaç duyuyor.
    fun isOptedInNow(): Boolean = readCurrentValue()

    fun setOptIn(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_OPT_IN, enabled) }
    }

    private fun readCurrentValue(): Boolean = prefs.getBoolean(KEY_OPT_IN, false)

    private companion object {
        const val PREFS_NAME = "ivir_prefs"
        const val KEY_OPT_IN = "weekly_digest_opt_in"
    }
}
