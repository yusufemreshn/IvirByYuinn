package com.yusufemre.ivirbyyuinn.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

// WeeklyDigestScheduler ile AYNI desen: kullanıcının açtığı/kapattığı bir
// tercihe bağlı, opsiyonel bir periyodik iş.
//
// 2026-10-25: kullanıcı isteğiyle saat ve sıklık (3/6/12 saatte bir gibi gün
// altı seçenekler de dahil) artık sabit DEĞİL, Ayarlar'dan seçilebiliyor.
// Birim SAAT: WorkManager'ın izin verdiği en küçük periyot 15 dakika, 3 saat
// rahatça üstünde. setInitialDelay ile "seçilen saate kadar bekle", SONRA
// her intervalHours'ta bir tekrarla; gün altı sıklıklarda (3/6/12 saat) ilk
// çalışma seçilen saate denk gelir, sonraki tekrarlar o saatten intervalHours
// sonra, bir sonraki gün aynı saate değil (WorkManager'ın doğal davranışı).
class GoogleDriveBackupScheduler(private val context: Context) {

    // policy varsayılanı KEEP: IvirApplication.onCreate'teki "zaten kurulu
    // bir işe dokunma, sadece kaybolmuşsa geri kur" çağrısı için. Kullanıcı
    // switch'i açtığında ya da saat/sıklık ayarını değiştirdiğinde
    // (SetGoogleDriveAutoBackupEnabledUseCase/SetGoogleDriveAutoBackupScheduleUseCase)
    // REPLACE ile çağrılıp yeni zamanlama HEMEN uygulanıyor.
    fun schedule(
        minuteOfDay: Int,
        intervalHours: Int,
        policy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP,
    ) {
        val request = PeriodicWorkRequestBuilder<GoogleDriveBackupWorker>(intervalHours.toLong(), TimeUnit.HOURS)
            .setInitialDelay(millisUntilNextOccurrence(minuteOfDay), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, policy, request)
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    // Seçilen saat bugün henüz geçmediyse bugüne, geçtiyse yarına denk
    // gelen ilk çalışma anına kadar kalan milisaniye.
    private fun millisUntilNextOccurrence(minuteOfDay: Int): Long {
        val now = Calendar.getInstance()
        val next = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, minuteOfDay / 60)
            set(Calendar.MINUTE, minuteOfDay % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        return next.timeInMillis - now.timeInMillis
    }

    private companion object {
        const val WORK_NAME = "google_drive_auto_backup"
    }
}
