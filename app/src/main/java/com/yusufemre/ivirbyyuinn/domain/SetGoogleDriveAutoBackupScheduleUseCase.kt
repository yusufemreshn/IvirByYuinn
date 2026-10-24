package com.yusufemre.ivirbyyuinn.domain

import androidx.work.ExistingPeriodicWorkPolicy
import com.yusufemre.ivirbyyuinn.data.GoogleDrivePreferencesRepository
import com.yusufemre.ivirbyyuinn.notification.GoogleDriveBackupScheduler

// SetGoogleDriveAutoBackupEnabledUseCase'in "ne zaman/ne sıklıkla" tarafı:
// kullanıcı Ayarlar'dan saati ya da sıklığı değiştirdiğinde çağrılıyor.
// Otomatik yedekleme o an KAPALIYSA sadece tercihi kaydediyoruz, WorkManager'a
// hiç dokunmuyoruz (açılınca zaten güncel tercihle kurulacak,
// bkz. SetGoogleDriveAutoBackupEnabledUseCase); açıksa REPLACE ile mevcut
// periyodik işi yeni saat/sıklıkla HEMEN yeniden kuruyoruz.
class SetGoogleDriveAutoBackupScheduleUseCase(
    private val repository: GoogleDrivePreferencesRepository,
    private val scheduler: GoogleDriveBackupScheduler,
) {
    operator fun invoke(minuteOfDay: Int, intervalHours: Int) {
        repository.setAutoBackupMinuteOfDay(minuteOfDay)
        repository.setAutoBackupIntervalHours(intervalHours)
        if (repository.isAutoBackupEnabledNow()) {
            scheduler.schedule(minuteOfDay, intervalHours, ExistingPeriodicWorkPolicy.REPLACE)
        }
    }
}
