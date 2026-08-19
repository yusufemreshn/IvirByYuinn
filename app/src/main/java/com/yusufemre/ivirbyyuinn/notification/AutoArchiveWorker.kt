package com.yusufemre.ivirbyyuinn.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yusufemre.ivirbyyuinn.IvirApplication

// WeeklyDigestWorker ile AYNI iskelet: WorkManager bu sınıfı
// AutoArchiveScheduler'ın kurduğu periyotta çalıştırıyor. Bildirim
// göstermediği için WeeklyDigestWorker'daki izin kontrolü/PendingIntent
// kurulumu burada yok, çok daha ince bir sınıf.
class AutoArchiveWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val application = applicationContext as IvirApplication
        application.autoArchiveCompletedTasksUseCase()
        return Result.success()
    }
}
