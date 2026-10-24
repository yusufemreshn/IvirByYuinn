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
            if (key in ALL_KEYS) {
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

    fun getAutoBackupMinuteOfDayNow(): Int = prefs.getInt(KEY_AUTO_BACKUP_MINUTE, DEFAULT_MINUTE_OF_DAY)

    fun getAutoBackupIntervalHoursNow(): Int = prefs.getInt(KEY_AUTO_BACKUP_INTERVAL_HOURS, DEFAULT_INTERVAL_HOURS)

    fun setAutoBackupEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_AUTO_BACKUP_ENABLED, enabled) }
    }

    fun setAutoBackupMinuteOfDay(minuteOfDay: Int) {
        prefs.edit { putInt(KEY_AUTO_BACKUP_MINUTE, minuteOfDay) }
    }

    fun setAutoBackupIntervalHours(hours: Int) {
        prefs.edit { putInt(KEY_AUTO_BACKUP_INTERVAL_HOURS, hours) }
    }

    fun setLastBackupAtMillis(millis: Long) {
        prefs.edit { putLong(KEY_LAST_BACKUP_AT, millis) }
    }

    // Drive'daki TÜM yedekler silinince (bkz. DeleteAllDriveBackupsUseCase)
    // "son yedek: ..." satırının eski, artık yanlış bir zamanı göstermeye
    // devam etmesini istemiyoruz, alanı tamamen kaldırıyoruz.
    fun clearLastBackupAtMillis() {
        prefs.edit { remove(KEY_LAST_BACKUP_AT) }
    }

    private fun readCurrentValue(): GoogleDriveBackupPreferences = GoogleDriveBackupPreferences(
        autoBackupEnabled = prefs.getBoolean(KEY_AUTO_BACKUP_ENABLED, false),
        lastBackupAtMillis = prefs.getLong(KEY_LAST_BACKUP_AT, -1L).takeIf { it >= 0 },
        autoBackupMinuteOfDay = prefs.getInt(KEY_AUTO_BACKUP_MINUTE, DEFAULT_MINUTE_OF_DAY),
        autoBackupIntervalHours = prefs.getInt(KEY_AUTO_BACKUP_INTERVAL_HOURS, DEFAULT_INTERVAL_HOURS),
    )

    private companion object {
        const val PREFS_NAME = "ivir_prefs"
        const val KEY_AUTO_BACKUP_ENABLED = "google_drive_auto_backup_enabled"
        const val KEY_LAST_BACKUP_AT = "google_drive_last_backup_at"
        const val KEY_AUTO_BACKUP_MINUTE = "google_drive_auto_backup_minute"
        // 2026-10-25: eski "gün" birimli anahtarın (google_drive_auto_backup_interval_days)
        // YERİNE yeni bir anahtar: 3/6/12 saatte bir gibi gün altı sıklıklar
        // eklenince birim gün->saat'e değişti, eski anahtarı saat olarak
        // YENİDEN YORUMLAMAK (1 gün -> 1 saat gibi) kullanıcının tercihini
        // sessizce bozardı, o yüzden sıfırdan yeni bir anahtar.
        const val KEY_AUTO_BACKUP_INTERVAL_HOURS = "google_drive_auto_backup_interval_hours"

        // 03:00: kullanıcının cihazı muhtemelen kullanmadığı, varsayılan
        // olarak makul bir gece saati. 24 saat: günde bir, önceki varsayılan
        // davranışla AYNI.
        const val DEFAULT_MINUTE_OF_DAY = 180
        const val DEFAULT_INTERVAL_HOURS = 24

        val ALL_KEYS = setOf(KEY_AUTO_BACKUP_ENABLED, KEY_LAST_BACKUP_AT, KEY_AUTO_BACKUP_MINUTE, KEY_AUTO_BACKUP_INTERVAL_HOURS)
    }
}
