package com.yusufemre.ivirbyyuinn.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

// WeeklyDigestScheduler ile AYNI desen: kullanıcının açtığı/kapattığı bir
// tercihe bağlı, opsiyonel bir periyodik iş. Günlük yedekleme kullanıcı
// için yeterli sıklık, görev/liste verisi saatlik değişecek kadar hızlı
// birikmiyor.
class GoogleDriveBackupScheduler(private val context: Context) {

    fun schedule() {
        val request = PeriodicWorkRequestBuilder<GoogleDriveBackupWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private companion object {
        const val WORK_NAME = "google_drive_auto_backup"
    }
}
