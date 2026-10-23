package com.yusufemre.ivirbyyuinn.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufemre.ivirbyyuinn.data.ConfirmationPreferences
import com.yusufemre.ivirbyyuinn.domain.GetConfirmationPreferencesUseCase
import com.yusufemre.ivirbyyuinn.domain.SetConfirmationPreferencesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// QuietHoursViewModel'in BİREBİR aynı deseni: küçük, tek bir tercihe bakan,
// ayrı bir ViewModel.
class ConfirmationSettingsViewModel(
    getConfirmationPreferencesUseCase: GetConfirmationPreferencesUseCase,
    private val setConfirmationPreferencesUseCase: SetConfirmationPreferencesUseCase,
) : ViewModel() {

    val confirmationPreferences: StateFlow<ConfirmationPreferences> = getConfirmationPreferencesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConfirmationPreferences())

    fun setConfirmationPreferences(preferences: ConfirmationPreferences) {
        setConfirmationPreferencesUseCase(preferences)
    }
}
