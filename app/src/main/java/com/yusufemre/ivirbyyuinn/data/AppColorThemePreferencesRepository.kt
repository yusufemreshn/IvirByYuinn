package com.yusufemre.ivirbyyuinn.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// ThemePreferencesRepository ile birebir aynı desen: bkz. oradaki
// yorumlar. Aynı "ivir_prefs" dosyasını paylaşıyor, sadece farklı bir
// anahtar (KEY_COLOR_THEME) kullanıyor.
class AppColorThemePreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getColorTheme(): Flow<AppColorTheme> = callbackFlow {
        trySend(readCurrentValue())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_COLOR_THEME) {
                trySend(readCurrentValue())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)

        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun setColorTheme(theme: AppColorTheme) {
        prefs.edit { putString(KEY_COLOR_THEME, theme.name) }
    }

    private fun readCurrentValue(): AppColorTheme {
        val stored = prefs.getString(KEY_COLOR_THEME, null) ?: return AppColorTheme.TEAL
        return runCatching { AppColorTheme.valueOf(stored) }.getOrDefault(AppColorTheme.TEAL)
    }

    private companion object {
        const val PREFS_NAME = "ivir_prefs"
        const val KEY_COLOR_THEME = "app_color_theme"
    }
}
