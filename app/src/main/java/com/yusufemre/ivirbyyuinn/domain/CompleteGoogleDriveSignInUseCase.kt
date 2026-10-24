package com.yusufemre.ivirbyyuinn.domain

import android.content.Intent
import com.yusufemre.ivirbyyuinn.data.GoogleDriveBackupRepository

// Başarılı girişte bağlanan hesabın e-postasını taşıyan bir Result, başarısız/
// iptal edilen girişte gerçek sebebi (ApiException statusCode'un okunabilir
// hali) taşıyan bir Result.failure döner.
class CompleteGoogleDriveSignInUseCase(private val repository: GoogleDriveBackupRepository) {
    operator fun invoke(data: Intent?): Result<String?> = repository.completeSignIn(data)
}
