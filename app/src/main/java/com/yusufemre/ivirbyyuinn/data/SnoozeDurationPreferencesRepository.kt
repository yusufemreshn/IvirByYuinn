package com.yusufemre.ivirbyyuinn.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// QuietHoursPreferencesRepository ile AYNI desen. Varsayılan 60 dakika:
// TaskReminderActionReceiver'daki eski sabit SNOOZE_DURATION_MILLIS (60*60*1000)
// ile AYNI değer, bu ayar eklenmeden önceki davranış hiç değişmesin diye.
class SnoozeDurationPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSnoozeMinutes(): Flow<Int> = callbackFlow {
        trySend(readCurrentValue())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_SNOOZE_MINUTES) trySend(readCurrentValue())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)

        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    // TaskReminderActionReceiver bir BroadcastReceiver, Flow toplamak için
    // uygun bir yaşam döngüsü yok; AppLockRepository.isLockEnabledNow() ile
    // AYNI gerekçeyle doğrudan senkron okuma lazım.
    fun getSnoozeMinutesNow(): Int = readCurrentValue()

    fun setSnoozeMinutes(minutes: Int) {
        prefs.edit { putInt(KEY_SNOOZE_MINUTES, minutes) }
    }

    private fun readCurrentValue(): Int = prefs.getInt(KEY_SNOOZE_MINUTES, DEFAULT_SNOOZE_MINUTES)

    companion object {
        const val DEFAULT_SNOOZE_MINUTES = 60
        private const val PREFS_NAME = "ivir_prefs"
        private const val KEY_SNOOZE_MINUTES = "snooze_duration_minutes"
    }
}
