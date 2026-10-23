package com.yusufemre.ivirbyyuinn.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// QuietHoursPreferencesRepository ile AYNI desen (aynı "ivir_prefs" dosyası,
// callbackFlow ile reaktifleştirme); tek fark dört bağımsız boolean taşıması.
class ConfirmationPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getConfirmationPreferences(): Flow<ConfirmationPreferences> = callbackFlow {
        trySend(readCurrentValue())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_TASK_DELETE || key == KEY_TASK_COMPLETE ||
                key == KEY_SUBTASK_DELETE || key == KEY_SUBTASK_COMPLETE
            ) {
                trySend(readCurrentValue())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)

        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    // TaskViewModel içindeki StateFlow zaten Flow'un ilk değerini yakalıyor,
    // ama TaskRow/TaskEditorSheet gibi composable'lar her render'da güncel
    // değeri senkron okumak isteyebilir; QuietHoursPreferencesRepository'deki
    // getQuietHoursNow() ile AYNI gerekçe.
    fun getConfirmationPreferencesNow(): ConfirmationPreferences = readCurrentValue()

    fun setConfirmationPreferences(preferences: ConfirmationPreferences) {
        prefs.edit {
            putBoolean(KEY_TASK_DELETE, preferences.confirmTaskDelete)
            putBoolean(KEY_TASK_COMPLETE, preferences.confirmTaskComplete)
            putBoolean(KEY_SUBTASK_DELETE, preferences.confirmSubtaskDelete)
            putBoolean(KEY_SUBTASK_COMPLETE, preferences.confirmSubtaskComplete)
        }
    }

    private fun readCurrentValue(): ConfirmationPreferences = ConfirmationPreferences(
        confirmTaskDelete = prefs.getBoolean(KEY_TASK_DELETE, true),
        confirmTaskComplete = prefs.getBoolean(KEY_TASK_COMPLETE, true),
        confirmSubtaskDelete = prefs.getBoolean(KEY_SUBTASK_DELETE, true),
        confirmSubtaskComplete = prefs.getBoolean(KEY_SUBTASK_COMPLETE, true),
    )

    private companion object {
        const val PREFS_NAME = "ivir_prefs"
        const val KEY_TASK_DELETE = "confirm_task_delete"
        const val KEY_TASK_COMPLETE = "confirm_task_complete"
        const val KEY_SUBTASK_DELETE = "confirm_subtask_delete"
        const val KEY_SUBTASK_COMPLETE = "confirm_subtask_complete"
    }
}
