package com.yusufemre.ivirbyyuinn.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// ThemePreferencesRepository/AppLockRepository ile AYNI desen (aynı
// "ivir_prefs" dosyası, callbackFlow ile reaktifleştirme); tek fark üç
// değeri birden taşıması (enabled + start + end).
class QuietHoursPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getQuietHours(): Flow<QuietHours> = callbackFlow {
        trySend(readCurrentValue())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_ENABLED || key == KEY_START || key == KEY_END) {
                trySend(readCurrentValue())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)

        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    // ReminderScheduler.scheduleReminder suspend DEĞİL (AlarmManager
    // çağrısı senkron), bu yüzden Flow'un ilk değerini beklemek yerine
    // AppLockRepository.isLockEnabledNow() ile aynı fikirde senkron bir
    // okumaya ihtiyacı var.
    fun getQuietHoursNow(): QuietHours = readCurrentValue()

    fun setQuietHours(quietHours: QuietHours) {
        prefs.edit {
            putBoolean(KEY_ENABLED, quietHours.enabled)
            putInt(KEY_START, quietHours.startMinute)
            putInt(KEY_END, quietHours.endMinute)
        }
    }

    private fun readCurrentValue(): QuietHours = QuietHours(
        enabled = prefs.getBoolean(KEY_ENABLED, false),
        startMinute = prefs.getInt(KEY_START, QuietHours.DEFAULT_START_MINUTE),
        endMinute = prefs.getInt(KEY_END, QuietHours.DEFAULT_END_MINUTE),
    )

    private companion object {
        const val PREFS_NAME = "ivir_prefs"
        const val KEY_ENABLED = "quiet_hours_enabled"
        const val KEY_START = "quiet_hours_start"
        const val KEY_END = "quiet_hours_end"
    }
}
