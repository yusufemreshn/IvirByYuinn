package com.yusufemre.ivirbyyuinn.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

// QuietHoursPreferencesRepository/ThemePreferencesRepository ile AYNI desen
// (aynı "ivir_prefs" dosyası), burada callbackFlow YOK çünkü tek bir
// tüketicisi var (FocusModeScreen kendi Compose state'ini okuyup/yazıyor),
// AppLockRepository'nin senkron getter'larına daha yakın.
//
// Enum, ordinal yerine .name string'i olarak saklanıyor (Priority'nin
// level alanıyla aynı gerekçe, enum sırası ileride değişirse bozulmasın).
class FocusSessionPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getState(): FocusSessionState {
        val phase = runCatching {
            FocusPhase.valueOf(prefs.getString(KEY_PHASE, null) ?: FocusPhase.IDLE.name)
        }.getOrDefault(FocusPhase.IDLE)
        return FocusSessionState(
            phase = phase,
            isRunning = prefs.getBoolean(KEY_IS_RUNNING, false),
            remainingSeconds = prefs.getInt(KEY_REMAINING, FocusSessionState.DEFAULT_WORK_DURATION_SECONDS),
            workDurationSeconds = prefs.getInt(KEY_WORK_DURATION, FocusSessionState.DEFAULT_WORK_DURATION_SECONDS),
            breakDurationSeconds = prefs.getInt(KEY_BREAK_DURATION, FocusSessionState.DEFAULT_BREAK_DURATION_SECONDS),
            lastTickEpochMillis = prefs.getLong(KEY_LAST_TICK, 0L),
        )
    }

    fun saveState(state: FocusSessionState) {
        prefs.edit {
            putString(KEY_PHASE, state.phase.name)
            putBoolean(KEY_IS_RUNNING, state.isRunning)
            putInt(KEY_REMAINING, state.remainingSeconds)
            putInt(KEY_WORK_DURATION, state.workDurationSeconds)
            putInt(KEY_BREAK_DURATION, state.breakDurationSeconds)
            putLong(KEY_LAST_TICK, state.lastTickEpochMillis)
        }
    }

    private companion object {
        const val PREFS_NAME = "ivir_prefs"
        const val KEY_PHASE = "focus_phase"
        const val KEY_IS_RUNNING = "focus_is_running"
        const val KEY_REMAINING = "focus_remaining_seconds"
        const val KEY_WORK_DURATION = "focus_work_duration_seconds"
        const val KEY_BREAK_DURATION = "focus_break_duration_seconds"
        const val KEY_LAST_TICK = "focus_last_tick_epoch_millis"
    }
}
