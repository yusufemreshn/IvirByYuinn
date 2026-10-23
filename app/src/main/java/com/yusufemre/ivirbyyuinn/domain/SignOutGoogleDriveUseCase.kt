package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.GoogleDriveBackupRepository

class SignOutGoogleDriveUseCase(private val repository: GoogleDriveBackupRepository) {
    operator fun invoke(onComplete: () -> Unit) = repository.signOut(onComplete)
}
