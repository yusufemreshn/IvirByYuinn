package com.yusufemre.ivirbyyuinn.domain

import androidx.work.ExistingPeriodicWorkPolicy
import com.yusufemre.ivirbyyuinn.data.GoogleDrivePreferencesRepository
import com.yusufemre.ivirbyyuinn.notification.GoogleDriveBackupScheduler

// SetWeeklyDigestOptInUseCase ile AYNI desen: tercihi kaydetmekle
// WorkManager kaydını (kur/iptal et) AYNI anda, tek bir çağrıda koordine
// ediyor. REPLACE kullanıyoruz (KEEP değil): kullanıcı switch'i kapatıp
// açtığında, o anda kayıtlı saat/sıklık tercihiyle YENİDEN kurulsun istiyoruz.
class SetGoogleDriveAutoBackupEnabledUseCase(
    private val repository: GoogleDrivePreferencesRepository,
    private val scheduler: GoogleDriveBackupScheduler,
) {
    operator fun invoke(enabled: Boolean) {
        repository.setAutoBackupEnabled(enabled)
        if (enabled) {
            scheduler.schedule(
                minuteOfDay = repository.getAutoBackupMinuteOfDayNow(),
                intervalHours = repository.getAutoBackupIntervalHoursNow(),
                policy = ExistingPeriodicWorkPolicy.REPLACE,
            )
        } else {
            scheduler.cancel()
        }
    }
}
