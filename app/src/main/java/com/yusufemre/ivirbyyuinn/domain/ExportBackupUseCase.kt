package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.BackupRepository

// Diğer UseCase'ler gibi ince bir sarmalayıcı: asıl iş (tabloları okuyup
// JSON'a çevirmek) BackupRepository'de. Bu katmanın var olma sebebi,
// ViewModel'in Repository'yi değil sadece UseCase'i tanıması: yarın
// "dışa aktarmadan önce bir şey daha yap" gibi bir iş kuralı eklenirse
// (örn. bir onay/loglama), o kural Repository'ye değil buraya eklenir.
class ExportBackupUseCase(private val backupRepository: BackupRepository) {
    suspend operator fun invoke(): String = backupRepository.exportToJson()
}
