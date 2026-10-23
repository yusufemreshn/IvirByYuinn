package com.yusufemre.ivirbyyuinn.domain

import android.content.Intent
import com.yusufemre.ivirbyyuinn.data.GoogleDriveBackupRepository

// Başarılı girişte bağlanan hesabın e-postasını, başarısız/iptal edilen
// girişte null döner.
class CompleteGoogleDriveSignInUseCase(private val repository: GoogleDriveBackupRepository) {
    operator fun invoke(data: Intent?): String? = repository.completeSignIn(data)
}
