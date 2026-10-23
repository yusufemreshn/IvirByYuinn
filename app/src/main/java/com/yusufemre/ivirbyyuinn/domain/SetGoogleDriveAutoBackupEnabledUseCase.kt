package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.GoogleDrivePreferencesRepository
import com.yusufemre.ivirbyyuinn.notification.GoogleDriveBackupScheduler

// SetWeeklyDigestOptInUseCase ile AYNI desen: tercihi kaydetmekle
// WorkManager kaydını (kur/iptal et) AYNI anda, tek bir çağrıda koordine
// ediyor.
class SetGoogleDriveAutoBackupEnabledUseCase(
    private val repository: GoogleDrivePreferencesRepository,
    private val scheduler: GoogleDriveBackupScheduler,
) {
    operator fun invoke(enabled: Boolean) {
        repository.setAutoBackupEnabled(enabled)
        if (enabled) scheduler.schedule() else scheduler.cancel()
    }
}
