package com.yusufemre.ivirbyyuinn.domain

import android.util.Log
import com.yusufemre.ivirbyyuinn.data.BackupRepository
import com.yusufemre.ivirbyyuinn.data.GoogleDriveBackupRepository
import com.yusufemre.ivirbyyuinn.data.GoogleDrivePreferencesRepository

// BackupRepository'nin ürettiği JSON'ı GoogleDriveBackupRepository ile
// appDataFolder'a yüklüyor, başarılı olursa "son yedek" zaman damgasını
// güncelliyor. Hem manuel (Ayarlar'daki buton) hem otomatik
// (GoogleDriveBackupWorker) yedekleme AYNI UseCase'i çağırıyor.
class UploadBackupToDriveUseCase(
    private val backupRepository: BackupRepository,
    private val googleDriveBackupRepository: GoogleDriveBackupRepository,
    private val googleDrivePreferencesRepository: GoogleDrivePreferencesRepository,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        val json = backupRepository.exportToJson()
        googleDriveBackupRepository.uploadBackup(json)
        googleDrivePreferencesRepository.setLastBackupAtMillis(System.currentTimeMillis())
    }.onFailure { Log.e("GoogleDriveBackup", "Upload failed", it) }
}
