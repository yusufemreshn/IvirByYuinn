package com.yusufemre.ivirbyyuinn.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// QuietHoursPreferencesRepository ile AYNI desen (aynı "ivir_prefs" dosyası,
// callbackFlow ile reaktifleştirme); tek fark on bağımsız boolean taşıması.
class ConfirmationPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getConfirmationPreferences(): Flow<ConfirmationPreferences> = callbackFlow {
        trySend(readCurrentValue())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key in ALL_KEYS) {
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
            putBoolean(KEY_TASK_ARCHIVE, preferences.confirmTaskArchive)
            putBoolean(KEY_SUBTASK_DELETE, preferences.confirmSubtaskDelete)
            putBoolean(KEY_SUBTASK_COMPLETE, preferences.confirmSubtaskComplete)
            putBoolean(KEY_SUBTASK_ADD, preferences.confirmSubtaskAdd)
            putBoolean(KEY_CATEGORY_DELETE, preferences.confirmCategoryDelete)
            putBoolean(KEY_LIST_DELETE, preferences.confirmListDelete)
            putBoolean(KEY_ARCHIVED_TASK_DELETE, preferences.confirmArchivedTaskDelete)
            putBoolean(KEY_TRASH_DELETE, preferences.confirmTrashDelete)
        }
    }

    private fun readCurrentValue(): ConfirmationPreferences = ConfirmationPreferences(
        confirmTaskDelete = prefs.getBoolean(KEY_TASK_DELETE, true),
        confirmTaskComplete = prefs.getBoolean(KEY_TASK_COMPLETE, true),
        confirmTaskArchive = prefs.getBoolean(KEY_TASK_ARCHIVE, true),
        confirmSubtaskDelete = prefs.getBoolean(KEY_SUBTASK_DELETE, true),
        confirmSubtaskComplete = prefs.getBoolean(KEY_SUBTASK_COMPLETE, true),
        confirmSubtaskAdd = prefs.getBoolean(KEY_SUBTASK_ADD, true),
        confirmCategoryDelete = prefs.getBoolean(KEY_CATEGORY_DELETE, true),
        confirmListDelete = prefs.getBoolean(KEY_LIST_DELETE, true),
        confirmArchivedTaskDelete = prefs.getBoolean(KEY_ARCHIVED_TASK_DELETE, true),
        confirmTrashDelete = prefs.getBoolean(KEY_TRASH_DELETE, true),
    )

    private companion object {
        const val PREFS_NAME = "ivir_prefs"
        const val KEY_TASK_DELETE = "confirm_task_delete"
        const val KEY_TASK_COMPLETE = "confirm_task_complete"
        const val KEY_TASK_ARCHIVE = "confirm_task_archive"
        const val KEY_SUBTASK_DELETE = "confirm_subtask_delete"
        const val KEY_SUBTASK_COMPLETE = "confirm_subtask_complete"
        const val KEY_SUBTASK_ADD = "confirm_subtask_add"
        const val KEY_CATEGORY_DELETE = "confirm_category_delete"
        const val KEY_LIST_DELETE = "confirm_list_delete"
        const val KEY_ARCHIVED_TASK_DELETE = "confirm_archived_task_delete"
        const val KEY_TRASH_DELETE = "confirm_trash_delete"

        val ALL_KEYS = setOf(
            KEY_TASK_DELETE, KEY_TASK_COMPLETE, KEY_TASK_ARCHIVE,
            KEY_SUBTASK_DELETE, KEY_SUBTASK_COMPLETE, KEY_SUBTASK_ADD,
            KEY_CATEGORY_DELETE, KEY_LIST_DELETE, KEY_ARCHIVED_TASK_DELETE, KEY_TRASH_DELETE,
        )
    }
}
