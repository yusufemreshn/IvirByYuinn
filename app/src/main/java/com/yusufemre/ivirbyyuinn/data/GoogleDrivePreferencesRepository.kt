package com.yusufemre.ivirbyyuinn.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// QuietHoursPreferencesRepository/ConfirmationPreferencesRepository ile AYNI
// desen (aynı "ivir_prefs" dosyası, callbackFlow ile reaktifleştirme).
class GoogleDrivePreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getPreferences(): Flow<GoogleDriveBackupPreferences> = callbackFlow {
        trySend(readCurrentValue())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_AUTO_BACKUP_ENABLED || key == KEY_LAST_BACKUP_AT) {
                trySend(readCurrentValue())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)

        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    // GoogleDriveBackupWorker, IvirApplication.onCreate ile AYNI gerekçeyle
    // (WeeklyDigestPreferencesRepository.isOptedInNow) senkron bir okumaya
    // ihtiyaç duyabiliyor.
    fun isAutoBackupEnabledNow(): Boolean = prefs.getBoolean(KEY_AUTO_BACKUP_ENABLED, false)

    fun setAutoBackupEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_AUTO_BACKUP_ENABLED, enabled) }
    }

    fun setLastBackupAtMillis(millis: Long) {
        prefs.edit { putLong(KEY_LAST_BACKUP_AT, millis) }
    }

    private fun readCurrentValue(): GoogleDriveBackupPreferences = GoogleDriveBackupPreferences(
        autoBackupEnabled = prefs.getBoolean(KEY_AUTO_BACKUP_ENABLED, false),
        lastBackupAtMillis = prefs.getLong(KEY_LAST_BACKUP_AT, -1L).takeIf { it >= 0 },
    )

    private companion object {
        const val PREFS_NAME = "ivir_prefs"
        const val KEY_AUTO_BACKUP_ENABLED = "google_drive_auto_backup_enabled"
        const val KEY_LAST_BACKUP_AT = "google_drive_last_backup_at"
    }
}
