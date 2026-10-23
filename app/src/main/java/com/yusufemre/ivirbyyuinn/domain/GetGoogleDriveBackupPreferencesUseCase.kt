package com.yusufemre.ivirbyyuinn.domain

import com.yusufemre.ivirbyyuinn.data.GoogleDriveBackupPreferences
import com.yusufemre.ivirbyyuinn.data.GoogleDrivePreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetGoogleDriveBackupPreferencesUseCase(private val repository: GoogleDrivePreferencesRepository) {
    operator fun invoke(): Flow<GoogleDriveBackupPreferences> = repository.getPreferences()
}
