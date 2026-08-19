package com.yusufemre.ivirbyyuinn.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

// ReminderScheduler'ın AlarmManager'ı sarmaladığı gibi, bu da
// WorkManager'ı sarmalıyor: UseCase'ler doğrudan WorkManager API'sini
// değil, sadece bu sınıfı tanıyor.
class WeeklyDigestScheduler(private val context: Context) {

    fun schedule() {
        val request = PeriodicWorkRequestBuilder<WeeklyDigestWorker>(7, TimeUnit.DAYS).build()
        // KEEP: zaten kurulu bir periyodik iş varsa DOKUNMA: kullanıcı
        // switch'i kapatıp tekrar açmadıkça (ya da uygulama her açılışta
        // "hâlâ ayakta mı" diye burayı çağırdığında) 7 günlük döngüyü
        // baştan saymasın.
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
        const val WORK_NAME = "weekly_digest"
    }
}
