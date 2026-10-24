package com.yusufemre.ivirbyyuinn.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yusufemre.ivirbyyuinn.domain.CompleteGoogleDriveSignInUseCase
import com.yusufemre.ivirbyyuinn.domain.DeleteAllDriveBackupsUseCase
import com.yusufemre.ivirbyyuinn.domain.GetGoogleDriveAccountEmailUseCase
import com.yusufemre.ivirbyyuinn.domain.GetGoogleDriveBackupPreferencesUseCase
import com.yusufemre.ivirbyyuinn.domain.GetGoogleDriveSignInIntentUseCase
import com.yusufemre.ivirbyyuinn.domain.RestoreBackupFromDriveUseCase
import com.yusufemre.ivirbyyuinn.domain.SetGoogleDriveAutoBackupEnabledUseCase
import com.yusufemre.ivirbyyuinn.domain.SetGoogleDriveAutoBackupScheduleUseCase
import com.yusufemre.ivirbyyuinn.domain.SignOutGoogleDriveUseCase
import com.yusufemre.ivirbyyuinn.domain.UploadBackupToDriveUseCase

class GoogleDriveViewModelFactory(
    private val getGoogleDriveSignInIntentUseCase: GetGoogleDriveSignInIntentUseCase,
    private val completeGoogleDriveSignInUseCase: CompleteGoogleDriveSignInUseCase,
    private val getGoogleDriveAccountEmailUseCase: GetGoogleDriveAccountEmailUseCase,
    private val signOutGoogleDriveUseCase: SignOutGoogleDriveUseCase,
    private val uploadBackupToDriveUseCase: UploadBackupToDriveUseCase,
    private val restoreBackupFromDriveUseCase: RestoreBackupFromDriveUseCase,
    private val getGoogleDriveBackupPreferencesUseCase: GetGoogleDriveBackupPreferencesUseCase,
    private val setGoogleDriveAutoBackupEnabledUseCase: SetGoogleDriveAutoBackupEnabledUseCase,
    private val setGoogleDriveAutoBackupScheduleUseCase: SetGoogleDriveAutoBackupScheduleUseCase,
    private val deleteAllDriveBackupsUseCase: DeleteAllDriveBackupsUseCase,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GoogleDriveViewModel(
            getGoogleDriveSignInIntentUseCase = getGoogleDriveSignInIntentUseCase,
            completeGoogleDriveSignInUseCase = completeGoogleDriveSignInUseCase,
            getGoogleDriveAccountEmailUseCase = getGoogleDriveAccountEmailUseCase,
            signOutGoogleDriveUseCase = signOutGoogleDriveUseCase,
            uploadBackupToDriveUseCase = uploadBackupToDriveUseCase,
            restoreBackupFromDriveUseCase = restoreBackupFromDriveUseCase,
            getGoogleDriveBackupPreferencesUseCase = getGoogleDriveBackupPreferencesUseCase,
            setGoogleDriveAutoBackupEnabledUseCase = setGoogleDriveAutoBackupEnabledUseCase,
            setGoogleDriveAutoBackupScheduleUseCase = setGoogleDriveAutoBackupScheduleUseCase,
            deleteAllDriveBackupsUseCase = deleteAllDriveBackupsUseCase,
        ) as T
    }
}
