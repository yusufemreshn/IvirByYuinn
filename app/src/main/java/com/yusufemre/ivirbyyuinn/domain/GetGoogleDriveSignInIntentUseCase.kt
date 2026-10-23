package com.yusufemre.ivirbyyuinn.domain

import android.content.Intent
import com.yusufemre.ivirbyyuinn.data.GoogleDriveBackupRepository

class GetGoogleDriveSignInIntentUseCase(private val repository: GoogleDriveBackupRepository) {
    operator fun invoke(): Intent = repository.getSignInIntent()
}
