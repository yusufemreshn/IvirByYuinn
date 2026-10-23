package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.BackupRepository
import com.yusufemre.ivirbyyuinn.data.GoogleDriveBackupRepository

// En güncel Drive yedeğini indirip BackupRepository.importFromJson ile
// cihaza yazıyor; ImportBackupUseCase'in (yerel dosya) Drive karşılığı.
class RestoreBackupFromDriveUseCase(
    private val backupRepository: BackupRepository,
    private val googleDriveBackupRepository: GoogleDriveBackupRepository,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        val json = googleDriveBackupRepository.downloadLatestBackup()
            ?: error("Drive'da yedek bulunamadı")
        backupRepository.importFromJson(json)
    }
}
