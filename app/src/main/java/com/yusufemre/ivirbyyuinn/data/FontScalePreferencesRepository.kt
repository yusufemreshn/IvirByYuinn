package com.yusufemre.ivirbyyuinn.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// ThemePreferencesRepository ile birebir aynı desen (bkz. oradaki
// yorumlar). Aynı "ivir_prefs" dosyasını paylaşıyor, sadece farklı bir
// anahtar (KEY_FONT_SCALE) kullanıyor.
class FontScalePreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getFontScalePreference(): Flow<FontScalePreference> = callbackFlow {
        trySend(readCurrentValue())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_FONT_SCALE) {
                trySend(readCurrentValue())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)

        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun setFontScalePreference(preference: FontScalePreference) {
        prefs.edit { putString(KEY_FONT_SCALE, preference.name) }
    }

    private fun readCurrentValue(): FontScalePreference {
        val stored = prefs.getString(KEY_FONT_SCALE, null) ?: return FontScalePreference.MEDIUM
        return runCatching { FontScalePreference.valueOf(stored) }.getOrDefault(FontScalePreference.MEDIUM)
    }

    private companion object {
        const val PREFS_NAME = "ivir_prefs"
        const val KEY_FONT_SCALE = "font_scale_preference"
    }
}
