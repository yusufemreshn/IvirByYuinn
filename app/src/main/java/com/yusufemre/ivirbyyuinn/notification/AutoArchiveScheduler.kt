package com.yusufemre.ivirbyyuinn.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

// WeeklyDigestScheduler ile AYNI desen. Tek fark: haftalık özet
// kullanıcının açtığı bir tercihe bağlıyken, bu iş HER ZAMAN kurulu:
// açılıp kapanabilen bir ayar değil, "tamamlanan görev 24 saat sonra
// arşive düşer" çekirdek bir davranış.
//
// WorkManager'ın izin verdiği en küçük periyot 15 dakika, 30 dakika
// seçtik, 24 saatlik bir pencerede birkaç on dakikalık gecikme önemsiz.
class AutoArchiveScheduler(private val context: Context) {

    fun schedule() {
        val request = PeriodicWorkRequestBuilder<AutoArchiveWorker>(30, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    private companion object {
        const val WORK_NAME = "auto_archive_completed_tasks"
    }
}
