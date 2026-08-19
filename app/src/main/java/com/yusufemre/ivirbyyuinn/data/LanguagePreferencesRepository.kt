package com.yusufemre.ivirbyyuinn.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// ThemePreferencesRepository ile birebir aynı desen, bkz. oradaki
// yorumlar. Aynı "ivir_prefs" dosyasını paylaşıyor, sadece farklı bir
// anahtar (KEY_LANGUAGE) kullanıyor.
class LanguagePreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getLanguagePreference(): Flow<LanguagePreference> = callbackFlow {
        trySend(readCurrentValue())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_LANGUAGE) {
                trySend(readCurrentValue())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)

        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun setLanguagePreference(preference: LanguagePreference) {
        prefs.edit { putString(KEY_LANGUAGE, preference.name) }
    }

    // MainActivity.attachBaseContext() ÇOK erken bir noktada çalışıyor:
    // Compose/ViewModel/coroutine dünyası henüz kurulmadan önce, Activity
    // henüz var olmadan önce bile. Orada bir Flow toplayamayız, düz
    // senkron bir okumaya ihtiyacımız var.
    fun getLanguagePreferenceNow(): LanguagePreference = readCurrentValue()

    private fun readCurrentValue(): LanguagePreference {
        val stored = prefs.getString(KEY_LANGUAGE, null) ?: return LanguagePreference.SYSTEM
        return runCatching { LanguagePreference.valueOf(stored) }.getOrDefault(LanguagePreference.SYSTEM)
    }

    private companion object {
        const val PREFS_NAME = "ivir_prefs"
        const val KEY_LANGUAGE = "language_preference"
    }
}
