package com.yusufemre.ivirbyyuinn.domain

import android.util.Log
import com.yusufemre.ivirbyyuinn.data.GoogleDriveBackupRepository
import com.yusufemre.ivirbyyuinn.data.GoogleDrivePreferencesRepository

// Ayarlar'daki "Drive yedeklerini sil" butonunun arkasındaki UseCase.
// Upload/RestoreBackupFromDriveUseCase İLE AYNI desen (runCatching +
// Log.e), Result<Unit> döner. Silme sonrası "son yedek: ..." satırı artık
// yanlış bir zamanı göstermesin diye lastBackupAtMillis'i de temizliyoruz.
class DeleteAllDriveBackupsUseCase(
    private val googleDriveBackupRepository: GoogleDriveBackupRepository,
    private val googleDrivePreferencesRepository: GoogleDrivePreferencesRepository,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        googleDriveBackupRepository.deleteAllBackups()
        googleDrivePreferencesRepository.clearLastBackupAtMillis()
    }.onFailure { Log.e("GoogleDriveBackup", "Delete backups failed", it) }
}
