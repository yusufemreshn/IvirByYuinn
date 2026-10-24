package com.yusufemre.ivirbyyuinn.ui.settings

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufemre.ivirbyyuinn.data.GoogleDriveBackupPreferences
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

// SettingsViewModel'in (yerel dosya yedekleme) Drive karşılığı, ayrı
// tutuluyor çünkü oturum durumu (accountEmail) gibi SettingsViewModel'in
// hiç bilmediği bir ek state taşıyor.
class GoogleDriveViewModel(
    private val getGoogleDriveSignInIntentUseCase: GetGoogleDriveSignInIntentUseCase,
    private val completeGoogleDriveSignInUseCase: CompleteGoogleDriveSignInUseCase,
    private val getGoogleDriveAccountEmailUseCase: GetGoogleDriveAccountEmailUseCase,
    private val signOutGoogleDriveUseCase: SignOutGoogleDriveUseCase,
    private val uploadBackupToDriveUseCase: UploadBackupToDriveUseCase,
    private val restoreBackupFromDriveUseCase: RestoreBackupFromDriveUseCase,
    getGoogleDriveBackupPreferencesUseCase: GetGoogleDriveBackupPreferencesUseCase,
    private val setGoogleDriveAutoBackupEnabledUseCase: SetGoogleDriveAutoBackupEnabledUseCase,
    private val setGoogleDriveAutoBackupScheduleUseCase: SetGoogleDriveAutoBackupScheduleUseCase,
    private val deleteAllDriveBackupsUseCase: DeleteAllDriveBackupsUseCase,
) : ViewModel() {

    // Oturum durumu SharedPreferences'ta DEĞİL, Google Play Services'in
    // kendi önbelleğinde yaşıyor, o yüzden QuietHours gibi bir Flow'a değil,
    // elle tetiklenen bir MutableStateFlow'a sarıyoruz: giriş/çıkış
    // sonrasında biz ne zaman güncelleneceğini biliyoruz.
    private val _accountEmail = MutableStateFlow(getGoogleDriveAccountEmailUseCase())
    val accountEmail: StateFlow<String?> = _accountEmail.asStateFlow()

    val backupPreferences: StateFlow<GoogleDriveBackupPreferences> = getGoogleDriveBackupPreferencesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GoogleDriveBackupPreferences())

    fun signInIntent(): Intent = getGoogleDriveSignInIntentUseCase()

    // DataBackupSettingsScreen başarısız girişte ekrana gerçek sebebi
    // basabilsin diye Result'ı olduğu gibi döndürüyoruz, state'i de biz
    // burada güncelliyoruz (başarılıysa e-posta, değilse null).
    fun onSignInResult(data: Intent?): Result<String?> {
        val result = completeGoogleDriveSignInUseCase(data)
        _accountEmail.value = result.getOrNull()
        return result
    }

    fun signOut() {
        signOutGoogleDriveUseCase { _accountEmail.value = null }
    }

    suspend fun uploadBackup(): Result<Unit> = uploadBackupToDriveUseCase()

    suspend fun restoreBackup(): Result<Unit> = restoreBackupFromDriveUseCase()

    suspend fun deleteAllBackups(): Result<Unit> = deleteAllDriveBackupsUseCase()

    fun setAutoBackupEnabled(enabled: Boolean) {
        setGoogleDriveAutoBackupEnabledUseCase(enabled)
    }

    fun setAutoBackupSchedule(minuteOfDay: Int, intervalDays: Int) {
        setGoogleDriveAutoBackupScheduleUseCase(minuteOfDay, intervalDays)
    }
}
