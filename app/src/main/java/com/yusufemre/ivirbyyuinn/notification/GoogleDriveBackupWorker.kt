package com.yusufemre.ivirbyyuinn.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yusufemre.ivirbyyuinn.IvirApplication

// WeeklyDigestWorker/AutoArchiveWorker ile AYNI desen: WorkManager bu
// sınıfı GoogleDriveBackupScheduler'ın kurduğu günlük periyotta çalıştırıyor.
// Arka planda kullanıcıya hiçbir şey SORAMAYIZ (giriş ekranı açamayız),
// bu yüzden hesap bağlı değilse ya da upload başarısızsa sessizce
// Result.success() ile bitiriyoruz; WorkManager'ın retry mekanizmasını
// (Result.retry) kasıtlı kullanmıyoruz çünkü "hesap bağlı değil" gibi
// kalıcı hatalarda tekrar denemek anlamsız, geçici ağ hatalarında ise bir
// sonraki günlük çalıştırma zaten yeni bir deneme.
class GoogleDriveBackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val application = applicationContext as IvirApplication
        application.uploadBackupToDriveUseCase()
        return Result.success()
    }
}
