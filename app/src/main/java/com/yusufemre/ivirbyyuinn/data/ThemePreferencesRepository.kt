package com.yusufemre.ivirbyyuinn.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// Room değil, düz SharedPreferences kullanıyoruz, tek bir küçük tercihi
// saklamak için DataStore gibi ekstra bir bağımlılık eklemek gereksiz
// olurdu, SharedPreferences zaten Android'in kendi platformunun bir
// parçası. .NET'te tek bir ayarı app.config yerine bir registry
// anahtarında tutmaya benzer. Küçük iş için küçük araç.
class ThemePreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // SharedPreferences kendi başına reaktif değil (Flow döndürmüyor),
    // bu yüzden OnSharedPreferenceChangeListener'ı callbackFlow ile
    // sarmalayıp Room'un Flow'larıyla aynı dile çeviriyoruz, ViewModel
    // tarafında ikisini de aynı şekilde toplayabiliyoruz.
    fun getThemePreference(): Flow<ThemePreference> = callbackFlow {
        trySend(readCurrentValue())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_THEME) {
                trySend(readCurrentValue())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)

        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun setThemePreference(preference: ThemePreference) {
        prefs.edit { putString(KEY_THEME, preference.name) }
    }

    private fun readCurrentValue(): ThemePreference {
        val stored = prefs.getString(KEY_THEME, null) ?: return ThemePreference.SYSTEM
        // Kayıtlı değer bozuksa (ileride enum'dan bir değer kaldırırsak
        // mesela) çökmek yerine güvenli varsayılana dönüyoruz.
        return runCatching { ThemePreference.valueOf(stored) }.getOrDefault(ThemePreference.SYSTEM)
    }

    private companion object {
        const val PREFS_NAME = "ivir_prefs"
        const val KEY_THEME = "theme_preference"
    }
}
